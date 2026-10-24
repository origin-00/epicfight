package yesman.epicfight.client.renderer.patched.layer;

import net.minecraft.client.model.EntityModel;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PatchedArrowLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<?>> extends PatchedStuckInBodyLayer<E, T, M, ArrowLayer<?>> {
	public PatchedArrowLayer(EntityRendererProvider.Context context) {
	}

	@Override
	protected int numStuck(E entity) {
		return entity.getArrowCount();
	}
}
