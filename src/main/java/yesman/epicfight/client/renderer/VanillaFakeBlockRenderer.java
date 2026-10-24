package yesman.epicfight.client.renderer;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/// Draws the targeted block's own model as a translucent highlight (mining guide).
///
/// Before 1.21.9 the highlight reproduced the terrain's chunk offset uniform to land on exactly the
/// same depth for an equal-depth test. Terrain offsets are now applied per section internally, so
/// the highlight is drawn camera-relative with a small depth bias instead (see
/// [EpicFightRenderTypes#blockHighlight()]).
public class VanillaFakeBlockRenderer implements FakeBlockRenderer {
	private static final Direction[] DIRECTIONS = Direction.values();
	
	@Override
	public void render(Vec3 cameraPos, PoseStack poseStack, MultiBufferSource buffers, Level level, BlockPos bp, float r, float g, float b, float a) {
		BlockState bs = level.getBlockState(bp);
		
		if (bs.getRenderShape() != RenderShape.MODEL) {
			return;
		}
		
		RandomSource randomsource = RandomSource.create();
		randomsource.setSeed(bs.getSeed(bp));
		
		BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(bs);
		List<BlockStateModelPart> parts = new ArrayList<> ();
		model.collectParts(randomsource, parts);
		
		Vec3 offset = bs.getOffset(bp);
		poseStack.pushPose();
		poseStack.translate(bp.getX() - cameraPos.x + offset.x, bp.getY() - cameraPos.y + offset.y, bp.getZ() - cameraPos.z + offset.z);
		
		VertexConsumer buffer = buffers.getBuffer(EpicFightRenderTypes.blockHighlight());
		BlockPos.MutableBlockPos mutablepos = bp.mutable();
		
		for (BlockStateModelPart part : parts) {
			for (Direction d : DIRECTIONS) {
				mutablepos.setWithOffset(bp, d);
				
				if (Block.shouldRenderFace(bs, level.getBlockState(mutablepos), d)) {
					this.renderPreviewBlocks(poseStack, buffer, level, part.getQuads(d), r, g, b, a);
				}
			}
			
			this.renderPreviewBlocks(poseStack, buffer, level, part.getQuads(null), r, g, b, a);
		}
		
		poseStack.popPose();
	}
	
	private void renderPreviewBlocks(PoseStack poseStack, VertexConsumer consumer, Level level, List<BakedQuad> quads, float r, float g, float b, float a) {
		for (BakedQuad bakedquad : quads) {
			float shade = bakedquad.materialInfo().shade() ? Minecraft.getInstance().level.cardinalLighting().byFace(bakedquad.direction()) : 1.0F;
			yesman.epicfight.api.client.model.Mesh.DrawingFunction.POSITION_COLOR_TEX_LIGHTMAP.putBulkData(poseStack.last(), bakedquad, consumer, r * shade, g * shade, b * shade, a, 0xF000F0, OverlayTexture.NO_OVERLAY, false);
		}
	}
}
