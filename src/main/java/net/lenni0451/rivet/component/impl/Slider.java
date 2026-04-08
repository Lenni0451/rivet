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

    public Slider(final Rivet rivet, final double min, final double max, final double value) {
        this(rivet, min, max, 1, value);
    }

    public Slider(final Rivet rivet, final double min, final double max, final double step, final double value) {
        super(rivet);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;
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
        float knobRadius = this.knobRadius();
        float barWidth = this.barWidth(size);
        float progress = (mouseX - knobRadius) / barWidth;
        progress = MathUtils.clamp(progress, 0, 1);
        double newValue = this.min + progress * (this.max - this.min);
        newValue = Math.round(newValue / this.step) * this.step;
        this.value = MathUtils.clamp(newValue, this.min, this.max);
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float knobRadius = this.knobRadius();
        float barHeight = this.barHeight();
        float visualBarWidth = size.width() - knobRadius;
        float sliderCenter = size.height() / 2F;
        if (this.ticks != null) {
            sliderCenter = knobRadius;
        }
        renderer.fillRoundedRect(knobRadius / 2, sliderCenter - barHeight / 2F, visualBarWidth, barHeight, barHeight / 2F, Color.RED);
        float barWidth = this.barWidth(size);
        double progress = (this.value - this.min) / (this.max - this.min);
        renderer.fillCircle((float) (knobRadius + barWidth * progress), sliderCenter, knobRadius, Color.BLUE);

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
                    renderer.fillRect(tickX - 1, tickStartY, TICK_OFFSET, majorTickLength, Color.WHITE);
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
                    renderer.fillRect(tickX, tickStartY, 1, minorTickLength, Color.WHITE);
                }
            }
        }
    }

    @Override
    public void computeIdealSize() {
        float height;
        if (this.ticks == null) {
            height = this.knobRadius() * 2;
        } else {
            height = this.knobRadius() * 2 + TICK_OFFSET + this.barHeight() + TICK_OFFSET + this.rivet.getBackend().getTextHeight() / 2F;
        }
        this.idealSize = new Size(this.rivet.getBackend().getTextHeight() * 10, height);
    }

    @Override
    public void computeLayout(final Size size) {
    }

    private float knobRadius() {
        return this.rivet.getBackend().getTextHeight() / 3F;
    }

    private float barHeight() {
        return this.rivet.getBackend().getTextHeight() / 3F;
    }

    private float barWidth(final Size size) {
        return size.width() - this.knobRadius() * 2;
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
