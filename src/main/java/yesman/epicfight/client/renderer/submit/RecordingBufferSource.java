package yesman.epicfight.client.renderer.submit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/// A [MultiBufferSource] for Epic Fight's patched renderers on top of the 26.1 submit pipeline.
///
/// Renderers keep writing through [#getBuffer] as they did before 1.21.9; the vertices are
/// recorded per render type ([RecordedGeometry]) and handed to the [SubmitNodeCollector] by
/// [#submit] as custom geometry. Vertices are recorded already transformed, so submissions use an
/// identity pose.
///
/// The collector is exposed for the few cases that must use a vanilla submission directly, such
/// as item models ([net.minecraft.client.renderer.item.ItemStackRenderState]).
public final class RecordingBufferSource implements MultiBufferSource {
	private static final PoseStack IDENTITY = new PoseStack();

	private final @Nullable SubmitNodeCollector collector;
	private final int outlineColor;
	private final Map<RenderType, RecordedGeometry> quadBuffers = new LinkedHashMap<> ();
	private final Map<RenderType, RecordedGeometry> triangleBuffers = new LinkedHashMap<> ();

	/// @param outlineColor the entity outline color (glowing effect), or 0 for none
	public RecordingBufferSource(SubmitNodeCollector collector, int outlineColor) {
		this.collector = collector;
		this.outlineColor = outlineColor;
	}

	/// Records without a collector: the geometry is handed over later by [#submit(OrderedSubmitNodeCollector)],
	/// e.g. by particle groups, which extract before the frame's collector exists.
	public RecordingBufferSource() {
		this(null, 0);
	}

	public SubmitNodeCollector collector() {
		if (this.collector == null) {
			throw new IllegalStateException("Recording without a collector");
		}

		return this.collector;
	}

	public int outlineColor() {
		return this.outlineColor;
	}

	/// Buffer for writers that already produce the render type's own vertex mode.
	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		return this.quadBuffers.computeIfAbsent(renderType, k -> new RecordedGeometry(false));
	}

	/// Buffer for triangle-list writers (Epic Fight meshes).
	public VertexConsumer getTriangleBuffer(RenderType renderType) {
		return this.triangleBuffers.computeIfAbsent(renderType, k -> new RecordedGeometry(true));
	}

	/// Writes triangle-list geometry produced by `writer` into `bufferSource` for `renderType`.
	///
	/// When `bufferSource` records submissions the triangles are kept as such until replay;
	/// otherwise they are padded to the render type's vertex mode immediately.
	public static void drawTriangles(MultiBufferSource bufferSource, RenderType renderType, Consumer<VertexConsumer> writer) {
		if (bufferSource instanceof RecordingBufferSource recordingBufferSource) {
			writer.accept(recordingBufferSource.getTriangleBuffer(renderType));
		} else {
			RecordedGeometry geometry = new RecordedGeometry(true);
			writer.accept(geometry);
			geometry.replay(bufferSource.getBuffer(renderType), renderType.mode(), null);
		}
	}

	/// Hands every recorded buffer to the collector and clears this source.
	public void submit() {
		this.submit(this.collector());
	}

	/// Hands every recorded buffer to `target` and clears this source.
	public void submit(OrderedSubmitNodeCollector target) {
		this.submitAll(target, this.quadBuffers);
		this.submitAll(target, this.triangleBuffers);
	}

	private void submitAll(OrderedSubmitNodeCollector target, Map<RenderType, RecordedGeometry> buffers) {
		for (Map.Entry<RenderType, RecordedGeometry> entry : buffers.entrySet()) {
			RenderType renderType = entry.getKey();
			RecordedGeometry geometry = entry.getValue();

			if (geometry.isEmpty()) {
				continue;
			}

			target.submitCustomGeometry(IDENTITY, renderType, (pose, buffer) -> geometry.replay(buffer, renderType.mode(), null));

			if (this.outlineColor != 0) {
				renderType.outline().ifPresent(outline -> {
					target.submitCustomGeometry(IDENTITY, outline, (pose, buffer) -> geometry.replay(buffer, outline.mode(), this.outlineColor));
				});
			}
		}

		buffers.clear();
	}
}
