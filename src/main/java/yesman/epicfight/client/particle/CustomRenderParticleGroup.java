package yesman.epicfight.client.particle;

import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import yesman.epicfight.client.renderer.submit.RecordingBufferSource;

/// The particle group of [CustomRenderParticle]s: records each particle's vertices while the
/// particles are extracted and submits them as custom geometry, one collector order per layer.
public class CustomRenderParticleGroup extends ParticleGroup<CustomRenderParticle> {
	public CustomRenderParticleGroup(ParticleEngine engine) {
		super(engine);
	}

	@Override
	public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
		LayeredBuffers buffers = new LayeredBuffers();

		for (CustomRenderParticle particle : this.particles) {
			try {
				particle.render(buffers, camera, partialTickTime);
			} catch (RuntimeException e) {
				LogUtils.getLogger().error("Failed to render particle {}", particle, e);
				particle.remove();
			}
		}

		return buffers;
	}

	/// Buffers by draw layer. Geometry of a higher layer is drawn after every lower layer, e.g. a
	/// translucent pass after the depth pass it is tested against.
	public static final class LayeredBuffers implements ParticleGroupRenderState {
		private final Int2ObjectSortedMap<RecordingBufferSource> layers = new Int2ObjectAVLTreeMap<> ();

		public MultiBufferSource layer(int layer) {
			return this.layers.computeIfAbsent(layer, k -> new RecordingBufferSource());
		}

		@Override
		public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
			this.layers.forEach((layer, buffers) -> buffers.submit(submitNodeCollector.order(layer)));
		}
	}
}
