package yesman.epicfight.api.data.reloader;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.resources.FileToIdConverter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightConditions;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.*;
import yesman.epicfight.world.capabilities.item.custom.CustomData;
import yesman.epicfight.world.capabilities.provider.ExtraEntryProvider;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ItemCapabilityReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
	public static final String DIRECTORY = "capabilities";
	private static final Gson GSON = new GsonBuilder().create();
	private static final Map<Item, CompoundTag> ARMOR_COMPOUNDS = new HashMap<> ();
	private static final Map<Item, CompoundTag> WEAPON_COMPOUNDS = new HashMap<> ();
	
	public ItemCapabilityReloadListener() {
		super(ExtraCodecs.JSON, FileToIdConverter.json(DIRECTORY));
	}
	
	@Override
	protected Map<Identifier, JsonElement> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profileIn) {
		ARMOR_COMPOUNDS.clear();
		WEAPON_COMPOUNDS.clear();
		
		return super.prepare(resourceManager, profileIn);
	}
	
	/// Parsed during the reload, built once item components are bound (see [#onComponentsBound()]).
	private static Map<Identifier, JsonElement> pendingEntries;

	@Override
	protected void apply(Map<Identifier, JsonElement> objectIn, @NotNull ResourceManager resourceManagerIn, @NotNull ProfilerFiller profilerIn) {
		// Since 26.1, item components are bound after every reload listener finished, and the capabilities read them
		pendingEntries = objectIn;
	}

	/// Builds the item capabilities from the last reload. Called after the server binds the default item components.
	public static void onComponentsBound() {
		if (pendingEntries == null) {
			return;
		}

		Map<Identifier, JsonElement> objectIn = pendingEntries;
		pendingEntries = null;

		for (Map.Entry<Identifier, JsonElement> entry : objectIn.entrySet()) {
			Identifier rl = entry.getKey();
			String path = rl.getPath();
			
			if (path.contains("/") && !path.contains("types") && !path.contains("item_keyword")) {
				String[] str = path.split("/", 2);
				Identifier registryName = Identifier.fromNamespaceAndPath(rl.getNamespace(), str[1]);
				
				if (!BuiltInRegistries.ITEM.containsKey(registryName)) {
					EpicFight.LOGGER.warn("Item Capability Exception: No item named {}", registryName);
					continue;
				}
				
				Item item = BuiltInRegistries.ITEM.getValue(registryName);
				CompoundTag tag = null;
				
				try {
					tag = TagParser.parseCompoundFully(entry.getValue().toString());
				} catch (CommandSyntaxException e) {
                    warnDeserialize(registryName, e);
					continue;
				}
				
				try {
					if (str[0].equals("armors")) {
						CapabilityItem capability = deserializeArmor(item, tag);
						EpicFightCapabilities.ITEM_CAPABILITY_PROVIDER.put(item, capability);
						ARMOR_COMPOUNDS.put(item, tag);
					} else if (str[0].equals("weapons")) {
						CapabilityItem capability = deserializeWeapon(item, tag);
						EpicFightCapabilities.ITEM_CAPABILITY_PROVIDER.put(item, capability);
						WEAPON_COMPOUNDS.put(item, tag);
					}
				} catch (Exception e) {
                    warnDeserialize(registryName, e);
				}
			}
		}
		
		EpicFightCapabilities.ITEM_CAPABILITY_PROVIDER.addDefaultItems();
	}

    private static void warnDeserialize(Identifier registryName, Exception e) {
        EpicFight.LOGGER.warn("Error while deserializing datapack for {}: {}", registryName, e.getLocalizedMessage());
    }

	public static CapabilityItem deserializeArmor(Item item, CompoundTag tag) {
		ArmorCapability.Builder builder = ArmorCapability.builder();
		
		if (tag.contains("attributes")) {
			CompoundTag attributes = tag.getCompoundOrEmpty("attributes");
			builder.weight(attributes.getDoubleOr("weight", 0.0D)).stunArmor(attributes.getDoubleOr("stun_armor", 0.0D));
		}
		
		builder.byItem(item);
		
		return builder.build();
	}

    private static void setSound(String location, Function<Holder<SoundEvent>, WeaponCapability.Builder> setter) {
        Identifier soundLocation = Identifier.parse(location);
        Holder<SoundEvent> sound = BuiltInRegistries.SOUND_EVENT.get(soundLocation).get();
        setter.apply(sound);
    }

    public static CapabilityItem deserializeWeapon(Item item, CompoundTag tag) {
        return deserializeWeapon(item, tag, null);
    }

    /// @deprecated Use Non-datapack sensitive version. [#deserializeWeapon(Item, CompoundTag)]
    /// @param extraEntryProvider Returns extra-entry created in runtime. (Datapack editor) Exists to access weapon types.
    public static CapabilityItem deserializeWeapon(Item item, CompoundTag tag, @Nullable ExtraEntryProvider extraEntryProvider) {
		CapabilityItem capability;

		if (tag.contains("variations")) {
			ListTag jsonArray = tag.getListOrEmpty("variations");
			List<Pair<Condition<ItemStack>, CapabilityItem>> list = Lists.newArrayList();
            CapabilityItem.Builder<?> innerDefaultCapabilityBuilder = tag.contains("type") ?
                (extraEntryProvider == null ?
                    WeaponTypeReloadListener.getOrThrow(tag.getStringOr("type", "")) : extraEntryProvider.getExtraOrBuiltInWeaponType(tag.getStringOr("type", ""))).apply(item)
                : CapabilityItem.builder();

            if (tag.contains("attributes")) {
				CompoundTag attributes = tag.getCompoundOrEmpty("attributes");
				
				for (String key : attributes.keySet()) {
					Map<Holder<Attribute>, AttributeModifier> attributeEntry = deserializeAttributes(attributes.getCompoundOrEmpty(key));
					
					for (Map.Entry<Holder<Attribute>, AttributeModifier> attribute : attributeEntry.entrySet()) {
						innerDefaultCapabilityBuilder.addStyleAttibutes(Style.ENUM_MANAGER.getOrThrow(key), attribute.getKey(), attribute.getValue());
					}
				}
			}
			
			for (Tag jsonElement : jsonArray) {
				CompoundTag innerTag = ((CompoundTag)jsonElement);
				Supplier<Condition<ItemStack>> conditionProvider = EpicFightConditions.getConditionOrThrow(Identifier.parse(innerTag.getStringOr("condition", "")));
				Condition<ItemStack> condition = conditionProvider.get().read(innerTag.getCompoundOrEmpty("predicate"));
				
				list.add(Pair.of(condition, deserializeWeapon(item, innerTag)));
			}
			
			capability = new RuntimeCapability(list, innerDefaultCapabilityBuilder.build());
		} else {
            CapabilityItem.Builder<?> builder = tag.contains("type") ?
                (extraEntryProvider == null ?
                    WeaponTypeReloadListener.getOrThrow(tag.getStringOr("type", "")) : extraEntryProvider.getExtraOrBuiltInWeaponType(tag.getStringOr("type", ""))).apply(item)
                : CapabilityItem.builder();

            if (tag.contains("attributes")) {
				CompoundTag attributes = tag.getCompoundOrEmpty("attributes");
				
				for (String key : attributes.keySet()) {
					Map<Holder<Attribute>, AttributeModifier> attributeEntry = deserializeAttributes(attributes.getCompoundOrEmpty(key));
					Style style = Style.ENUM_MANAGER.getOrThrow(key);
					
					for (Map.Entry<Holder<Attribute>, AttributeModifier> attribute : attributeEntry.entrySet()) {
						builder.addStyleAttibutes(style, attribute.getKey(), attribute.getValue());
					}
				}
			}

			if (tag.contains("custom_data")) {
				CompoundTag data = tag.getCompoundOrEmpty("custom_data");

				for (var key : data.keySet()) {
					//Debugging
					Optional<Holder.Reference<CustomData<?>>> trueData = EpicFightRegistries.WEAPON_DATA.get(Identifier.parse(key));
					if (trueData.isPresent())
					{
						if (trueData.get().value().deserializer().isPresent()) {
							builder.setCustomDataInternal(trueData.get(), trueData.get().value().deserializer().get().apply(data.get(key)));
						}
						else
						{
							EpicFight.LOGGER.warn("Custom Data does not contain a deserializer");
						}
					}
				}
			}

            if (builder instanceof WeaponCapability.Builder weaponBuilder) {
                if (tag.contains("swing_sound"))
                {
                    setSound(tag.getStringOr("swing_sound", ""), weaponBuilder::swingSound);
                }
                if (tag.contains("hit_sound"))
                {
                    setSound(tag.getStringOr("hit_sound", ""), weaponBuilder::hitSound);
                }
                if (tag.contains("hit_particle"))
                {
                    Identifier hitParticleLocation = Identifier.parse(tag.getStringOr("hit_particle", ""));
                    Holder<ParticleType<?>> hitParticle = BuiltInRegistries.PARTICLE_TYPE.get(hitParticleLocation).get();
                    if (hitParticle.value() instanceof HitParticleType trueParticle)
                    {
                        weaponBuilder.hitParticle(hitParticle);
                    }
                    else
                    {
                        EpicFight.LOGGER.warn("Hit Particle Type not found: {}", hitParticleLocation);
                    }
                }

                if (tag.contains("custom_tags")) {
                    for (Tag customTag : tag.getListOrEmpty("custom_tags")) {
                        weaponBuilder.addTag(Identifier.parse(customTag.asString().orElse("")));
                    }
                }
            }

			if (tag.contains("collider")) {
				CompoundTag colliderTag = tag.getCompoundOrEmpty("collider");
				
				try {
					Collider collider = ColliderPreset.deserializeSimpleCollider(colliderTag);
					builder.collider(collider);
				} catch (IllegalArgumentException e) {
                    EpicFight.LOGGER.warn("Can't deserialize collider of {}: {}", item, e.getMessage());
				}
			}

			capability = builder.build();
		}
		
		return capability;
	}
	
	private static Map<Holder<Attribute>, AttributeModifier> deserializeAttributes(CompoundTag tag) {
		Map<Holder<Attribute>, AttributeModifier> modifierMap = Maps.newHashMap();
		
		if (tag.contains("armor_negation")) {
			modifierMap.put(EpicFightAttributes.ARMOR_NEGATION, EpicFightAttributes.getArmorNegationModifier(tag.getDoubleOr("armor_negation", 0.0D)));
		}
		if (tag.contains("impact")) {
			modifierMap.put(EpicFightAttributes.IMPACT, EpicFightAttributes.getImpactModifier(tag.getDoubleOr("impact", 0.0D)));
		}
		if (tag.contains("max_strikes")) {
			modifierMap.put(EpicFightAttributes.MAX_STRIKES, EpicFightAttributes.getMaxStrikesModifier(tag.getIntOr("max_strikes", 0)));
		}
		if (tag.contains("damage_bonus")) {
			modifierMap.put(Attributes.ATTACK_DAMAGE, EpicFightAttributes.getDamageBonusModifier(tag.getDoubleOr("damage_bonus", 0.0D)));
		}
		if (tag.contains("speed_bonus")) {
			modifierMap.put(Attributes.ATTACK_SPEED, EpicFightAttributes.getSpeedBonusModifier(tag.getDoubleOr("speed_bonus", 0.0D)));
		}
		
		return modifierMap;
	}

    private static Stream<CompoundTag> getStream(Map<Item, CompoundTag> map) {
        return map.entrySet().stream().map((entry) -> {
            entry.getValue().putInt("id", Item.getId(entry.getKey()));
            return entry.getValue();
        });
    }

    public static Stream<CompoundTag> getArmorDataStream() {
        return getStream(ARMOR_COMPOUNDS);
    }

    public static Stream<CompoundTag> getWeaponDataStream() {
        return getStream(WEAPON_COMPOUNDS);
    }
	
	private static boolean armorReceived = false;
	private static boolean weaponReceived = false;
	private static boolean weaponTypeReceived = false;
	
	public static void weaponTypeProcessedCheck() {
		weaponTypeReceived = true;
	}
	
	public static void reset() {
		armorReceived = false;
		weaponReceived = false;
		weaponTypeReceived = false;
	}
	
	public static void processServerPacket(SPDatapackSync packet) {
		switch (packet.packetType()) {
		case ARMOR:
			for (CompoundTag tag : packet.tags()) {
				Item item = Item.byId(tag.getIntOr("id", 0));
				ARMOR_COMPOUNDS.put(item, tag);
			}
			armorReceived = true;
			break;
		case WEAPON:
			for (CompoundTag tag : packet.tags()) {
				Item item = Item.byId(tag.getIntOr("id", 0));
				WEAPON_COMPOUNDS.put(item, tag);
			}
			weaponReceived = true;
			break;
		default:
			break;
		}
		
		if (weaponTypeReceived && armorReceived && weaponReceived) {
			ARMOR_COMPOUNDS.forEach((item, tag) -> {
				try {
					CapabilityItem itemCap = deserializeArmor(item, tag);
					EpicFightCapabilities.ITEM_CAPABILITY_PROVIDER.put(item, itemCap);
				} catch (NoSuchElementException e) {
                    EpicFight.LOGGER.warn("Error while creating capability {}: {}", item, e.getLocalizedMessage());
				} catch (Exception e) {
                    EpicFight.LOGGER.warn("Can't read item capability for {}: {}", item, e.getLocalizedMessage());
				}
			});
			
			WEAPON_COMPOUNDS.forEach((item, tag) -> {
				try {
					CapabilityItem itemCap = deserializeWeapon(item, tag);
					EpicFightCapabilities.ITEM_CAPABILITY_PROVIDER.put(item, itemCap);
				} catch (NoSuchElementException ignored) {
				} catch (Exception e) {
					EpicFight.LOGGER.warn("Can't read item capability for {}: {}", item, e.getLocalizedMessage());
				}
			});
			
			EpicFightCapabilities.ITEM_CAPABILITY_PROVIDER.addDefaultItems();
		}
	}
}