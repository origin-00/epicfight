package yesman.epicfight.client.renderer.patched.layer;

import yesman.epicfight.client.renderer.submit.RecordingBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class PatchedStuckInBodyLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<?>, R extends StuckInBodyLayer<?, ?>> extends PatchedLayer<E, T, M, R> {
	private static final Vec3f VECTOR = new Vec3f();

	@Override
	protected void renderLayer(T entitypatch, E entityliving, R vanillaLayer, LivingEntityRenderState renderState, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		int i = Math.min(ClientConfig.maxStuckProjectiles, this.numStuck(entityliving));
		RandomSource randomsource = RandomSource.create((long) entityliving.getId());

		if (i > 0 && vanillaLayer != null) {
			for (int j = 0; j < i; ++j) {
				poseStack.pushPose();

				int randomJoint = Math.abs(randomsource.nextInt()) % entitypatch.getArmature().getJointNumber();
				MathUtils.mulStack(poseStack, poses[randomJoint]);
	            entitypatch.getArmature().searchJointById(randomJoint).getLocalTransform().toTranslationVector(VECTOR);

				float f = randomsource.nextFloat();
				float f1 = randomsource.nextFloat();
				float f2 = randomsource.nextFloat();
				float f3 = Mth.lerp(f, -VECTOR.x * 0.5F, VECTOR.x * 0.5F);
				float f4 = Mth.lerp(f1, -VECTOR.y * 0.5F, VECTOR.y * 0.5F);
				float f5 = Mth.lerp(f2, -VECTOR.z * 0.5F, VECTOR.z * 0.5F);
				poseStack.translate(f3, f4, f5);
				f = -1.0F * (f * 2.0F - 1.0F);
				f1 = -1.0F * (f1 * 2.0F - 1.0F);
				f2 = -1.0F * (f2 * 2.0F - 1.0F);
				this.submitStuckItem(vanillaLayer, poseStack, buffer, packedLight, renderState.outlineColor, f, f1, f2);

				poseStack.popPose();
			}
		}
	}

	/// Orients and submits the vanilla layer's stuck model (an arrow, a bee stinger), as
	/// `StuckInBodyLayer#submitStuckItem` does.
	@SuppressWarnings("unchecked")
	protected void submitStuckItem(R vanillaLayer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int outlineColor, float directionX, float directionY, float directionZ) {
		if (!(buffer instanceof RecordingBufferSource recordingBufferSource)) {
			return;
		}

		float directionXZ = Mth.sqrt(directionX * directionX + directionZ * directionZ);
		float yRot = (float)(Math.atan2(directionX, directionZ) * 180.0F / (float)Math.PI);
		float xRot = (float)(Math.atan2(directionY, directionXZ) * 180.0F / (float)Math.PI);
		poseStack.mulPose(Axis.YP.rotationDegrees(yRot - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(xRot));
		recordingBufferSource.collector().submitModel((Model<Object>)vanillaLayer.model, vanillaLayer.modelState, poseStack, vanillaLayer.texture, packedLight, OverlayTexture.NO_OVERLAY, outlineColor, null);
	}

	protected abstract int numStuck(E entity);
}
