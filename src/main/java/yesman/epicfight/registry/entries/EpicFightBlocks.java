package yesman.epicfight.registry.entries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.level.block.FractureBlock;

public final class EpicFightBlocks {
	private EpicFightBlocks() {}
	
	// Blocks need their id in their properties since 1.21.2, which DeferredRegister.Blocks sets.
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(EpicFightMod.MODID);
	
	public static final DeferredHolder<Block, FractureBlock> FRACTURE = REGISTRY.registerBlock("fracture_block", FractureBlock::new);
}