package yesman.epicfight.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;

/// A particle that writes its own vertices, as every particle could before 1.21.9.
///
/// Vanilla particles now either extract a single quad ([net.minecraft.client.particle.SingleQuadParticle])
/// or belong to a dedicated [net.minecraft.client.particle.ParticleGroup]. Custom-drawn particles all
/// belong to [EpicFightParticleRenderTypes#CUSTOM], whose [CustomRenderParticleGroup] records the
/// vertices written here and submits them.
///
/// Vertices are written relative to the camera position, as before.
public abstract class CustomRenderParticle extends Particle {
	// Color, alpha and roll moved from Particle to SingleQuadParticle in 1.21.9.
	protected float rCol = 1.0F;
	protected float gCol = 1.0F;
	protected float bCol = 1.0F;
	protected float alpha = 1.0F;
	protected float roll;
	protected float oRoll;

	protected CustomRenderParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
	}

	protected CustomRenderParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
		super(level, x, y, z, xd, yd, zd);
	}

	@Override
	public ParticleRenderType getGroup() {
		return EpicFightParticleRenderTypes.CUSTOM;
	}

	/// The render type [#render(VertexConsumer, Camera, float)] writes for.
	public abstract RenderType getRenderType();

	/// Writes this particle's geometry. Override to draw with several render types or passes;
	/// passes in a higher layer are drawn after the lower ones.
	public void render(CustomRenderParticleGroup.LayeredBuffers buffers, Camera camera, float partialTicks) {
		this.render(buffers.layer(0).getBuffer(this.getRenderType()), camera, partialTicks);
	}

	public abstract void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks);

	public void setColor(float r, float g, float b) {
		this.rCol = r;
		this.gCol = g;
		this.bCol = b;
	}

	protected void setAlpha(float alpha) {
		this.alpha = alpha;
	}
}
