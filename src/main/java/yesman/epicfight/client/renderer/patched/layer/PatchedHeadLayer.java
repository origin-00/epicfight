package yesman.epicfight.client.renderer.patched.layer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PatchedHeadLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<?> & HeadedModel> extends PatchedLayer<E, T, M, CustomHeadLayer<?, ?>> {
	@Override
	protected void renderLayer(T entitypatch, E entityliving, CustomHeadLayer<?, ?> vanillaLayer, LivingEntityRenderState renderState, PoseStack postStack, MultiBufferSource buffer, int packedLightIn, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		if (!renderState.headItem.isEmpty() || renderState.wornHeadType != null) {
			// The vanilla layer positions the item with the head part; the Epic Fight head joint
			// replaces that, so the part is reset to the origin first.
			ModelPart head = ((HeadedModel)vanillaLayer.getParentModel()).getHead();
			head.setPos(0.0F, 0.0F, 0.0F);
			head.setRotation(0.0F, 0.0F, 0.0F);

			OpenMatrix4f modelMatrix = new OpenMatrix4f();
			modelMatrix.scale(new Vec3f(-1.0F, -1.0F, 1.0F)).mulFront(poses[9]).translate(0, 0.02F, 0);
			postStack.pushPose();

			MathUtils.mulStack(postStack, modelMatrix);

			if (entitypatch.getOriginal().isBaby()) {
				postStack.translate(0.0F, -1.2F, 0.0F);
				postStack.scale(1.6F, 1.6F, 1.6F);
			}

			submitVanillaLayer(vanillaLayer, postStack, buffer, packedLightIn, renderState, yRot, xRot);
			postStack.popPose();
		}
	}
}
