package yesman.epicfight.client.renderer.patched.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PatchedEyesLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<?>, AM extends SkinnedMesh> extends ModelRenderLayer<E, T, M, EyesLayer<?, ?>, AM> {
	private final RenderType renderType;
	
	public PatchedEyesLayer(Identifier eyeTexture, AssetAccessor<AM> mesh) {
		super(mesh);
		this.renderType = RenderTypes.eyes(eyeTexture);
	}
	
	@Override
	protected void renderLayer(T entitypatch, E entityliving, EyesLayer<?, ?> vanillaLayer, LivingEntityRenderState renderState, PoseStack postStack, MultiBufferSource buffer, int packedLightIn, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		this.mesh.get().draw(postStack, buffer, this.renderType, 15728640, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, entitypatch.getArmature(), poses);
	}
}