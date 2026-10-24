package yesman.epicfight.client.particle;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class FeatherParticle extends SingleQuadParticle {
	protected FeatherParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, TextureAtlasSprite sprite) {
		super(level, x, y, z, dx, dy, dz, sprite);
		
		this.lifetime = 8 + this.random.nextInt(22);
		this.hasPhysics = true;
		this.gravity = 0.4F;
		this.friction = 0.8F;
		
		float roll = this.random.nextFloat() * 180F;
		this.oRoll = roll;
		this.roll = roll;
		
		this.xd = dx;
		this.yd = dy;
		this.zd = dz;
	}
	
	@Override
	protected SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.OPAQUE;
	}
	
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet sprite) {
			this.sprite = sprite;
		}
		
		@Override
		public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double dx, double dy, double dz, RandomSource random) {
			FeatherParticle featuerparticle = new FeatherParticle(level, x, y, z, dx, dy, dz, this.sprite.get(random));
			return featuerparticle;
		}
	}
}