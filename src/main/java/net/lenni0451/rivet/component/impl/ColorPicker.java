package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.input.keyboard.ModifierKey;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class ColorPicker extends Component {

    private static final int HUE_STEPS = 12;
    private static final Color DISABLED_OVERLAY_COLOR = Color.fromRGBA(30, 30, 30, 150);

    @Getter
    private Color color;
    @Getter
    private final ListenerList<Consumer<Color>> colorChangeListener = new ListenerList<>();

    private float hue;
    private float saturation;
    private float brightness;
    private float alpha;

    private boolean draggingPicker;
    private boolean draggingHue;
    private boolean draggingAlpha;

    @Getter
    private final ThemeOption<Float> outlineWidth = new ThemeOption<>(this, Theme.ColorPicker.OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Color> outlineColor = new ThemeOption<>(this, Theme.ColorPicker.OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Float> pickerSize = new ThemeOption<>(this, Theme.ColorPicker.PICKER_SIZE);
    @Getter
    private final ThemeOption<Float> sliderWidth = new ThemeOption<>(this, Theme.ColorPicker.SLIDER_WIDTH);
    @Getter
    private final ThemeOption<Float> gap = new ThemeOption<>(this, Theme.ColorPicker.GAP);
    @Getter
    private final ThemeOption<Float> selectorSize = new ThemeOption<>(this, Theme.ColorPicker.SELECTOR_SIZE);
    @Getter
    private final ThemeOption<Boolean> showAlpha = new ThemeOption<>(this, Theme.ColorPicker.SHOW_ALPHA);
    @Getter
    private final ThemeOption<Boolean> showPreview = new ThemeOption<>(this, Theme.ColorPicker.SHOW_PREVIEW);
    @Getter
    private final ThemeOption<Boolean> allowScaling = new ThemeOption<>(this, Theme.ColorPicker.ALLOW_SCALING);
    @Getter
    private final ThemeOption<Float> pickerIndicatorOutlineWidth = new ThemeOption<>(this, Theme.ColorPicker.PICKER_INDICATOR_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Float> sliderIndicatorOutlineWidth = new ThemeOption<>(this, Theme.ColorPicker.SLIDER_INDICATOR_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Float> pickerIndicatorRadius = new ThemeOption<>(this, Theme.ColorPicker.PICKER_INDICATOR_RADIUS);
    @Getter
    private final ThemeOption<Float> sliderIndicatorInnerWidth = new ThemeOption<>(this, Theme.ColorPicker.SLIDER_INDICATOR_INNER_WIDTH);

    public ColorPicker(final Color color) {
        this.color = color;
        this.updateHSB();
    }

    public final ColorPicker color(final Color color) {
        return this.color(color, true);
    }

    public final ColorPicker color(final Color color, final boolean fireListeners) {
        if (!color.equals(this.color)) {
            this.color = color;
            this.updateHSB();
            if (fireListeners) {
                this.colorChangeListener.call(c -> c.accept(this.color));
            }
        }
        return this;
    }

    private void updateHSB() {
        float[] hsb = this.color.toHSB();
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = this.color.getAlpha() / 255F;
    }

    private void updateColor() {
        this.color = Color.fromHSB(this.hue, this.saturation, this.brightness).withAlphaF(this.alpha);
        this.colorChangeListener.call(c -> c.accept(this.color));
    }

    @Override
    protected void onComponentRemoved() {
        this.draggingPicker = false;
        this.draggingHue = false;
        this.draggingAlpha = false;
    }

    @Override
    protected void onComponentDisabled() {
        this.onComponentRemoved();
    }

    @Override
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            LayoutInfo layout = new LayoutInfo(size);

            if (layout.saturationArea.contains(event.x(), event.y())) {
                this.draggingPicker = true;
                this.updatePicker(event.x(), event.y(), layout.saturationArea);
            } else if (layout.hueArea.contains(event.x(), event.y())) {
                this.draggingHue = true;
                this.updateHue(event.y(), layout.hueArea);
            } else if (layout.alphaArea != null && layout.alphaArea.contains(event.x(), event.y())) {
                this.draggingAlpha = true;
                this.updateAlpha(event.x(), layout.alphaArea);
            }
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        this.draggingPicker = false;
        this.draggingHue = false;
        this.draggingAlpha = false;
        return true;
    }

    @Override
    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
        if (this.draggingPicker || this.draggingHue || this.draggingAlpha) {
            LayoutInfo layout = new LayoutInfo(size);

            if (this.draggingPicker) {
                this.updatePicker(event.x(), event.y(), layout.saturationArea);
            } else if (this.draggingHue) {
                this.updateHue(event.y(), layout.hueArea);
            } else if (this.draggingAlpha) {
                this.updateAlpha(event.x(), layout.alphaArea);
            }
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseScroll(final MouseScrollEvent event, final Size size) {
        if (this.rivet().backend().isKeyDown(ModifierKey.SHIFT)) {
            this.alpha = MathUtils.clamp(this.alpha + event.scrollY() / 20, 0, 1);
            this.updateColor();
            return true;
        } else if (this.rivet().backend().isKeyDown(ModifierKey.CONTROL)) {
            this.hue = MathUtils.clamp(this.hue - event.scrollY() / 20, 0, 1);
            this.updateColor();
            return true;
        }
        return false;
    }

    private void updatePicker(final float mouseX, final float mouseY, final Rectangle satValBounds) {
        this.saturation = MathUtils.clamp((mouseX - satValBounds.x()) / satValBounds.width(), 0, 1);
        this.brightness = MathUtils.clamp(1 - ((mouseY - satValBounds.y()) / satValBounds.height()), 0, 1);
        this.updateColor();
    }

    private void updateHue(final float mouseY, final Rectangle hueBounds) {
        this.hue = MathUtils.clamp((mouseY - hueBounds.y()) / hueBounds.height(), 0, 1);
        this.updateColor();
    }

    private void updateAlpha(final float mouseX, final Rectangle alphaBounds) {
        this.alpha = MathUtils.clamp((mouseX - alphaBounds.x()) / alphaBounds.width(), 0, 1);
        this.updateColor();
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        LayoutInfo layout = new LayoutInfo(size);

        this.renderSaturationValue(renderer, layout);
        this.renderHueSlider(renderer, layout);
        if (this.showAlpha.value()) {
            this.renderAlphaSlider(renderer, layout);
        }
        if (this.showPreview.value()) {
            this.renderPreview(renderer, layout);
        }
    }

    private void renderSaturationValue(final Renderer renderer, final LayoutInfo layout) {
        float x = layout.saturationArea.x();
        float y = layout.saturationArea.y();
        float width = layout.saturationArea.width();
        float height = layout.saturationArea.height();

        renderer.fillRect(x, y, width, height, Color.fromHSB(this.hue, 1F, 1F));
        renderer.fillGradientRect(x, y, width, height, Color.WHITE, Color.WHITE, Color.WHITE.withAlpha(0), Color.WHITE.withAlpha(0));
        renderer.fillGradientRect(x, y, width, height, Color.BLACK.withAlpha(0), Color.BLACK, Color.BLACK, Color.BLACK.withAlpha(0));
        renderer.outlineRect(x, y, width, height, this.outlineWidth.value(), this.outlineColor.value());

        // Selection circle
        float cursorX = x + this.saturation * width;
        float cursorY = y + (1F - this.brightness) * height;
        float cursorWidth = this.pickerIndicatorOutlineWidth.value();
        float cursorHeight = this.pickerIndicatorRadius.value();
        renderer.outlineCircle(cursorX, cursorY, cursorHeight + cursorWidth, cursorWidth, Color.BLACK);
        renderer.outlineCircle(cursorX, cursorY, cursorHeight, cursorWidth, Color.WHITE);

        if (this.disabled()) {
            renderer.fillRect(x, y, width, height, DISABLED_OVERLAY_COLOR);
        }
    }

    private void renderHueSlider(final Renderer renderer, final LayoutInfo layout) {
        float x = layout.hueArea.x();
        float y = layout.hueArea.y();
        float width = layout.hueArea.width();
        float height = layout.hueArea.height();

        float stepHeight = height / HUE_STEPS;
        for (int i = 0; i < HUE_STEPS; i++) {
            float h1 = (float) i / HUE_STEPS;
            float h2 = (float) (i + 1) / HUE_STEPS;
            Color c1 = Color.fromHSB(h1, 1, 1);
            Color c2 = Color.fromHSB(h2, 1, 1);
            renderer.fillGradientRect(x, y + i * stepHeight, width, stepHeight, c1, c2, c2, c1);
        }
        renderer.outlineRect(x, y, width, height, this.outlineWidth.value(), this.outlineColor.value());

        // Selector slider indicator
        float cursorY = y + this.hue * height;
        float sWidth = this.sliderIndicatorOutlineWidth.value();
        float innerW = this.sliderIndicatorInnerWidth.value();
        float halfInner = innerW / 2F;
        renderer.outlineRect(x - 2 * sWidth, cursorY - 2 * sWidth - halfInner, width + 4 * sWidth, 4 * sWidth + innerW, sWidth, Color.BLACK);
        renderer.outlineRect(x - sWidth, cursorY - sWidth - halfInner, width + 2 * sWidth, 2 * sWidth + innerW, sWidth, Color.WHITE);

        if (this.disabled()) {
            renderer.fillRect(x, y, width, height, DISABLED_OVERLAY_COLOR);
        }
    }

    private void renderAlphaSlider(final Renderer renderer, final LayoutInfo layout) {
        float x = layout.alphaArea.x();
        float y = layout.alphaArea.y();
        float width = layout.alphaArea.width();
        float height = layout.alphaArea.height();

        // Background checkerboard
        float checkSize = height / 2F;
        for (float cx = 0; cx < width; cx += checkSize) {
            for (float cy = 0; cy < height; cy += checkSize) {
                boolean light = ((int) (cx / checkSize) + (int) (cy / checkSize)) % 2 == 0;
                renderer.fillRect(x + cx, y + cy, Math.min(checkSize, width - cx), Math.min(checkSize, height - cy), light ? Color.WHITE : Color.LIGHT_GRAY);
            }
        }
        // Alpha gradient
        Color opaque = Color.fromHSB(this.hue, this.saturation, this.brightness);
        Color transparent = opaque.withAlpha(0);
        renderer.fillGradientRect(x, y, width, height, transparent, transparent, opaque, opaque);
        renderer.outlineRect(x, y, width, height, this.outlineWidth.value(), this.outlineColor.value());

        // Selector slider indicator
        float cursorX = x + this.alpha * width;
        float cursorWidth = this.sliderIndicatorOutlineWidth.value();
        float innerCursorWidth = this.sliderIndicatorInnerWidth.value();
        renderer.outlineRect(cursorX - 2 * cursorWidth - innerCursorWidth / 2F, y - 2 * cursorWidth, 4 * cursorWidth + innerCursorWidth, height + 4 * cursorWidth, cursorWidth, Color.BLACK);
        renderer.outlineRect(cursorX - cursorWidth - innerCursorWidth / 2F, y - cursorWidth, 2 * cursorWidth + innerCursorWidth, height + 2 * cursorWidth, cursorWidth, Color.WHITE);

        if (this.disabled()) {
            renderer.fillRect(x, y, width, height, DISABLED_OVERLAY_COLOR);
        }
    }

    private void renderPreview(final Renderer renderer, final LayoutInfo layout) {
        float x = layout.previewArea.x();
        float y = layout.previewArea.y();
        float width = layout.previewArea.width();
        float height = layout.previewArea.height();

        renderer.fillRect(x, y, width / 2F, height / 2F, Color.WHITE);
        renderer.fillRect(x + width / 2F, y, width / 2F, height / 2F, Color.LIGHT_GRAY);
        renderer.fillRect(x + width / 2F, y + height / 2F, width / 2F, height / 2F, Color.WHITE);
        renderer.fillRect(x, y + height / 2F, width / 2F, height / 2F, Color.LIGHT_GRAY);
        renderer.fillRect(x, y, width, height, this.color);
        renderer.outlineRect(x, y, width, height, this.outlineWidth.value(), this.outlineColor.value());
        if (this.disabled()) {
            renderer.fillRect(x, y, width, height, DISABLED_OVERLAY_COLOR);
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        float pickerSize = this.pickerSize.value();
        float sliderWidth = this.sliderWidth.value();
        float gap = this.gap.value();
        return new Size(
                pickerSize + gap + sliderWidth,
                this.showAlpha.value() ? (pickerSize + gap + sliderWidth) : pickerSize
        );
    }


    private class LayoutInfo {
        final float scale;
        final float offsetX;
        final float offsetY;
        final Rectangle saturationArea;
        final Rectangle hueArea;
        final Rectangle alphaArea;
        final Rectangle previewArea;

        LayoutInfo(final Size actualSize) {
            float pickerSize = ColorPicker.this.pickerSize.value();
            float sliderWidth = ColorPicker.this.sliderWidth.value();
            float gap = ColorPicker.this.gap.value();
            boolean showAlpha = ColorPicker.this.showAlpha.value();
            boolean showPreview = ColorPicker.this.showPreview.value();

            float idealWidth = pickerSize + gap + sliderWidth;
            float idealHeight = showAlpha ? (pickerSize + gap + sliderWidth) : pickerSize;

            float scaleX = actualSize.width() / idealWidth;
            float scaleY = actualSize.height() / idealHeight;
            if (ColorPicker.this.allowScaling.value()) {
                this.scale = Math.min(scaleX, scaleY);
            } else {
                this.scale = 1F;
            }

            this.offsetX = (actualSize.width() - idealWidth * this.scale) / 2;
            this.offsetY = (actualSize.height() - idealHeight * this.scale) / 2;

            Rectangle idealSaturationArea = new Rectangle(0, 0, pickerSize, pickerSize);
            Rectangle idealHueArea;
            if (!showAlpha && showPreview) {
                idealHueArea = new Rectangle(pickerSize + gap, 0, sliderWidth, pickerSize - gap - sliderWidth);
            } else {
                idealHueArea = new Rectangle(pickerSize + gap, 0, sliderWidth, pickerSize);
            }
            Rectangle idealAlphaArea = showAlpha ? new Rectangle(0, pickerSize + gap, pickerSize, sliderWidth) : null;
            Rectangle idealPreviewArea = null;
            if (showPreview) {
                float previewY;
                if (showAlpha) {
                    previewY = pickerSize + gap;
                } else {
                    previewY = pickerSize - sliderWidth;
                }
                idealPreviewArea = new Rectangle(pickerSize + gap, previewY, sliderWidth, sliderWidth);
            }

            this.saturationArea = this.scale(idealSaturationArea);
            this.hueArea = this.scale(idealHueArea);
            this.alphaArea = this.scale(idealAlphaArea);
            this.previewArea = this.scale(idealPreviewArea);
        }

        private Rectangle scale(final Rectangle rect) {
            if (rect == null) return null;
            return new Rectangle(
                    this.offsetX + rect.x() * this.scale,
                    this.offsetY + rect.y() * this.scale,
                    rect.width() * this.scale,
                    rect.height() * this.scale
            );
        }
    }

}
