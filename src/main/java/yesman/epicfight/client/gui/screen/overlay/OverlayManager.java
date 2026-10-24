package yesman.epicfight.client.gui.screen.overlay;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class OverlayManager {
	private Map<String, OverlayManager.Overlay> overlays = Maps.newHashMap();
	private double modifiedGamma;
	private double originalGamma;
	private boolean isGammaChanged;

	public void renderTick(GuiGraphicsExtractor guiGraphics, int xResolution, int yResolution) {
		this.isGammaChanged = false;
		this.overlays.entrySet().removeIf((entry) -> entry.getValue().render(guiGraphics, xResolution, yResolution));
	}
	
	public void remove(String overlayId) {
		this.overlays.remove(overlayId);
	}
	
	public void setModifiedGamma(double originalGamma) {
		this.isGammaChanged = true;
		this.modifiedGamma = originalGamma;
	}

	public double getModifiedGamma(double originalGamma) {
		this.originalGamma = originalGamma;
		return this.modifiedGamma;
	}

	public double getOriginalGamma() {
		return this.originalGamma;
	}

	public boolean isGammaChanged() {
		return this.isGammaChanged;
	}

	public void blendingTexture(String overlayId, Identifier texture) {
		if (Minecraft.renderNames()) {
			this.overlays.put(overlayId, new BlendingTextureOverlay(texture));
		}
	}
	
	public void flickering(String overlayId, float deltaTime, float strength) {
		if (Minecraft.renderNames()) {
			this.overlays.put(overlayId, new FlickeringOverlay(deltaTime, strength));
		}
	}
	
	public abstract static class Overlay {
		/// @return whether the overlay is finished and should be removed
		public abstract boolean render(GuiGraphicsExtractor guiGraphics, int xResolution, int yResolution);
	}
}