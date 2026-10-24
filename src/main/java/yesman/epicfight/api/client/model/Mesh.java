package yesman.epicfight.api.client.model;

import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.ARGB;
import net.minecraft.client.model.geom.builders.UVPair;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.annotation.Nullable;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.renderer.submit.RecordingBufferSource;

public interface Mesh {
	
	void initialize();
	
	/* Draw wihtout mesh deformation */
	void draw(PoseStack poseStack, VertexConsumer vertexConsumer, Mesh.DrawingFunction drawingFunction, int packedLight, float r, float g, float b, float a, int overlay);
	
	/* Draw with mesh deformation */
	void drawPosed(PoseStack poseStack, VertexConsumer vertexConsumer, Mesh.DrawingFunction drawingFunction, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses);
	
	/* Universal method */
	default void draw(PoseStack poseStack, MultiBufferSource bufferSources, RenderType renderType, Mesh.DrawingFunction drawingFunction, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses) {
		RecordingBufferSource.drawTriangles(bufferSources, renderType, buffer -> this.drawPosed(poseStack, buffer, drawingFunction, packedLight, r, g, b, a, overlay, armature, poses));
	}
	
	public static record RenderProperties(Identifier customTexturePath, Vec3f customColor, boolean isTransparent) {
		public static class Builder {
			protected String customTexturePath;
			protected Vec3f customColor = new Vec3f();
			protected boolean isTransparent;
			
			public RenderProperties.Builder customTexturePath(String path) {
				this.customTexturePath = path;
				return this;
			}
			
			public RenderProperties.Builder transparency(boolean isTransparent) {
				this.isTransparent = isTransparent;
				return this;
			}
			
			public RenderProperties.Builder customColor(float r, float g, float b) {
				this.customColor.x = r;
				this.customColor.y = g;
				this.customColor.z = b;
				return this;
			}
			
			public RenderProperties build() {
				return new RenderProperties(this.customTexturePath == null ? null : Identifier.parse(this.customTexturePath), this.customColor, this.isTransparent);
			}
			
			public static RenderProperties.Builder create() {
				return new RenderProperties.Builder();
			}
		}
	}
	
	@FunctionalInterface
	public interface DrawingFunction {
		public static final DrawingFunction NEW_ENTITY = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
			builder.addVertex(posX, posY, posZ)
			        .setColor(r, g, b, a)
			        .setUv(u, v)
			        .setOverlay(overlay)
			        .setLight(packedLight)
			        .setNormal(normX, normY, normZ);
		};
		
		public static final DrawingFunction POSITION_TEX = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
			builder.addVertex(posX, posY, posZ)
					.setUv(u, v);
		};
		
		public static final DrawingFunction POSITION_TEX_COLOR_NORMAL = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
			builder.addVertex(posX, posY, posZ)
					.setUv(u, v)
					.setColor(r, g, b, a)
					.setNormal(normX, normY, normZ);
		};
		
		public static final DrawingFunction POSITION_TEX_COLOR_LIGHTMAP = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
			builder.addVertex(posX, posY, posZ)
					.setUv(u, v)
					.setColor(r, g, b, a)
					.setLight(packedLight);
		};
		
		public static final DrawingFunction POSITION_COLOR_LIGHTMAP = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
			builder.addVertex(posX, posY, posZ)
					.setColor(r, g, b, a)
					.setLight(packedLight);
		};
		
		public static final DrawingFunction POSITION_COLOR_NORMAL = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
			builder.addVertex(posX, posY, posZ)
					.setColor(r, g, b, a)
					.setNormal(normX, normY, normZ);
		};
		
		public static final DrawingFunction POSITION_COLOR_TEX_LIGHTMAP = (builder, posX, posY, posZ, normX, normY, normZ, packedLight, r, g, b, a, u, v, overlay) -> {
			builder.addVertex(posX, posY, posZ)
					.setColor(r, g, b, a)
					.setUv(u, v)
					.setLight(packedLight);
		};
		
		public void draw(VertexConsumer vertexConsumer, float posX, float posY, float posZ, float normX, float normY, float normZ, int packedLight, float r, float g, float b, float a, float u, float v, int overlay);
		
	    default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int packedLight, int packedOverlay, boolean readExistingColor) {
	    	putBulkDataWithDrawingFunction(this, vertexConsumer, pose, bakedQuad, new float[] { 1.0F, 1.0F, 1.0F, 1.0F }, red, green, blue, alpha, new int[] { packedLight, packedLight, packedLight, packedLight }, packedOverlay, readExistingColor);
	    }

	    /// Writes a baked quad through a drawing function, like vanilla `VertexConsumer#putBakedQuad`.
	    ///
	    /// Since 1.21.5 a [BakedQuad] is a record of positions, packed UVs and per-vertex baked
	    /// normals/colors rather than packed vertex data, so the attributes are read directly.
		static void putBulkDataWithDrawingFunction(DrawingFunction drawingFunction, VertexConsumer builder, PoseStack.Pose pose, BakedQuad quad, float[] colorMuls, float red, float green, float blue, float alpha, int[] combinedLights, int combinedOverlay, boolean mulColor) {
			Matrix4f matrix = pose.pose();
			Vector3f faceNormal = pose.transformNormal(quad.direction().getUnitVec3f(), new Vector3f());
			int lightEmission = quad.materialInfo().lightEmission();

			for (int vertex = 0; vertex < BakedQuad.VERTEX_COUNT; vertex++) {
				Vector3f position = matrix.transformPosition(quad.position(vertex), new Vector3f());
				long packedUv = quad.packedUV(vertex);
				float r, g, b;

				if (mulColor) {
					int bakedColor = quad.bakedColors().color(vertex);
					r = ARGB.redFloat(bakedColor) * colorMuls[vertex] * red;
					g = ARGB.greenFloat(bakedColor) * colorMuls[vertex] * green;
					b = ARGB.blueFloat(bakedColor) * colorMuls[vertex] * blue;
				} else {
					r = colorMuls[vertex] * red;
					g = colorMuls[vertex] * green;
					b = colorMuls[vertex] * blue;
				}

				Vector3f normal = faceNormal;

				int bakedNormal = quad.bakedNormals().normal(vertex);
				
				if (!BakedNormals.isUnspecified(bakedNormal)) {
					normal = pose.transformNormal(BakedNormals.unpack(bakedNormal, null), new Vector3f());
				}

				drawingFunction.draw(builder, position.x(), position.y(), position.z(), normal.x(), normal.y(), normal.z(), LightCoordsUtil.lightCoordsWithEmission(combinedLights[vertex], lightEmission), r, g, b, alpha, UVPair.unpackU(packedUv), UVPair.unpackV(packedUv), combinedOverlay);
			}
		}
	}
}