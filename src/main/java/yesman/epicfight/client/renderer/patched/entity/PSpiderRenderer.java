package yesman.epicfight.client.renderer.patched.entity;

import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.spider.Spider;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.mesh.SpiderMesh;
import yesman.epicfight.client.renderer.patched.layer.PatchedEyesLayer;
import yesman.epicfight.world.capabilities.entitypatch.mob.SpiderPatch;

public class PSpiderRenderer extends PatchedLivingEntityRenderer<Spider, SpiderPatch<Spider>, SpiderModel, SpiderRenderer<Spider>, SpiderMesh> {
	private static final Identifier SPIDER_EYE_TEXTURE = Identifier.withDefaultNamespace("textures/entity/spider_eyes.png");
	
	public PSpiderRenderer(EntityRendererProvider.Context context, EntityType<?> entityType) {
		super(context, entityType);
		this.addPatchedLayer(SpiderEyesLayer.class, new PatchedEyesLayer<>(SPIDER_EYE_TEXTURE, Meshes.SPIDER));
	}
	
	@Override
	public AssetAccessor<SpiderMesh> getDefaultMesh() {
		return Meshes.SPIDER;
	}
}