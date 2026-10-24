package yesman.epicfight.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import yesman.epicfight.client.renderer.LightningRenderHelper;

public class DroppedNetherStarRenderer extends ItemEntityRenderer {
	public DroppedNetherStarRenderer(Context context) {
		super(context);
	}

	@Override
	public void submit(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		super.submit(state, poseStack, submitNodeCollector, camera);

		poseStack.pushPose();
		poseStack.translate(0.0D, state.boundingBoxHeight + Mth.sin(state.ageInTicks / 10.0F + state.bobOffset) * 0.1F + 0.1F, 0.0D);
		float progression = state.ageInTicks * 0.01F;
		float repeater = ((float)Math.sin(progression * 5.0F) + 1.0F) * 0.5F;

		submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, vertexBuilder) -> {
			PoseStack lightPose = new PoseStack();
			lightPose.last().set(pose);
			LightningRenderHelper.renderCirclingLight(vertexBuilder, lightPose, 32, 0, 255, 9, 0.05F, progression, repeater);
		});

		poseStack.popPose();
	}
}
