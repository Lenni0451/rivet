package net.lenni0451.rivet.component.impl.slider;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import net.lenni0451.rivet.utils.FormatUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Accessors(fluent = true, chain = true, makeFinal = true)
public abstract class AbstractSlider<S extends AbstractSlider<S>> extends Component {

    private static final int TICK_OFFSET = 2;
    private static final int DEFAULT_STEP_COUNT = 100;

    @Getter
    protected Font font;
    @Getter
    protected double min;
    @Getter
    protected double max;
    @Getter
    protected double step;
    @Getter
    @Nullable
    protected SliderTicks ticks;

    private SliderThumb draggedThumb = null;
    private final List<SliderThumb> thumbs = new ArrayList<>();

    private final Map<Double, ShapedText> tickLabels = new HashMap<>();
    private String cachedFormatString = null;

    @Getter
    private final ThemeOption<Color> barColor = new ThemeOption<>(this, Theme.Slider.BAR_COLOR);
    @Getter
    private final ThemeOption<Color> barFillColor = new ThemeOption<>(this, Theme.Slider.BAR_FILL_COLOR);
    @Getter
    private final ThemeOption<Color> thumbColor = new ThemeOption<>(this, Theme.Slider.THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> thumbClickColor = new ThemeOption<>(this, Theme.Slider.THUMB_CLICK_COLOR);
    @Getter
    private final ThemeOption<Color> tickColor = new ThemeOption<>(this, Theme.Slider.TICK_COLOR);
    @Getter
    private final ThemeOption<Float> barHeight = new ThemeOption<>(this, Theme.Slider.BAR_HEIGHT);
    @Getter
    private final ThemeOption<Float> thumbWidth = new ThemeOption<>(this, Theme.Slider.THUMB_WIDTH);
    @Getter
    private final ThemeOption<Float> thumbHeight = new ThemeOption<>(this, Theme.Slider.THUMB_HEIGHT);
    @Getter
    private final ThemeOption<Corners> barCornerRadius = new ThemeOption<>(this, Theme.Slider.BAR_CORNER_RADIUS);
    @Getter
    private final ThemeOption<Corners> thumbCornerRadius = new ThemeOption<>(this, Theme.Slider.THUMB_CORNER_RADIUS);
    @Getter
    private final ThemeOption<Boolean> thumbEncased = new ThemeOption<>(this, Theme.Slider.THUMB_ENCASED);
    @Getter
    private final ThemeOption<ThumbShape> thumbShape = new ThemeOption<>(this, Theme.Slider.THUMB_SHAPE);
    @Getter
    private final ThemeOption<Color> thumbOutlineColor = new ThemeOption<>(this, Theme.Slider.THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> thumbClickOutlineColor = new ThemeOption<>(this, Theme.Slider.THUMB_CLICK_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Float> thumbOutlineWidth = new ThemeOption<>(this, Theme.Slider.THUMB_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Boolean> showTooltip = new ThemeOption<>(this, Theme.Slider.SHOW_TOOLTIP);
    @Getter
    private final ThemeOption<String> tooltipFormat = new ThemeOption<>(this, Theme.Slider.TOOLTIP_FORMAT);
    @Getter
    private final ThemeOption<Color> disabledBarColor = new ThemeOption<>(this, Theme.Slider.DISABLED_BAR_COLOR);
    @Getter
    private final ThemeOption<Color> disabledBarFillColor = new ThemeOption<>(this, Theme.Slider.DISABLED_BAR_FILL_COLOR);
    @Getter
    private final ThemeOption<Color> disabledThumbColor = new ThemeOption<>(this, Theme.Slider.DISABLED_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> disabledThumbOutlineColor = new ThemeOption<>(this, Theme.Slider.DISABLED_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledTickColor = new ThemeOption<>(this, Theme.Slider.DISABLED_TICK_COLOR);
    @Getter
    private final ThemeOption<Color> thumbHoverColor = new ThemeOption<>(this, Theme.Slider.THUMB_HOVER_COLOR);
    @Getter
    private final ThemeOption<Color> thumbHoverOutlineColor = new ThemeOption<>(this, Theme.Slider.THUMB_HOVER_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<AnimationConfig> hoverAnimationConfig = new ThemeOption<>(this, Theme.Slider.HOVER_ANIMATION);
    @Getter
    private final ThemeOption<AnimationConfig> clickAnimationConfig = new ThemeOption<>(this, Theme.Slider.CLICK_ANIMATION);
    @Getter
    private final ThemeOption<Boolean> ensureValuesReachable = new ThemeOption<>(this, Theme.Slider.ENSURE_VALUES_REACHABLE);
    @Getter
    private final ThemeOption<Boolean> clickableRail = new ThemeOption<>(this, Theme.Slider.CLICKABLE_RAIL);

    public AbstractSlider(final double min, final double max) {
        this(min, max, (max - min) / DEFAULT_STEP_COUNT);
    }

    public AbstractSlider(final double min, final double max, final double step) {
        this.min = min;
        this.max = max;
        this.step = step;

        this.tooltipFormat.initListener().add(f -> this.cachedFormatString = null);
    }

    public final S font(final Font font) {
        if (this.font != font) {
            this.font = font;
            this.tickLabels.clear();
            if (this.parent() != null) {
                this.parent().requestLayoutRecalculation();
            }
        }
        return (S) this;
    }

    public final S min(final double min) {
        this.min = min;
        this.tickLabels.clear();
        return (S) this;
    }

    public final S max(final double max) {
        this.max = max;
        this.tickLabels.clear();
        return (S) this;
    }

    public final S step(final double step) {
        this.step = step;
        this.cachedFormatString = null;
        return (S) this;
    }

    public final S ticks(@Nullable final SliderTicks ticks) {
        this.ticks = ticks;
        this.tickLabels.clear();
        return (S) this;
    }

    protected final float thumbX(final double value, final Size size) {
        return this.thumbX(value, this.effectiveThumbWidth(), this.barWidth(size));
    }

    protected final float thumbX(final double value, final float thumbWidth, final float barWidth) {
        double progress = MathUtils.clamp((value - this.min) / (this.max - this.min), 0, 1);
        return (float) (thumbWidth / 2F + barWidth * progress);
    }

    protected final double valueAtX(final float mouseX, final Size size) {
        return this.valueAtX(mouseX, this.effectiveThumbWidth(), this.barWidth(size));
    }

    protected final double valueAtX(final float mouseX, final float thumbWidth, final float barWidth) {
        double progress = (mouseX - thumbWidth / 2F) / barWidth;
        progress = MathUtils.clamp(progress, 0, 1);
        return this.min + progress * (this.max - this.min);
    }

    protected final float effectiveThumbWidth() {
        if (this.thumbShape.value() == ThumbShape.CIRCLE) {
            return Math.min(this.thumbWidth.value(), this.thumbHeight.value());
        }
        return this.thumbWidth.value();
    }

    protected final float effectiveThumbHeight() {
        if (this.thumbShape.value() == ThumbShape.CIRCLE) {
            return Math.min(this.thumbWidth.value(), this.thumbHeight.value());
        }
        return this.thumbHeight.value();
    }

    protected final Font usedFont() {
        return this.font != null ? this.font : this.rivet().backend().font();
    }

    protected final SliderThumb addThumb(final double value) {
        SliderThumb thumb = new SliderThumb(this, value);
        if (this.rivet() != null) {
            thumb.initTransitions();
        }
        this.thumbs.add(thumb);
        return thumb;
    }

    protected final void removeThumb(final SliderThumb thumb) {
        if (this.draggedThumb == thumb) {
            this.draggedThumb(null);
        }
        if (thumb.tooltip != null) {
            thumb.tooltip.remove();
            thumb.tooltip = null;
        }
        this.thumbs.remove(thumb);
    }

    protected final void clearThumbs() {
        this.draggedThumb(null);
        for (SliderThumb thumb : this.thumbs) {
            if (thumb.tooltip != null) {
                thumb.tooltip.remove();
                thumb.tooltip = null;
            }
        }
        this.thumbs.clear();
    }

    @Nullable
    protected final SliderThumb draggedThumb() {
        return this.draggedThumb;
    }

    protected final void draggedThumb(@Nullable final SliderThumb thumb) {
        if (this.draggedThumb == thumb) return;

        if (this.draggedThumb != null) {
            this.draggedThumb.dragged = false;
            if (this.draggedThumb.tooltip != null) {
                this.draggedThumb.tooltip.remove();
                this.draggedThumb.tooltip = null;
            }
        }
        this.draggedThumb = thumb;
        if (this.draggedThumb != null) {
            this.draggedThumb.dragged = true;
            if (this.showTooltip.value() && this.rivet() != null) {
                this.draggedThumb.tooltip = new SliderTooltip(this.formatValue(this.draggedThumb.value()));
                this.draggedThumb.tooltip.add(this.rivet());
                this.draggedThumb.tooltip.font(this.font);
                this.updatePositionInternal(this.absoluteBounds());
            }
        }
    }

    @Nullable
    protected SliderThumb getClosestThumb(final float mouseX, final Size size) {
        float thumbWidth = this.effectiveThumbWidth();
        float barWidth = this.barWidth(size);

        SliderThumb closest = null;
        double minDistance = Double.MAX_VALUE;
        for (SliderThumb thumb : this.thumbs) {
            float thumbX = this.thumbX(thumb.value(), thumbWidth, barWidth);
            double distance = Math.abs(mouseX - thumbX);
            if (distance < minDistance && (this.clickableRail.value() || distance < thumbWidth / 2)) {
                closest = thumb;
                minDistance = distance;
            }
        }
        return closest;
    }

    protected final float barWidth(final Size size) {
        return size.width() - this.effectiveThumbWidth();
    }

    protected final String formatValue(final double value) {
        if (this.cachedFormatString == null) {
            this.cachedFormatString = FormatUtils.formatDecimalString(this.tooltipFormat.value(), this.step);
        }
        try {
            return String.format(this.cachedFormatString, value);
        } catch (Throwable t) {
            return Double.toString(value);
        }
    }

    protected abstract void onThumbDrag(final SliderThumb thumb, final double newValue);

    @Override
    protected void onAddedInternal() {
        for (SliderThumb thumb : this.thumbs) {
            thumb.initTransitions();
        }
    }

    @Override
    protected void onRemovedInternal() {
        for (SliderThumb thumb : this.thumbs) {
            if (thumb.tooltip != null) {
                thumb.tooltip.remove();
                thumb.tooltip = null;
            }
            thumb.dragged = false;
            thumb.hovered = false;
        }
        this.tickLabels.clear();
        this.draggedThumb = null;
    }

    @Override
    protected void onDisabledInternal() {
        this.onRemovedInternal();
        this.tickLabels.clear();
    }

    @Override
    protected void onEnabledInternal() {
        this.tickLabels.clear();
    }

    @Override
    protected void onThemeChangedInternal() {
        this.tickLabels.clear();
    }

    @Override
    protected void onMouseLeaveInternal() {
        for (SliderThumb thumb : this.thumbs) {
            thumb.hovered = false;
        }
    }

    @Override
    protected boolean onMouseDownInternal(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            SliderThumb thumb = this.getClosestThumb(event.x(), size);
            if (thumb != null) {
                this.draggedThumb(thumb);
                thumb.updateValue(event.x(), size);
            }
        }
        return true;
    }

    @Override
    protected boolean onMouseUpInternal(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            this.draggedThumb(null);
        }
        return true;
    }

    @Override
    protected boolean onMouseMoveInternal(final MouseMoveEvent event, final Size size) {
        if (this.draggedThumb != null) {
            this.draggedThumb.updateValue(event.x(), size);
        } else {
            SliderThumb closest = this.getClosestThumb(event.x(), size);
            for (SliderThumb thumb : this.thumbs) {
                thumb.hovered = (thumb == closest);
            }
        }
        return true;
    }

    @Override
    protected void updatePositionInternal(final Rectangle absoluteBounds) {
        float thumbWidth = this.effectiveThumbWidth();
        float barWidth = this.barWidth(absoluteBounds.size());
        for (SliderThumb thumb : this.thumbs) {
            if (thumb.tooltip != null) {
                float thumbX = this.thumbX(thumb.value(), thumbWidth, barWidth);
                thumb.tooltip.position(absoluteBounds.x() + thumbX, absoluteBounds.y(), absoluteBounds.height());
            }
        }
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size) {
        float thumbWidth = this.effectiveThumbWidth();
        float thumbHeight = this.effectiveThumbHeight();
        float barHeight = this.barHeight.value();
        float sliderCenter = this.ticks != null ? Math.max(thumbHeight, barHeight) / 2F : size.height() / 2F;
        float barWidth = this.barWidth(size);

        this.renderBar(renderer, size, sliderCenter, barHeight, thumbWidth);
        this.renderThumbs(renderer, size, sliderCenter, thumbWidth, thumbHeight, barWidth);
        if (this.ticks != null) {
            this.renderTicks(renderer, sliderCenter, barHeight, thumbWidth, thumbHeight, barWidth);
        }
    }

    protected void renderBar(final Renderer renderer, final Size size, final float sliderCenter, final float barHeight, final float thumbWidth) {
        Color barColor = this.disabled() ? this.disabledBarColor.value() : this.barColor.value();
        if (this.thumbEncased.value()) {
            renderer.optimizedFillRoundedRect(0, sliderCenter - barHeight / 2F, size.width(), barHeight, this.barCornerRadius.value(), barColor);
        } else {
            renderer.optimizedFillRoundedRect(thumbWidth / 2F, sliderCenter - barHeight / 2F, size.width() - thumbWidth, barHeight, this.barCornerRadius.value(), barColor);
        }

        Color barFillColor = this.disabled() ? this.disabledBarFillColor().value() : this.barFillColor().value();
        this.renderFills(renderer, size, this.barWidth(size), barHeight, sliderCenter, thumbWidth, barFillColor);
    }

    protected abstract void renderFills(final Renderer renderer, final Size size, final float barWidth, final float barHeight, final float sliderCenter, final float thumbWidth, final Color barFillColor);

    protected void renderFill(final Renderer renderer, final float startX, final float endX, final float sliderCenter, final float barHeight, final Color color) {
        float x = Math.min(startX, endX);
        float width = Math.abs(endX - startX);
        renderer.optimizedFillRoundedRect(x, sliderCenter - barHeight / 2F, width, barHeight, this.barCornerRadius.value(), color);
    }

    protected void renderThumbs(final Renderer renderer, final Size size, final float sliderCenter, final float thumbWidth, final float thumbHeight, final float barWidth) {
        for (SliderThumb thumb : this.thumbs) {
            float thumbX = this.thumbX(thumb.value(), thumbWidth, barWidth);
            thumb.render(renderer, sliderCenter, thumbWidth, thumbHeight, thumbX);
        }
    }

    protected void renderTicks(final Renderer renderer, final float sliderCenter, final float barHeight, final float thumbWidth, final float thumbHeight, final float barWidth) {
        float tickStartY = sliderCenter + thumbHeight / 2F + TICK_OFFSET;
        float majorTickLength = barHeight;
        float minorTickLength = barHeight / 2F;
        Color color = this.disabled() ? this.disabledTickColor.value() : this.tickColor.value();

        if (this.ticks.majorTickSpacing() > 0) {
            for (double tick = 0; ; tick += this.ticks.majorTickSpacing()) {
                boolean lastTick = false;
                if (tick >= this.max - this.min) {
                    tick = this.max - this.min;
                    lastTick = true;
                }
                float tickX = this.thumbX(this.min + tick, thumbWidth, barWidth);
                renderer.fillRect(tickX - 1, tickStartY, TICK_OFFSET, majorTickLength, color);

                double tickValue = this.min + tick;
                ShapedText text = this.tickLabels.computeIfAbsent(tickValue, v -> {
                    Color textColor = this.disabled() ? this.rivet().theme().get(Theme.General.DISABLED_TEXT_COLOR) : this.rivet().theme().get(Theme.General.TEXT_COLOR);
                    return this.usedFont().shapeText(this.ticks.labelProvider().getLabel(v), textColor);
                });
                renderer.translate(tickX, tickStartY + majorTickLength + 2, () -> {
                    renderer.scale(0.5F, () -> {
                        renderer.text(text, 0, 0, TextOrigin.Horizontal.VISUAL_CENTER, TextOrigin.Vertical.LOGICAL_TOP);
                    });
                });
                if (lastTick) break;
            }
        }
        if (this.ticks.minorTickSpacing() > 0) {
            for (double tick = 0; tick < this.max - this.min; tick += this.ticks.minorTickSpacing()) {
                if (this.ticks.majorTickSpacing() > 0 && Math.abs(tick % this.ticks.majorTickSpacing()) < 1e-6) {
                    continue;
                }
                float tickX = this.thumbX(this.min + tick, thumbWidth, barWidth);
                renderer.fillRect(tickX, tickStartY, 1, minorTickLength, color);
            }
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        float height;
        if (this.ticks == null) {
            height = Math.max(this.effectiveThumbHeight(), this.barHeight.value());
        } else {
            height = Math.max(this.effectiveThumbHeight(), this.barHeight.value()) + TICK_OFFSET + this.barHeight.value() + TICK_OFFSET + this.usedFont().height() / 2F;
        }
        float width;
        if (this.ensureValuesReachable.value()) {
            width = (float) ((this.max - this.min) / this.step) + this.effectiveThumbWidth();
        } else {
            width = 0;
        }
        return new Size(
                Math.max(width, this.usedFont().height() * 10),
                height
        );
    }


    public enum ThumbShape {
        CIRCLE,
        RECTANGLE,
        PIN
    }

    public enum State {
        INACTIVE, HOVERED, DRAGGED, DISABLED
    }

}
