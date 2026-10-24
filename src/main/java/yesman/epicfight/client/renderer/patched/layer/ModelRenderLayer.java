package yesman.epicfight.client.renderer.patched.layer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class ModelRenderLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<?>, R extends RenderLayer<?, ?>, AM extends SkinnedMesh> extends PatchedLayer<E, T, M, R> {
	protected final AssetAccessor<AM> mesh;
	
	public ModelRenderLayer(AssetAccessor<AM> mesh) {
		this.mesh = mesh;
	}
	
	@Override
	public void renderLayer(E entityliving, T entitypatch, RenderLayer<?, ?> vanillaLayer, LivingEntityRenderState renderState, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		this.initMesh();
		this.renderLayer(entitypatch, entityliving, this.castLayer(vanillaLayer), renderState, poseStack, buffer, packedLight, poses, bob, yRot, xRot, partialTicks);
	}
	
	protected void initMesh() {
		if (this.mesh != null) {
			this.mesh.get().initialize();
		}
	}
}