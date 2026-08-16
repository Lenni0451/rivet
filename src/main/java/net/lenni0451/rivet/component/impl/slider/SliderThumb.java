package net.lenni0451.rivet.component.impl.slider;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.Interpolator;
import net.lenni0451.rivet.animation.StateTransition;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Size;

import javax.annotation.Nullable;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class SliderThumb {

    private final AbstractSlider<?> slider;

    @Getter
    private double value;
    @Getter
    boolean dragged = false;
    @Getter
    boolean hovered = false;
    @Nullable
    SliderTooltip tooltip;
    private StateTransition<Color, AbstractSlider.State> thumbColorTransition;
    private StateTransition<Color, AbstractSlider.State> thumbOutlineColorTransition;

    public SliderThumb(final AbstractSlider<?> slider, final double initialValue) {
        this.slider = slider;
        this.value = initialValue;
    }

    protected void initTransitions() {
        this.thumbColorTransition = new StateTransition<>(
                this.slider,
                this::state,
                (start, target) -> {
                    if (start.equals(AbstractSlider.State.DRAGGED) || target.equals(AbstractSlider.State.DRAGGED)) {
                        return this.slider.clickAnimationConfig().value();
                    } else {
                        return this.slider.hoverAnimationConfig().value();
                    }
                },
                () -> switch (this.state()) {
                    case INACTIVE -> this.slider.thumbColor().value();
                    case HOVERED -> this.slider.thumbHoverColor().value();
                    case DRAGGED -> this.slider.thumbClickColor().value();
                    case DISABLED -> this.slider.disabledThumbColor().value();
                },
                Interpolator.COLOR
        );
        this.thumbOutlineColorTransition = new StateTransition<>(
                this.slider,
                this::state,
                (start, target) -> {
                    if (start.equals(AbstractSlider.State.DRAGGED) || target.equals(AbstractSlider.State.DRAGGED)) {
                        return this.slider.clickAnimationConfig().value();
                    } else {
                        return this.slider.hoverAnimationConfig().value();
                    }
                },
                () -> switch (this.state()) {
                    case INACTIVE -> this.slider.thumbOutlineColor().value();
                    case HOVERED -> this.slider.thumbHoverOutlineColor().value();
                    case DRAGGED -> this.slider.thumbClickOutlineColor().value();
                    case DISABLED -> this.slider.disabledThumbOutlineColor().value();
                },
                Interpolator.COLOR
        );
    }

    private AbstractSlider.State state() {
        if (this.slider.disabled()) {
            return AbstractSlider.State.DISABLED;
        } else if (this.dragged) {
            return AbstractSlider.State.DRAGGED;
        } else {
            return this.hovered ? AbstractSlider.State.HOVERED : AbstractSlider.State.INACTIVE;
        }
    }

    protected void value(final double newValue) {
        if (this.value != newValue) {
            this.value = newValue;
            if (this.tooltip != null) {
                this.tooltip.text(this.slider.formatValue(this.value));
                this.slider.updatePositionInternal(this.slider.absoluteBounds());
            }
        }
    }

    protected void updateValue(final float mouseX, final Size size) {
        double newValue = this.slider.valueAtX(mouseX, size);
        newValue = net.lenni0451.rivet.utils.MathUtils.snap(newValue, this.slider.min(), this.slider.max(), this.slider.step());
        this.slider.onThumbDrag(this, newValue);
    }

    protected void render(final Renderer renderer, final float sliderCenter, final float thumbWidth, final float thumbHeight, final float thumbX) {
        Color color = this.thumbColorTransition.value();
        Color outlineColor = this.thumbOutlineColorTransition.value();
        float outlineWidth = this.slider.thumbOutlineWidth().value();
        Corners cornerRadius = this.slider.thumbCornerRadius().value();

        switch (this.slider.thumbShape().value()) {
            case CIRCLE -> {
                renderer.fillCircle(thumbX, sliderCenter, Math.min(thumbWidth, thumbHeight) / 2F, color);
                if (outlineWidth > 0) {
                    renderer.outlineCircle(thumbX, sliderCenter, Math.min(thumbWidth, thumbHeight) / 2F, outlineWidth, outlineColor);
                }
            }
            case RECTANGLE -> {
                renderer.optimizedFillRoundedRect(thumbX - thumbWidth / 2F, sliderCenter - thumbHeight / 2F, thumbWidth, thumbHeight, cornerRadius, color);
                if (outlineWidth > 0) {
                    renderer.optimizedOutlineRoundedRect(thumbX - thumbWidth / 2F, sliderCenter - thumbHeight / 2F, thumbWidth, thumbHeight, cornerRadius, outlineWidth, outlineColor);
                }
            }
            case PIN -> {
                if (outlineWidth > 0) {
                    renderer.fillRect(thumbX - thumbWidth / 2F, sliderCenter - thumbHeight / 2F, thumbWidth, thumbHeight / 2F, outlineColor);
                    renderer.fillTriangle(thumbX - thumbWidth / 2F, sliderCenter, thumbX, sliderCenter + thumbHeight / 2F, thumbX + thumbWidth / 2F, sliderCenter, outlineColor);

                    float innerWidth = thumbWidth - outlineWidth * 2;
                    float innerHeight = thumbHeight - outlineWidth * 2;
                    if (innerWidth > 0 && innerHeight > 0) {
                        renderer.fillRect(thumbX - innerWidth / 2F, sliderCenter - thumbHeight / 2F + outlineWidth, innerWidth, thumbHeight / 2F - outlineWidth, color);
                        renderer.fillTriangle(thumbX - innerWidth / 2F, sliderCenter, thumbX, sliderCenter + thumbHeight / 2F - outlineWidth, thumbX + innerWidth / 2F, sliderCenter, color);
                    }
                } else {
                    renderer.fillRect(thumbX - thumbWidth / 2F, sliderCenter - thumbHeight / 2F, thumbWidth, thumbHeight / 2F, color);
                    renderer.fillTriangle(thumbX - thumbWidth / 2F, sliderCenter, thumbX, sliderCenter + thumbHeight / 2F, thumbX + thumbWidth / 2F, sliderCenter, color);
                }
            }
        }
    }

}
