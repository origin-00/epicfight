package yesman.epicfight.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = Gui.class)
public abstract class MixinGui {
	
	@Final
    @Shadow
	private Minecraft minecraft;

	/**
	 * Render crosshair in third-person TPS mode
	 */
	@Inject(
			at = @At(
					value = "TAIL"
			),
			method = "extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
	)
	private void renderCrosshairINJECT(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo callback) {
		// Draw crosshair in TPS mode, since vanilla crosshair aren't rendered in third person.
		if (EpicFightCameraAPI.getInstance().isTPSMode()) {
			this.epicfight$renderCrosshair(guiGraphics, () -> guiGraphics.blitSprite(RenderPipelines.CROSSHAIR, Gui.CROSSHAIR_SPRITE, (guiGraphics.guiWidth() - 15) / 2, (guiGraphics.guiHeight() - 15) / 2, 15, 15));
		}
	}

	/**
	 * Replace the crosshair into a mining indicator
	 */
	@WrapOperation(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
					ordinal = 0
			),
			method = "extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
	)
	private void renderCrosshairREDIRECT(GuiGraphicsExtractor guiGraphics, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original) {
		this.epicfight$renderCrosshair(guiGraphics, () -> original.call(guiGraphics, pipeline, sprite, x, y, width, height));
	}

	@Unique
	private void epicfight$renderCrosshair(GuiGraphicsExtractor guiGraphics, Runnable vanillaCrosshairRenderer) {
		MutableBoolean drawVanillaCrosshair = new MutableBoolean(true);

		if (ClientConfig.mineBlockGuideOption.switchCrosshair()) {
			EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
				if (playerpatch.isVanillaMode()) {
					drawVanillaCrosshair.setValue(RenderEngine.hitResultNotEquals(this.minecraft.hitResult, HitResult.Type.BLOCK));
				} else {
					drawVanillaCrosshair.setValue(playerpatch.canPlayAttackAnimation());
				}
			});
		}

		if (drawVanillaCrosshair.booleanValue()) {
			vanillaCrosshairRenderer.run();
		} else {
			guiGraphics.blit(RenderPipelines.CROSSHAIR, EntityUI.BATTLE_ICON, (guiGraphics.guiWidth() - 15) / 2, (guiGraphics.guiHeight() - 15) / 2, 0, 240, 15, 15, 256, 256);
		}
	}
}
