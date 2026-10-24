package yesman.epicfight.client.gui.screen;

import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import yesman.epicfight.client.gui.GuiColors;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.client.gui.datapack.screen.DatapackEditScreen;
import yesman.epicfight.client.gui.screen.config.EpicFightSettingScreen;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.client.gui.widgets.common.PressableWidget;
import yesman.epicfight.epicskins.client.screen.AvatarEditScreen;
import yesman.epicfight.main.EpicFightMod;

import java.util.List;
import java.util.function.Supplier;

import static yesman.epicfight.generated.LangKeys.*;

public class SideBarScreen extends Screen {
    public static List<SideNavigationBarEntry> createConfigScreenSideNavBar(@Nullable Screen parentScreen) {
        return List.of(
            new SideNavigationBarEntry(GUI_TITLE_SETTINGS, EpicFightSettingScreen.class, () -> new EpicFightSettingScreen(null, parentScreen)),
            new SideNavigationBarEntry(GUI_TITLE_DATAPACK_EDITOR, DatapackEditScreen.class, () -> new DatapackEditScreen(parentScreen)),
            new SideNavigationBarEntry(GUI_TITLE_COSMETICS, AvatarEditScreen.class, () -> new AvatarEditScreen(parentScreen))
        );
    }

    private final SwitchableScreenList switchableScreenList;
    private final CloseButton closeButton;
    private Screen backgroundScreen;

    public SideBarScreen(Screen backgroundScreen, List<SideNavigationBarEntry> entries, int sideBarWidth) {
        super(Component.empty());

        this.switchableScreenList = new SwitchableScreenList(backgroundScreen.getMinecraft(), sideBarWidth);
        entries.forEach(this.switchableScreenList::addScreenEntry);

        this.backgroundScreen = backgroundScreen;
        this.closeButton = new CloseButton(sideBarWidth);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(this.switchableScreenList);
        this.addRenderableWidget(this.closeButton);
        this.repositionElements();

        SwitchableScreenList.SideNavigationBarEntry entryToFocus = null;

        for (SwitchableScreenList.SideNavigationBarEntry entry : this.switchableScreenList.children()) {
            if (entry.screenClass.equals(this.backgroundScreen.getClass())) {
                entryToFocus = entry;
            }
        }

        this.setFocused(this.switchableScreenList);
        this.switchableScreenList.setFocused(entryToFocus);
    }

    @Override
    public void repositionElements() {
        this.backgroundScreen.resize(this.width, this.height);
        this.switchableScreenList.relocate(this.getRectangle());
        this.closeButton.relocate(this.getRectangle());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Tricky way to disable the hover highlight by pushing mouse X, Y +10000
        this.backgroundScreen.extractRenderState(guiGraphics, mouseX + 10000, mouseY + 10000, partialTick);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(0F, 0F);

        this.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        for (Renderable renderable : this.renderables) {
            renderable.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        }

        guiGraphics.pose().popMatrix();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xA0101010, 0xB0101010);
    }

    @Override
    public void onClose() {
        this.backgroundScreen.setFocused(null);
        this.minecraft.setScreen(this.backgroundScreen);
    }

    public record SideNavigationBarEntry(String title, Class<?> screenCls, Supplier<Screen> screenProvider) {
    }

    private class CloseButton extends AbstractButton implements AnchoredWidget {
        private static final Identifier ARROW_SPRITE = EpicFightMod.identifier("widget/arrow");
        private static final Identifier ARROW_HIGHLIGHTED_SPRITE = EpicFightMod.identifier("widget/arrow_highlighted");

        private CloseButton(int sideBarWidth) {
            super(0, 0, 0, 0, Component.empty());

            this.horizontalAnchorType = HorizontalAnchorType.LEFT_WIDTH;
            this.verticalAnchorType = VerticalAnchorType.CENTER_HEIGHT;
            this.x1 = sideBarWidth;
            this.x2 = 8;
            this.y1 = 0;
            this.y2 = 50;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            onClose();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth() + 1, this.getY() + this.getHeight(), 0xFFABABAB);
            guiGraphics.fill(this.getX(), this.getY() + 1, this.getX() + this.getWidth(), this.getY() + this.getHeight() - 1, 0xFF000000);

            Identifier resourcelocation = this.isHoveredOrFocused() ? ARROW_HIGHLIGHTED_SPRITE : ARROW_SPRITE;

            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)(this.getX() + this.getWidth() / 2.0F - 3 + 4), (float)(this.getY() + this.getHeight() / 2.0F + 3));
            guiGraphics.pose().rotate((float)Math.toRadians(180.0F));
            guiGraphics.pose().translate((float)(-(this.getX() + this.getWidth() / 2.0F - 3)), (float)(-(this.getY() + this.getHeight() / 2.0F - 3)));
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourcelocation, this.getX() + this.getWidth() / 2 - 3, this.getY() + this.getHeight() / 2 - 3, 4, 6);
            guiGraphics.pose().popMatrix();
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        /*******************************************************************
         * {@link AnchoredWidget} implementations                         *
         *******************************************************************/
        private int x1;
        private int x2;
        private int y1;
        private int y2;
        private final AnchoredWidget.HorizontalAnchorType horizontalAnchorType;
        private final AnchoredWidget.VerticalAnchorType verticalAnchorType;

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

    public class SwitchableScreenList extends ObjectSelectionList<SwitchableScreenList.SideNavigationBarEntry> implements AnchoredWidget {
        private final Minecraft minecraft;
        private final Font font;

        public SwitchableScreenList(Minecraft minecraft, int width) {
            super(minecraft, 0, 0, 0, 21);

            this.minecraft = minecraft;
            this.font = minecraft.font;
            this.x1 = 0;
            this.x2 = width;
            this.y1 = 40;
            this.y2 = 40;
            this.horizontalAnchorType = HorizontalAnchorType.LEFT_WIDTH;
            this.verticalAnchorType = VerticalAnchorType.TOP_BOTTOM;
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor guiGraphics) {
            guiGraphics.fill(this.getX(), 0, this.getX() + this.getWidth() + 1, SideBarScreen.this.height, 0xFFABABAB);
            guiGraphics.fill(this.getX(), 0, this.getX() + this.getWidth(), SideBarScreen.this.height, 0xFF000000);
        }

        public void addScreenEntry(SideBarScreen.SideNavigationBarEntry entry) {
            this.addEntry(new SwitchableScreenList.SideNavigationBarEntry(entry.title(), entry.screenProvider(), entry.screenCls()));
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            int keyCode = event.key();
            int scanCode = event.scancode();
            int modifiers = event.modifiers();
            if (this.active && this.visible && this.getSelected() != null) {
                return this.getSelected().keyPressed(event);
            } else {
                return false;
            }
        }

        public class SideNavigationBarEntry extends ObjectSelectionList.Entry<SideNavigationBarEntry> implements PressableWidget {
            private final Supplier<Screen> screenProvider;
            private final Class<?> screenClass;
            private final String title;

            public SideNavigationBarEntry(String title, Supplier<Screen> screenProvider, Class<?> screenClass) {
                this.screenProvider = screenProvider;
                this.screenClass = screenClass;
                this.title = title;
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                double mouseX = event.x();
                double mouseY = event.y();
                int button = event.button();
                this.onPressed();
                return true;
            }

            @Override
            public boolean keyPressed(KeyEvent event) {
                int keyCode = event.key();
                int scanCode = event.scancode();
                int modifiers = event.modifiers();
                if (new KeyEvent(keyCode, 0, 0).isSelection()) {
                    this.onPressed();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onPressed() {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                backgroundScreen = this.screenProvider.get();
                backgroundScreen.init(SideBarScreen.this.width, SideBarScreen.this.height);
                backgroundScreen.initialized = false;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
                int top = this.getY();
                int left = this.getX();
                int width = this.getWidth();
                int height = this.getContentHeight();
                int color = this.isFocused() || isMouseOver ? 0xFFE8E8E8 : 0xFF9F9F9F;
                guiGraphics.text(font, Component.translatable(this.title).withStyle(this.isFocused() || isMouseOver ? new ChatFormatting[] {ChatFormatting.ITALIC, ChatFormatting.UNDERLINE} : new ChatFormatting[] {ChatFormatting.ITALIC}), 6, top + SwitchableScreenList.this.defaultEntryHeight / 2 - 6, GuiColors.text(color));
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.translatable("narrator.select", this.title);
            }
        }

        /*******************************************************************
         * {@link AnchoredWidget} implementations                         *
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
}
