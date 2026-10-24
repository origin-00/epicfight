package yesman.epicfight.client.renderer.blockentity;

import org.jspecify.annotations.Nullable;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.world.level.block.entity.FractureBlockEntity;

/// Renders the original block of a ground fracture, tilted and bouncing.
///
/// Since 1.21.9 block entities render in two steps: the animation is computed into a render state
/// at extraction, then the block is submitted as a moving block (like piston-moved blocks).
public class FractureBlockRenderer implements BlockEntityRenderer<FractureBlockEntity, FractureBlockRenderer.FractureRenderState> {
	public FractureBlockRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public boolean shouldRender(FractureBlockEntity p_173568_, Vec3 p_173569_) {
		return Vec3.atCenterOf(p_173568_.getBlockPos()).closerThan(p_173569_, this.getViewDistance());
	}

	@Override
	public FractureRenderState createRenderState() {
		return new FractureRenderState();
	}

	@Override
	public void extractRenderState(FractureBlockEntity blockEntity, FractureRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

		float turnBackTime = 5.0F;
		float lerpAmount = Mth.clamp(partialTicks * (1.0F / turnBackTime) + (turnBackTime - (blockEntity.getMaxLifeTime() - blockEntity.getLifeTime())) * (1.0F / turnBackTime), 0.0F, 1.0F);

		state.translate = blockEntity.getMaxLifeTime() > blockEntity.getLifeTime() + turnBackTime ? blockEntity.getTranslate() : MathUtils.lerpMojangVector(blockEntity.getTranslate(), new Vector3f(), lerpAmount);
		state.rotation = blockEntity.getMaxLifeTime() > blockEntity.getLifeTime() + turnBackTime ? blockEntity.getRotation() : MathUtils.lerpQuaternion(blockEntity.getRotation(), new Quaternionf(), lerpAmount);

		double BOUNCE_MAX_HEIGHT = blockEntity.getBouncing();
		double TIME = Math.max(BOUNCE_MAX_HEIGHT * 8.0D, 8.0D);
		double EXTENDER = 1 / Math.pow(TIME * 0.5, 2);
		double MOVE_GRAPH = Math.sqrt(BOUNCE_MAX_HEIGHT / EXTENDER);
		state.bouncing = Math.max(-EXTENDER * Math.pow(blockEntity.getLifeTime() + partialTicks - MOVE_GRAPH, 2.0D) + BOUNCE_MAX_HEIGHT, 0.0D);

		state.block = null;

		if (blockEntity.getLevel() instanceof ClientLevel level && !blockEntity.getOriginalBlockState().isAir()) {
			BlockPos pos = blockEntity.getBlockPos().above();
			MovingBlockRenderState movingBlock = new MovingBlockRenderState();
			movingBlock.randomSeedPos = pos;
			movingBlock.blockPos = pos;
			movingBlock.blockState = blockEntity.getOriginalBlockState();
			movingBlock.biome = level.getBiome(pos);
			movingBlock.cardinalLighting = level.cardinalLighting();
			movingBlock.lightEngine = level.getLightEngine();
			state.block = movingBlock;
		}
	}

	@Override
	public void submit(FractureRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		if (state.block == null) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0.5D, 0.5D, 0.5D);
		poseStack.mulPose(state.rotation);
		poseStack.translate(state.translate.x(), state.translate.y() + state.bouncing, state.translate.z());
		poseStack.translate(-0.5D, -0.5D, -0.5D);
		submitNodeCollector.submitMovingBlock(poseStack, state.block);
		poseStack.popPose();
	}

	public static class FractureRenderState extends BlockEntityRenderState {
		Vector3f translate = new Vector3f();
		Quaternionf rotation = new Quaternionf();
		double bouncing;
		@Nullable MovingBlockRenderState block;
	}
}
