package yesman.epicfight.client.renderer.patched.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.monster.zombie.DrownedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.Drowned;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.world.capabilities.entitypatch.mob.DrownedPatch;

public class OuterLayerRenderer extends ModelRenderLayer<Drowned, DrownedPatch, DrownedModel, DrownedOuterLayer, HumanoidMesh> {
	public static final Identifier DROWNED_OUTER_LAYER = Identifier.withDefaultNamespace("textures/entity/zombie/drowned_outer_layer.png");
	
	public OuterLayerRenderer() {
		super(Meshes.BIPED_OUTLAYER);
	}
	
	@Override
	protected void renderLayer(DrownedPatch entitypatch, Drowned entityliving, DrownedOuterLayer vanillaLayer, LivingEntityRenderState renderState, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		this.mesh.get().draw(poseStack, buffer, RenderTypes.entityCutout(DROWNED_OUTER_LAYER), packedLight, 1.0F, 1.0F, 1.0F, 1.0F, LivingEntityRenderer.getOverlayCoords(renderState, 0.0F), entitypatch.getArmature(), poses);
	}
	
	@Override
	protected void initMesh() {
		this.mesh.get().initialize();
	}
}