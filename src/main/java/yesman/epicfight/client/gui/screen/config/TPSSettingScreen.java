package yesman.epicfight.client.gui.screen.config;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import yesman.epicfight.client.gui.GuiColors;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import yesman.epicfight.client.gui.widgets.AnchoredButton;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.generated.LangKeys;
import yesman.epicfight.main.EpicFightMod;

public class TPSSettingScreen extends Screen {
    private static final Identifier DIRECTION_BUTTON_SPRITE = EpicFightMod.identifier("widget/direction_button");
    private static final Identifier DIRECTION_BUTTON_DISABLED_SPRITE = EpicFightMod.identifier("widget/direction_button_disabled");
    private static final Identifier DIRECTION_BUTTON_HIGHLIGHTED_SPRITE = EpicFightMod.identifier("widget/direction_button_highlighted");
    private static final Identifier ZOOM_SCROLL_SPRITE = EpicFightMod.identifier("widget/zoom_scroll");

    private static final Component NO_WORLD_WARNING = Component.translatable(LangKeys.GUI_MESSAGE_SETTINGS_CAMERA_TPS_PERSPECTIVE_RECOMMEND_JOIN_WORLD);

    private final Screen parentScreen;
    private final AnchoredButton up;
    private final AnchoredButton down;
    private final AnchoredButton left;
    private final AnchoredButton right;
    private final ZoomScroll zoomScroll;

    protected TPSSettingScreen(Screen parentScreen) {
        super(Component.translatable(LangKeys.GUI_TITLE_SETTINGS_CAMERA_TPS_SETTINGS));

        this.parentScreen = parentScreen;

        this.up = new CameraMoveButton(
            0,
            14,
            parentScreen.height / 5 - 7,
            14,
            button -> {
                ClientConfig.cameraVerticalLocation = Math.min(5, ClientConfig.cameraVerticalLocation + 1);
                this.getDown().active = true;
                if (ClientConfig.cameraVerticalLocation >= 5) this.getUp().active = false;
            },
            AnchoredWidget.HorizontalAnchorType.CENTER_WIDTH,
            AnchoredWidget.VerticalAnchorType.TOP_HEIGHT,
            CameraMoveButton.Direction.UP
        );

        this.down = new CameraMoveButton(
            0,
            14,
            parentScreen.height / 5 - 7,
            14,
            button -> {
                ClientConfig.cameraVerticalLocation = Math.max(-2, ClientConfig.cameraVerticalLocation - 1);
                this.getUp().active = true;
                if (ClientConfig.cameraVerticalLocation <= -2) this.getDown().active = false;
            },
            AnchoredWidget.HorizontalAnchorType.CENTER_WIDTH,
            AnchoredWidget.VerticalAnchorType.BOTTOM_HEGIHT,
            CameraMoveButton.Direction.DOWN
        );

        this.left = new CameraMoveButton(
            this.width / 5 - 7,
            14,
            0,
            14,
            button -> {
                ClientConfig.cameraHorizontalLocation = Math.min(10, ClientConfig.cameraHorizontalLocation + 1);
                this.getRight().active = true;
                if (ClientConfig.cameraHorizontalLocation >= 10) this.getLeft().active = false;
            },
            AnchoredWidget.HorizontalAnchorType.LEFT_WIDTH,
            AnchoredWidget.VerticalAnchorType.CENTER_HEIGHT,
            CameraMoveButton.Direction.LEFT
        );

        this.right = new CameraMoveButton(
            this.width / 5 - 7,
            14,
            0,
            14,
            button -> {
                ClientConfig.cameraHorizontalLocation = Math.max(-10, ClientConfig.cameraHorizontalLocation - 1);
                this.getLeft().active = true;
                if (ClientConfig.cameraHorizontalLocation <= -10) this.getRight().active = false;
            },
            AnchoredWidget.HorizontalAnchorType.RIGHT_WIDTH,
            AnchoredWidget.VerticalAnchorType.CENTER_HEIGHT,
            CameraMoveButton.Direction.RIGHT
        );

        this.zoomScroll = new ZoomScroll(this.width / 2 + 24, this.height / 2 - 26, ClientConfig.cameraZoom - 3);
    }

    private AnchoredButton getUp() {
        return this.up;
    }

    private AnchoredButton getDown() {
        return this.down;
    }

    private AnchoredButton getLeft() {
        return this.left;
    }

    private AnchoredButton getRight() {
        return this.right;
    }

    @Override
    public void init() {
        this.up.setY1(this.height / 5 - 7);
        this.down.setY1(this.height / 5 - 7);
        this.left.setX1(this.width / 5 - 7);
        this.right.setX1(this.width / 5 - 7);
        this.up.relocate(this.getRectangle());
        this.down.relocate(this.getRectangle());
        this.left.relocate(this.getRectangle());
        this.right.relocate(this.getRectangle());
        this.zoomScroll.setPosition(this.width / 2 + 24, this.height / 2 - 26);

        if (ClientConfig.cameraVerticalLocation >= 5) this.up.active = false;
        if (ClientConfig.cameraVerticalLocation <= -2) this.down.active = false;
        if (ClientConfig.cameraHorizontalLocation >= 10) this.left.active = false;
        if (ClientConfig.cameraHorizontalLocation <= -10) this.right.active = false;

        this.addRenderableWidget(this.up);
        this.addRenderableWidget(this.down);
        this.addRenderableWidget(this.left);
        this.addRenderableWidget(this.right);
        this.addRenderableWidget(this.zoomScroll);

        this.minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft.level != null) guiGraphics.fillGradient(0, 0, this.width, this.height, 0xAF222222, 0x8F222222);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.text(this.font, this.title, 6, 6, GuiColors.text(16777215));
        guiGraphics.text(this.font, Component.literal("Exit"), this.width - 24, 6, GuiColors.text((mouseX > this.width - 24 && mouseY < 16) ? 7368816 : 16777215));

        if (this.minecraft.level == null) {
            guiGraphics.text(this.font, NO_WORLD_WARNING, this.width / 2 - this.font.width(NO_WORLD_WARNING) / 2, this.height / 2 + 40, GuiColors.text(16777215));
        }
    }

    @Override
    protected void extractBlurredBackground(GuiGraphicsExtractor guiGraphics) {
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parentScreen);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (mouseX > this.width - 24 && mouseY < 16) {
            this.minecraft.setScreen(null);
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double xScroll, double yScroll) {
        if (!super.mouseScrolled(pMouseX, pMouseY, xScroll, yScroll)) {
            this.zoomScroll.setScrollPosition(this.zoomScroll.scrollPosition - yScroll);
            return false;
        }

        return true;
    }

    private static class CameraMoveButton extends AnchoredButton {
        private final Direction direction;

        protected CameraMoveButton(
            int x1,
            int x2,
            int y1,
            int y2,
            Button.OnPress onPress,
            AnchoredWidget.HorizontalAnchorType horizontalAnchorType,
            VerticalAnchorType verticalAnchorType,
            Direction direction
        ) {
            super(x1, x2, y1, y2, Component.empty(), onPress, Button.DEFAULT_NARRATION, horizontalAnchorType, verticalAnchorType, BuiltInTheme.TRANSPARENT, AbstractWidget::isActive);
            this.direction = direction;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            GuiColors.setColor(guiGraphics, 1.0F, 1.0F, 1.0F, this.alpha);
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)(this.getX()), (float)(this.getY()));
            guiGraphics.pose().translate(8.0F, 8.0F);

            switch (this.direction) {
                case UP -> { guiGraphics.pose().rotate((float)Math.toRadians(90.0F)); }
                case DOWN -> { guiGraphics.pose().rotate((float)Math.toRadians(270.0F)); }
                case RIGHT -> { guiGraphics.pose().rotate((float)Math.toRadians(180.0F)); }
            }

            guiGraphics.pose().translate(-8.0F, -8.0F);
            //guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, 0, 0, u, v, 16, 16, 64, 64);
            Identifier sprite = this.isActive() ? (this.isHoveredOrFocused() ? DIRECTION_BUTTON_HIGHLIGHTED_SPRITE : DIRECTION_BUTTON_SPRITE) : DIRECTION_BUTTON_DISABLED_SPRITE;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 0, 0, 16, 16);
            GuiColors.setColor(guiGraphics, 1.0F, 1.0F, 1.0F, 1.0F);

            guiGraphics.pose().popMatrix();
        }

        public enum Direction {
            UP, DOWN, LEFT, RIGHT
        }
    }

    private static class ZoomScroll extends AbstractWidget {
        private double scrollPosition;

        public ZoomScroll(int x, int y, int initPos) {
            super(x, y, 12, 64, Component.empty());
            this.scrollPosition = initPos;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ZOOM_SCROLL_SPRITE, this.getX(), this.getY(), 12, 52);
            int scrollCoord = (int)this.scrollPosition * 6;
            guiGraphics.fill(this.getX() + 3, this.getY() + scrollCoord + 3, this.getX() + 9, this.getY() + scrollCoord + 7, -1);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            double mouseX = event.x();
            double mouseY = event.y();
            if (this.getX() + 3 <= mouseX && mouseX <= this.getX() + 9 && this.getY() + 1 <= mouseY && mouseY <= this.getY() + 63) {
                this.setScrollBaseOnYPressed(mouseY);
            }
        }

        @Override
        protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
            double mouseX = event.x();
            double mouseY = event.y();
            if (this.getX() + 3 <= mouseX && mouseX <= this.getX() + 9 && this.getY() + 1 <= mouseY + dragY && mouseY + dragY <= this.getY() + 63) {
                this.setScrollBaseOnYPressed(mouseY + dragY);
            }
        }

        private void setScrollBaseOnYPressed(double y) {
            double relativeY = y - this.getY()+1;
            this.setScrollPosition(relativeY / 6.0D);
        }

        private void setScrollPosition(double pos) {
            this.scrollPosition = (int)Mth.clamp(pos, 0.0D, 7.0D);
            ClientConfig.cameraZoom = (int)this.scrollPosition + 3;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        }
    }
}
