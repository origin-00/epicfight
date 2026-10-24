package yesman.epicfight.client.particle;

import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.ClassicMesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.main.EpicFightMod;

public class AirBurstParticle extends TexturedCustomModelParticle {
    public static final Identifier AIR_BURST_PARTICLE = EpicFightMod.identifier("textures/particle/air_burst.png");
	
	public AirBurstParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, AssetAccessor<ClassicMesh> particleMesh, Identifier texture) {
		super(level, x, y, z, xd, yd, zd, particleMesh, texture);
		
		this.scale = 0.1F;
		this.scaleO = 0.1F;
		this.lifetime = zd <= 0.0D ? 2 : (int)zd;
		this.pitch = (float)xd;
		this.pitchO = (float)xd;
		this.yaw = (float)yd;
		this.yawO = (float)yd;
	}
	
@Override
	public void tick() {
		super.tick();
		this.scale += 0.5F;
	}
	
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			return new AirBurstParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, Meshes.AIR_BURST, AIR_BURST_PARTICLE);
		}
	}
}