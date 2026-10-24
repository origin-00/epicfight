package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.screen.overlay.OverlayManager;

/// Applies the overlay manager's gamma while the lightmap state is extracted. The lightmap is
/// computed from a render state since 1.21.9, where the brightness option is read.
@Mixin(value = LightmapRenderStateExtractor.class)
public abstract class MixinLightTexture {
	@Inject(at = @At(value = "HEAD"), method = "extract(Lnet/minecraft/client/renderer/state/LightmapRenderState;F)V", cancellable = true)
	private void epicfight$head_updateLightTexture(CallbackInfo info) {
		OverlayManager overlayManager = RenderEngine.getInstance().getOverlayManager();
		
		if (overlayManager.isGammaChanged()) {
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.options.gamma().set(overlayManager.getModifiedGamma(minecraft.options.gamma().get()));
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "extract(Lnet/minecraft/client/renderer/state/LightmapRenderState;F)V", cancellable = true)
	private void epicfight$tail_updateLightTexture(CallbackInfo info) {
		OverlayManager overlayManager = RenderEngine.getInstance().getOverlayManager();
		
		if (overlayManager.isGammaChanged()) {
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.options.gamma().set(overlayManager.getOriginalGamma());
		}
	}
}