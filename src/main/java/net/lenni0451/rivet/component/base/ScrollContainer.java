package net.lenni0451.rivet.component.base;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.animation.DynamicAnimation;
import net.lenni0451.commons.animation.easing.EasingFunction;
import net.lenni0451.commons.animation.easing.EasingMode;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Container;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.input.keyboard.KeyboardListener;
import net.lenni0451.rivet.input.mouse.*;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

@Accessors(fluent = true, chain = true)
public class ScrollContainer extends Component implements MouseListener, KeyboardListener, Renderable {

    private final Component child;
    @Getter
    private final boolean horizontalScrolling;
    @Getter
    private final boolean verticalScrolling;

    @Getter
    private final ThemeOption<Color> barColor;
    @Getter
    private final ThemeOption<Color> barHoverColor;
    @Getter
    private final ThemeOption<Color> barClickColor;
    @Getter
    private final ThemeOption<Float> barWidth;
    @Getter
    private final ThemeOption<Float> barCornerRadius;
    @Getter
    private final ThemeOption<Float> barOutlineWidth;
    @Getter
    private final ThemeOption<Color> barOutlineColor;
    @Getter
    private final ThemeOption<Float> scrollSpeed;
    @Getter
    private final ThemeOption<Boolean> smoothScrolling;
    @Getter
    private final ThemeOption<Integer> animationDuration;
    @Getter
    private final ThemeOption<Long> nestedScrollTimeout;

    private Size childSize = Size.EMPTY;

    private final NestedScrollCoordinator nestedScrollCoordinator = new NestedScrollCoordinator();
    private float scrollX;
    private float scrollY;
    private float targetScrollX;
    private float targetScrollY;
    private final DynamicAnimation scrollXAnimation;
    private final DynamicAnimation scrollYAnimation;

    private boolean vBarHovered;
    private boolean vBarPressed;
    private boolean hBarHovered;
    private boolean hBarPressed;
    private float dragStartX;
    private float dragStartY;
    private float initialScrollX;
    private float initialScrollY;
    private boolean childHovered;

    public ScrollContainer(final Rivet rivet, final Component child) {
        this(rivet, child, false, true);
    }

    public ScrollContainer(final Rivet rivet, final Component child, final boolean horizontalScrolling, final boolean verticalScrolling) {
        super(rivet);
        this.child = child;
        this.horizontalScrolling = horizontalScrolling;
        this.verticalScrolling = verticalScrolling;

        this.barColor = new ThemeOption<>(rivet, Theme.SCROLL_BAR_COLOR);
        this.barHoverColor = new ThemeOption<>(rivet, Theme.SCROLL_BAR_HOVER_COLOR);
        this.barClickColor = new ThemeOption<>(rivet, Theme.SCROLL_BAR_CLICK_COLOR);
        this.barWidth = new ThemeOption<>(rivet, Theme.SCROLL_BAR_WIDTH);
        this.barCornerRadius = new ThemeOption<>(rivet, Theme.SCROLL_BAR_CORNER_RADIUS);
        this.barOutlineWidth = new ThemeOption<>(rivet, Theme.SCROLL_BAR_OUTLINE_WIDTH);
        this.barOutlineColor = new ThemeOption<>(rivet, Theme.SCROLL_BAR_OUTLINE_COLOR);
        this.scrollSpeed = new ThemeOption<>(rivet, Theme.SCROLL_SPEED);
        this.smoothScrolling = new ThemeOption<>(rivet, Theme.SCROLL_SMOOTH);
        this.animationDuration = new ThemeOption<>(rivet, Theme.SCROLL_ANIMATION_DURATION);
        this.nestedScrollTimeout = new ThemeOption<>(rivet, Theme.SCROLL_NESTED_SCROLL_TIMEOUT);

        this.scrollXAnimation = new DynamicAnimation(EasingFunction.SINE, EasingMode.EASE_OUT, (long) this.animationDuration.value(), 0);
        this.scrollYAnimation = new DynamicAnimation(EasingFunction.SINE, EasingMode.EASE_OUT, (long) this.animationDuration.value(), 0);
    }

    @Override
    public void onMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
        if (event.button().equals(MouseButton.LEFT)) {
            Rectangle vBar = this.getVBarBounds(bounds);
            Rectangle hBar = this.getHBarBounds(bounds);
            if (vBar != null && vBar.contains(event.x(), event.y())) {
                this.vBarPressed = true;
                this.dragStartY = event.y();
                this.initialScrollY = this.targetScrollY;
                return;
            } else if (hBar != null && hBar.contains(event.x(), event.y())) {
                this.hBarPressed = true;
                this.dragStartX = event.x();
                this.initialScrollX = this.targetScrollX;
                return;
            }
        }
        this.rivet.focusedComponent(this.child);
        if (this.child instanceof MouseListener mouseListener) {
            mouseListener.onMouseDown(event.withX(event.x() + this.scrollX).withY(event.y() + this.scrollY), new Rectangle(bounds.x() - this.scrollX, bounds.y() - this.scrollY, this.childSize));
        }
    }

    @Override
    public void onMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
        if (event.button().equals(MouseButton.LEFT) && (this.vBarPressed || this.hBarPressed)) {
            this.vBarPressed = false;
            this.hBarPressed = false;
        } else {
            if (this.child instanceof MouseListener mouseListener) {
                mouseListener.onMouseUp(event.withX(event.x() + this.scrollX).withY(event.y() + this.scrollY), new Rectangle(bounds.x() - this.scrollX, bounds.y() - this.scrollY, this.childSize));
            }
        }
    }

    @Override
    public void onMouseMove(final MouseMoveEvent event, final Rectangle bounds) {
        Rectangle vBar = this.getVBarBounds(bounds);
        Rectangle hBar = this.getHBarBounds(bounds);
        this.vBarHovered = vBar != null && vBar.contains(event.x(), event.y());
        this.hBarHovered = hBar != null && hBar.contains(event.x(), event.y());
        if (this.vBarPressed && vBar != null) {
            float contentHeight = this.childSize.height();
            float maxScroll = contentHeight - bounds.height();
            float barHeight = vBar.height();
            float scrollableHeight = bounds.height() - barHeight;
            float dragDelta = event.y() - this.dragStartY;
            this.targetScrollY = MathUtils.clamp(this.initialScrollY + (dragDelta / scrollableHeight) * maxScroll, 0, maxScroll);
        } else if (this.hBarPressed && hBar != null) {
            float contentWidth = this.childSize.width();
            float maxScroll = contentWidth - bounds.width();
            float barWidth = hBar.width();
            float scrollableWidth = bounds.width() - barWidth;
            float dragDelta = event.x() - this.dragStartX;
            this.targetScrollX = MathUtils.clamp(this.initialScrollX + (dragDelta / scrollableWidth) * maxScroll, 0, maxScroll);
        } else {
            if (this.child instanceof MouseListener mouseListener) {
                boolean contains = new Rectangle(0, 0, bounds.width(), bounds.height()).contains(event.x(), event.y());
                if (contains && !this.childHovered) {
                    this.childHovered = true;
                    mouseListener.onMouseEnter();
                } else if (!contains && this.childHovered) {
                    this.childHovered = false;
                    mouseListener.onMouseLeave();
                }
                mouseListener.onMouseMove(event.withX(event.x() + this.scrollX).withY(event.y() + this.scrollY), new Rectangle(bounds.x() - this.scrollX, bounds.y() - this.scrollY, this.childSize));
            }
        }
    }

    @Override
    public boolean onMouseScroll(final MouseScrollEvent event, final Rectangle bounds) {
        return this.nestedScrollCoordinator.handleScrolling(
                () -> {
                    if (this.verticalScrolling && event.scrollY() != 0 && !this.vBarPressed) {
                        float contentHeight = this.childSize.height();
                        float maxScroll = Math.max(0, contentHeight - bounds.height());
                        if (maxScroll > 0) {
                            this.targetScrollY = MathUtils.clamp(this.targetScrollY - event.scrollY() * this.scrollSpeed.value(), 0, maxScroll);
                            return true;
                        }
                    }
                    if (this.horizontalScrolling && event.scrollX() != 0 && !this.hBarPressed) {
                        float contentWidth = this.childSize.width();
                        float maxScroll = Math.max(0, contentWidth - bounds.width());
                        if (maxScroll > 0) {
                            this.targetScrollX = MathUtils.clamp(this.targetScrollX - event.scrollX() * this.scrollSpeed.value(), 0, maxScroll);
                            return true;
                        }
                    }
                    return false;
                },
                () -> {
                    if (this.child instanceof MouseListener mouseListener) {
                        return mouseListener.onMouseScroll(event.withX(event.x() + this.scrollX).withY(event.y() + this.scrollY), new Rectangle(bounds.x() - this.scrollX, bounds.y() - this.scrollY, this.childSize));
                    }
                    return false;
                }
        );
    }

    @Override
    public void render(final Renderer renderer, final Rectangle bounds) {
        this.updateAnimation();

        renderer.componentBounds(0, 0, bounds.width(), bounds.height(), () -> {
            renderer.translate(-this.scrollX, -this.scrollY, () -> {
                if (this.child instanceof Renderable renderable) {
                    renderable.render(renderer, new Rectangle(bounds.x() - this.scrollX, bounds.y() - this.scrollY, this.childSize));
                }
            });
        });

        this.renderBar(renderer, this.getVBarBounds(bounds), this.vBarHovered, this.vBarPressed);
        this.renderBar(renderer, this.getHBarBounds(bounds), this.hBarHovered, this.hBarPressed);
    }

    private void updateAnimation() {
        if (this.smoothScrolling.value()) {
            this.scrollXAnimation.setTarget(this.targetScrollX);
            this.scrollYAnimation.setTarget(this.targetScrollY);
        } else {
            this.scrollXAnimation.setTarget(this.targetScrollX).finish();
            this.scrollYAnimation.setTarget(this.targetScrollY).finish();
        }
        this.scrollX = this.scrollXAnimation.getValue();
        this.scrollY = this.scrollYAnimation.getValue();
    }

    private void renderBar(final Renderer renderer, final Rectangle bounds, final boolean hovered, final boolean pressed) {
        if (bounds == null) return;

        Color color = this.barColor.value();
        if (pressed) color = this.barClickColor.value();
        else if (hovered) color = this.barHoverColor.value();

        renderer.fillOptimizedRoundedRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), this.barCornerRadius.value(), color);
        if (this.barOutlineWidth.value() > 0) {
            renderer.outlineOptimizedRoundedRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), this.barCornerRadius.value(), this.barOutlineWidth.value(), this.barOutlineColor.value());
        }
    }

    @Nullable
    private Rectangle getVBarBounds(final Rectangle bounds) {
        if (!this.verticalScrolling) return null;
        float contentHeight = this.childSize.height();
        if (contentHeight <= bounds.height()) return null;

        float barWidth = this.barWidth.value();
        float barHeight = Math.max(20, (bounds.height() / contentHeight) * bounds.height());
        float maxScroll = contentHeight - bounds.height();
        float scrollPercentage = this.scrollY / maxScroll;
        float barY = scrollPercentage * (bounds.height() - barHeight);

        return new Rectangle(bounds.width() - barWidth, barY, barWidth, barHeight);
    }

    @Nullable
    private Rectangle getHBarBounds(final Rectangle bounds) {
        if (!this.horizontalScrolling) return null;
        float contentWidth = this.childSize.width();
        if (contentWidth <= bounds.width()) return null;

        float barWidth = Math.max(20, (bounds.width() / contentWidth) * bounds.width());
        float barHeight = this.barWidth.value();
        float maxScroll = contentWidth - bounds.width();
        float scrollPercentage = this.scrollX / maxScroll;
        float barX = scrollPercentage * (bounds.width() - barWidth);

        return new Rectangle(barX, bounds.height() - barHeight, barWidth, barHeight);
    }

    @Override
    public void computeIdealSize(final Size constraints) {
        this.child.computeIdealSize(new Size(
                this.horizontalScrolling ? Float.MAX_VALUE : constraints.width(),
                this.verticalScrolling ? Float.MAX_VALUE : constraints.height()
        ));
        this.idealSize = this.child.idealSize();
    }

    @Override
    public void computeLayout(final Size size) {
        Size childSize = new Size(
                MathUtils.clamp(size.width(), this.child.minSize().width(), this.child.maxSize().width()),
                MathUtils.clamp(size.height(), this.child.minSize().height(), this.child.maxSize().height())
        );
        this.child.computeLayout(childSize);
        if (this.child instanceof Container container) {
            childSize = container.contentSize();
        }
        this.childSize = childSize;

        float contentHeight = childSize.height();
        float maxScrollY = Math.max(0, contentHeight - size.height());
        this.targetScrollY = MathUtils.clamp(this.targetScrollY, 0, maxScrollY);
        this.scrollY = MathUtils.clamp(this.scrollY, 0, maxScrollY);

        float contentWidth = childSize.width();
        float maxScrollX = Math.max(0, contentWidth - size.width());
        this.targetScrollX = MathUtils.clamp(this.targetScrollX, 0, maxScrollX);
        this.scrollX = MathUtils.clamp(this.scrollX, 0, maxScrollX);
    }


    private class NestedScrollCoordinator {
        @Nullable
        private ScrollTarget lastTarget;
        private long lastScroll;

        public boolean handleScrolling(final BooleanSupplier parent, final BooleanSupplier child) {
            if (System.nanoTime() - this.lastScroll > TimeUnit.MILLISECONDS.toNanos(ScrollContainer.this.nestedScrollTimeout.value())) {
                this.lastTarget = null;
            }
            if (this.lastTarget == null || this.lastTarget.equals(ScrollTarget.CHILD)) {
                if (child.getAsBoolean()) {
                    this.lastTarget = ScrollTarget.CHILD;
                    this.lastScroll = System.nanoTime();
                    return true;
                }
            }
            if (this.lastTarget == null || this.lastTarget.equals(ScrollTarget.PARENT)) {
                if (parent.getAsBoolean()) {
                    this.lastTarget = ScrollTarget.PARENT;
                    this.lastScroll = System.nanoTime();
                    return true;
                }
            }
            return false;
        }


        private enum ScrollTarget {
            PARENT, CHILD
        }
    }

}
