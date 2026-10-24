package yesman.epicfight.api.client.event.types.render;

import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.Event;

public class RenderEnderDragonEvent extends Event implements CancelableEvent {
	private final EnderDragon entity;
    private final EnderDragonRenderer renderer;
    private final float partialRenderTick;
    private final PoseStack poseStack;
    private final MultiBufferSource buffers;
    private final int light;
    private final EnderDragonRenderState renderState;
	
	public RenderEnderDragonEvent(EnderDragon entity, EnderDragonRenderer renderer, EnderDragonRenderState renderState, float partialRenderTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
		this.renderState = renderState;
		this.entity = entity;
        this.renderer = renderer;
        this.partialRenderTick = partialRenderTick;
        this.poseStack = poseStack;
        this.buffers = buffers;
        this.light = light;
	}
	
	/// The vanilla render state of this frame (render states replaced entity arguments in 1.21.2).
	public EnderDragonRenderState getRenderState() {
		return this.renderState;
	}

	public EnderDragon getEntity() {
		return entity;
	}

	public EnderDragonRenderer getRenderer() {
		return renderer;
	}
	
	public float getPartialRenderTick() {
		return partialRenderTick;
	}

	public PoseStack getPoseStack() {
		return poseStack;
	}

	public MultiBufferSource getBuffers() {
		return buffers;
	}

	public int getLight() {
		return light;
	}
}