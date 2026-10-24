package yesman.epicfight.client.particle;

import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.RandomSource;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class BlastParticle extends HitParticle {
	public BlastParticle(ClientLevel world, double x, double y, double z, SpriteSet animatedSprite) {
		super(world, x, y, z, animatedSprite);
		this.rCol = 1.0F;
	    this.gCol = 1.0F;
	    this.bCol = 1.0F;
	    this.quadSize = 1.5F;
		this.lifetime = 2;
	}
	
	@Override
	public void tick() {
		this.xo = this.x;
	    this.yo = this.y;
	    this.zo = this.z;
	}
	
	@Override
	public void extract(QuadParticleRenderState particleTypeRenderState, Camera camera, float partialTicks) {
		if (!this.removed) {
			super.extract(particleTypeRenderState, camera, partialTicks);
			
			if (this.age++ >= this.lifetime) {
		       this.remove();
			}
		}
	}
	
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public Provider(SpriteSet p_i50607_1_) {
	         this.spriteSet = p_i50607_1_;
	    }
	    
		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			BlastParticle particle = new BlastParticle(worldIn, x, y, z, spriteSet);
			return particle;
		}
	}
}