package yesman.epicfight.client.gui.screen.overlay;



import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class BlendingTextureOverlay extends OverlayManager.Overlay {
	public Identifier texture;
	private boolean isAlive = true;
	
	public BlendingTextureOverlay(Identifier texture) {
		this.texture = texture;
	}
	
	public void remove() {
		this.isAlive = false;
	}
	
	@Override
	public boolean render(GuiGraphicsExtractor guiGraphics, int xResolution, int yResolution) {
		// GUI_TEXTURED blends with straight alpha, as the previous blend function set up.
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, 0, 0, 0.0F, 0.0F, xResolution, yResolution, xResolution, yResolution);
	    return !this.isAlive;
	}
}