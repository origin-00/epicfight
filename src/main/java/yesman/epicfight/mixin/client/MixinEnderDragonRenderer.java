package yesman.epicfight.mixin.client;

import yesman.epicfight.client.renderer.submit.RecordingBufferSource;
import yesman.epicfight.client.renderer.EpicFightRenderUtils;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.render.RenderEnderDragonEvent;

@Mixin(value = EnderDragonRenderer.class)
public abstract class MixinEnderDragonRenderer extends EntityRenderer<EnderDragon, EnderDragonRenderState> {
	protected MixinEnderDragonRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Inject(at = @At(value = "HEAD"), method = "submit(Lnet/minecraft/client/renderer/entity/state/EnderDragonRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", cancellable = true)
	private void epicfight$submit(EnderDragonRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo info) {
		if (!(state.getRenderData(EpicFightRenderUtils.RENDERED_ENTITY) instanceof EnderDragon enderdragon)) {
			return;
		}

		RecordingBufferSource buffer = new RecordingBufferSource(submitNodeCollector, state.outlineColor);
		RenderEnderDragonEvent renderDragonEvent = new RenderEnderDragonEvent(enderdragon, (EnderDragonRenderer)((Object)this), state, state.partialTick, poseStack, buffer, state.lightCoords);
		boolean canceled = EpicFightClientEventHooks.Render.RENDER_ENDER_DRAGON.post(renderDragonEvent).isCanceled();
		buffer.submit();

		if (canceled) {
			// Leash and name display, as the vanilla submit ends with.
			super.submit(state, poseStack, submitNodeCollector, camera);
			info.cancel();
		}
	}
}
