package yesman.epicfight.client.renderer.patched.layer;

import net.minecraft.client.model.EntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PatchedBeeStingerLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<?>> extends PatchedStuckInBodyLayer<E, T, M, BeeStingerLayer<?>> {
	@Override
	protected int numStuck(E entity) {
		return entity.getStingerCount();
	}
}
