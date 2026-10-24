package yesman.epicfight.client.renderer.patched.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.monster.zombie.ZombieVillagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.resources.metadata.animation.VillagerMetadataSection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerDataHolder;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.VillagerMesh;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class PatchedVillagerProfessionLayer extends ModelRenderLayer<ZombieVillager, MobPatch<ZombieVillager>, ZombieVillagerModel<?>, VillagerProfessionLayer<?, ?>, VillagerMesh> {
	public PatchedVillagerProfessionLayer() {
		super(Meshes.VILLAGER_ZOMBIE);
	}
	
	@Override
	protected void renderLayer(MobPatch<ZombieVillager> entitypatch, ZombieVillager entityliving, VillagerProfessionLayer<?, ?> vanillaLayer, LivingEntityRenderState renderState, PoseStack postStack, MultiBufferSource buffer, int packedLight,
			OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		
		if (!entityliving.isInvisible()) {
			VillagerData villagerdata = ((VillagerDataHolder)entitypatch.getOriginal()).getVillagerData();
			
			VillagerMetadataSection.Hat typeHat = vanillaLayer.getHatData(vanillaLayer.typeHatCache, "type", villagerdata.type());
	        VillagerMetadataSection.Hat professionHat = vanillaLayer.getHatData(vanillaLayer.professionHatCache, "profession", villagerdata.profession());
	        
	        if (!(typeHat == VillagerMetadataSection.Hat.NONE || typeHat == VillagerMetadataSection.Hat.PARTIAL && professionHat != VillagerMetadataSection.Hat.FULL)
	        		|| !entityliving.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
	        	this.mesh.get().head.setHidden(true);
	        	this.mesh.get().hat.setHidden(true);
	        }
	        
	        if (!entitypatch.getOriginal().getItemBySlot(EquipmentSlot.LEGS).isEmpty()) {
				this.mesh.get().jacket.setHidden(true);
			}
	        
			this.mesh.get().draw(postStack, buffer, RenderTypes.entityCutout(vanillaLayer.getIdentifier("type", villagerdata.type())),
					packedLight, 1.0F, 1.0F, 1.0F, 1.0F, LivingEntityRenderer.getOverlayCoords(renderState, 0.0F), entitypatch.getArmature(), poses);
			
			if (!villagerdata.profession().is(VillagerProfession.NONE)) {
				this.mesh.get().draw(postStack, buffer, RenderTypes.entityCutout(vanillaLayer.getIdentifier("profession", villagerdata.profession())),
						packedLight, 1.0F, 1.0F, 1.0F, 1.0F, LivingEntityRenderer.getOverlayCoords(renderState, 0.0F), entitypatch.getArmature(), poses);
			}
		}
	}
}