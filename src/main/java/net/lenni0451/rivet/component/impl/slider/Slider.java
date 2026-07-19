package net.lenni0451.rivet.component.impl.slider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.Interpolator;
import net.lenni0451.rivet.animation.StateTransition;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import net.lenni0451.rivet.utils.FormatUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class Slider extends Component {

    private static final int TICK_OFFSET = 2;

    @Getter
    private Font font;
    @Getter
    private double min;
    @Getter
    private double max;
    @Getter
    private double step;
    @Getter
    @Setter
    private double value;
    @Getter
    private final ListenerList<Consumer<Double>> valueChangeListener = new ListenerList<>();
    @Getter
    @Nullable
    private Ticks ticks;

    private boolean dragged = false;
    private boolean hovered = false;
    @Nullable
    private SliderTooltip tooltip;
    private final Map<Double, ShapedText> tickLabels = new HashMap<>();
    private String cachedFormatString = null;

    @Getter
    private final ThemeOption<Color> barColor = new ThemeOption<>(this, Theme.SLIDER_BAR_COLOR);
    @Getter
    private final ThemeOption<Color> activeBarColor = new ThemeOption<>(this, Theme.SLIDER_ACTIVE_BAR_COLOR);
    @Getter
    private final ThemeOption<Color> thumbColor = new ThemeOption<>(this, Theme.SLIDER_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> thumbClickColor = new ThemeOption<>(this, Theme.SLIDER_THUMB_CLICK_COLOR);
    @Getter
    private final ThemeOption<Color> tickColor = new ThemeOption<>(this, Theme.SLIDER_TICK_COLOR);
    @Getter
    private final ThemeOption<Float> barHeight = new ThemeOption<>(this, Theme.SLIDER_BAR_HEIGHT);
    @Getter
    private final ThemeOption<Float> thumbWidth = new ThemeOption<>(this, Theme.SLIDER_THUMB_WIDTH);
    @Getter
    private final ThemeOption<Float> thumbHeight = new ThemeOption<>(this, Theme.SLIDER_THUMB_HEIGHT);
    @Getter
    private final ThemeOption<Float> barCornerRadius = new ThemeOption<>(this, Theme.SLIDER_BAR_CORNER_RADIUS);
    @Getter
    private final ThemeOption<Float> thumbCornerRadius = new ThemeOption<>(this, Theme.SLIDER_THUMB_CORNER_RADIUS);
    @Getter
    private final ThemeOption<Boolean> thumbEncased = new ThemeOption<>(this, Theme.SLIDER_THUMB_ENCASED);
    @Getter
    private final ThemeOption<ThumbShape> thumbShape = new ThemeOption<>(this, Theme.SLIDER_THUMB_SHAPE);
    @Getter
    private final ThemeOption<Color> thumbOutlineColor = new ThemeOption<>(this, Theme.SLIDER_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> thumbClickOutlineColor = new ThemeOption<>(this, Theme.SLIDER_THUMB_CLICK_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Float> thumbOutlineWidth = new ThemeOption<>(this, Theme.SLIDER_THUMB_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Boolean> showTooltip = new ThemeOption<>(this, Theme.SLIDER_SHOW_TOOLTIP);
    @Getter
    private final ThemeOption<String> tooltipFormat = new ThemeOption<>(this, Theme.SLIDER_TOOLTIP_FORMAT);
    @Getter
    private final ThemeOption<Color> disabledBarColor = new ThemeOption<>(this, Theme.SLIDER_DISABLED_BAR_COLOR);
    @Getter
    private final ThemeOption<Color> disabledActiveBarColor = new ThemeOption<>(this, Theme.SLIDER_DISABLED_ACTIVE_BAR_COLOR);
    @Getter
    private final ThemeOption<Color> disabledThumbColor = new ThemeOption<>(this, Theme.SLIDER_DISABLED_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> disabledThumbOutlineColor = new ThemeOption<>(this, Theme.SLIDER_DISABLED_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledTickColor = new ThemeOption<>(this, Theme.SLIDER_DISABLED_TICK_COLOR);
    @Getter
    private final ThemeOption<Color> thumbHoverColor = new ThemeOption<>(this, Theme.SLIDER_THUMB_HOVER_COLOR);
    @Getter
    private final ThemeOption<Color> thumbHoverOutlineColor = new ThemeOption<>(this, Theme.SLIDER_THUMB_HOVER_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<AnimationConfig> hoverAnimationConfig = new ThemeOption<>(this, Theme.SLIDER_HOVER_ANIMATION);
    @Getter
    private final ThemeOption<AnimationConfig> clickAnimationConfig = new ThemeOption<>(this, Theme.SLIDER_CLICK_ANIMATION);

    private StateTransition<Color, State> thumbColorTransition;
    private StateTransition<Color, State> thumbOutlineColorTransition;

    public Slider(final double min, final double max, final double value) {
        this(min, max, 1, value);
    }

    public Slider(final double min, final double max, final double step, final double value) {
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;

        this.tooltipFormat.initListener().add(f -> this.cachedFormatString = null);
    }

    public Slider font(final Font font) {
        if (this.font != font) {
            this.font = font;
            this.tickLabels.clear();
            if (this.parent() != null) {
                this.parent().requestLayoutRecalculation();
            }
        }
        return this;
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

    public Slider step(final double step) {
        this.step = step;
        this.cachedFormatString = null;
        return this;
    }

    public Slider ticks(@Nullable final Ticks ticks) {
        this.ticks = ticks;
        this.tickLabels.clear();
        return this;
    }

    private Font usedFont() {
        return this.font != null ? this.font : this.rivet().backend().font();
    }

    private State state() {
        if (this.disabled()) {
            return State.DISABLED;
        } else if (this.dragged) {
            return State.DRAGGED;
        } else {
            return this.hovered ? State.HOVERED : State.INACTIVE;
        }
    }

    @Override
    protected void onComponentAdded() {
        this.thumbColorTransition = new StateTransition<>(
                this,
                this::state,
                (start, target) -> {
                    if (start.equals(State.DRAGGED) || target.equals(State.DRAGGED)) {
                        return this.clickAnimationConfig.value();
                    } else {
                        return this.hoverAnimationConfig.value();
                    }
                },
                () -> switch (this.state()) {
                    case INACTIVE -> this.thumbColor.value();
                    case HOVERED -> this.thumbHoverColor.value();
                    case DRAGGED -> this.thumbClickColor.value();
                    case DISABLED -> this.disabledThumbColor.value();
                },
                Interpolator.COLOR
        );
        this.thumbOutlineColorTransition = new StateTransition<>(
                this,
                this::state,
                (start, target) -> {
                    if (start.equals(State.DRAGGED) || target.equals(State.DRAGGED)) {
                        return this.clickAnimationConfig.value();
                    } else {
                        return this.hoverAnimationConfig.value();
                    }
                },
                () -> switch (this.state()) {
                    case INACTIVE -> this.thumbOutlineColor.value();
                    case HOVERED -> this.thumbHoverOutlineColor.value();
                    case DRAGGED -> this.thumbClickOutlineColor.value();
                    case DISABLED -> this.disabledThumbOutlineColor.value();
                },
                Interpolator.COLOR
        );
    }

    @Override
    protected void onComponentRemoved() {
        if (this.tooltip != null) {
            this.tooltip.remove();
            this.tooltip = null;
        }
        this.tickLabels.clear();
        this.dragged = false;
        this.hovered = false;
    }

    @Override
    protected void onComponentDisabled() {
        this.onComponentRemoved();
        this.tickLabels.clear();
    }

    @Override
    protected void onComponentEnabled() {
        this.tickLabels.clear();
    }

    @Override
    protected void onComponentThemeChanged() {
        this.tickLabels.clear();
    }

    @Override
    protected void onComponentMouseEnter() {
        this.hovered = true;
    }

    @Override
    protected void onComponentMouseLeave() {
        this.hovered = false;
    }

    @Override
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            this.dragged = true;
            if (this.showTooltip.value()) {
                this.tooltip = new SliderTooltip(this.formatValue(this.value));
                this.tooltip.add(this.rivet());
                this.tooltip.font(this.font);
            }
            this.updateValue(event.x(), size);
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            this.dragged = false;
            if (this.tooltip != null) {
                this.tooltip.remove();
                this.tooltip = null;
            }
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
        if (this.dragged) {
            this.updateValue(event.x(), size);
        }
        return true;
    }

    private void updateValue(final float mouseX, final Size size) {
        float thumbWidth = this.thumbWidth.value();
        float barWidth = this.barWidth(size);
        float progress = (mouseX - thumbWidth / 2F) / barWidth;
        progress = MathUtils.clamp(progress, 0, 1);
        double newValue = this.min + progress * (this.max - this.min);
        newValue = net.lenni0451.rivet.utils.MathUtils.snap(newValue, this.min, this.max, this.step);
        if (this.value != newValue) {
            this.value = newValue;
            if (this.tooltip != null) {
                this.tooltip.text(this.formatValue(this.value));
            }
            this.valueChangeListener.callVoid(c -> c.accept(this.value));
        }
    }

    @Override
    protected void updateComponentPosition(final Rectangle absoluteBounds) {
        if (this.tooltip != null) {
            float thumbWidth = this.thumbWidth.value();
            float barWidth = this.barWidth(absoluteBounds.size());
            double progress = MathUtils.clamp((this.value - this.min) / (this.max - this.min), 0, 1);
            float thumbX = (float) (thumbWidth / 2F + barWidth * progress);

            this.tooltip.position(absoluteBounds.x() + thumbX, absoluteBounds.y(), absoluteBounds.height());
        }
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float thumbWidth = this.thumbWidth.value();
        float thumbHeight = this.thumbHeight.value();
        float barHeight = this.barHeight.value();
        float sliderCenter = this.ticks != null ? thumbHeight / 2F : size.height() / 2F;
        float barWidth = this.barWidth(size);
        double progress = MathUtils.clamp((this.value - this.min) / (this.max - this.min), 0, 1);
        float thumbX = (float) (thumbWidth / 2F + barWidth * progress);

        this.renderBar(renderer, size, sliderCenter, barHeight, thumbWidth, thumbX);
        this.renderThumb(renderer, sliderCenter, thumbWidth, thumbHeight, thumbX);
        if (this.ticks != null) this.renderTicks(renderer, sliderCenter, barHeight, thumbWidth, thumbHeight, barWidth);
    }

    private String formatValue(final double value) {
        if (this.cachedFormatString == null) {
            this.cachedFormatString = FormatUtils.formatDecimalString(this.tooltipFormat.value(), this.step);
        }
        try {
            return String.format(this.cachedFormatString, value);
        } catch (Throwable t) {
            return Double.toString(value);
        }
    }

    private void renderBar(final Renderer renderer, final Size size, final float sliderCenter, final float barHeight, final float thumbWidth, final float thumbX) {
        Color barColor = this.disabled() ? this.disabledBarColor.value() : this.barColor.value();
        Color activeBarColor = this.disabled() ? this.disabledActiveBarColor.value() : this.activeBarColor.value();
        if (this.thumbEncased.value()) {
            renderer.optimizedFillRoundedRect(0, sliderCenter - barHeight / 2F, size.width(), barHeight, this.barCornerRadius.value(), barColor);
            renderer.optimizedFillRoundedRect(0, sliderCenter - barHeight / 2F, thumbX, barHeight, this.barCornerRadius.value(), activeBarColor);
        } else {
            renderer.optimizedFillRoundedRect(thumbWidth / 2F, sliderCenter - barHeight / 2F, size.width() - thumbWidth, barHeight, this.barCornerRadius.value(), barColor);
            renderer.optimizedFillRoundedRect(thumbWidth / 2F, sliderCenter - barHeight / 2F, thumbX - thumbWidth / 2F, barHeight, this.barCornerRadius.value(), activeBarColor);
        }
    }

    private void renderThumb(final Renderer renderer, final float sliderCenter, final float thumbWidth, final float thumbHeight, final float thumbX) {
        Color color = this.thumbColorTransition.value();
        Color outlineColor = this.thumbOutlineColorTransition.value();
        float outlineWidth = this.thumbOutlineWidth.value();
        float cornerRadius = this.thumbCornerRadius.value();

        switch (this.thumbShape.value()) {
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

    private void renderTicks(final Renderer renderer, final float sliderCenter, final float barHeight, final float thumbWidth, final float thumbHeight, final float barWidth) {
        float tickStartY = sliderCenter + thumbHeight / 2F + TICK_OFFSET;
        float majorTickLength = barHeight;
        float minorTickLength = barHeight / 2F;
        Color color = this.disabled() ? this.disabledTickColor.value() : this.tickColor.value();

        if (this.ticks.majorTickSpacing > 0) {
            for (double tick = 0; ; tick += this.ticks.majorTickSpacing) {
                boolean lastTick = false;
                if (tick >= this.max - this.min) {
                    tick = this.max - this.min;
                    lastTick = true;
                }
                float tickX = (float) (thumbWidth / 2F + barWidth * (tick / (this.max - this.min)));
                renderer.fillRect(tickX - 1, tickStartY, TICK_OFFSET, majorTickLength, color);

                double tickValue = this.min + tick;
                ShapedText text = this.tickLabels.computeIfAbsent(tickValue, v -> {
                    Color textColor = this.disabled() ? this.rivet().theme().get(Theme.DISABLED_TEXT_COLOR) : this.rivet().theme().get(Theme.TEXT_COLOR);
                    return this.usedFont().shapeText(this.ticks.labelProvider.getLabel(v), textColor);
                });
                renderer.translate(tickX, tickStartY + majorTickLength + 2, () -> {
                    renderer.scale(0.5F, () -> {
                        renderer.text(text, 0, 0, TextOrigin.Horizontal.VISUAL_CENTER, TextOrigin.Vertical.LOGICAL_TOP);
                    });
                });
                if (lastTick) break;
            }
        }
        if (this.ticks.minorTickSpacing > 0) {
            for (double tick = 0; tick < this.max - this.min; tick += this.ticks.minorTickSpacing) {
                if (this.ticks.majorTickSpacing > 0 && Math.abs(tick % this.ticks.majorTickSpacing) < 1e-6) {
                    continue;
                }
                float tickX = (float) (thumbWidth / 2F + barWidth * (tick / (this.max - this.min)));
                renderer.fillRect(tickX, tickStartY, 1, minorTickLength, color);
            }
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        float height;
        if (this.ticks == null) {
            height = this.thumbHeight.value();
        } else {
            height = this.thumbHeight.value() + TICK_OFFSET + this.barHeight.value() + TICK_OFFSET + this.usedFont().height() / 2F;
        }
        return new Size(this.usedFont().height() * 10, height);
    }

    private float barWidth(final Size size) {
        return size.width() - this.thumbWidth.value();
    }


    public record Ticks(double majorTickSpacing, double minorTickSpacing, TickLabelProvider labelProvider) {
        public Ticks(final double majorTickSpacing, final double minorTickSpacing) {
            this(majorTickSpacing, minorTickSpacing, defaultLabelProvider(majorTickSpacing));
        }

        private static TickLabelProvider defaultLabelProvider(final double majorTickSpacing) {
            String format = FormatUtils.formatDecimalString("%,f", majorTickSpacing);
            return d -> {
                try {
                    return String.format(format, d);
                } catch (Throwable t) {
                    return Double.toString(d);
                }
            };
        }
    }

    @FunctionalInterface
    public interface TickLabelProvider {
        String getLabel(final double value);
    }

    public enum ThumbShape {
        CIRCLE,
        RECTANGLE,
        PIN
    }

    private enum State {
        INACTIVE, HOVERED, DRAGGED, DISABLED
    }

}
