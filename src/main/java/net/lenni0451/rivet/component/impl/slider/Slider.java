package net.lenni0451.rivet.component.impl.slider;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.property.DoubleProperty;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class Slider extends AbstractSlider<Slider> {

    private final SliderThumb thumb;
    @Getter
    private final DoubleProperty value;

    public Slider(final double min, final double max, final double value) {
        super(min, max);
        this.thumb = this.addThumb(value);
        this.value = new DoubleProperty(value);
        this.init();
    }

    public Slider(final double min, final double max, final double step, final double value) {
        super(min, max, step);
        this.thumb = this.addThumb(value);
        this.value = new DoubleProperty(value);
        this.init();
    }

    private void init() {
        this.value.updateListener().add(this.thumb::value);
    }

    public final Slider value(final double value) {
        this.value.set(value);
        return this;
    }

    @Override
    protected void onThumbDrag(final SliderThumb thumb, final double newValue) {
        this.value.set(newValue);
    }

    @Override
    protected void renderFills(final Renderer renderer, final Size size, final float barWidth, final float barHeight, final float sliderCenter, final float thumbWidth, final Color barFillColor) {
        float startX = this.thumbEncased().value() ? 0 : thumbWidth / 2F;
        float thumbX = this.thumbX(this.value.get(), thumbWidth, barWidth);
        this.renderFill(renderer, startX, thumbX, sliderCenter, barHeight, barFillColor);
    }

}
