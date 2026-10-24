package yesman.epicfight.api.data.reloader;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.resources.FileToIdConverter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.skill.PlayerSkills;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SkillReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
	private static final List<CompoundTag> SKILL_PARAMS = Lists.newArrayList();
	private static final Gson GSON = (new GsonBuilder()).create();
	
	public static List<CompoundTag> getSkillParams() {
		return Collections.unmodifiableList(SKILL_PARAMS);
	}
	
	public static Skill getSkill(String name) {
		Identifier rl;
		
		if (name.indexOf(':') >= 0) {
			rl = Identifier.parse(name);
		} else {
            rl = EpicFightMod.identifier(name);
		}
		
		if (EpicFightRegistries.SKILL.containsKey(rl)) {
			return EpicFightRegistries.SKILL.getValue(rl);
		} else {
			return null;
		}
	}
	
	public static Collection<Skill> getSkills(Predicate<Skill> predicate) {
		return EpicFightRegistries.SKILL.listElements().map(Holder::value).filter(skill -> predicate.test(skill)).toList();
	}
	
	public static Stream<Identifier> getSkillNames(Predicate<Skill> predicate) {
		return EpicFightRegistries.SKILL.listElements().map(Holder::value).filter(skill -> predicate.test(skill)).map(skill -> skill.getRegistryName());
	}
	
	public static void reloadAllSkillsAnimations() {
		EpicFightRegistries.SKILL.listElements().map(Holder::value).forEach((skill) -> skill.registerPropertiesToAnimation());
	}
	
    @ClientOnly
	public static void processServerPacket(SPDatapackSync packet) {
		for (CompoundTag tag : packet.tags()) {
			if (!EpicFightRegistries.SKILL.containsKey(Identifier.parse(tag.getStringOr("id", "")))) {
				EpicFightMod.LOGGER.warn("Failed to syncronize Datapack for skill: " + tag.getString("id"));
				continue;
			}
			
			EpicFightRegistries.SKILL.getValue(Identifier.parse(tag.getStringOr("id", ""))).loadDatapackParameters(tag);
		}
		
		LocalPlayerPatch localplayerpatch = EpicFightCapabilities.getCachedLocalPlayerPatch();
		
		if (localplayerpatch != null) {
			PlayerSkills skillCapability = localplayerpatch.getPlayerSkills();
			
			for (SkillContainer skill : skillCapability.skillContainers) {
				if (skill.getSkill() != null) {
					// Reload skill
					skill.setSkill(getSkill(skill.getSkill().toString()), true);
				}
			}
			
			skillCapability.skillContainers[SkillCategories.BASIC_ATTACK.universalOrdinal()].setSkill(EpicFightSkills.COMBO_ATTACKS.get());
			skillCapability.skillContainers[SkillCategories.KNOCKDOWN_WAKEUP.universalOrdinal()].setSkill(EpicFightSkills.KNOCKDOWN_WAKEUP.get());
		}
	}
	
	private static Pair<Identifier, CompoundTag> parseParameters(Map.Entry<Identifier, JsonElement> entry) {
		try {
			CompoundTag tag = TagParser.parseCompoundFully(entry.getValue().toString());
			tag.putString("id", entry.getKey().toString());
			SKILL_PARAMS.add(tag);
			
			return Pair.of(entry.getKey(), tag);
		} catch (CommandSyntaxException e) {
			EpicFightMod.LOGGER.warn("Can't parse skill parameter for " + entry.getKey() + " because of " + e.getMessage());
			e.printStackTrace();
			
			return Pair.of(entry.getKey(), new CompoundTag());
		}
	}
	
	private static final SkillReloadListener INSTANCE = new SkillReloadListener();
	
	public static SkillReloadListener getInstance() {
		return INSTANCE;
	}
	
	public SkillReloadListener() {
		super(ExtraCodecs.JSON, FileToIdConverter.json("skill_parameters"));
	}
	
	@Override
	protected void apply(Map<Identifier, JsonElement> objectIn, ResourceManager resourceManager, ProfilerFiller profileFiller) {
		SKILL_PARAMS.clear();
		
		objectIn.entrySet().stream().filter((entry) -> {
			if (!EpicFightRegistries.SKILL.containsKey(entry.getKey())) {
				EpicFightMod.LOGGER.warn("Skill " + entry.getKey() + " doesn't exist in the registry.");
				return false;
			}
			
			return true;
		}).map(SkillReloadListener::parseParameters).forEach((pair) -> EpicFightRegistries.SKILL.getValue(pair.getFirst()).loadDatapackParameters(pair.getSecond()));
	}
}