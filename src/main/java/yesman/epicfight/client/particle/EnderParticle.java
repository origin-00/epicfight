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

public class EnderParticle extends SingleQuadParticle {
	private EnderParticle.Usage usage;
	
	public EnderParticle(ClientLevel worldIn, double x, double y, double z, double xd, double yd, double dz, TextureAtlasSprite sprite) {
		super(worldIn, x, y, z, sprite);
	    this.xd = xd;
	    this.yd = yd;
	    this.zd = dz;
	    this.x = x;
	    this.y = y;
	    this.z = z;
	    this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
	    float f = this.random.nextFloat() * 0.6F + 0.4F;
	    this.rCol = f * 0.9F;
	    this.gCol = f * 0.3F;
	    this.bCol = f;
	    this.lifetime = (int)(Math.random() * 10.0D) + 40;
	    this.hasPhysics = false;
	}
	
	@Override
	protected SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.OPAQUE;
	}
	
	@Override
	public int getLightCoords(float p_107086_) {
		if (this.usage == EnderParticle.Usage.DRAGON_BREATH_FLAME) {
			int i = super.getLightCoords(p_107086_);
			int k = i >> 16 & 255;
			return 240 | k << 16;
		} else {
			return super.getLightCoords(p_107086_);
		}
	}
	
	public void setUsage(EnderParticle.Usage usage) {
		this.usage = usage;
	}
	
	private enum Usage {
		DRAGON_BREATH_FLAME, ENDERMAN_DEATH
    }
	
	public static class EndermanDeathEmitProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public EndermanDeathEmitProvider(SpriteSet p_i50607_1_) {
	         this.spriteSet = p_i50607_1_;
	    }
		
		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			EnderParticle particle = new EnderParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet.get(random));
			particle.setUsage(EnderParticle.Usage.ENDERMAN_DEATH);
			return particle;
		}
	}
	
	public static class BreathFlameProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public BreathFlameProvider(SpriteSet p_i50607_1_) {
	         this.spriteSet = p_i50607_1_;
	    }
		
		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			EnderParticle particle = new EnderParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet.get(random));
			particle.quadSize *= 2.0F;
			particle.setUsage(EnderParticle.Usage.DRAGON_BREATH_FLAME);
			return particle;
		}
	}
}