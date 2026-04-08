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

@Accessors(chain = true, fluent = true)
public class Slider extends Component implements MouseListener, Renderable {

    private static final int TICK_OFFSET = 2;

    @Getter
    @Setter
    private double min;
    @Getter
    @Setter
    private double max;
    @Getter
    @Setter
    private double step;
    @Getter
    @Setter
    private double value;
    @Getter
    @Setter
    @Nullable
    private Ticks ticks;
    private boolean dragged;

    @Getter
    private final ThemeOption<Color> barColor;
    @Getter
    private final ThemeOption<Color> knobColor;
    @Getter
    private final ThemeOption<Color> tickColor;
    @Getter
    private final ThemeOption<Integer> barHeight;
    @Getter
    private final ThemeOption<Integer> knobRadius;
    @Getter
    private final ThemeOption<Integer> barCornerRadius;
    @Getter
    private final ThemeOption<Integer> knobCornerRadius;
    @Getter
    private final ThemeOption<Boolean> knobEncased;

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
        this.knobColor = new ThemeOption<>(rivet, Theme.SLIDER_KNOB_COLOR);
        this.tickColor = new ThemeOption<>(rivet, Theme.SLIDER_TICK_COLOR);
        this.barHeight = new ThemeOption<>(rivet, Theme.SLIDER_BAR_HEIGHT, () -> (int) (rivet.getBackend().getTextHeight() / 3F));
        this.knobRadius = new ThemeOption<>(rivet, Theme.SLIDER_KNOB_RADIUS, () -> (int) (rivet.getBackend().getTextHeight() / 3F));
        this.barCornerRadius = new ThemeOption<>(rivet, Theme.SLIDER_BAR_CORNER_RADIUS, () -> this.barHeight.value() / 2);
        this.knobCornerRadius = new ThemeOption<>(rivet, Theme.SLIDER_KNOB_CORNER_RADIUS, () -> this.knobRadius.value());
        this.knobEncased = new ThemeOption<>(rivet, Theme.SLIDER_KNOB_ENCASED, () -> false);
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
        float knobRadius = this.knobRadius.value();
        float barWidth = this.barWidth(size);
        float progress = (mouseX - knobRadius) / barWidth;
        progress = MathUtils.clamp(progress, 0, 1);
        double newValue = this.min + progress * (this.max - this.min);
        newValue = Math.round(newValue / this.step) * this.step;
        this.value = MathUtils.clamp(newValue, this.min, this.max);
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float knobRadius = this.knobRadius.value();
        float barHeight = this.barHeight.value();
        float sliderCenter = size.height() / 2F;
        if (this.ticks != null) {
            sliderCenter = knobRadius;
        }

        if (this.knobEncased.value()) {
            renderer.fillOptimizedRoundedRect(0, sliderCenter - barHeight / 2F, size.width(), barHeight, this.barCornerRadius.value(), this.barColor.value());
        } else {
            renderer.fillOptimizedRoundedRect(knobRadius / 2, sliderCenter - barHeight / 2F, size.width() - knobRadius, barHeight, this.barCornerRadius.value(), this.barColor.value());
        }
        float barWidth = this.barWidth(size);
        double progress = (this.value - this.min) / (this.max - this.min);
        float knobX = (float) (knobRadius + barWidth * progress);
        renderer.fillOptimizedRoundedRect(knobX - knobRadius, sliderCenter - knobRadius, knobRadius * 2, knobRadius * 2, this.knobCornerRadius.value(), this.knobColor.value());

        if (this.ticks != null) {
            float tickStartY = sliderCenter + knobRadius + TICK_OFFSET;
            float majorTickLength = barHeight;
            float minorTickLength = barHeight / 2F;

            if (this.ticks.majorTickSpacing > 0) {
                for (double tick = 0; ; tick += this.ticks.majorTickSpacing) {
                    boolean lastTick = false;
                    if (tick >= this.max - this.min) {
                        tick = this.max - this.min;
                        lastTick = true;
                    }
                    float tickX = (float) (knobRadius + barWidth * (tick / (this.max - this.min)));
                    renderer.fillRect(tickX - 1, tickStartY, TICK_OFFSET, majorTickLength, this.tickColor.value());
                    String label = this.ticks.labelProvider.getLabel(this.min + tick);
                    ShapedText text = this.rivet.getBackend().shapeText(label);
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
                        continue; //Skip if it's a major tick
                    }
                    float tickX = (float) (knobRadius + barWidth * (tick / (this.max - this.min)));
                    renderer.fillRect(tickX, tickStartY, 1, minorTickLength, this.tickColor.value());
                }
            }
        }
    }

    @Override
    public void computeIdealSize() {
        float height;
        if (this.ticks == null) {
            height = this.knobRadius.value() * 2;
        } else {
            height = this.knobRadius.value() * 2 + TICK_OFFSET + this.barHeight.value() + TICK_OFFSET + this.rivet.getBackend().getTextHeight() / 2F;
        }
        this.idealSize = new Size(this.rivet.getBackend().getTextHeight() * 10, height);
    }

    @Override
    public void computeLayout(final Size size) {
    }

    private float barWidth(final Size size) {
        return size.width() - this.knobRadius.value() * 2;
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

}
