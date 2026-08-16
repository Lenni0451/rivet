package net.lenni0451.rivet.component.impl.slider;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.math.Size;

import java.util.function.BiConsumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class RangeSlider extends AbstractSlider<RangeSlider> {

    private final SliderThumb lowerThumb;
    private final SliderThumb upperThumb;
    @Getter
    private final ListenerList<BiConsumer<Double, Double>> rangeChangeListener = new ListenerList<>();

    public RangeSlider(final double min, final double max, final double lowerValue, final double upperValue) {
        super(min, max);
        this.lowerThumb = this.addThumb(lowerValue);
        this.upperThumb = this.addThumb(upperValue);
    }

    public RangeSlider(final double min, final double max, final double step, final double lowerValue, final double upperValue) {
        super(min, max, step);
        this.lowerThumb = this.addThumb(lowerValue);
        this.upperThumb = this.addThumb(upperValue);
    }

    public final double lowerValue() {
        return this.lowerThumb.value();
    }

    public final RangeSlider lowerValue(final double value) {
        return this.lowerValue(value, true);
    }

    public final RangeSlider lowerValue(final double value, final boolean fireListeners) {
        return this.range(value, this.upperThumb.value(), fireListeners);
    }

    public final double upperValue() {
        return this.upperThumb.value();
    }

    public final RangeSlider upperValue(final double value) {
        return this.upperValue(value, true);
    }

    public final RangeSlider upperValue(final double value, final boolean fireListeners) {
        return this.range(this.lowerThumb.value(), value, fireListeners);
    }

    public final RangeSlider range(final double lowerValue, final double upperValue) {
        return this.range(lowerValue, upperValue, true);
    }

    public final RangeSlider range(final double lowerValue, final double upperValue, final boolean fireListeners) {
        boolean changed = false;
        if (this.lowerThumb.value() != lowerValue) {
            this.lowerThumb.value(lowerValue);
            changed = true;
        }
        if (this.upperThumb.value() != upperValue) {
            this.upperThumb.value(upperValue);
            changed = true;
        }

        if (changed && fireListeners) {
            this.rangeChangeListener.call(c -> c.accept(lowerValue, upperValue));
        }
        return this;
    }

    @Override
    protected void onThumbDrag(final SliderThumb thumb, final double newValue) {
        if (thumb == this.lowerThumb) {
            if (newValue > this.upperThumb.value()) {
                this.range(this.upperThumb.value(), newValue);
                this.draggedThumb(this.upperThumb);
            } else {
                this.lowerValue(newValue);
            }
        } else if (thumb == this.upperThumb) {
            if (newValue < this.lowerThumb.value()) {
                this.range(newValue, this.lowerThumb.value());
                this.draggedThumb(this.lowerThumb);
            } else {
                this.upperValue(newValue);
            }
        }
    }

    @Override
    protected void renderFills(final Renderer renderer, final Size size, final float barWidth, final float barHeight, final float sliderCenter, final float thumbWidth, final Color barFillColor) {
        float startX = this.thumbX(this.lowerThumb.value(), thumbWidth, barWidth);
        float endX = this.thumbX(this.upperThumb.value(), thumbWidth, barWidth);
        this.renderFill(renderer, startX, endX, sliderCenter, barHeight, barFillColor);
    }

}
