package yesman.epicfight.client.gui.render;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

/// A 3D model preview drawn into a GUI area by [ModelPreviewRenderer].
///
/// Up to 1.21.5 the model previewers drew into their own render target in the middle of GUI
/// rendering. GUI rendering is deferred since 1.21.6, so the scene is captured as a painter and
/// drawn when the GUI renders its picture-in-picture elements.
///
/// @param painter draws the scene in view space (camera at the origin, looking down -Z)
public record ModelPreviewRenderState(
	Painter painter,
	int x0,
	int y0,
	int x1,
	int y1,
	@Nullable ScreenRectangle scissorArea,
	@Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {
	public ModelPreviewRenderState(Painter painter, int x0, int y0, int x1, int y1, @Nullable ScreenRectangle scissorArea) {
		this(painter, x0, y0, x1, y1, scissorArea, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
	}

	@Override
	public float scale() {
		return 1.0F;
	}

	@FunctionalInterface
	public interface Painter {
		/// @param bufferSource drawn right after painting, for geometry written through vertex consumers
		/// @param collector    rendered after the buffer source, for vanilla submissions such as item models
		void paint(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, SubmitNodeCollector collector);
	}
}
