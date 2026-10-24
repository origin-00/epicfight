package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/// Draws a block-shaped highlight over a block in the world.
///
/// @param cameraPos the camera position; geometry is drawn relative to it, as the level's
///                  model-view matrix expects since 1.21.9 (the camera is no longer passed in)
public interface FakeBlockRenderer {
	public void render(Vec3 cameraPos, PoseStack poseStack, MultiBufferSource buffers, Level level, BlockPos bp, float r, float g, float b, float a);
}
