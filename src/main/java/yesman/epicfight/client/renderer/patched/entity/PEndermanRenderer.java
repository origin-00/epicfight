package yesman.epicfight.client.renderer.patched.entity;

import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.mesh.EndermanMesh;
import yesman.epicfight.client.renderer.patched.layer.PatchedEyesLayer;
import yesman.epicfight.world.capabilities.entitypatch.mob.EndermanPatch;

public class PEndermanRenderer extends PatchedLivingEntityRenderer<EnderMan, EndermanPatch, EndermanModel<?>, EndermanRenderer, EndermanMesh> {
	private static final Identifier ENDERMAN_EYE_TEXTURE = Identifier.withDefaultNamespace("textures/entity/enderman/enderman_eyes.png");
	
	public PEndermanRenderer(EntityRendererProvider.Context context, EntityType<?> entityType) {
		super(context, entityType);
		
		this.addPatchedLayer(EnderEyesLayer.class, new PatchedEyesLayer<>(ENDERMAN_EYE_TEXTURE, Meshes.ENDERMAN));
	}
	
	@Override
	public AssetAccessor<EndermanMesh> getDefaultMesh() {
		return Meshes.ENDERMAN;
	}
}