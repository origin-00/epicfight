package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;

@Mixin(value = LivingEntityRenderer.class)
public interface MixinLivingEntityRenderer {
	@Invoker("isBodyVisible")
	public boolean invokeIsBodyVisible(LivingEntityRenderState state);
	
	@Invoker("getRenderType")
	public RenderType invokeGetRenderType(LivingEntityRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing);
}
