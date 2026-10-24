package yesman.epicfight.client.renderer.patched.item;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RenderShield extends RenderItemBase {
	public RenderShield(JsonElement jsonElement) {
		super(jsonElement);
	}
	
	@Override
	public void renderItemInHand(ItemStack itemstack, LivingEntityPatch<?> entitypatch, InteractionHand hand, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {


        OpenMatrix4f modelMatrix = this.getCorrectionMatrix(entitypatch, hand, poses);
		poseStack.pushPose();
		MathUtils.mulStack(poseStack, modelMatrix);
		ItemDisplayContext transformType = (hand == InteractionHand.MAIN_HAND) ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
		submitItem(entitypatch.getOriginal(), itemstack, transformType, poseStack, buffer, packedLight);
		poseStack.popPose();
	}
}
