package yesman.epicfight.client.renderer.patched.layer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PatchedElytraLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<?>> extends PatchedLayer<E, T, M, WingsLayer<?, ?>> {
	@Override
	protected void renderLayer(T entitypatch, E livingentity, WingsLayer<?, ?> vanillaLayer, LivingEntityRenderState renderState, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		Equippable equippable = livingentity.getItemBySlot(EquipmentSlot.CHEST).get(DataComponents.EQUIPPABLE);

		if (equippable != null && equippable.assetId().isPresent()) {
			OpenMatrix4f modelMatrix = new OpenMatrix4f();
			modelMatrix.scale(new Vec3f(-0.9F, -0.9F, 0.9F)).translate(new Vec3f(0.0F, -0.5F, -0.1F)).mulFront(poses[8]);
			poseStack.pushPose();
			MathUtils.mulStack(poseStack, modelMatrix);
			submitVanillaLayer(vanillaLayer, poseStack, buffer, packedLight, renderState, yRot, xRot);
			poseStack.popPose();
		}
	}
}
