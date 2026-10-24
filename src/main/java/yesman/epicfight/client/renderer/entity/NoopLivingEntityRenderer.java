package yesman.epicfight.client.renderer.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/// A living entity renderer without a vanilla model, for entities only Epic Fight renders.
///
/// Epic Fight takes over in the vanilla submit (see `MixinLivingEntityRendererSubmit`); without a
/// render type vanilla submits nothing of its own.
public class NoopLivingEntityRenderer<T extends LivingEntity> extends LivingEntityRenderer<T, LivingEntityRenderState, EntityModel<LivingEntityRenderState>> {
	public NoopLivingEntityRenderer(Context context, float shadowRadius) {
		super(context, null, shadowRadius);
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	protected RenderType getRenderType(LivingEntityRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
		return null;
	}

	@Override
	public Identifier getTextureLocation(LivingEntityRenderState state) {
		return null;
	}
}
