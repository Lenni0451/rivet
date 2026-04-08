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
    private final ThemeOption<Integer> barHeight;
    @Getter
    private final ThemeOption<Integer> thumbRadius;
    @Getter
    private final ThemeOption<Integer> barCornerRadius;
    @Getter
    private final ThemeOption<Integer> thumbCornerRadius;
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
        this.barHeight = new ThemeOption<>(rivet, Theme.SLIDER_BAR_HEIGHT, () -> (int) (rivet.getBackend().getTextHeight() / 3F));
        this.thumbRadius = new ThemeOption<>(rivet, Theme.SLIDER_THUMB_RADIUS, () -> (int) (rivet.getBackend().getTextHeight() / 3F));
        this.barCornerRadius = new ThemeOption<>(rivet, Theme.SLIDER_BAR_CORNER_RADIUS, () -> this.barHeight.value() / 2);
        this.thumbCornerRadius = new ThemeOption<>(rivet, Theme.SLIDER_THUMB_CORNER_RADIUS, () -> this.thumbRadius.value());
        this.thumbEncased = new ThemeOption<>(rivet, Theme.SLIDER_THUMB_ENCASED, () -> false);
        this.thumbShape = new ThemeOption<>(rivet, Theme.SLIDER_THUMB_SHAPE, () -> ThumbShape.CIRCLE);
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
        float thumbRadius = this.thumbRadius.value();
        float barWidth = this.barWidth(size);
        float progress = (mouseX - thumbRadius) / barWidth;
        progress = MathUtils.clamp(progress, 0, 1);
        double newValue = this.min + progress * (this.max - this.min);
        newValue = Math.round(newValue / this.step) * this.step;
        this.value = MathUtils.clamp(newValue, this.min, this.max);
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float thumbRadius = this.thumbRadius.value();
        float barHeight = this.barHeight.value();
        float sliderCenter = this.ticks != null ? thumbRadius : size.height() / 2F;
        float barWidth = this.barWidth(size);
        double progress = (this.value - this.min) / (this.max - this.min);
        float thumbX = (float) (thumbRadius + barWidth * progress);

        this.renderBar(renderer, size, sliderCenter, barHeight, thumbRadius);
        this.renderThumb(renderer, sliderCenter, thumbRadius, thumbX);
        if (this.ticks != null) this.renderTicks(renderer, sliderCenter, barHeight, thumbRadius, barWidth);
    }

    private void renderBar(final Renderer renderer, final Size size, final float sliderCenter, final float barHeight, final float thumbRadius) {
        if (this.thumbEncased.value()) {
            renderer.fillOptimizedRoundedRect(0, sliderCenter - barHeight / 2F, size.width(), barHeight, this.barCornerRadius.value(), this.barColor.value());
        } else {
            renderer.fillOptimizedRoundedRect(thumbRadius / 2, sliderCenter - barHeight / 2F, size.width() - thumbRadius, barHeight, this.barCornerRadius.value(), this.barColor.value());
        }
    }

    private void renderThumb(final Renderer renderer, final float sliderCenter, final float thumbRadius, final float thumbX) {
        Color color = this.thumbColor.value();
        int cornerRadius = this.thumbCornerRadius.value();
        switch (this.thumbShape.value()) {
            case CIRCLE, SQUARE -> renderer.fillOptimizedRoundedRect(thumbX - thumbRadius, sliderCenter - thumbRadius, thumbRadius * 2, thumbRadius * 2, cornerRadius, color);
            case RECTANGLE -> renderer.fillOptimizedRoundedRect(thumbX - thumbRadius / 2F, sliderCenter - thumbRadius, thumbRadius, thumbRadius * 2, cornerRadius, color);
            case PIN -> {
                renderer.fillOptimizedRoundedRect(thumbX - thumbRadius, sliderCenter - thumbRadius, thumbRadius * 2, thumbRadius, cornerRadius, color);
                renderer.fillTriangle(thumbX - thumbRadius, sliderCenter, thumbX, sliderCenter + thumbRadius, thumbX + thumbRadius, sliderCenter, color);
            }
        }
    }

    private void renderTicks(final Renderer renderer, final float sliderCenter, final float barHeight, final float thumbRadius, final float barWidth) {
        float tickStartY = sliderCenter + thumbRadius + TICK_OFFSET;
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
                float tickX = (float) (thumbRadius + barWidth * (tick / (this.max - this.min)));
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
                float tickX = (float) (thumbRadius + barWidth * (tick / (this.max - this.min)));
                renderer.fillRect(tickX, tickStartY, 1, minorTickLength, color);
            }
        }
    }

    @Override
    public void computeIdealSize() {
        float height;
        if (this.ticks == null) {
            height = this.thumbRadius.value() * 2;
        } else {
            height = this.thumbRadius.value() * 2 + TICK_OFFSET + this.barHeight.value() + TICK_OFFSET + this.rivet.getBackend().getTextHeight() / 2F;
        }
        this.idealSize = new Size(this.rivet.getBackend().getTextHeight() * 10, height);
    }

    @Override
    public void computeLayout(final Size size) {
    }

    private float barWidth(final Size size) {
        return size.width() - this.thumbRadius.value() * 2;
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
