package yesman.epicfight.client.renderer.patched.layer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.layers.IronGolemCrackinessLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.animal.golem.IronGolem;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.IronGolemMesh;
import yesman.epicfight.world.capabilities.entitypatch.mob.IronGolemPatch;

public class PatchedGolemCrackLayer extends ModelRenderLayer<IronGolem, IronGolemPatch, IronGolemModel, IronGolemCrackinessLayer, IronGolemMesh> {
	private static final Map<Crackiness.Level, Identifier> CRACK_MAP = ImmutableMap.of(
			Crackiness.Level.LOW, Identifier.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_low.png"),
			Crackiness.Level.MEDIUM, Identifier.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_medium.png"),
			Crackiness.Level.HIGH, Identifier.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_high.png"));
	
	public PatchedGolemCrackLayer(AssetAccessor<IronGolemMesh> mesh) {
		super(mesh);
	}
	
	@Override
	protected void renderLayer(IronGolemPatch entitypatch, IronGolem golementity, IronGolemCrackinessLayer vanillaLayer, LivingEntityRenderState renderState, PoseStack postStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		Crackiness.Level crack = golementity.getCrackiness();
		
		if (crack != Crackiness.Level.NONE) {
			this.mesh.get().draw(postStack, buffer, RenderTypes.entityCutout(CRACK_MAP.get(crack)), packedLight, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, entitypatch.getArmature(), poses);
		}
	}
}