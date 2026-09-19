package net.lenni0451.rivet.component.impl.slider;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.math.Size;

import net.lenni0451.rivet.property.DoubleProperty;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class RangeSlider extends AbstractSlider<RangeSlider> {

    private final SliderThumb lowerThumb;
    private final SliderThumb upperThumb;
    @Getter
    private final DoubleProperty lowerValue;
    @Getter
    private final DoubleProperty upperValue;

    public RangeSlider(final double min, final double max, final double lowerValue, final double upperValue) {
        super(min, max);
        this.lowerThumb = this.addThumb(lowerValue);
        this.upperThumb = this.addThumb(upperValue);
        this.lowerValue = new DoubleProperty(lowerValue);
        this.upperValue = new DoubleProperty(upperValue);
        this.init();
    }

    public RangeSlider(final double min, final double max, final double step, final double lowerValue, final double upperValue) {
        super(min, max, step);
        this.lowerThumb = this.addThumb(lowerValue);
        this.upperThumb = this.addThumb(upperValue);
        this.lowerValue = new DoubleProperty(lowerValue);
        this.upperValue = new DoubleProperty(upperValue);
        this.init();
    }

    private void init() {
        this.lowerValue.updateListener().add(this.lowerThumb::value);
        this.lowerValue.updateListener().add(v -> {
            if (v > this.upperValue.get()) {
                this.upperValue.set(v);
            }
        });

        this.upperValue.updateListener().add(this.upperThumb::value);
        this.upperValue.updateListener().add(v -> {
            if (v < this.lowerValue.get()) {
                this.lowerValue.set(v);
            }
        });
    }

    public final RangeSlider lowerValue(final double lowerValue) {
        this.lowerValue.set(lowerValue);
        return this;
    }

    public final RangeSlider upperValue(final double upperValue) {
        this.upperValue.set(upperValue);
        return this;
    }

    public final RangeSlider range(final double lowerValue, final double upperValue) {
        return this.range(lowerValue, upperValue, true);
    }

    public final RangeSlider range(double lowerValue, double upperValue, final boolean fireListeners) {
        if (lowerValue > upperValue) {
            double temp = lowerValue;
            lowerValue = upperValue;
            upperValue = temp;
        }

        if (lowerValue > this.upperValue.get()) {
            this.upperValue.set(upperValue, fireListeners);
            this.lowerValue.set(lowerValue, fireListeners);
        } else {
            this.lowerValue.set(lowerValue, fireListeners);
            this.upperValue.set(upperValue, fireListeners);
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
                this.lowerValue.set(newValue);
            }
        } else if (thumb == this.upperThumb) {
            if (newValue < this.lowerThumb.value()) {
                this.range(newValue, this.lowerThumb.value());
                this.draggedThumb(this.lowerThumb);
            } else {
                this.upperValue.set(newValue);
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
