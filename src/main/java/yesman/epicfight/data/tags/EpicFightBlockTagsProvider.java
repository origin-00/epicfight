package yesman.epicfight.data.tags;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightBlockTagsProvider extends BlockTagsProvider {
	public EpicFightBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, EpicFightMod.MODID);
    }
	
	@Override
	protected void addTags(Provider provider) {
	}
}
