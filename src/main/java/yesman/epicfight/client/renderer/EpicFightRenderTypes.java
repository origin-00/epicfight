package yesman.epicfight.client.renderer;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.EpicFight;

/// Epic Fight's render types on the 26.1 [RenderPipeline]/[RenderSetup] model.
///
/// The pre-1.21.2 composite state shards map onto pipeline state as follows: shader, blending,
/// depth test/write, cull and vertex format live in the [RenderPipeline]; textures, lightmap,
/// overlay, layering, output target and texture transform live in the [RenderSetup].
///
/// Epic Fight meshes are triangle lists. They are no longer drawn through triangulated copies of
/// vanilla render types; [yesman.epicfight.client.renderer.submit.RecordingBufferSource#drawTriangles]
/// pads them into the quads every vanilla entity pipeline expects, so any vanilla render type
/// (including its outline) can be used as is.
public final class EpicFightRenderTypes {
	private EpicFightRenderTypes() {}

	private static final Identifier WHITE_TEXTURE = EpicFight.identifier("textures/common/white.png");
	private static final Identifier GLINT_WHITE_TEXTURE = EpicFight.identifier("textures/entity/overlay/glint_white.png");

	/// Kept for API compatibility: meshes are padded to quads instead of using triangle pipelines.
	@Deprecated
	public static RenderType makeTriangulated(RenderType renderType) {
		return renderType;
	}

	/// Kept for API compatibility: meshes are padded to quads instead of using triangle pipelines.
	@Deprecated
	public static RenderType getTriangulated(RenderType renderType) {
		return renderType;
	}

	/*****************************************
	 * Pipelines
	 *****************************************/
	private static final DepthStencilState EQUAL_NO_WRITE = new DepthStencilState(CompareOp.EQUAL, false);
	private static final ColorTargetState TRANSLUCENT_COLOR_ONLY = new ColorTargetState(java.util.Optional.of(BlendFunction.TRANSLUCENT), ColorTargetState.WRITE_COLOR);

	private static final RenderPipeline ENTITY_UI_COLOR_PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
		.withLocation(EpicFight.identifier("pipeline/entity_ui_color"))
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.build();

	private static final RenderPipeline ENTITY_UI_TEXTURE_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
		.withLocation(EpicFight.identifier("pipeline/entity_ui_texture"))
		.withVertexShader("core/position_tex")
		.withFragmentShader("core/position_tex")
		.withSampler("Sampler0")
		.withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.build();

	private static final RenderPipeline DEBUG_QUADS_PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
		.withLocation(EpicFight.identifier("pipeline/debug_quads"))
		.withColorTargetState(ColorTargetState.DEFAULT)
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.withCull(false)
		.build();

	/// Translucent entity shading drawn only where the entity itself was already drawn (equal
	/// depth, no depth write): decoration overlays and textured afterimages.
	private static final RenderPipeline ENTITY_TRANSLUCENT_EQUAL_DEPTH_PIPELINE = RenderPipelines.ENTITY_TRANSLUCENT.toBuilder()
		.withLocation(EpicFight.identifier("pipeline/entity_translucent_equal_depth"))
		.withColorTargetState(TRANSLUCENT_COLOR_ONLY)
		.withDepthStencilState(EQUAL_NO_WRITE)
		.withCull(false)
		.build();

	private static final RenderPipeline PARTICLE_EQUAL_DEPTH_PIPELINE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
		.withLocation(EpicFight.identifier("pipeline/particle_equal_depth"))
		.withColorTargetState(TRANSLUCENT_COLOR_ONLY)
		.withDepthStencilState(EQUAL_NO_WRITE)
		.withCull(false)
		.build();

	/// Writes depth only: lays down the afterimage silhouette for the equal-depth passes.
	private static final RenderPipeline DEPTH_STENCIL_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
		.withLocation(EpicFight.identifier("pipeline/afterimage_depth"))
		.withVertexShader("core/position_tex")
		.withFragmentShader("core/position_tex")
		.withSampler("Sampler0")
		.withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
		.withColorTargetState(new ColorTargetState(java.util.Optional.empty(), ColorTargetState.WRITE_NONE))
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.build();

	/// Drawn over the terrain block it highlights: depth-biased toward the camera instead of an
	/// equal-depth test, which needed the terrain's exact chunk offset (no longer a per-draw uniform).
	private static final RenderPipeline BLOCK_HIGHLIGHT_PIPELINE = RenderPipelines.TRANSLUCENT_BLOCK.toBuilder()
		.withLocation(EpicFight.identifier("pipeline/block_highlight"))
		.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false, -1.0F, -1.0F))
		.build();

	/// Armor glint tinted by vertex color. The 1.21.1 version tinted through the shader color
	/// uniform, which no longer exists per draw; the color now travels with each vertex.
	private static final RenderPipeline COLORED_GLINT_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET, RenderPipelines.FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
		.withLocation(EpicFight.identifier("pipeline/colored_glint"))
		.withVertexShader(EpicFight.identifier("core/colored_glint"))
		.withFragmentShader(EpicFight.identifier("core/colored_glint"))
		.withSampler("Sampler0")
		.withCull(false)
		.withColorTargetState(new ColorTargetState(BlendFunction.GLINT))
		.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
		.withDepthStencilState(EQUAL_NO_WRITE)
		.build();

	/// Every custom pipeline, registered for precompilation in RegisterRenderPipelinesEvent.
	public static final List<RenderPipeline> PIPELINES = ImmutableList.of(
		ENTITY_UI_COLOR_PIPELINE, ENTITY_UI_TEXTURE_PIPELINE, DEBUG_QUADS_PIPELINE, ENTITY_TRANSLUCENT_EQUAL_DEPTH_PIPELINE,
		PARTICLE_EQUAL_DEPTH_PIPELINE, DEPTH_STENCIL_PIPELINE, BLOCK_HIGHLIGHT_PIPELINE, COLORED_GLINT_PIPELINE, EpicFightShaders.SOLID_MODEL
	);

	/*****************************************
	 * Render types
	 *****************************************/
	private static final RenderType ENTITY_UI_COLORED = RenderType.create(
		EpicFight.identifier("ui_color").toString(),
		RenderSetup.builder(ENTITY_UI_COLOR_PIPELINE).createRenderSetup()
	);

	private static final Function<Identifier, RenderType> ENTITY_UI_TEXTURE = Util.memoize(texture -> RenderType.create(
		EpicFight.identifier("ui_texture").toString(),
		RenderSetup.builder(ENTITY_UI_TEXTURE_PIPELINE).withTexture("Sampler0", texture).createRenderSetup()
	));

	private static final RenderType DEBUG_QUADS = RenderType.create(
		EpicFight.identifier("debug_quad").toString(),
		RenderSetup.builder(DEBUG_QUADS_PIPELINE).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup()
	);

	private static final Function<Identifier, RenderType> OVERLAY_MODEL = Util.memoize(texture -> RenderType.create(
		EpicFight.identifier("overlay_model").toString(),
		RenderSetup.builder(ENTITY_TRANSLUCENT_EQUAL_DEPTH_PIPELINE).withTexture("Sampler0", texture).useLightmap().useOverlay().createRenderSetup()
	));

	private static final RenderType ENTITY_AFTERIMAGE_WHITE = RenderType.create(
		EpicFight.identifier("entity_afterimage").toString(),
		RenderSetup.builder(PARTICLE_EQUAL_DEPTH_PIPELINE).withTexture("Sampler0", WHITE_TEXTURE).useLightmap().sortOnUpload().createRenderSetup()
	);

	private static final RenderType ITEM_AFTERIMAGE_WHITE = RenderType.create(
		EpicFight.identifier("item_afterimage").toString(),
		RenderSetup.builder(PARTICLE_EQUAL_DEPTH_PIPELINE).withTexture("Sampler0", WHITE_TEXTURE).useLightmap().sortOnUpload().createRenderSetup()
	);

	private static final Function<Identifier, RenderType> ENTITY_PARTICLE = Util.memoize(texture -> RenderType.create(
		EpicFight.identifier("entity_particle").toString(),
		RenderSetup.builder(ENTITY_TRANSLUCENT_EQUAL_DEPTH_PIPELINE).withTexture("Sampler0", texture).useLightmap().useOverlay().sortOnUpload().createRenderSetup()
	));

	private static final RenderType ITEM_PARTICLE = RenderType.create(
		EpicFight.identifier("item_particle").toString(),
		RenderSetup.builder(ENTITY_TRANSLUCENT_EQUAL_DEPTH_PIPELINE).withTexture("Sampler0", TextureAtlas.LOCATION_ITEMS).useLightmap().useOverlay().sortOnUpload().createRenderSetup()
	);

	private static final Function<Identifier, RenderType> ENTITY_PARTICLE_STENCIL = Util.memoize(texture -> RenderType.create(
		EpicFight.identifier("entity_particle_stencil").toString(),
		RenderSetup.builder(DEPTH_STENCIL_PIPELINE).withTexture("Sampler0", texture).createRenderSetup()
	));

	private static final RenderType ITEM_PARTICLE_STENCIL = RenderType.create(
		EpicFight.identifier("item_particle_stencil").toString(),
		RenderSetup.builder(DEPTH_STENCIL_PIPELINE).withTexture("Sampler0", TextureAtlas.LOCATION_ITEMS).createRenderSetup()
	);

	private static final RenderType BLOCK_HIGHLIGHT = RenderType.create(
		EpicFight.identifier("block_highlight").toString(),
		RenderSetup.builder(BLOCK_HIGHLIGHT_PIPELINE).withTexture("Sampler0", WHITE_TEXTURE).useLightmap().sortOnUpload().createRenderSetup()
	);

	private static final RenderType COLORED_GLINT = RenderType.create(
		EpicFight.identifier("colored_glint").toString(),
		RenderSetup.builder(COLORED_GLINT_PIPELINE)
			.withTexture("Sampler0", GLINT_WHITE_TEXTURE)
			.setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
			.createRenderSetup()
	);

	/*****************************************
	 * Texture replacement
	 *****************************************/
	private static final Map<RenderType, Map<Identifier, RenderType>> TEXTURE_REPLACED = Maps.newConcurrentMap();

	/// The given render type with its main texture (`Sampler0`) replaced, for mesh parts that
	/// declare their own texture. Returns `renderType` itself if it has no texture to replace.
	public static RenderType replaceTexture(Identifier texture, RenderType renderType) {
		return TEXTURE_REPLACED.computeIfAbsent(renderType, k -> Maps.newConcurrentMap()).computeIfAbsent(texture, tex -> {
			RenderSetup state = renderType.state;

			if (!state.textures.containsKey("Sampler0")) {
				return renderType;
			}

			RenderSetup.RenderSetupBuilder builder = RenderSetup.builder(state.pipeline)
				.bufferSize(state.bufferSize)
				.setLayeringTransform(state.layeringTransform)
				.setOutputTarget(state.outputTarget)
				.setTextureTransform(state.textureTransform)
				.setOutline(state.outlineProperty);

			state.textures.forEach((sampler, binding) -> {
				builder.withTexture(sampler, sampler.equals("Sampler0") ? tex : binding.location(), binding.sampler());
			});

			if (state.useLightmap) builder.useLightmap();
			if (state.useOverlay) builder.useOverlay();
			if (state.affectsCrumbling) builder.affectsCrumbling();
			if (state.sortOnUpload) builder.sortOnUpload();

			return RenderType.create(renderType.name, builder.createRenderSetup());
		});
	}

	/*****************************************
	 * Accessors
	 *****************************************/
	public static RenderType entityUIColor() {
		return ENTITY_UI_COLORED;
	}

	public static RenderType entityUITexture(Identifier texture) {
		return ENTITY_UI_TEXTURE.apply(texture);
	}

	/// Collider debug lines: vanilla lines (with per-vertex line width since 1.21.9).
	public static RenderType debugCollider() {
		return RenderTypes.lines();
	}

	public static RenderType debugQuads() {
		return DEBUG_QUADS;
	}

	public static RenderType overlayModel(Identifier texture) {
		return OVERLAY_MODEL.apply(texture);
	}

	public static RenderType entityAfterimageStencil(Identifier texture) {
		return ENTITY_PARTICLE_STENCIL.apply(texture);
	}

	public static RenderType itemAfterimageStencil() {
		return ITEM_PARTICLE_STENCIL;
	}

	public static RenderType entityAfterimageTranslucent(Identifier texture) {
		return ENTITY_PARTICLE.apply(texture);
	}

	public static RenderType itemAfterimageTranslucent() {
		return ITEM_PARTICLE;
	}

	public static RenderType entityAfterimageWhite() {
		return ENTITY_AFTERIMAGE_WHITE;
	}

	public static RenderType itemAfterimageWhite() {
		return ITEM_AFTERIMAGE_WHITE;
	}

	public static RenderType blockHighlight() {
		return BLOCK_HIGHLIGHT;
	}

	/// Kept for API compatibility: colored glint no longer allocates a render type per entity.
	@Deprecated
	public static void freeUnusedWorldRenderTypes() {
	}

	/// Kept for API compatibility: colored glint no longer allocates a render type per entity.
	@Deprecated
	public static void clearWorldRenderTypes() {
	}

	/// Glint tinted by vertex color: draw it with a drawing function that writes the color
	/// (e.g. [yesman.epicfight.api.client.model.Mesh.DrawingFunction#NEW_ENTITY]) and pass the
	/// tint as the vertex color.
	public static RenderType coloredGlint() {
		return COLORED_GLINT;
	}

	/// @deprecated the tint is a vertex color now; use [#coloredGlint()] and pass the color to the draw call
	@Deprecated
	public static RenderType coloredGlintWorldRendertype(Entity owner, float r, float g, float b) {
		return COLORED_GLINT;
	}

	/// @deprecated the tint is a vertex color now; use [#coloredGlint()] and pass the color to the draw call
	@Deprecated
	public static RenderType coloredGlintWorldRendertype(Entity owner, int r, int g, int b) {
		return COLORED_GLINT;
	}
}
