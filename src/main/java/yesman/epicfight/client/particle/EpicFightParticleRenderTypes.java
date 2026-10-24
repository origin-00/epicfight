package yesman.epicfight.client.particle;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import yesman.epicfight.EpicFight;

/// Render types of Epic Fight's custom-drawn particles ([CustomRenderParticle]).
///
/// Before 1.21.9 these were [ParticleRenderType]s that set the GL state themselves. Particle render
/// types are now only group keys, so all custom-drawn particles share the [#CUSTOM] group and each
/// particle names the [RenderType] its vertices are drawn with.
public final class EpicFightParticleRenderTypes {
	private EpicFightParticleRenderTypes() {}

	/// The particle group of every [CustomRenderParticle] (see [CustomRenderParticleGroup]).
	public static final ParticleRenderType CUSTOM = new ParticleRenderType("epicfight:custom");

	private static final DepthStencilState DEPTH_TEST_NO_WRITE = new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false);

	/// Textured particle meshes (triangle lists) without normals.
	private static final RenderPipeline PARTICLE_MODEL_NO_NORMAL_PIPELINE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
		.withLocation(EpicFight.identifier("pipeline/particle_model_no_normal"))
		.withVertexFormat(DefaultVertexFormat.PARTICLE, VertexFormat.Mode.TRIANGLES)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withCull(false)
		.build();

	/// Additive, untextured flashes.
	private static final RenderPipeline LIGHTNING_PIPELINE = RenderPipelines.LIGHTNING.toBuilder()
		.withLocation(EpicFight.identifier("pipeline/particle_lightning"))
		.withDepthStencilState(DEPTH_TEST_NO_WRITE)
		.build();

	private static final RenderPipeline TRAIL_EFFECT_PIPELINE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
		.withLocation(EpicFight.identifier("pipeline/trail_effect"))
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(DEPTH_TEST_NO_WRITE)
		.withCull(false)
		.build();

	/// Translucent, untextured triangle meshes (lasers).
	private static final RenderPipeline TRANSLUCENT_GLOWING_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
		.withLocation(EpicFight.identifier("pipeline/translucent_glowing"))
		.withVertexShader("core/position_color")
		.withFragmentShader("core/position_color")
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.build();

	/// Registered for precompilation with [yesman.epicfight.client.renderer.EpicFightRenderTypes#PIPELINES].
	public static final List<RenderPipeline> PIPELINES = ImmutableList.of(
		PARTICLE_MODEL_NO_NORMAL_PIPELINE, LIGHTNING_PIPELINE, TRAIL_EFFECT_PIPELINE, TRANSLUCENT_GLOWING_PIPELINE
	);

	private static final Function<Identifier, RenderType> PARTICLE_MODEL_NO_NORMAL = Util.memoize(texture -> RenderType.create(
		EpicFight.identifier("particle_model_no_normal").toString(),
		RenderSetup.builder(PARTICLE_MODEL_NO_NORMAL_PIPELINE).withTexture("Sampler0", texture).useLightmap().sortOnUpload().createRenderSetup()
	));

	private static final RenderType LIGHTNING = RenderType.create(
		EpicFight.identifier("particle_lightning").toString(),
		RenderSetup.builder(LIGHTNING_PIPELINE).sortOnUpload().createRenderSetup()
	);

	/// Trail textures are sampled clamped: a trail stretches its texture once along its length.
	private static final Function<Identifier, RenderType> TRAIL_EFFECT = Util.memoize(texture -> RenderType.create(
		EpicFight.identifier("trail_effect").toString(),
		RenderSetup.builder(TRAIL_EFFECT_PIPELINE)
			.withTexture("Sampler0", texture, () -> RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR))
			.useLightmap()
			.sortOnUpload()
			.createRenderSetup()
	));

	private static final RenderType TRANSLUCENT_GLOWING = RenderType.create(
		EpicFight.identifier("translucent_glowing").toString(),
		RenderSetup.builder(TRANSLUCENT_GLOWING_PIPELINE).sortOnUpload().createRenderSetup()
	);

	public static RenderType particleModelNoNormal(Identifier texture) {
		return PARTICLE_MODEL_NO_NORMAL.apply(texture);
	}

	public static RenderType lightning() {
		return LIGHTNING;
	}

	public static RenderType trailEffect(Identifier texture) {
		return TRAIL_EFFECT.apply(texture);
	}

	public static RenderType translucentGlowing() {
		return TRANSLUCENT_GLOWING;
	}
}
