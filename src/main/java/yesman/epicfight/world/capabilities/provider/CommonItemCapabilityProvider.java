package yesman.epicfight.world.capabilities.provider;

import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import yesman.epicfight.registry.deferred.holders.DeferredPreset;
import yesman.epicfight.registry.entries.EpicFightItemCapabilityPresets;
import yesman.epicfight.world.capabilities.item.*;
import yesman.epicfight.world.item.ArmorItems;
import yesman.epicfight.world.item.VanillaItemKinds;

public final class CommonItemCapabilityProvider implements ICapabilityProvider<ItemStack, Void, CapabilityItem> {
	public static final CommonItemCapabilityProvider INSTANCE = new CommonItemCapabilityProvider();
	
	private CommonItemCapabilityProvider() {}
	
	private final Map<Class<? extends Item>, DeferredPreset<? extends CapabilityItem.Builder<?>>> typedCapabilities = new HashMap<> ();
	/// Default capabilities for items recognized by their components rather than their class.
	/// Since 1.21.2 swords, pickaxes and armor are plain [Item]s, so there is no class to key on.
	private final List<ComponentTypedCapability> componentTypedCapabilities = new ArrayList<> ();
	
	private record ComponentTypedCapability(Predicate<Item> predicate, DeferredPreset<? extends CapabilityItem.Builder<?>> preset) {}
	private final Map<Item, CapabilityItem> capabilities = new HashMap<> ();
	
	public void registerWeaponTypesByClass() {
		this.componentTypedCapabilities.clear();
		this.componentTypedCapabilities.add(new ComponentTypedCapability(ArmorItems::isArmor, EpicFightItemCapabilityPresets.ARMOR));
		this.componentTypedCapabilities.add(new ComponentTypedCapability(VanillaItemKinds::isSword, EpicFightItemCapabilityPresets.BOKKEN));
		this.componentTypedCapabilities.add(new ComponentTypedCapability(VanillaItemKinds::isPickaxe, EpicFightItemCapabilityPresets.PICKAXE));
		this.componentTypedCapabilities.add(new ComponentTypedCapability(item -> item.components().has(DataComponents.KINETIC_WEAPON), EpicFightItemCapabilityPresets.SPEAR));
		
		this.typedCapabilities.put(ShieldItem.class, EpicFightItemCapabilityPresets.SHIELD);
        this.typedCapabilities.put(AxeItem.class, EpicFightItemCapabilityPresets.AXE);
        this.typedCapabilities.put(ShovelItem.class, EpicFightItemCapabilityPresets.SHOVEL);
        this.typedCapabilities.put(HoeItem.class, EpicFightItemCapabilityPresets.HOE);
        this.typedCapabilities.put(BowItem.class, EpicFightItemCapabilityPresets.BOW);
        this.typedCapabilities.put(CrossbowItem.class, EpicFightItemCapabilityPresets.CROSSBOW);
		this.typedCapabilities.put(MapItem.class, EpicFightItemCapabilityPresets.MAP);
	}
	
	public void put(Item item, CapabilityItem cap) {
		this.capabilities.put(item, cap);
	}
	
	/// Default capabilities by item type, resolved on first lookup. They read item data components,
	/// which are bound only after the data pack reload listeners have run (26.1).
	private final Map<Item, Optional<CapabilityItem>> defaultCapabilities = new java.util.concurrent.ConcurrentHashMap<> ();

	public CapabilityItem get(Item item) {
		CapabilityItem capability = this.capabilities.get(item);
		return capability != null ? capability : this.getCachedDefault(item);
	}

	private @Nullable CapabilityItem getCachedDefault(Item item) {
		if (item instanceof BlockItem) {
			return null;
		}

		return this.defaultCapabilities.computeIfAbsent(item, key -> Optional.ofNullable(this.getDefault(key))).orElse(null);
	}

	private @Nullable DeferredPreset<? extends CapabilityItem.Builder<?>> findTypedPreset(Item item) {
		for (Class<?> clazz = item.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			DeferredPreset<? extends CapabilityItem.Builder<?>> preset = this.typedCapabilities.get(clazz);
			
			if (preset != null) {
				return preset;
			}
		}
		
		for (ComponentTypedCapability componentTyped : this.componentTypedCapabilities) {
			if (componentTyped.predicate().test(item)) {
				return componentTyped.preset();
			}
		}
		
		return null;
	}
	
	private CapabilityItem getDefault(Item item)
	{
		DeferredPreset<? extends CapabilityItem.Builder<?>> builderEntry = this.findTypedPreset(item);
		CapabilityItem.Builder<?> result = null;
		if (builderEntry != null)
		{
			if (builderEntry.value() instanceof WeaponCapability.Builder)
			{
				result = WeaponCapabilityPresets.registerPreset(builderEntry.value(), item);
			}
			else if (builderEntry.value() instanceof ArmorCapability.Builder builder)
			{
				result = builder.byItem(item);
			}
			else
			{
				result = builderEntry.value();
			}
		}

		return result != null ? result.build() : null;
	}
	
	public void clear() {
		this.capabilities.clear();
		this.defaultCapabilities.clear();
	}
	
	public void addDefaultItems() {
		BuiltInRegistries.ITEM.entrySet().stream().filter(entry -> !this.capabilities.containsKey(entry.getValue())).forEach(entry -> {
			Function<Item, ? extends CapabilityItem.Builder<?>> type = null;
			Item item = entry.getValue();
			
			if (item instanceof BlockItem) {
				return;
			}
			
			for (Map.Entry<Identifier, ItemKeywordReloadListener.ItemRegex> regexEntry : ItemKeywordReloadListener.getRegexes().entrySet()) {
				if (regexEntry.getValue().matchesAny(entry.getKey().identifier().toString())) {
					type = WeaponTypeReloadListener.get(regexEntry.getKey());
					
					if (type != null) {
						this.capabilities.put(item, type.apply(item).build());
						break;
					}
				}
			}
			
			// Items without a keyword match get their default capability on first lookup (see #get).
		});
	}
	
	@Override
	public @Nullable CapabilityItem getCapability(ItemStack itemstack, Void context) {
		CapabilityItem itemCapability = this.get(itemstack.getItem());
		
		if (itemCapability instanceof RuntimeCapability) {
			return itemCapability.findRecursive(itemstack);
		}
		
		return itemCapability;
	}
}