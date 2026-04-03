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
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseListener;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Size;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class Slider extends Component implements MouseListener, Renderable {

    private float min;
    private float max;
    private float step;
    private float value;
    private boolean dragged;

    public Slider(final Rivet rivet, final float min, final float max, final float value) {
        this(rivet, min, max, 1, value);
    }

    public Slider(final Rivet rivet, final float min, final float max, final float step, final float value) {
        super(rivet);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;
    }

    @Override
    public void onMouseDown(final MouseButtonEvent event, final Size size) {
        this.dragged = true;
        this.updateValue(event.x(), size);
    }

    @Override
    public void onMouseUp(final MouseButtonEvent event, final Size size) {
        this.dragged = false;
    }

    @Override
    public void onMouseMove(final MouseMoveEvent event, final Size size) {
        if (this.dragged) {
            this.updateValue(event.x(), size);
        }
    }

    private void updateValue(final float mouseX, final Size size) {
        float knobRadius = size.height() / 3F;
        float barWidth = size.width() - knobRadius * 2;
        float progress = (mouseX - knobRadius) / barWidth;
        progress = MathUtils.clamp(progress, 0, 1);
        float newValue = this.min + progress * (this.max - this.min);
        newValue = Math.round(newValue / this.step) * this.step;
        this.value = MathUtils.clamp(newValue, this.min, this.max);
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float knobRadius = size.height() / 3F;
        float barHeight = size.height() / 3F;
        float visualBarWidth = size.width() - knobRadius;
        renderer.fillRoundedRect(knobRadius / 2, size.height() / 2F - barHeight / 2F, visualBarWidth, barHeight, barHeight / 2F, Color.RED);
        float barWidth = size.width() - knobRadius * 2;
        float progress = (this.value - this.min) / (this.max - this.min);
        renderer.fillCircle(knobRadius + barWidth * progress, size.height() / 2F, knobRadius, Color.BLUE);
    }

    @Override
    public void computeIdealSize() {
        this.idealSize = new Size(100, 20);
    }

    @Override
    public void computeLayout(final Size size) {
    }

}
