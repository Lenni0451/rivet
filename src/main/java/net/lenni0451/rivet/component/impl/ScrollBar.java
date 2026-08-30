package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static net.lenni0451.rivet.utils.MathUtils.EPSILON;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class ScrollBar extends Component {

    @Getter
    @Setter
    private Orientation orientation;
    @Getter
    private float scroll;
    @Getter
    private float contentSize;
    @Getter
    private float visibleSize;

    @Getter
    private final ListenerList<Consumer<Float>> scrollListener = new ListenerList<>();

    @Getter
    private final ThemeOption<Color> barColor = new ThemeOption<>(this, Theme.ScrollBar.BAR_COLOR);
    @Getter
    private final ThemeOption<Color> barHoverColor = new ThemeOption<>(this, Theme.ScrollBar.BAR_HOVER_COLOR);
    @Getter
    private final ThemeOption<Color> barClickColor = new ThemeOption<>(this, Theme.ScrollBar.BAR_CLICK_COLOR);
    @Getter
    private final ThemeOption<Float> barWidth = new ThemeOption<>(this, Theme.ScrollBar.BAR_WIDTH);
    @Getter
    private final ThemeOption<Corners> barCornerRadius = new ThemeOption<>(this, Theme.ScrollBar.BAR_CORNER_RADIUS);
    @Getter
    private final ThemeOption<Float> barOutlineWidth = new ThemeOption<>(this, Theme.ScrollBar.BAR_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Color> barOutlineColor = new ThemeOption<>(this, Theme.ScrollBar.BAR_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<ScrollBarType> barType = new ThemeOption<>(this, Theme.ScrollBar.BAR_TYPE);
    @Getter
    private final ThemeOption<Boolean> railClickJump = new ThemeOption<>(this, Theme.ScrollBar.RAIL_CLICK_JUMP);
    @Getter
    private final ThemeOption<Color> railColor = new ThemeOption<>(this, Theme.ScrollBar.RAIL_COLOR);
    @Getter
    private final ThemeOption<Color> railOutlineColor = new ThemeOption<>(this, Theme.ScrollBar.RAIL_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Float> railOutlineWidth = new ThemeOption<>(this, Theme.ScrollBar.RAIL_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Color> disabledBarColor = new ThemeOption<>(this, Theme.ScrollBar.BAR_DISABLED_COLOR);
    @Getter
    private final ThemeOption<Color> disabledBarOutlineColor = new ThemeOption<>(this, Theme.ScrollBar.BAR_DISABLED_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledRailColor = new ThemeOption<>(this, Theme.ScrollBar.RAIL_DISABLED_COLOR);
    @Getter
    private final ThemeOption<Color> disabledRailOutlineColor = new ThemeOption<>(this, Theme.ScrollBar.RAIL_DISABLED_OUTLINE_COLOR);

    @Getter
    private boolean barHovered;
    @Getter
    private boolean barPressed;
    @Getter
    private boolean railHovered;
    @Getter
    private boolean railPressed;
    private float dragStartPos;
    private float initialScroll;

    public ScrollBar(final Orientation orientation) {
        this.orientation = orientation;
    }

    public final ScrollBar scroll(final float scroll) {
        return this.scroll(scroll, true);
    }

    public final ScrollBar scroll(final float scroll, final boolean fireListeners) {
        float newScroll = MathUtils.clamp(scroll, 0, this.maxScroll());
        if (this.scroll != newScroll) {
            this.scroll = newScroll;
            if (fireListeners) {
                this.scrollListener.call(c -> c.accept(this.scroll));
            }
        }
        return this;
    }

    public final ScrollBar contentSize(final float contentSize) {
        this.contentSize = contentSize;
        this.scroll(this.scroll, false);
        return this;
    }

    public final ScrollBar visibleSize(final float visibleSize) {
        this.visibleSize = visibleSize;
        this.scroll(this.scroll, false);
        return this;
    }

    public final float maxScroll() {
        float max = this.contentSize - this.visibleSize;
        return max <= EPSILON ? 0 : max;
    }

    @Override
    protected void onRemovedInternal() {
        super.onRemovedInternal();
        this.barHovered = false;
        this.barPressed = false;
        this.railHovered = false;
        this.railPressed = false;
    }

    @Override
    protected void onDisabledInternal() {
        super.onDisabledInternal();
        this.barHovered = false;
        this.barPressed = false;
        this.railHovered = false;
        this.railPressed = false;
    }

    @Override
    protected void onMouseLeaveInternal() {
        super.onMouseLeaveInternal();
        this.barHovered = false;
        this.railHovered = false;
    }

    @Override
    protected boolean onMouseDownInternal(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            Rectangle thumb = this.getThumbBounds(size);
            Rectangle rail = this.getRailBounds(size);
            if (thumb != null && thumb.contains(event.x(), event.y())) {
                this.barPressed = true;
                this.dragStartPos = this.orientation == Orientation.HORIZONTAL ? event.x() : event.y();
                this.initialScroll = this.scroll;
                this.rivet().focusedComponent(this);
                return true;
            } else if (rail != null && rail.contains(event.x(), event.y())) {
                this.railPressed = true;
                float maxScroll = this.maxScroll();
                float visible = this.visibleSize;
                if (this.railClickJump.value()) {
                    float thumbSize = this.orientation == Orientation.HORIZONTAL ? thumb.width() : thumb.height();
                    float railSize = this.orientation == Orientation.HORIZONTAL ? rail.width() : rail.height();
                    float scrollableSize = railSize - thumbSize;
                    float clickPos = (this.orientation == Orientation.HORIZONTAL ? event.x() - rail.x() : event.y() - rail.y()) - thumbSize / 2F;
                    float target = scrollableSize > 0 ? MathUtils.clamp((clickPos / scrollableSize) * maxScroll, 0, maxScroll) : 0;
                    this.scroll(target, true);
                } else {
                    float thumbPos = this.orientation == Orientation.HORIZONTAL ? thumb.x() : thumb.y();
                    float clickPos = this.orientation == Orientation.HORIZONTAL ? event.x() : event.y();
                    if (clickPos < thumbPos) this.scroll(this.scroll - visible, true);
                    else this.scroll(this.scroll + visible, true);
                }
                this.rivet().focusedComponent(this);
                return true;
            }
        }
        return super.onMouseDownInternal(event, size);
    }

    @Override
    protected boolean onMouseUpInternal(final MouseButtonEvent event, final Size size) {
        Rectangle thumb = this.getThumbBounds(size);
        Rectangle rail = this.getRailBounds(size);
        boolean thumbHovered = thumb != null && thumb.contains(event.x(), event.y());
        boolean railHovered = rail != null && rail.contains(event.x(), event.y());
        boolean wasPressed = this.barPressed || this.railPressed;
        if (event.button().equals(MouseButton.LEFT)) {
            this.barPressed = false;
            this.railPressed = false;
        }
        if (thumbHovered || railHovered || wasPressed) {
            return true;
        }
        return super.onMouseUpInternal(event, size);
    }

    @Override
    protected boolean onMouseMoveInternal(final MouseMoveEvent event, final Size size) {
        Rectangle thumb = this.getThumbBounds(size);
        Rectangle rail = this.getRailBounds(size);

        this.barHovered = thumb != null && thumb.contains(event.x(), event.y());
        this.railHovered = rail != null && rail.contains(event.x(), event.y());

        if (this.barPressed && thumb != null && rail != null) {
            float maxScroll = this.maxScroll();
            float thumbSize = this.orientation == Orientation.HORIZONTAL ? thumb.width() : thumb.height();
            float railSize = this.orientation == Orientation.HORIZONTAL ? rail.width() : rail.height();
            float scrollableSize = railSize - thumbSize;
            float currentPos = this.orientation == Orientation.HORIZONTAL ? event.x() : event.y();
            float dragDelta = currentPos - this.dragStartPos;
            float target = scrollableSize > 0 ? MathUtils.clamp(this.initialScroll + (dragDelta / scrollableSize) * maxScroll, 0, maxScroll) : 0;
            this.scroll(target, true);
            return true;
        }
        return super.onMouseMoveInternal(event, size);
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        Rectangle rail = this.getRailBounds(size);
        if (rail != null) {
            if (this.barType.value() == ScrollBarType.NORMAL) {
                this.renderRail(renderer, rail);
            }

            Rectangle thumbBounds = this.getThumbBounds(size);
            if (thumbBounds != null) {
                this.renderThumb(renderer, thumbBounds);
            }
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return switch (this.orientation) {
            case HORIZONTAL -> new Size(0, this.barWidth.value());
            case VERTICAL -> new Size(this.barWidth.value(), 0);
        };
    }

    private void renderRail(final Renderer renderer, final Rectangle bounds) {
        Color color = this.disabled() ? this.disabledRailColor.value() : this.railColor.value();
        renderer.fillRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), color);
        if (this.railOutlineWidth.value() > 0) {
            Color outlineColor = this.disabled() ? this.disabledRailOutlineColor.value() : this.railOutlineColor.value();
            renderer.outlineRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), this.railOutlineWidth.value(), outlineColor);
        }
    }

    private void renderThumb(final Renderer renderer, final Rectangle bounds) {
        Color color;
        Color outlineColor;
        if (this.disabled()) {
            color = this.disabledBarColor.value();
            outlineColor = this.disabledBarOutlineColor.value();
        } else {
            if (this.barPressed) color = this.barClickColor.value();
            else if (this.barHovered) color = this.barHoverColor.value();
            else color = this.barColor.value();
            outlineColor = this.barOutlineColor.value();
        }

        renderer.optimizedFillRoundedRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), this.barCornerRadius.value(), color);
        if (this.barOutlineWidth.value() > 0) {
            renderer.optimizedOutlineRoundedRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), this.barCornerRadius.value(), this.barOutlineWidth.value(), outlineColor);
        }
    }

    public Rectangle getRailBounds(final Size size) {
        return new Rectangle(0, 0, size.width(), size.height());
    }

    @Nullable
    public Rectangle getThumbBounds(final Size size) {
        Rectangle rail = this.getRailBounds(size);
        if (rail == null || rail.width() <= 0 || rail.height() <= 0) return null;
        float maxScroll = this.maxScroll();
        if (this.orientation == Orientation.HORIZONTAL) {
            float railWidth = rail.width();
            float thumbWidth = this.contentSize <= 0 ? railWidth : Math.max(20, (this.visibleSize / this.contentSize) * railWidth);
            float scrollPercentage = maxScroll <= 0 ? 0 : MathUtils.clamp(this.scroll / maxScroll, 0, 1);
            float thumbX = rail.x() + scrollPercentage * (railWidth - thumbWidth);
            return new Rectangle(thumbX, rail.y(), thumbWidth, rail.height());
        } else {
            float railHeight = rail.height();
            float thumbHeight = this.contentSize <= 0 ? railHeight : Math.max(20, (this.visibleSize / this.contentSize) * railHeight);
            float scrollPercentage = maxScroll <= 0 ? 0 : MathUtils.clamp(this.scroll / maxScroll, 0, 1);
            float thumbY = rail.y() + scrollPercentage * (railHeight - thumbHeight);
            return new Rectangle(rail.x(), thumbY, rail.width(), thumbHeight);
        }
    }


    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    public enum ScrollBarType {
        FLOATING, NORMAL
    }

}
