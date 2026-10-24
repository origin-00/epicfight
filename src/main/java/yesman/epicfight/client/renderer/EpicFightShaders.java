package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderPipelines;
import yesman.epicfight.EpicFight;

/// Epic Fight's own shaders. Since 1.21.5 a shader is referenced by a [RenderPipeline] (which also
/// fixes its vertex format and render state) instead of being loaded as a `ShaderInstance`.
public final class EpicFightShaders {
	private EpicFightShaders() {}
	
	/// Lit, vertex-colored triangles (`epicfight:core/solid_model`) for the model previews.
	public static final RenderPipeline SOLID_MODEL = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
		.withLocation(EpicFight.identifier("pipeline/solid_model"))
		.withVertexShader(EpicFight.identifier("core/solid_model"))
		.withFragmentShader(EpicFight.identifier("core/solid_model"))
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.TRIANGLES)
		.withDepthStencilState(DepthStencilState.DEFAULT)
		.build();
}
