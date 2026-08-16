package net.lenni0451.rivet.component.impl.slider;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.math.Size;

import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class Slider extends AbstractSlider<Slider> {

    private final SliderThumb thumb;
    @Getter
    private final ListenerList<Consumer<Double>> valueChangeListener = new ListenerList<>();

    public Slider(final double min, final double max, final double value) {
        super(min, max);
        this.thumb = this.addThumb(value);
    }

    public Slider(final double min, final double max, final double step, final double value) {
        super(min, max, step);
        this.thumb = this.addThumb(value);
    }

    public final double value() {
        return this.thumb.value();
    }

    public final Slider value(final double value) {
        return this.value(value, true);
    }

    public final Slider value(final double value, final boolean fireListeners) {
        if (this.thumb.value() != value) {
            this.thumb.value(value);
            if (fireListeners) {
                this.valueChangeListener.call(c -> c.accept(value));
            }
        }
        return this;
    }

    @Override
    protected void onThumbDrag(final SliderThumb thumb, final double newValue) {
        this.value(newValue, true);
    }

    @Override
    protected void renderFills(final Renderer renderer, final Size size, final float barWidth, final float barHeight, final float sliderCenter, final float thumbWidth, final Color barFillColor) {
        float startX = this.thumbEncased().value() ? 0 : thumbWidth / 2F;
        float thumbX = this.thumbX(this.value(), thumbWidth, barWidth);
        this.renderFill(renderer, startX, thumbX, sliderCenter, barHeight, barFillColor);
    }

}
