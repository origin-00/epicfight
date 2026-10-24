package yesman.epicfight.client.gui.widgets;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import yesman.epicfight.client.gui.screen.SideBarScreen;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.client.gui.widgets.common.PressableWidget;
import yesman.epicfight.main.EpicFightMod;

import java.util.List;
import java.util.function.Function;

public class SideNavigationBarOpener extends AbstractWidget implements AnchoredWidget, PressableWidget {
    private static final Identifier ARROW_SPRITE = EpicFightMod.identifier("widget/arrow");
    private static final Identifier ARROW_HIGHLIGHTED_SPRITE = EpicFightMod.identifier("widget/arrow_highlighted");

    private final Screen owner;
    private final Screen twoStepsParent;
    private final Function<Screen, List<SideBarScreen.SideNavigationBarEntry>> entriesProvider;

    public SideNavigationBarOpener(Screen owner, Screen twoStepsParent, Function<Screen, List<SideBarScreen.SideNavigationBarEntry>> entriesProvider) {
        super(0, 0, 0, 21, Component.empty());

        this.owner = owner;
        this.twoStepsParent = twoStepsParent;
        this.entriesProvider = entriesProvider;
        this.x1 = 0;
        this.x2 = 8;
        this.y1 = 0;
        this.y2 = 34;
        this.horizontalAnchorType = HorizontalAnchorType.LEFT_WIDTH;
        this.verticalAnchorType = VerticalAnchorType.CENTER_HEIGHT;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.setWidth(this.isHoveredOrFocused() ? 16 : 8);
        int outlineColor = this.isHoveredOrFocused() ? -6250336 : -12566463;
        int minX = this.getX();
        int minY = this.getY();
        int maxX = this.getX() + this.getWidth();
        int maxY = this.getY() + this.getHeight();

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(0F, 0F);

        guiGraphics.fill(minX, minY, maxX, maxY, outlineColor);
        guiGraphics.fill(minX, minY + 1, maxX - 1, maxY - 1, 0xFF000000);

        Identifier resourcelocation = this.isHoveredOrFocused() ? ARROW_HIGHLIGHTED_SPRITE : ARROW_SPRITE;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourcelocation, this.getX() + this.getWidth() / 2 - 3, this.getY() + this.getHeight() / 2 - 3, 4, 6);

        guiGraphics.pose().popMatrix();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (this.active && this.visible) {
            if (this.isValidClickButton(event.buttonInfo())) {
                boolean flag = this.isMouseOver(mouseX, mouseY);
                if (flag) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onPressed();
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int scanCode = event.scancode();
        int modifiers = event.modifiers();
        if (this.active && this.visible) {
            if (new KeyEvent(keyCode, 0, 0).isSelection()) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.onPressed();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void onPressed() {
        this.owner.getMinecraft().setScreen(new SideBarScreen(this.owner, this.entriesProvider.apply(this.twoStepsParent), 110));
    }

    /*******************************************************************
     * {@link AnchoredWidget} implementations                          *
     *******************************************************************/
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private final AnchoredWidget.HorizontalAnchorType horizontalAnchorType;
    private final VerticalAnchorType verticalAnchorType;

    @Override
    public int getX1() {
        return this.x1;
    }

    @Override
    public int getX2() {
        return this.x2;
    }

    @Override
    public int getY1() {
        return this.y1;
    }

    @Override
    public int getY2() {
        return this.y2;
    }

    @Override
    public void setX1(int i) {
        this.x1 = i;
    }

    @Override
    public void setX2(int i) {
        this.x2 = i;
    }

    @Override
    public void setY1(int i) {
        this.y1 = i;
    }

    @Override
    public void setY2(int i) {
        this.y2 = i;
    }

    @Override
    public HorizontalAnchorType getHorizontalAnchorType() {
        return this.horizontalAnchorType;
    }

    @Override
    public VerticalAnchorType getVerticalAnchorType() {
        return this.verticalAnchorType;
    }
}
