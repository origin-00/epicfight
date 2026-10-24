package yesman.epicfight.client.renderer.patched.layer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.submit.RecordingBufferSource;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class PatchedLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<?>, R extends RenderLayer<?, ?>> {
	public void renderLayer(E entityliving, T entitypatch, RenderLayer<?, ?> vanillaLayer, LivingEntityRenderState renderState, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		this.renderLayer(entitypatch, entityliving, this.castLayer(vanillaLayer), renderState, poseStack, buffer, packedLight, poses, bob, yRot, xRot, partialTicks);
	}
	
	protected abstract void renderLayer(T entitypatch, E entityliving, @Nullable R vanillaLayer, LivingEntityRenderState renderState, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks);
	
	/// Lets a vanilla layer submit itself with the given (joint-transformed) pose.
	///
	/// Since 1.21.9 vanilla layers submit models with their render state, which is safe to defer;
	/// they reach the frame's collector through `buffer` (a
	/// [yesman.epicfight.client.renderer.submit.RecordingBufferSource] during entity rendering).
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected static void submitVanillaLayer(RenderLayer<?, ?> vanillaLayer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntityRenderState renderState, float yRot, float xRot) {
		if (buffer instanceof RecordingBufferSource recordingBufferSource) {
			((RenderLayer)vanillaLayer).submit(poseStack, recordingBufferSource.collector(), packedLight, renderState, yRot, xRot);
		}
	}

	@SuppressWarnings("unchecked")
	protected R castLayer(RenderLayer<?, ?> layer) {
		return (R)layer;
	}
}