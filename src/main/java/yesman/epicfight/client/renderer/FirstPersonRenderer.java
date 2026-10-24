package yesman.epicfight.client.renderer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SkinnedMesh.SkinnedMeshPart;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.client.renderer.patched.layer.EmptyLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedItemInHandLayer;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

public class FirstPersonRenderer extends PatchedLivingEntityRenderer<LocalPlayer, LocalPlayerPatch, PlayerModel, LivingEntityRenderer<LocalPlayer, ?, ?>, HumanoidMesh> {
	public FirstPersonRenderer(EntityRendererProvider.Context context, EntityType<?> entityType) {
		super(context, entityType);
		
		this.addPatchedLayer(WingsLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(PlayerItemInHandLayer.class, new PatchedItemInHandLayer<>());
		this.addPatchedLayer(HumanoidArmorLayer.class, new WearableItemLayer<>(Meshes.BIPED, true, context));
		this.addPatchedLayer(CustomHeadLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(ArrowLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(BeeStingerLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(SpinAttackEffectLayer.class, new EmptyLayer<>());
		this.addPatchedLayer(CapeLayer.class, new EmptyLayer<>());
	}
	
	@Override
	public void render(LocalPlayer entity, LocalPlayerPatch localPlayerPatch, LivingEntityRenderer<LocalPlayer, ?, ?> renderer, EntityRenderState entityRenderState, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTick) {
		LivingEntityRenderState renderState = (LivingEntityRenderState)entityRenderState;
		
		if (localPlayerPatch.getPovSettings() != null) {
			Pose pose = localPlayerPatch.getFirstPersonLayer().getEnabledPose(localPlayerPatch, true, partialTick);
			OpenMatrix4f[] poses = localPlayerPatch.getArmature().getPoseAsTransformMatrix(pose, false);
			poseStack.pushPose();
			float standingEyeHeight = entity.getDimensions(net.minecraft.world.entity.Pose.STANDING).eyeHeight();
			
			if (localPlayerPatch.hasCameraAnimation()) {
				float time = Mth.lerp(partialTick, localPlayerPatch.getFirstPersonLayer().animationPlayer.getPrevElapsedTime(), localPlayerPatch.getFirstPersonLayer().animationPlayer.getElapsedTime());
				JointTransform cameraTransform;
				
				if (localPlayerPatch.getFirstPersonLayer().animationPlayer.getAnimation().get().isLinkAnimation() || localPlayerPatch.getPovSettings() == null) {
					cameraTransform = localPlayerPatch.getFirstPersonLayer().getLinkCameraTransform().getInterpolatedTransform(time);
				} else {
					cameraTransform = localPlayerPatch.getPovSettings().cameraTransform().getInterpolatedTransform(time);
				}
				
				MathUtils.mulStack(poseStack, cameraTransform.toMatrix().invert());
			}
			
			switch (localPlayerPatch.getPovSettings().rootTransformation()) {
			case CAMERA -> {
				poseStack.translate(0.0F, -standingEyeHeight, 0.0F);
			}
			case WORLD -> {
				float yRotModel = 180.0F - Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
				float yRotWorld = 180.0F - Mth.rotLerp(partialTick, localPlayerPatch.getYRotO(), localPlayerPatch.getYRot());
				float yRot = yRotWorld - yRotModel;
				float xRot = Mth.rotLerp(partialTick, entity.xRotO, entity.getXRot());

				poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
				poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
				poseStack.translate(0.0F, -standingEyeHeight, 0.0F);
			}
			}
			
			HumanoidMesh mesh = this.getMeshProvider(localPlayerPatch).get();
			this.prepareModel(mesh, entity, localPlayerPatch, renderer);
			
			if (!localPlayerPatch.getOriginal().isInvisible()) {
				Map<String, Boolean> visibilities = localPlayerPatch.getPovSettings().visibilities();
				boolean defaultVisibility = localPlayerPatch.getPovSettings().visibilityOthers();
				
				for (Map.Entry<String, SkinnedMeshPart> entry : mesh.getPartEntry()) {
					if (visibilities.containsKey(entry.getKey())) {
						entry.getValue().setHidden(!visibilities.get(entry.getKey()));
					} else {
						entry.getValue().setHidden(!defaultVisibility);
					}
				}
				
				RenderType renderType = RenderTypes.entityCutout(entity.getSkin().body().texturePath());
				mesh.draw(poseStack, buffer, renderType, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, localPlayerPatch.getArmature(), poses);
			}
			
			if (!entity.isSpectator()) {
				this.renderLayer(renderer, localPlayerPatch, entity, renderState, poses, buffer, poseStack, packedLight, partialTick);
			}
			
			poseStack.popPose();
		} else {
			Pose pose = localPlayerPatch.getAnimator().getPose(partialTick);
			OpenMatrix4f[] poses = localPlayerPatch.getArmature().getPoseAsTransformMatrix(pose, false);
			poseStack.pushPose();
			
			float correction = 0.0F; 
			
			if (entity.isVisuallySwimming()) {
				correction = 0.25F;
			} else if (entity.isFallFlying()) {
				correction = 100.0F;
			}
			
			float standingEyeHeight = entity.getDimensions(net.minecraft.world.entity.Pose.STANDING).eyeHeight();
			
			poseStack.translate(0.0F, -standingEyeHeight - 0.05F, correction);
			
			HumanoidMesh mesh = this.getMeshProvider(localPlayerPatch).get();
			this.prepareModel(mesh, entity, localPlayerPatch, renderer);
			
			if (!localPlayerPatch.getOriginal().isInvisible()) {
				for (SkinnedMeshPart p : mesh.getAllParts()) {
					p.setHidden(true);
				}
				
				mesh.leftArm.setHidden(false);
				mesh.rightArm.setHidden(false);
				mesh.leftSleeve.setHidden(false);
				mesh.rightSleeve.setHidden(false);
				
				RenderType renderType = RenderTypes.entityCutout(entity.getSkin().body().texturePath());
				mesh.draw(poseStack, buffer, renderType, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, localPlayerPatch.getArmature(), poses);
			}
			
			if (!entity.isSpectator()) {
				this.renderLayer(renderer, localPlayerPatch, entity, renderState, poses, buffer, poseStack, packedLight, partialTick);
			}
			
			poseStack.popPose();
		}
	}
	
	@Override
	protected void renderLayer(LivingEntityRenderer<LocalPlayer, ?, ?> renderer, LocalPlayerPatch entitypatch, LocalPlayer entity, LivingEntityRenderState renderState, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		float f2 = renderState.yRot;
		float f7 = renderState.xRot;
		float bob = renderState.ageInTicks;

		for (RenderLayer<?, ?> layer : renderer.layers) {
			Class<?> rendererClass = layer.getClass();

			if (rendererClass.isAnonymousClass()) {
				rendererClass = rendererClass.getSuperclass();
			}

			if (this.patchedLayers.containsKey(rendererClass)) {
				this.patchedLayers.get(rendererClass).renderLayer(entity, entitypatch, layer, renderState, poseStack, buffer, packedLight, poses, bob, f2, f7, partialTicks);
			}
		}
	}

	@Override
	public AssetAccessor<HumanoidMesh> getMeshProvider(LocalPlayerPatch entitypatch) {
		return PlayerModelType.WIDE == entitypatch.getOriginal().getSkin().model() ? Meshes.BIPED : Meshes.ALEX;
	}
	
	@Override
	public AssetAccessor<HumanoidMesh> getDefaultMesh() {
		return Meshes.BIPED;
	}
	
	@Override
	protected void prepareModel(HumanoidMesh mesh, LocalPlayer entity, LocalPlayerPatch entitypatch, LivingEntityRenderer<LocalPlayer, ?, ?> renderer) {
		mesh.initialize();
		mesh.head.setHidden(true);
		mesh.hat.setHidden(true);
	}
}