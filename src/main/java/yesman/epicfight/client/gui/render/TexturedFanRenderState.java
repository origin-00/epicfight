package yesman.epicfight.client.gui.render;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import yesman.epicfight.client.gui.GuiColors;

/// A textured triangle fan on the GUI, e.g. a cooldown clock wipe over a skill icon.
///
/// Up to 1.21.5 such shapes were drawn straight to a `TRIANGLE_FAN` buffer. GUI elements are now
/// batched as quads, so each triangle of the fan is emitted as a quad whose last vertex repeats.
///
/// @param vertices x, y pairs in GUI coordinates; the first vertex is the fan's center
/// @param uvs      u, v pairs, one per vertex
public record TexturedFanRenderState(
	RenderPipeline pipeline,
	TextureSetup textureSetup,
	Matrix3x2fc pose,
	float[] vertices,
	float[] uvs,
	int color,
	@Nullable ScreenRectangle scissorArea,
	@Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
	/// Submits a fan drawn with `texture`, tinted by `color` and by the graphics' current tint
	/// ([GuiColors#setColor]).
	public static void submit(GuiGraphicsExtractor guiGraphics, Identifier texture, float[] vertices, float[] uvs, int color) {
		AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
		Matrix3x2f pose = new Matrix3x2f(guiGraphics.pose());
		ScreenRectangle scissorArea = guiGraphics.peekScissorStack();
		int tint = ((GuiColors.Tintable)guiGraphics).epicfight$getTint();
		int tinted = tint == -1 ? color : net.minecraft.util.ARGB.multiply(color, tint);
		
		guiGraphics.submitGuiElementRenderState(new TexturedFanRenderState(
			RenderPipelines.GUI_TEXTURED,
			TextureSetup.singleTexture(abstractTexture.getTextureView(), abstractTexture.getSampler()),
			pose,
			vertices,
			uvs,
			tinted,
			scissorArea,
			bounds(vertices, pose, scissorArea)
		));
	}
	
	@Override
	public void buildVertices(VertexConsumer vertexConsumer) {
		int count = this.vertices.length / 2;
		
		for (int i = 1; i < count - 1; i++) {
			this.vertex(vertexConsumer, 0);
			this.vertex(vertexConsumer, i);
			this.vertex(vertexConsumer, i + 1);
			this.vertex(vertexConsumer, i + 1);
		}
	}
	
	private void vertex(VertexConsumer vertexConsumer, int index) {
		vertexConsumer.addVertexWith2DPose(this.pose, this.vertices[index * 2], this.vertices[index * 2 + 1]).setUv(this.uvs[index * 2], this.uvs[index * 2 + 1]).setColor(this.color);
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
