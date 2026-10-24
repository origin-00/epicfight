package yesman.epicfight.client.gui.widgets;

import yesman.epicfight.client.gui.GuiColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class TextButton extends Button {
    private int hoveredOrFocusedColor = 7368816;
    private int defaultColor = 16777215;
    private final Font font;

    public TextButton(Font font, int x, int y, int width, int height, Component message, Button.OnPress onPress, Button.CreateNarration createNarration, int hoveredOrFocusedColor, int defaultColor) {
        super(x, y, width, height, message, onPress, createNarration);

        this.font = font;
        this.hoveredOrFocusedColor = hoveredOrFocusedColor;
        this.defaultColor = defaultColor;
    }

    public TextButton setHoveredOrFocusedColor(int packedARGB) {
        this.hoveredOrFocusedColor = packedARGB;
        return this;
    }

    public TextButton setDefaultColor(int packedARGB) {
        this.defaultColor = packedARGB;
        return this;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.text(this.font, this.getMessage(), this.getX() + 2, this.getY(), GuiColors.text(this.isHoveredOrFocused() ? this.hoveredOrFocusedColor : this.defaultColor));
    }
}
