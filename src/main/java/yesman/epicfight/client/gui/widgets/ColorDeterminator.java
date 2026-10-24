package yesman.epicfight.client.gui.widgets;

import yesman.epicfight.client.gui.render.ColoredQuadRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import yesman.epicfight.client.gui.GuiColors;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.client.gui.widgets.common.DataBoundWidget;
import yesman.epicfight.client.gui.widgets.common.ThemeApplicableWidget;
import yesman.epicfight.client.gui.widgets.common.WidgetTheme;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class ColorDeterminator extends AbstractSliderButton implements AnchoredWidget, DataBoundWidget<Double>, ThemeApplicableWidget<ColorDeterminator.Theme> {
    public static final int[] RGB_COMBINATIONS = { 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000 };

    private final Font font;
    private final int[] colors;
    private Theme theme;

    public ColorDeterminator(
        Font font,
        int x1,
        int x2,
        int y1,
        int y2,
        AnchoredWidget.HorizontalAnchorType horizontalAnchor,
        VerticalAnchorType verticalAnchor,
        Callable<Double> dataProvider,
        Consumer<Double> onWidgetChanged,
        Component message,
        Theme theme,
        double initColor
    ) {
        this(font, x1, x2, y1, y2, horizontalAnchor, verticalAnchor, dataProvider, onWidgetChanged, message, theme, initColor, RGB_COMBINATIONS);
    }

    public ColorDeterminator(
        Font font,
        int x1,
        int x2,
        int y1,
        int y2,
        AnchoredWidget.HorizontalAnchorType horizontalAnchor,
        VerticalAnchorType verticalAnchor,
        Callable<Double> dataProvider,
        Consumer<Double> onWidgetChanged,
        Component message,
        Theme theme,
        double initColor,
        int... colors
    ) {
        super(0, 0, 0, 0, message, initColor);

        this.font = font;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.horizontalAnchorType = horizontalAnchor;
        this.verticalAnchorType = verticalAnchor;
        this.dataProvider = dataProvider;
        this.onWidgetChanged = onWidgetChanged;
        this.theme = theme;
        this.colors = colors;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.theme.backgroundRenderer.render(this, guiGraphics);
        this.theme.selectorRenderer.render(this, guiGraphics);
        this.theme.titleRenderer.render(this, guiGraphics, this.font);
    }

    public void changeBasisColor(int color, int index) {
        this.colors[index] = color;
    }

    @Override
    protected void applyValue() {
        this.onWidgetChanged.accept(this.value);
    }

    @Override
    protected void updateMessage() {
    }

    public double getPosition() {
        return this.value;
    }

    @Override
    public boolean isSupportedTheme(WidgetTheme theme) {
        return Theme.class.isAssignableFrom(theme.getClass());
    }

    @Override
    public Theme getTheme() {
        return this.theme;
    }

    @Override
    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public int getPackedRGBAColor() {
        return positionToPackedRGBA(this.value, this.colors);
    }

    public static int positionToPackedRGBA(double value, int[] colors) {
        int packedColor = 0;
        int colorBlocks = colors.length - 1;

        for (int i = 0; i < colorBlocks; i++) {
            double min = 1.0D / colorBlocks * i;
            double max = 1.0D / colorBlocks * (i + 1);

            if (value >= min && value <= max) {
                double lerpFactor = (value - min) / (max - min);
                int startColor = colors[i];
                int endColor = colors[i + 1];
                int f = startColor >> 24 & 255;
                int f1 = startColor >> 16 & 255;
                int f2 = startColor >> 8 & 255;
                int f3 = startColor & 255;
                int f4 = endColor >> 24 & 255;
                int f5 = endColor >> 16 & 255;
                int f6 = endColor >> 8 & 255;
                int f7 = endColor & 255;
                int r = (int) Mth.lerp(lerpFactor, f, f4);
                int g = (int) Mth.lerp(lerpFactor, f1, f5);
                int b = (int) Mth.lerp(lerpFactor, f2, f6);
                int a = (int) Mth.lerp(lerpFactor, f3, f7);

                packedColor = r << 24 | g << 16 | b << 8 | a;
            }
        }

        return packedColor;
    }

    public static int positionToPackedRGBA(double position) {
        return positionToPackedRGBA(position, RGB_COMBINATIONS);
    }

    private static void fillGradient(GuiGraphicsExtractor guiGraphics, int x1, int y1, int x2, int y2, int startColor, int endColor) {
        ColoredQuadRenderState.horizontalGradient(guiGraphics, x1, y1, x2, y2, startColor, endColor);
    }

    private static void fillRhombus(GuiGraphicsExtractor guiGraphics, int minX, int minY, int maxX, int maxY, int color) {
        ColoredQuadRenderState.rhombus(guiGraphics, minX, minY, maxX, maxY, color);
    }

    public enum Theme implements WidgetTheme {
        CLASSIC(
            (ColorDeterminator widget, GuiGraphicsExtractor guiGraphics) -> {
                int minX = widget.getX() + (int) (widget.value * (double) (widget.width - 8));
                guiGraphics.fill(minX, widget.getY(), minX + 8, widget.getY() + 20, 0xFFFFFFFF);
                guiGraphics.fill(minX + 1, widget.getY() + 1, minX + 7, widget.getY() + 19, positionToPackedRGBA(widget.value, widget.colors));
            },
            (ColorDeterminator widget, GuiGraphicsExtractor guiGraphics) -> {
                int y1 = widget.getY();
                int y2 = widget.getY() + widget.height;
                int blocks = widget.colors.length - 1;
                int prevEnd = widget.getX();

                for (int i = 0; i < widget.colors.length - 1; i++) {
                    int nextX = widget.getX() + (widget.width * (i + 1) / blocks);
                    fillGradient(guiGraphics, prevEnd, y1, nextX, y2, widget.colors[i], widget.colors[i+1]);
                    prevEnd = nextX;
                }
            },
            (ColorDeterminator widget, GuiGraphicsExtractor guiGraphics, Font font) -> {
                int j = widget.getFGColor();
                guiGraphics.centeredText(widget.font, widget.getMessage(), widget.getX() + widget.width / 2, widget.getY() + (widget.height - 8) / 2, GuiColors.text(j | Mth.ceil(widget.alpha * 255.0F) << 24));
            }
        ),
        SIMPLE(
            (ColorDeterminator widget, GuiGraphicsExtractor guiGraphics) -> {
                int outlineColor = widget.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF000000;
                int minX = widget.getX() + (int)(widget.value * (widget.width - 8));
                int centerY = widget.getY() + widget.getHeight() / 2;
                fillRhombus(guiGraphics, minX, centerY - 5, minX + 10, centerY + 5, outlineColor);
                fillRhombus(guiGraphics, minX + 1, centerY - 4, minX + 9, centerY + 4, positionToPackedRGBA(widget.value, widget.colors));
            },
            (ColorDeterminator widget, GuiGraphicsExtractor guiGraphics) -> {
                int y1 = widget.getY() + widget.height / 2 - 1;
                int y2 = widget.getY() + widget.height / 2 + 1;
                int blocks = widget.colors.length - 1;
                int prevEnd = widget.getX();

                guiGraphics.fill(widget.getX() - 1, y1 - 1, widget.getX() + widget.width + 1, y2 + 1, 0xFF000000);

                for (int i = 0; i < widget.colors.length - 1; i++) {
                    int nextX = widget.getX() + (widget.width * (i + 1) / blocks);
                    fillGradient(guiGraphics, prevEnd, y1, nextX, y2, widget.colors[i], widget.colors[i+1]);
                    prevEnd = nextX;
                }
            },
            (ColorDeterminator widget, GuiGraphicsExtractor guiGraphics, Font font) -> {
            }
        );

        final SelectorRenderer selectorRenderer;
        final BackgroundRenderer backgroundRenderer;
        final TextRenderer titleRenderer;
        final int id;

        Theme(SelectorRenderer selectorRenderer, BackgroundRenderer backgroundRenderer, TextRenderer titleRenderer) {
            this.selectorRenderer = selectorRenderer;
            this.backgroundRenderer = backgroundRenderer;
            this.titleRenderer = titleRenderer;
            this.id = WidgetTheme.ENUM_MANAGER.assign(this);
        }

        @FunctionalInterface
        interface SelectorRenderer {
            void render(ColorDeterminator widget, GuiGraphicsExtractor guiGraphics);
        }

        @FunctionalInterface
        interface BackgroundRenderer {
            void render(ColorDeterminator widget, GuiGraphicsExtractor guiGraphics);
        }

        @FunctionalInterface
        interface TextRenderer {
            void render(ColorDeterminator widget, GuiGraphicsExtractor guiGraphics, Font font);
        }

        @Override
        public int universalOrdinal() {
            return this.id;
        }
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

    /*******************************************************************
     * {@link DataBoundWidget} implementations                         *
     *******************************************************************/
    private final Callable<Double> dataProvider;
    private final Consumer<Double> onWidgetChanged;

    @Override
    public Callable<Double> getDataProvider() {
        return this.dataProvider;
    }

    @Override
    public Consumer<Double> valueChangeCallback() {
        return this.onWidgetChanged;
    }

    @Override
    public Consumer<Double> valueSetter() {
        return this::setValue;
    }

    @Override
    public void reset() {
        this.value = 0.0D;
    }
}