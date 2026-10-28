package yesman.epicfight.client.renderer.submit;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/// A [VertexConsumer] that stores fully transformed vertices so they can be written later.
///
/// Since 1.21.9 entity geometry is *submitted* and drawn after every entity has been processed.
/// Epic Fight's armatures and meshes are shared between entities and mutated per entity (pose
/// matrices, hidden parts), so drawing in a deferred callback would read whatever the last entity
/// left behind. Recording at submit time captures each entity's own state; [#replay] writes it
/// into the real buffer when the submission is drawn.
///
/// Epic Fight meshes are triangle lists while vanilla's entity pipelines draw quads. When the
/// target pipeline uses quads, [#replay] repeats the third vertex of each triangle, turning it
/// into a degenerate quad that rasterises exactly as the original triangle.
public final class RecordedGeometry implements VertexConsumer {
	private static final int HAS_COLOR = 1;
	private static final int HAS_UV0 = 1 << 1;
	private static final int HAS_UV1 = 1 << 2;
	private static final int HAS_UV2 = 1 << 3;
	private static final int HAS_NORMAL = 1 << 4;
	private static final int HAS_LINE_WIDTH = 1 << 5;

	/// Written for line formats whose writer never set a width (the collider debug lines), since 26.1
	/// requires every line vertex to carry one.
	private static final float DEFAULT_LINE_WIDTH = 1.0F;

	// Per vertex: x, y, z, u, v, nx, ny, nz, lineWidth
	private static final int FLOATS = 9;
	// Per vertex: element flags, color, uv1, uv2
	private static final int INTS = 4;

	private final FloatArrayList floats = new FloatArrayList();
	private final IntArrayList ints = new IntArrayList();
	private final boolean triangles;
	private int vertexCount;

	/// @param triangles whether the recorded vertices form a triangle list (Epic Fight meshes) or
	///                  already match the target mode (vanilla-style quad writers)
	public RecordedGeometry(boolean triangles) {
		this.triangles = triangles;
	}

	public boolean isEmpty() {
		return this.vertexCount == 0;
	}

	private int floatBase() {
		return (this.vertexCount - 1) * FLOATS;
	}

	private int intBase() {
		return (this.vertexCount - 1) * INTS;
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		this.vertexCount++;
		this.floats.add(x);
		this.floats.add(y);
		this.floats.add(z);

		for (int i = 3; i < FLOATS; i++) {
			this.floats.add(0.0F);
		}

		for (int i = 0; i < INTS; i++) {
			this.ints.add(0);
		}

		return this;
	}

	@Override
	public VertexConsumer setColor(int r, int g, int b, int a) {
		return this.setColor((a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF));
	}

	@Override
	public VertexConsumer setColor(int color) {
		int base = this.intBase();
		this.ints.set(base, this.ints.getInt(base) | HAS_COLOR);
		this.ints.set(base + 1, color);
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		int base = this.floatBase();
		this.floats.set(base + 3, u);
		this.floats.set(base + 4, v);
		this.ints.set(this.intBase(), this.ints.getInt(this.intBase()) | HAS_UV0);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		int base = this.intBase();
		this.ints.set(base, this.ints.getInt(base) | HAS_UV1);
		this.ints.set(base + 2, (v & 0xFFFF) << 16 | (u & 0xFFFF));
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		int base = this.intBase();
		this.ints.set(base, this.ints.getInt(base) | HAS_UV2);
		this.ints.set(base + 3, (v & 0xFFFF) << 16 | (u & 0xFFFF));
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		int base = this.floatBase();
		this.floats.set(base + 5, x);
		this.floats.set(base + 6, y);
		this.floats.set(base + 7, z);
		this.ints.set(this.intBase(), this.ints.getInt(this.intBase()) | HAS_NORMAL);
		return this;
	}

	@Override
	public VertexConsumer setLineWidth(float width) {
		this.floats.set(this.floatBase() + 8, width);
		this.ints.set(this.intBase(), this.ints.getInt(this.intBase()) | HAS_LINE_WIDTH);
		return this;
	}

	/// Writes the recorded vertices into `target`, padding triangles to quads when `mode` is quads and
	/// supplying a line width when `format` needs one the writer never set.
	///
	/// @param colorOverride ARGB color replacing the recorded colors (used for entity outlines), or
	///                      `null` to keep them
	public void replay(VertexConsumer target, VertexFormat format, VertexFormat.Mode mode, Integer colorOverride) {
		boolean padToQuads = this.triangles && mode == VertexFormat.Mode.QUADS;
		boolean needsLineWidth = format.contains(VertexFormatElement.LINE_WIDTH);

		for (int i = 0; i < this.vertexCount; i++) {
			this.writeVertex(target, i, colorOverride, needsLineWidth);

			if (padToQuads && i % 3 == 2) {
				this.writeVertex(target, i, colorOverride, needsLineWidth);
			}
		}
	}

	private void writeVertex(VertexConsumer target, int index, Integer colorOverride, boolean needsLineWidth) {
		int f = index * FLOATS;
		int n = index * INTS;
		int flags = this.ints.getInt(n);

		target.addVertex(this.floats.getFloat(f), this.floats.getFloat(f + 1), this.floats.getFloat(f + 2));

		if (colorOverride != null) {
			target.setColor(colorOverride);
		} else if ((flags & HAS_COLOR) != 0) {
			target.setColor(this.ints.getInt(n + 1));
		}

		if ((flags & HAS_UV0) != 0) {
			target.setUv(this.floats.getFloat(f + 3), this.floats.getFloat(f + 4));
		}

		if ((flags & HAS_UV1) != 0) {
			int packed = this.ints.getInt(n + 2);
			target.setUv1(packed & 0xFFFF, packed >>> 16);
		}

		if ((flags & HAS_UV2) != 0) {
			int packed = this.ints.getInt(n + 3);
			target.setUv2(packed & 0xFFFF, packed >>> 16);
		}

		if ((flags & HAS_NORMAL) != 0) {
			target.setNormal(this.floats.getFloat(f + 5), this.floats.getFloat(f + 6), this.floats.getFloat(f + 7));
		}

		if ((flags & HAS_LINE_WIDTH) != 0) {
			target.setLineWidth(this.floats.getFloat(f + 8));
		} else if (needsLineWidth) {
			target.setLineWidth(DEFAULT_LINE_WIDTH);
		}
	}
}
