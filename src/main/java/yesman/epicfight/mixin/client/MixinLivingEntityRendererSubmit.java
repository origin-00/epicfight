package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.client.events.engine.RenderEngine;

/// Hands living entities with an Epic Fight renderer to [RenderEngine] instead of the vanilla model.
///
/// NeoForge's `RenderLivingEvent.Pre` can cancel vanilla rendering too, but a cancelled submit
/// also skips the leash and name display that [EntityRenderer#submit] adds, and the event does not
/// carry the camera state needed to submit them. Injecting here keeps both.
@Mixin(value = LivingEntityRenderer.class)
public abstract class MixinLivingEntityRendererSubmit<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> {
	protected MixinLivingEntityRendererSubmit(EntityRendererProvider.Context context) {
		super(context);
	}
	
	@Inject(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void epicfight$submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo info) {
		if (RenderEngine.getInstance().submitLivingEntity((LivingEntityRenderer<?, ?, ?>)(Object)this, state, poseStack, submitNodeCollector)) {
			// The parts of the vanilla submit that are not the model itself: leash and name display.
			super.submit(state, poseStack, submitNodeCollector, camera);
			info.cancel();
		}
	}
}
