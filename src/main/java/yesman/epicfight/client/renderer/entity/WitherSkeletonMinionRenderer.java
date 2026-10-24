package yesman.epicfight.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import yesman.epicfight.main.EpicFightMod;

public class WitherSkeletonMinionRenderer extends WitherSkeletonRenderer {
    private static final Identifier WITHER_SKELETON_LOCATION = EpicFightMod.identifier("textures/entity/wither_skeleton_minion.png");

	public WitherSkeletonMinionRenderer(Context context) {
		super(context);
	}
	
	@Override
	public Identifier getTextureLocation(SkeletonRenderState state) {
		return WITHER_SKELETON_LOCATION;
	}
}