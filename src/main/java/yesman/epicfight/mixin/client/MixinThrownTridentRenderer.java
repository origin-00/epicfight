package yesman.epicfight.mixin.client;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.projectile.ThrownTridentPatch;

/// Turns a trident returning through its innate skill toward its owner.
///
/// Since 1.21.9 the renderer orients the model from its render state, so the rotation is computed
/// when the state is extracted and applied before the vanilla rotations, which are then neutralized
/// on the state rather than by rewriting the entity's own rotation as before.
@Mixin(value = ThrownTridentRenderer.class)
public abstract class MixinThrownTridentRenderer {
	private static final ContextKey<Quaternionf> EPICFIGHT_RETURN_ROTATION = new ContextKey<> (EpicFight.identifier("trident_return_rotation"));

	@Inject(at = @At(value = "TAIL"), method = "extractRenderState(Lnet/minecraft/world/entity/projectile/arrow/ThrownTrident;Lnet/minecraft/client/renderer/entity/state/ThrownTridentRenderState;F)V")
	private void epicfight$extractRenderState(ThrownTrident tridentEntity, ThrownTridentRenderState state, float partialTicks, CallbackInfo info) {
		ThrownTridentPatch tridentPatch = EpicFightCapabilities.getEntityPatch(tridentEntity, ThrownTridentPatch.class);

		if (tridentPatch != null && tridentPatch.isInnateActivated() && tridentEntity.getOwner() != null) {
			Entity owner = tridentEntity.getOwner();
			Vec3 toOwner = owner.position().subtract(tridentEntity.position());
			Vec3 toOwnerHorizontalNorm = owner.position().subtract(tridentEntity.position()).subtract(0, toOwner.y, 0).normalize();
			Vec3 toOwnerNorm = toOwner.normalize();
			Vec3 rotAxis = toOwnerHorizontalNorm.cross(toOwnerNorm).normalize();
			float deg = (float) MathUtils.getAngleBetween(toOwnerNorm, toOwnerHorizontalNorm);

			Quaternionf rotation = QuaternionUtils.rotationDegrees(new Vector3f((float)rotAxis.x, (float)rotAxis.y, (float)rotAxis.z), deg)
				.mul(QuaternionUtils.XP.rotationDegrees(90.0F))
				.mul(QuaternionUtils.ZP.rotationDegrees(state.xRot + 90.0F));

			state.setRenderData(EPICFIGHT_RETURN_ROTATION, rotation);
			// Makes the vanilla YP(yRot - 90) and ZP(xRot + 90) rotations identities.
			state.yRot = 90.0F;
			state.xRot = -90.0F;
		}
	}

	@Inject(
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V",
            ordinal = 0
        ),
        method = "submit(Lnet/minecraft/client/renderer/entity/state/ThrownTridentRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
    )
	private void epicfight$submit(ThrownTridentRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo info) {
		Quaternionf rotation = state.getRenderData(EPICFIGHT_RETURN_ROTATION);

		if (rotation != null) {
			poseStack.mulPose(rotation);
			poseStack.translate(0.0D, -0.8D, -0.0D);
		}
	}
}
