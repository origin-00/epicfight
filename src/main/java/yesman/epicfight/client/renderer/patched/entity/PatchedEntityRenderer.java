package yesman.epicfight.client.renderer.patched.entity;

import yesman.epicfight.client.renderer.EpicFightRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/// Renders an entity with its Epic Fight armature and mesh instead of the vanilla model.
///
/// Since 1.21.9 vanilla renderers work from a render state extracted before rendering, and the
/// geometry they produce is submitted and drawn later. The entity is still passed for Epic Fight's
/// own state (its patch, armature and pose); `renderState` is the vanilla state of the same frame,
/// for anything vanilla (render types, vanilla layers, models). `buffer` records into the frame's
/// submissions (see [yesman.epicfight.client.renderer.submit.RecordingBufferSource]).
///
/// Name tags and leashes are submitted by the vanilla renderer itself after Epic Fight renders the
/// body (see `MixinLivingEntityRenderer`), so renderers no longer draw them.
public abstract class PatchedEntityRenderer<E extends LivingEntity, T extends LivingEntityPatch<E>, R extends EntityRenderer<E, ?>, AM extends SkinnedMesh> {
	public void render(E entity, T entitypatch, R renderer, EntityRenderState renderState, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTick) {
	}

	public void mulPoseStack(PoseStack poseStack, Armature armature, E entity, T entitypatch, float partialTicks) {
		OpenMatrix4f modelMatrix = entitypatch.getModelMatrix(partialTicks);
        poseStack.mulPose(QuaternionUtils.YP.rotationDegrees(180.0F));
        MathUtils.mulStack(poseStack, modelMatrix);

        if (EpicFightRenderUtils.isUpsideDown(entity)) {
        	poseStack.translate(0.0D, entity.getBbHeight() + 0.1F, 0.0D);
        	poseStack.mulPose(QuaternionUtils.ZP.rotationDegrees(180.0F));
		}
	}

	public void setArmaturePose(T entitypatch, Armature armature, float partialTicks) {
		Pose pose = entitypatch.getAnimator().getPose(partialTicks);
        this.setJointTransforms(entitypatch, armature, pose, partialTicks);
        armature.setPose(pose);
	}

	public AssetAccessor<AM> getMeshProvider(T entitypatch) {
		return this.getDefaultMesh();
	}

	public abstract AssetAccessor<AM> getDefaultMesh();

	/**
	 * Developers shouldn't implement any interpolations in this method
	 * Use {@link LivingEntityPatch#poseTick} instead
	 */
	public void setJointTransforms(T entitypatch, Armature armature, Pose pose, float partialTicks) {
	}
}
