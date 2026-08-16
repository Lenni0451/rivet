package net.lenni0451.rivet.component.impl.slider;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.math.Size;

public class RangeSlider extends AbstractSlider<RangeSlider> {

    private final SliderThumb lowerThumb;
    private final SliderThumb upperThumb;

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

    public final double upperValue() {
        return this.upperThumb.value();
    }

    @Override
    protected void onThumbDrag(final SliderThumb thumb, final double newValue) {
        if (this.lowerThumb.value() == this.upperThumb.value()) {
            if (newValue < this.lowerThumb.value()) {
                this.lowerThumb.value(newValue);
                this.draggedThumb(this.lowerThumb);
            } else {
                this.upperThumb.value(newValue);
                this.draggedThumb(this.upperThumb);
            }
        } else if (thumb == this.lowerThumb) {
            this.lowerThumb.value(newValue);
            if (newValue >= this.upperThumb.value()) {
                this.upperThumb.value(newValue);
            }
        } else if (thumb == this.upperThumb) {
            this.upperThumb.value(newValue);
            if (newValue <= this.lowerThumb.value()) {
                this.lowerThumb.value(newValue);
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
