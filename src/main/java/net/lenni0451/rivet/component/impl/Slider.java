package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseListener;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class Slider extends Component implements MouseListener, Renderable {

    private static final int TICK_OFFSET = 2;

    @Getter
    private double min;
    @Getter
    private double max;
    @Getter
    @Setter
    private double step;
    @Getter
    @Setter
    private double value;
    @Getter
    @Nullable
    private Ticks ticks;
    private boolean dragged;
    private final Map<Double, ShapedText> tickLabels = new HashMap<>();

    @Getter
    private final ThemeOption<Color> barColor;
    @Getter
    private final ThemeOption<Color> thumbColor;
    @Getter
    private final ThemeOption<Color> tickColor;
    @Getter
    private final ThemeOption<Float> barHeight;
    @Getter
    private final ThemeOption<Float> thumbWidth;
    @Getter
    private final ThemeOption<Float> thumbHeight;
    @Getter
    private final ThemeOption<Float> barCornerRadius;
    @Getter
    private final ThemeOption<Float> thumbCornerRadius;
    @Getter
    private final ThemeOption<Boolean> thumbEncased;
    @Getter
    private final ThemeOption<ThumbShape> thumbShape;

    public Slider(final Rivet rivet, final double min, final double max, final double value) {
        this(rivet, min, max, 1, value);
    }

    public Slider(final Rivet rivet, final double min, final double max, final double step, final double value) {
        super(rivet);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;

        this.barColor = new ThemeOption<>(rivet, Theme.SLIDER_BAR_COLOR);
        this.thumbColor = new ThemeOption<>(rivet, Theme.SLIDER_THUMB_COLOR);
        this.tickColor = new ThemeOption<>(rivet, Theme.SLIDER_TICK_COLOR);
        this.barHeight = new ThemeOption<>(rivet, Theme.SLIDER_BAR_HEIGHT, () -> rivet.getBackend().getTextHeight() / 3F);
        this.thumbWidth = new ThemeOption<>(rivet, Theme.SLIDER_THUMB_WIDTH, () -> rivet.getBackend().getTextHeight() / 3F * 2F);
        this.thumbHeight = new ThemeOption<>(rivet, Theme.SLIDER_THUMB_HEIGHT, () -> rivet.getBackend().getTextHeight() / 3F * 2F);
        this.barCornerRadius = new ThemeOption<>(rivet, Theme.SLIDER_BAR_CORNER_RADIUS, () -> this.barHeight.value() / 2F);
        this.thumbShape = new ThemeOption<>(rivet, Theme.SLIDER_THUMB_SHAPE, () -> ThumbShape.CIRCLE);
        this.thumbCornerRadius = new ThemeOption<>(rivet, Theme.SLIDER_THUMB_CORNER_RADIUS, () -> {
            if (this.thumbShape.value() == ThumbShape.PIN) return 0F;
            return Math.min(this.thumbWidth.value(), this.thumbHeight.value()) / 2F;
        });
        this.thumbEncased = new ThemeOption<>(rivet, Theme.SLIDER_THUMB_ENCASED, () -> false);
    }

    public Slider min(final double min) {
        this.min = min;
        this.tickLabels.clear();
        return this;
    }

    public Slider max(final double max) {
        this.max = max;
        this.tickLabels.clear();
        return this;
    }

    public Slider ticks(@Nullable final Ticks ticks) {
        this.ticks = ticks;
        this.tickLabels.clear();
        return this;
    }

    @Override
    public void onMouseDown(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            this.dragged = true;
            this.updateValue(event.x(), size);
        }
    }

    @Override
    public void onMouseUp(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            this.dragged = false;
        }
    }

    @Override
    public void onMouseMove(final MouseMoveEvent event, final Size size) {
        if (this.dragged) {
            this.updateValue(event.x(), size);
        }
    }

    private void updateValue(final float mouseX, final Size size) {
        float thumbWidth = this.thumbWidth.value();
        float barWidth = this.barWidth(size);
        float progress = (mouseX - thumbWidth / 2F) / barWidth;
        progress = MathUtils.clamp(progress, 0, 1);
        double newValue = this.min + progress * (this.max - this.min);
        newValue = Math.round(newValue / this.step) * this.step;
        this.value = MathUtils.clamp(newValue, this.min, this.max);
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float thumbWidth = this.thumbWidth.value();
        float thumbHeight = this.thumbHeight.value();
        float barHeight = this.barHeight.value();
        float sliderCenter = this.ticks != null ? thumbHeight / 2F : size.height() / 2F;
        float barWidth = this.barWidth(size);
        double progress = (this.value - this.min) / (this.max - this.min);
        float thumbX = (float) (thumbWidth / 2F + barWidth * progress);

        this.renderBar(renderer, size, sliderCenter, barHeight, thumbWidth);
        this.renderThumb(renderer, sliderCenter, thumbWidth, thumbHeight, thumbX);
        if (this.ticks != null) this.renderTicks(renderer, sliderCenter, barHeight, thumbWidth, thumbHeight, barWidth);
    }

    private void renderBar(final Renderer renderer, final Size size, final float sliderCenter, final float barHeight, final float thumbWidth) {
        if (this.thumbEncased.value()) {
            renderer.fillOptimizedRoundedRect(0, sliderCenter - barHeight / 2F, size.width(), barHeight, this.barCornerRadius.value(), this.barColor.value());
        } else {
            renderer.fillOptimizedRoundedRect(thumbWidth / 2F, sliderCenter - barHeight / 2F, size.width() - thumbWidth, barHeight, this.barCornerRadius.value(), this.barColor.value());
        }
    }

    private void renderThumb(final Renderer renderer, final float sliderCenter, final float thumbWidth, final float thumbHeight, final float thumbX) {
        Color color = this.thumbColor.value();
        float cornerRadius = this.thumbCornerRadius.value();
        switch (this.thumbShape.value()) {
            case CIRCLE -> renderer.fillCircle(thumbX, sliderCenter, Math.min(thumbWidth, thumbHeight) / 2F, color);
            case SQUARE -> {
                float size = Math.min(thumbWidth, thumbHeight);
                renderer.fillOptimizedRoundedRect(thumbX - size / 2F, sliderCenter - size / 2F, size, size, cornerRadius, color);
            }
            case RECTANGLE -> renderer.fillOptimizedRoundedRect(thumbX - thumbWidth / 2F, sliderCenter - thumbHeight / 2F, thumbWidth, thumbHeight, cornerRadius, color);
            case PIN -> {
                renderer.fillOptimizedRoundedRect(thumbX - thumbWidth / 2F, sliderCenter - thumbHeight / 2F, thumbWidth, thumbHeight / 2F, cornerRadius, color);
                renderer.fillTriangle(thumbX - thumbWidth / 2F, sliderCenter, thumbX, sliderCenter + thumbHeight / 2F, thumbX + thumbWidth / 2F, sliderCenter, color);
            }
        }
    }

    private void renderTicks(final Renderer renderer, final float sliderCenter, final float barHeight, final float thumbWidth, final float thumbHeight, final float barWidth) {
        float tickStartY = sliderCenter + thumbHeight / 2F + TICK_OFFSET;
        float majorTickLength = barHeight;
        float minorTickLength = barHeight / 2F;
        Color color = this.tickColor.value();

        if (this.ticks.majorTickSpacing > 0) {
            for (double tick = 0; ; tick += this.ticks.majorTickSpacing) {
                boolean lastTick = false;
                if (tick >= this.max - this.min) {
                    tick = this.max - this.min;
                    lastTick = true;
                }
                float tickX = (float) (thumbWidth / 2F + barWidth * (tick / (this.max - this.min)));
                renderer.fillRect(tickX - 1, tickStartY, TICK_OFFSET, majorTickLength, color);

                double tickValue = this.min + tick;
                ShapedText text = this.tickLabels.computeIfAbsent(tickValue, v -> this.rivet.getBackend().shapeText(this.ticks.labelProvider.getLabel(v)));
                renderer.push();
                renderer.translate(tickX, tickStartY + majorTickLength + 2);
                renderer.scale(0.5F);
                renderer.renderText(text, 0, 0, TextOrigin.Horizontal.VISUAL_CENTER, TextOrigin.Vertical.LOGICAL_TOP);
                renderer.pop();
                if (lastTick) break;
            }
        }
        if (this.ticks.minorTickSpacing > 0) {
            for (double tick = 0; tick < this.max - this.min; tick += this.ticks.minorTickSpacing) {
                if (this.ticks.majorTickSpacing > 0 && Math.abs(tick % this.ticks.majorTickSpacing) < 1e-6) {
                    continue;
                }
                float tickX = (float) (thumbWidth / 2F + barWidth * (tick / (this.max - this.min)));
                renderer.fillRect(tickX, tickStartY, 1, minorTickLength, color);
            }
        }
    }

    @Override
    public void computeIdealSize() {
        float height;
        if (this.ticks == null) {
            height = this.thumbHeight.value();
        } else {
            height = this.thumbHeight.value() + TICK_OFFSET + this.barHeight.value() + TICK_OFFSET + this.rivet.getBackend().getTextHeight() / 2F;
        }
        this.idealSize = new Size(this.rivet.getBackend().getTextHeight() * 10, height);
    }

    @Override
    public void computeLayout(final Size size) {
    }

    private float barWidth(final Size size) {
        return size.width() - this.thumbWidth.value();
    }


    public record Ticks(double majorTickSpacing, double minorTickSpacing, TickLabelProvider labelProvider) {
        public Ticks(final double majorTickSpacing, final double minorTickSpacing) {
            this(majorTickSpacing, minorTickSpacing, d -> String.format("%,f", d));
        }
    }

    @FunctionalInterface
    public interface TickLabelProvider {
        String getLabel(final double value);
    }

    public enum ThumbShape {
        CIRCLE,
        SQUARE,
        RECTANGLE,
        PIN
    }

}
