package yesman.epicfight.compat.vampirism.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import de.teamlapen.vampirism.client.renderer.entity.layers.VampirePlayerHeadLayer;
import net.minecraft.resources.Identifier;

@Mixin(value = VampirePlayerHeadLayer.class, remap = false)
public interface MixinVampirePlayerHeadLayer {
	@Accessor(remap = false)
    public Identifier[] getEyeOverlays();

	@Accessor(remap = false)
	public Identifier[] getFangOverlays();
}