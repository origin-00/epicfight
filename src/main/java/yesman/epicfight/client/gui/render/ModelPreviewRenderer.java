package yesman.epicfight.client.gui.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;

/// Draws [ModelPreviewRenderState]s with a perspective camera, as the 1.21.1 model previewers did
/// in their own render targets. Registered in RegisterPictureInPictureRenderersEvent.
public class ModelPreviewRenderer extends PictureInPictureRenderer<ModelPreviewRenderState> {
	public static final float FOV = 50.0F;
	public static final float Z_NEAR = 0.05F;
	public static final float Z_FAR = 100.0F;

	private final Projection projection = new Projection();
	private final ProjectionMatrixBuffer projectionMatrixBuffer = new ProjectionMatrixBuffer("Epic Fight model preview");

	public ModelPreviewRenderer(MultiBufferSource.BufferSource bufferSource) {
		super(bufferSource);
	}

	@Override
	protected void renderToTexture(ModelPreviewRenderState renderState, PoseStack guiPoseStack) {
		// The base sets up an orthographic projection in texture pixels; previews use a perspective camera.
		this.projection.setupPerspective(Z_NEAR, Z_FAR, FOV, renderState.x1() - renderState.x0(), renderState.y1() - renderState.y0());
		RenderSystem.setProjectionMatrix(this.projectionMatrixBuffer.getBuffer(this.projection), ProjectionType.PERSPECTIVE);
		Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);

		FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
		renderState.painter().paint(new PoseStack(), this.bufferSource, featureRenderDispatcher.getSubmitNodeStorage());
		this.bufferSource.endBatch();
		featureRenderDispatcher.renderAllFeatures();
	}

	@Override
	public Class<ModelPreviewRenderState> getRenderStateClass() {
		return ModelPreviewRenderState.class;
	}

	@Override
	protected String getTextureLabel() {
		return "epicfight model preview";
	}

	@Override
	public void close() {
		super.close();
		this.projectionMatrixBuffer.close();
	}
}
