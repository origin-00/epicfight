package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import yesman.epicfight.client.gui.GuiColors;

/// Gives [GuiGraphicsExtractor] the color multiplier `GuiGraphics#setColor` (and the shader color)
/// applied to textures and fills up to 1.21.5. See [GuiColors#setColor].
@Mixin(value = GuiGraphicsExtractor.class)
public abstract class MixinGuiGraphicsExtractor implements GuiColors.Tintable {
	@Unique
	private int epicfight$tint = -1;
	
	@Override
	public void epicfight$setTint(int argb) {
		this.epicfight$tint = argb;
	}
	
	@Override
	public int epicfight$getTint() {
		return this.epicfight$tint;
	}
	
	@Unique
	private int epicfight$applyTint(int color) {
		return this.epicfight$tint == -1 ? color : ARGB.multiply(color, this.epicfight$tint);
	}
	
	@ModifyVariable(method = "innerBlit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lcom/mojang/blaze3d/textures/GpuTextureView;Lcom/mojang/blaze3d/textures/GpuSampler;IIIIFFFFI)V", at = @At("HEAD"), argsOnly = true, ordinal = 4)
	private int epicfight$tintBlit(int color) {
		return this.epicfight$applyTint(color);
	}
	
	@ModifyVariable(method = "innerTiledBlit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lcom/mojang/blaze3d/textures/GpuTextureView;Lcom/mojang/blaze3d/textures/GpuSampler;IIIIIIFFFFI)V", at = @At("HEAD"), argsOnly = true, ordinal = 6)
	private int epicfight$tintTiledBlit(int color) {
		return this.epicfight$applyTint(color);
	}
	
	@ModifyVariable(method = "innerFill(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;IIIIILjava/lang/Integer;)V", at = @At("HEAD"), argsOnly = true, ordinal = 4)
	private int epicfight$tintFill(int color) {
		return this.epicfight$applyTint(color);
	}
	
	@ModifyVariable(method = "innerFill(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;IIIIILjava/lang/Integer;)V", at = @At("HEAD"), argsOnly = true)
	private Integer epicfight$tintFillGradient(Integer color) {
		return color == null ? null : this.epicfight$applyTint(color);
	}
}
