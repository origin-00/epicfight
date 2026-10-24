package yesman.epicfight.client.gui.render;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

/// An untextured quad with a color per vertex on the GUI.
///
/// Up to 1.21.5 such shapes (horizontal gradients, rhombi) were written straight to the GUI buffer
/// source, which GUI rendering no longer exposes.
///
/// @param vertices x, y pairs of the four corners, in drawing order
/// @param colors   ARGB color of each corner
public record ColoredQuadRenderState(
	RenderPipeline pipeline,
	TextureSetup textureSetup,
	Matrix3x2fc pose,
	float[] vertices,
	int[] colors,
	@Nullable ScreenRectangle scissorArea,
	@Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
	public static void submit(GuiGraphicsExtractor guiGraphics, float[] vertices, int[] colors) {
		Matrix3x2f pose = new Matrix3x2f(guiGraphics.pose());
		ScreenRectangle scissorArea = guiGraphics.peekScissorStack();
		guiGraphics.submitGuiElementRenderState(new ColoredQuadRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), pose, vertices, colors, scissorArea, bounds(vertices, pose, scissorArea)));
	}

	/// A gradient from `colorLeft` on the left edge to `colorRight` on the right edge
	/// ([GuiGraphicsExtractor#fillGradient] only draws top to bottom).
	public static void horizontalGradient(GuiGraphicsExtractor guiGraphics, int x1, int y1, int x2, int y2, int colorLeft, int colorRight) {
		submit(guiGraphics, new float[] { x1, y1, x1, y2, x2, y2, x2, y1 }, new int[] { colorLeft, colorLeft, colorRight, colorRight });
	}

	/// A rhombus inscribed in the given rectangle.
	public static void rhombus(GuiGraphicsExtractor guiGraphics, int minX, int minY, int maxX, int maxY, int color) {
		float centerX = minX + (maxX - minX) / 2;
		float centerY = minY + (maxY - minY) / 2;
		submit(guiGraphics, new float[] { centerX, minY, minX, centerY, centerX, maxY, maxX, centerY }, new int[] { color, color, color, color });
	}

	@Override
	public void buildVertices(VertexConsumer vertexConsumer) {
		for (int i = 0; i < 4; i++) {
			vertexConsumer.addVertexWith2DPose(this.pose, this.vertices[i * 2], this.vertices[i * 2 + 1]).setColor(this.colors[i]);
		}
	}

	private static @Nullable ScreenRectangle bounds(float[] vertices, Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea) {
		float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

		for (int i = 0; i < vertices.length; i += 2) {
			minX = Math.min(minX, vertices[i]);
			maxX = Math.max(maxX, vertices[i]);
			minY = Math.min(minY, vertices[i + 1]);
			maxY = Math.max(maxY, vertices[i + 1]);
		}

		int x0 = (int)Math.floor(minX), y0 = (int)Math.floor(minY);
		ScreenRectangle bounds = new ScreenRectangle(x0, y0, (int)Math.ceil(maxX) - x0, (int)Math.ceil(maxY) - y0).transformMaxBounds(pose);
		return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
	}
}
