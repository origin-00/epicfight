package yesman.epicfight.registry.entries;

import net.minecraft.core.registries.Registries;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.data.loot.function.SetSkillFunction;
import yesman.epicfight.main.EpicFightMod;

public final class EpicFightLootItemFunctions {
	private EpicFightLootItemFunctions() {}
	
	public static final DeferredRegister<MapCodec<? extends LootItemFunction>> REGISTRY = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, EpicFightMod.MODID);
	
	public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<SetSkillFunction>> SKILLS = REGISTRY.register("skillbook_loot_table_modifier", () -> SetSkillFunction.CODEC);
}
