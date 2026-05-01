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
    @Getter
    private final ThemeOption<ScrollBarType> barType;
    @Getter
    private final ThemeOption<Boolean> railClickJump;
    @Getter
    private final ThemeOption<Color> railColor;
    @Getter
    private final ThemeOption<Color> railOutlineColor;
    @Getter
    private final ThemeOption<Float> railOutlineWidth;

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
    private boolean vRailHovered;
    private boolean vRailPressed;
    private boolean hBarHovered;
    private boolean hBarPressed;
    private boolean hRailHovered;
    private boolean hRailPressed;
    private float dragStartX;
    private float dragStartY;
    private float initialScrollX;
    private float initialScrollY;
    private boolean childHovered;
    private boolean vScrollVisible;
    private boolean hScrollVisible;

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
        this.barType = new ThemeOption<>(rivet, Theme.SCROLL_BAR_TYPE);
        this.railClickJump = new ThemeOption<>(rivet, Theme.SCROLL_RAIL_CLICK_JUMP);
        this.railColor = new ThemeOption<>(rivet, Theme.SCROLL_RAIL_COLOR);
        this.railOutlineColor = new ThemeOption<>(rivet, Theme.SCROLL_RAIL_OUTLINE_COLOR);
        this.railOutlineWidth = new ThemeOption<>(rivet, Theme.SCROLL_RAIL_OUTLINE_WIDTH);

        this.scrollXAnimation = new DynamicAnimation(EasingFunction.SINE, EasingMode.EASE_OUT, (long) this.animationDuration.value(), 0);
        this.scrollYAnimation = new DynamicAnimation(EasingFunction.SINE, EasingMode.EASE_OUT, (long) this.animationDuration.value(), 0);
    }

    @Override
    public void onMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
        if (event.button().equals(MouseButton.LEFT)) {
            Rectangle vThumb = this.getVThumbBounds(bounds);
            Rectangle vRail = this.getVRailBounds(bounds);
            Rectangle hThumb = this.getHThumbBounds(bounds);
            Rectangle hRail = this.getHRailBounds(bounds);

            if (vThumb != null && vThumb.contains(event.x(), event.y())) {
                this.vBarPressed = true;
                this.dragStartY = event.y();
                this.initialScrollY = this.targetScrollY;
                return;
            } else if (vRail != null && vRail.contains(event.x(), event.y())) {
                this.vRailPressed = true;
                float visibleHeight = this.visibleHeight(bounds);
                float maxScroll = Math.max(0, this.childSize.height() - visibleHeight);
                if (this.railClickJump.value()) {
                    float thumbHeight = vThumb.height();
                    float scrollableHeight = vRail.height() - thumbHeight;
                    float clickY = event.y() - vRail.y() - thumbHeight / 2F;
                    this.targetScrollY = MathUtils.clamp((clickY / scrollableHeight) * maxScroll, 0, maxScroll);
                } else {
                    if (event.y() < vThumb.y()) this.targetScrollY = MathUtils.clamp(this.targetScrollY - visibleHeight, 0, maxScroll);
                    else this.targetScrollY = MathUtils.clamp(this.targetScrollY + visibleHeight, 0, maxScroll);
                }
                return;
            } else if (hThumb != null && hThumb.contains(event.x(), event.y())) {
                this.hBarPressed = true;
                this.dragStartX = event.x();
                this.initialScrollX = this.targetScrollX;
                return;
            } else if (hRail != null && hRail.contains(event.x(), event.y())) {
                this.hRailPressed = true;
                float visibleWidth = this.visibleWidth(bounds);
                float maxScroll = Math.max(0, this.childSize.width() - visibleWidth);
                if (this.railClickJump.value()) {
                    float thumbWidth = hThumb.width();
                    float scrollableWidth = hRail.width() - thumbWidth;
                    float clickX = event.x() - hRail.x() - thumbWidth / 2F;
                    this.targetScrollX = MathUtils.clamp((clickX / scrollableWidth) * maxScroll, 0, maxScroll);
                } else {
                    if (event.x() < hThumb.x()) this.targetScrollX = MathUtils.clamp(this.targetScrollX - visibleWidth, 0, maxScroll);
                    else this.targetScrollX = MathUtils.clamp(this.targetScrollX + visibleWidth, 0, maxScroll);
                }
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
        if (event.button().equals(MouseButton.LEFT)) {
            this.vBarPressed = false;
            this.vRailPressed = false;
            this.hBarPressed = false;
            this.hRailPressed = false;
        }
        if (this.child instanceof MouseListener mouseListener) {
            mouseListener.onMouseUp(event.withX(event.x() + this.scrollX).withY(event.y() + this.scrollY), new Rectangle(bounds.x() - this.scrollX, bounds.y() - this.scrollY, this.childSize));
        }
    }

    @Override
    public void onMouseMove(final MouseMoveEvent event, final Rectangle bounds) {
        Rectangle vThumb = this.getVThumbBounds(bounds);
        Rectangle vRail = this.getVRailBounds(bounds);
        Rectangle hThumb = this.getHThumbBounds(bounds);
        Rectangle hRail = this.getHRailBounds(bounds);

        this.vBarHovered = vThumb != null && vThumb.contains(event.x(), event.y());
        this.vRailHovered = vRail != null && vRail.contains(event.x(), event.y());
        this.hBarHovered = hThumb != null && hThumb.contains(event.x(), event.y());
        this.hRailHovered = hRail != null && hRail.contains(event.x(), event.y());

        if (this.vBarPressed && vThumb != null) {
            float contentHeight = this.childSize.height();
            float visibleHeight = this.visibleHeight(bounds);
            float maxScroll = Math.max(0, contentHeight - visibleHeight);
            Rectangle rail = this.getVRailBounds(bounds);
            float barHeight = vThumb.height();
            float scrollableHeight = rail.height() - barHeight;
            float dragDelta = event.y() - this.dragStartY;
            this.targetScrollY = MathUtils.clamp(this.initialScrollY + (dragDelta / scrollableHeight) * maxScroll, 0, maxScroll);
        } else if (this.hBarPressed && hThumb != null) {
            float contentWidth = this.childSize.width();
            float visibleWidth = this.visibleWidth(bounds);
            float maxScroll = Math.max(0, contentWidth - visibleWidth);
            Rectangle rail = this.getHRailBounds(bounds);
            float barWidth = hThumb.width();
            float scrollableWidth = rail.width() - barWidth;
            float dragDelta = event.x() - this.dragStartX;
            this.targetScrollX = MathUtils.clamp(this.initialScrollX + (dragDelta / scrollableWidth) * maxScroll, 0, maxScroll);
        } else {
            if (this.child instanceof MouseListener mouseListener) {
                float childWidth = this.visibleWidth(bounds);
                float childHeight = this.visibleHeight(bounds);
                boolean contains = new Rectangle(0, 0, childWidth, childHeight).contains(event.x(), event.y());
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
                    if (this.vScrollVisible && event.scrollY() != 0 && !this.vBarPressed) {
                        float contentHeight = this.childSize.height();
                        float visibleHeight = this.visibleHeight(bounds);
                        float maxScroll = Math.max(0, contentHeight - visibleHeight);
                        if (maxScroll > 0) {
                            this.targetScrollY = MathUtils.clamp(this.targetScrollY - event.scrollY() * this.scrollSpeed.value(), 0, maxScroll);
                            return true;
                        }
                    }
                    if (this.hScrollVisible && event.scrollX() != 0 && !this.hBarPressed) {
                        float contentWidth = this.childSize.width();
                        float visibleWidth = this.visibleWidth(bounds);
                        float maxScroll = Math.max(0, contentWidth - visibleWidth);
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

        float childWidth = this.visibleWidth(bounds);
        float childHeight = this.visibleHeight(bounds);

        renderer.componentBounds(0, 0, childWidth, childHeight, () -> {
            renderer.translate(-this.scrollX, -this.scrollY, () -> {
                if (this.child instanceof Renderable renderable) {
                    renderable.render(renderer, new Rectangle(bounds.x() - this.scrollX, bounds.y() - this.scrollY, this.childSize));
                }
            });
        });

        this.renderVerticalScrollbar(renderer, bounds);
        this.renderHorizontalScrollbar(renderer, bounds);
        this.renderCorner(renderer, bounds);
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

    private void renderVerticalScrollbar(final Renderer renderer, final Rectangle bounds) {
        Rectangle rail = this.getVRailBounds(bounds);
        if (rail == null) return;
        if (this.barType.value() == ScrollBarType.NORMAL) {
            this.renderRail(renderer, rail, this.vRailHovered, this.vRailPressed);
        }
        this.renderThumb(renderer, this.getVThumbBounds(bounds), this.vBarHovered, this.vBarPressed);
    }

    private void renderHorizontalScrollbar(final Renderer renderer, final Rectangle bounds) {
        Rectangle rail = this.getHRailBounds(bounds);
        if (rail == null) return;
        if (this.barType.value() == ScrollBarType.NORMAL) {
            this.renderRail(renderer, rail, this.hRailHovered, this.hRailPressed);
        }
        this.renderThumb(renderer, this.getHThumbBounds(bounds), this.hBarHovered, this.hBarPressed);
    }

    private void renderCorner(final Renderer renderer, final Rectangle bounds) {
        if (this.barType.value() == ScrollBarType.NORMAL && this.vScrollVisible && this.hScrollVisible) {
            float barWidth = this.barWidth.value();
            renderer.fillRect(bounds.width() - barWidth, bounds.height() - barWidth, barWidth, barWidth, this.railColor.value());
        }
    }

    private void renderRail(final Renderer renderer, final Rectangle bounds, final boolean hovered, final boolean pressed) {
        renderer.fillRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), this.railColor.value());
        if (this.railOutlineWidth.value() > 0) {
            renderer.outlineRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), this.railOutlineWidth.value(), this.railOutlineColor.value());
        }
    }

    private void renderThumb(final Renderer renderer, final Rectangle bounds, final boolean hovered, final boolean pressed) {
        if (bounds == null) return;

        Color color = this.barColor.value();
        if (pressed) color = this.barClickColor.value();
        else if (hovered) color = this.barHoverColor.value();

        renderer.fillOptimizedRoundedRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), this.barCornerRadius.value(), color);
        if (this.barOutlineWidth.value() > 0) {
            renderer.outlineOptimizedRoundedRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), this.barCornerRadius.value(), this.barOutlineWidth.value(), this.barOutlineColor.value());
        }
    }

    private float visibleWidth(final Rectangle bounds) {
        return bounds.width() - (this.vScrollVisible && this.barType.value() == ScrollBarType.NORMAL ? this.barWidth.value() : 0);
    }

    private float visibleHeight(final Rectangle bounds) {
        return bounds.height() - (this.hScrollVisible && this.barType.value() == ScrollBarType.NORMAL ? this.barWidth.value() : 0);
    }

    @Nullable
    private Rectangle getVScrollArea(final Rectangle bounds) {
        if (!this.vScrollVisible) return null;
        float barWidth = this.barWidth.value();
        return new Rectangle(bounds.width() - barWidth, 0, barWidth, bounds.height() - (this.hScrollVisible && this.barType.value() == ScrollBarType.NORMAL ? barWidth : 0));
    }

    @Nullable
    private Rectangle getVRailBounds(final Rectangle bounds) {
        return this.getVScrollArea(bounds);
    }

    @Nullable
    private Rectangle getVThumbBounds(final Rectangle bounds) {
        Rectangle rail = this.getVRailBounds(bounds);
        if (rail == null) return null;
        float contentHeight = this.childSize.height();
        float visibleHeight = this.visibleHeight(bounds);
        float thumbHeight = Math.max(20, (visibleHeight / contentHeight) * rail.height());
        float maxScroll = Math.max(1, contentHeight - visibleHeight);
        float scrollPercentage = this.scrollY / maxScroll;
        float thumbY = rail.y() + scrollPercentage * (rail.height() - thumbHeight);
        return new Rectangle(rail.x(), thumbY, rail.width(), thumbHeight);
    }

    @Nullable
    private Rectangle getHScrollArea(final Rectangle bounds) {
        if (!this.hScrollVisible) return null;
        float barWidth = this.barWidth.value();
        return new Rectangle(0, bounds.height() - barWidth, bounds.width() - (this.vScrollVisible && this.barType.value() == ScrollBarType.NORMAL ? barWidth : 0), barWidth);
    }

    @Nullable
    private Rectangle getHRailBounds(final Rectangle bounds) {
        return this.getHScrollArea(bounds);
    }

    @Nullable
    private Rectangle getHThumbBounds(final Rectangle bounds) {
        Rectangle rail = this.getHRailBounds(bounds);
        if (rail == null) return null;
        float contentWidth = this.childSize.width();
        float visibleWidth = this.visibleWidth(bounds);
        float thumbWidth = Math.max(20, (visibleWidth / contentWidth) * rail.width());
        float maxScroll = Math.max(1, contentWidth - visibleWidth);
        float scrollPercentage = this.scrollX / maxScroll;
        float thumbX = rail.x() + scrollPercentage * (rail.width() - thumbWidth);
        return new Rectangle(thumbX, rail.y(), thumbWidth, rail.height());
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
        float availableWidth = size.width();
        float availableHeight = size.height();

        this.vScrollVisible = false;
        this.hScrollVisible = false;
        if (this.barType.value() == ScrollBarType.NORMAL) {
            Size idealChildSize = this.child.idealSize();

            this.vScrollVisible = this.verticalScrolling && idealChildSize.height() > availableHeight;
            this.hScrollVisible = this.horizontalScrolling && idealChildSize.width() > (this.vScrollVisible ? availableWidth - this.barWidth.value() : availableWidth);
            if (this.hScrollVisible && !this.vScrollVisible) {
                this.vScrollVisible = this.verticalScrolling && idealChildSize.height() > (availableHeight - this.barWidth.value());
            }

            if (this.vScrollVisible) availableWidth -= this.barWidth.value();
            if (this.hScrollVisible) availableHeight -= this.barWidth.value();
        }

        Size childSize = new Size(
                MathUtils.clamp(availableWidth, this.child.minSize().width(), this.child.maxSize().width()),
                MathUtils.clamp(availableHeight, this.child.minSize().height(), this.child.maxSize().height())
        );
        this.child.computeLayout(childSize);
        if (this.child instanceof Container container) {
            childSize = container.contentSize();
        }
        this.childSize = childSize;

        float contentHeight = childSize.height();
        if (this.barType.value() == ScrollBarType.FLOATING) {
            this.vScrollVisible = this.verticalScrolling && contentHeight > availableHeight;
        }
        float maxScrollY = Math.max(0, contentHeight - availableHeight);
        this.targetScrollY = MathUtils.clamp(this.targetScrollY, 0, maxScrollY);
        this.scrollY = MathUtils.clamp(this.scrollY, 0, maxScrollY);

        float contentWidth = childSize.width();
        if (this.barType.value() == ScrollBarType.FLOATING) {
            this.hScrollVisible = this.horizontalScrolling && contentWidth > availableWidth;
        }
        float maxScrollX = Math.max(0, contentWidth - availableWidth);
        this.targetScrollX = MathUtils.clamp(this.targetScrollX, 0, maxScrollX);
        this.scrollX = MathUtils.clamp(this.scrollX, 0, maxScrollX);
    }


    public enum ScrollBarType {
        FLOATING, NORMAL
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
