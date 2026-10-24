package yesman.epicfight.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;

/// Color helpers for [GuiGraphicsExtractor].
public final class GuiColors {
	private GuiColors() {}

	/// The color the 1.21.1 `Font#adjustColor` produced: opaque when no alpha bits are set.
	///
	/// Up to 1.21.5 the font renderer treated a color without alpha bits (e.g. `0xFFFFFF`) as
	/// opaque. Since 1.21.6 text with zero alpha is skipped entirely, so the many RGB-only colors in
	/// Epic Fight's screens would turn invisible.
	public static int text(int color) {
		return (color & 0xFC000000) == 0 ? color | 0xFF000000 : color;
	}

	/// Multiplies the color of every following blit and fill of `guiGraphics`, as
	/// `GuiGraphics#setColor` and `RenderSystem#setShaderColor` did up to 1.21.5. Reset with
	/// `setColor(guiGraphics, 1.0F, 1.0F, 1.0F, 1.0F)`. Text is not affected.
	public static void setColor(GuiGraphicsExtractor guiGraphics, float r, float g, float b, float a) {
		((Tintable)guiGraphics).epicfight$setTint(ARGB.colorFromFloat(a, r, g, b));
	}

	/// Implemented by [GuiGraphicsExtractor] through `MixinGuiGraphicsExtractor`.
	public interface Tintable {
		void epicfight$setTint(int argb);

		int epicfight$getTint();
	}
}
