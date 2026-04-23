package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseListener;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public class ColorPicker extends Component implements MouseListener, Renderable {

    private static final int SATURATION_STEPS = 16;
    private static final int HUE_STEPS = 12;

    @Getter
    private Color color;
    @Setter
    private Consumer<Color> onColorChange;

    private float hue;
    private float saturation;
    private float brightness;
    private float alpha;

    private boolean draggingPicker;
    private boolean draggingHue;
    private boolean draggingAlpha;

    private final ThemeOption<Float> outlineWidth;
    private final ThemeOption<Color> outlineColor;
    private final ThemeOption<Float> pickerSize;
    private final ThemeOption<Float> sliderWidth;
    private final ThemeOption<Float> gap;
    private final ThemeOption<Float> selectorSize;

    public ColorPicker(final Rivet rivet, final Color color) {
        super(rivet);
        this.color = color;
        this.updateHSB();

        this.outlineWidth = new ThemeOption<>(rivet, Theme.COLOR_PICKER_OUTLINE_WIDTH);
        this.outlineColor = new ThemeOption<>(rivet, Theme.COLOR_PICKER_OUTLINE_COLOR);
        this.pickerSize = new ThemeOption<>(rivet, Theme.COLOR_PICKER_PICKER_SIZE);
        this.sliderWidth = new ThemeOption<>(rivet, Theme.COLOR_PICKER_SLIDER_WIDTH);
        this.gap = new ThemeOption<>(rivet, Theme.COLOR_PICKER_GAP);
        this.selectorSize = new ThemeOption<>(rivet, Theme.COLOR_PICKER_SELECTOR_SIZE);
    }

    public ColorPicker color(final Color color) {
        this.color = color;
        this.updateHSB();
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
        if (this.onColorChange != null) this.onColorChange.accept(this.color);
    }

    @Override
    public void onMouseDown(final MouseButtonEvent event, final Size size) {
        if (!event.button().equals(MouseButton.LEFT)) return;

        float pickerSize = this.pickerSize.value();
        float sliderWidth = this.sliderWidth.value();
        float gap = this.gap.value();

        if (event.x() >= 0 && event.x() <= pickerSize && event.y() >= 0 && event.y() <= pickerSize) {
            this.draggingPicker = true;
            this.updatePicker(event.x(), event.y());
        } else if (event.x() >= pickerSize + gap && event.x() <= pickerSize + gap + sliderWidth && event.y() >= 0 && event.y() <= pickerSize) {
            this.draggingHue = true;
            this.updateHue(event.y());
        } else if (event.x() >= 0 && event.x() <= pickerSize && event.y() >= pickerSize + gap && event.y() <= pickerSize + gap + sliderWidth) {
            this.draggingAlpha = true;
            this.updateAlpha(event.x());
        }
    }

    @Override
    public void onMouseUp(final MouseButtonEvent event, final Size size) {
        this.draggingPicker = false;
        this.draggingHue = false;
        this.draggingAlpha = false;
    }

    @Override
    public void onMouseMove(final MouseMoveEvent event, final Size size) {
        if (this.draggingPicker) this.updatePicker(event.x(), event.y());
        else if (this.draggingHue) this.updateHue(event.y());
        else if (this.draggingAlpha) this.updateAlpha(event.x());
    }

    private void updatePicker(float mouseX, float mouseY) {
        float pickerSize = this.pickerSize.value();
        this.saturation = MathUtils.clamp(mouseX / pickerSize, 0, 1);
        this.brightness = MathUtils.clamp(1 - (mouseY / pickerSize), 0, 1);
        this.updateColor();
    }

    private void updateHue(float mouseY) {
        float pickerSize = this.pickerSize.value();
        this.hue = MathUtils.clamp(mouseY / pickerSize, 0, 1);
        this.updateColor();
    }

    private void updateAlpha(float mouseX) {
        float pickerSize = this.pickerSize.value();
        this.alpha = MathUtils.clamp(mouseX / pickerSize, 0, 1);
        this.updateColor();
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float pickerSize = this.pickerSize.value();
        float sliderWidth = this.sliderWidth.value();
        float gap = this.gap.value();

        this.renderSaturationValue(renderer, pickerSize);
        this.renderHueSlider(renderer, pickerSize, sliderWidth, gap);
        this.renderAlphaSlider(renderer, pickerSize, sliderWidth, gap);
        this.renderPreview(renderer, pickerSize, sliderWidth, gap);
    }

    private void renderSaturationValue(final Renderer renderer, final float pickerSize) {
        // Subdivide the saturation/value box into a grid of quads for smooth interpolation.
        float stepSize = pickerSize / SATURATION_STEPS;
        for (int x = 0; x < SATURATION_STEPS; x++) {
            for (int y = 0; y < SATURATION_STEPS; y++) {
                float x1 = x * stepSize;
                float y1 = y * stepSize;
                float x2 = (x + 1) * stepSize;
                float y2 = (y + 1) * stepSize;

                float s1 = (float) x / SATURATION_STEPS;
                float s2 = (float) (x + 1) / SATURATION_STEPS;
                float b1 = 1 - (float) y / SATURATION_STEPS;
                float b2 = 1 - (float) (y + 1) / SATURATION_STEPS;

                Color ctl = Color.fromHSB(this.hue, s1, b1);
                Color ctr = Color.fromHSB(this.hue, s2, b1);
                Color cbr = Color.fromHSB(this.hue, s2, b2);
                Color cbl = Color.fromHSB(this.hue, s1, b2);

                renderer.fillGradientRect(x1, y1, x2 - x1, y2 - y1, ctl, cbl, cbr, ctr);
            }
        }

        // Selection circle
        float cursorX = this.saturation * pickerSize;
        float cursorY = (1 - this.brightness) * pickerSize;
        renderer.outlineCircle(cursorX, cursorY, this.selectorSize.value(), 1, this.brightness > 0.5 ? Color.BLACK : Color.WHITE);
        renderer.outlineRect(0, 0, pickerSize, pickerSize, this.outlineWidth.value(), this.outlineColor.value());
    }

    private void renderHueSlider(final Renderer renderer, final float pickerSize, final float sliderWidth, final float gap) {
        float x = pickerSize + gap;
        float stepHeight = pickerSize / HUE_STEPS;
        for (int i = 0; i < HUE_STEPS; i++) {
            float h1 = (float) i / HUE_STEPS;
            float h2 = (float) (i + 1) / HUE_STEPS;
            Color c1 = Color.fromHSB(h1, 1, 1);
            Color c2 = Color.fromHSB(h2, 1, 1);

            renderer.fillGradientRect(x, i * stepHeight, sliderWidth, stepHeight, c1, c2, c2, c1);
        }

        float cursorY = this.hue * pickerSize;
        renderer.outlineRect(x - 1, cursorY - 2, sliderWidth + 2, 4, 1, Color.WHITE);
        renderer.outlineRect(x, 0, sliderWidth, pickerSize, this.outlineWidth.value(), this.outlineColor.value());
    }

    private void renderAlphaSlider(final Renderer renderer, final float pickerSize, final float sliderWidth, final float gap) {
        float y = pickerSize + gap;
        // Background checkerboard
        float checkSize = sliderWidth / 2F;
        for (float cx = 0; cx < pickerSize; cx += checkSize) {
            for (float cy = 0; cy < sliderWidth; cy += checkSize) {
                boolean light = ((int) (cx / checkSize) + (int) (cy / checkSize)) % 2 == 0;
                renderer.fillRect(cx, y + cy, Math.min(checkSize, pickerSize - cx), Math.min(checkSize, sliderWidth - cy), light ? Color.WHITE : Color.LIGHT_GRAY);
            }
        }

        // Alpha gradient
        Color opaque = Color.fromHSB(this.hue, this.saturation, this.brightness);
        Color transparent = Color.fromHSB(this.hue, this.saturation, this.brightness).withAlpha(0);
        renderer.fillGradientRect(0, y, pickerSize, sliderWidth, transparent, transparent, opaque, opaque);

        float cursorX = this.alpha * pickerSize;
        renderer.outlineRect(cursorX - 2, y - 1, 4, sliderWidth + 2, 1, Color.WHITE);
        renderer.outlineRect(0, y, pickerSize, sliderWidth, this.outlineWidth.value(), this.outlineColor.value());
    }

    private void renderPreview(final Renderer renderer, final float pickerSize, final float sliderWidth, final float gap) {
        renderer.fillRect(pickerSize + gap, pickerSize + gap, sliderWidth, sliderWidth, this.color);
        renderer.outlineRect(pickerSize + gap, pickerSize + gap, sliderWidth, sliderWidth, this.outlineWidth.value(), this.outlineColor.value());
    }

    @Override
    public void computeIdealSize() {
        float pickerSize = this.pickerSize.value();
        float sliderWidth = this.sliderWidth.value();
        float gap = this.gap.value();
        this.idealSize = new Size(pickerSize + gap + sliderWidth, pickerSize + gap + sliderWidth);
    }

}
