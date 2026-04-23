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
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.keyboard.KeyboardListener;
import net.lenni0451.rivet.input.mouse.*;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import javax.annotation.Nullable;

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

    private Size childSize = Size.EMPTY;

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

        this.scrollXAnimation = new DynamicAnimation(EasingFunction.SINE, EasingMode.EASE_OUT, (long) this.animationDuration.value(), 0);
        this.scrollYAnimation = new DynamicAnimation(EasingFunction.SINE, EasingMode.EASE_OUT, (long) this.animationDuration.value(), 0);
    }

    @Override
    public void onMouseDown(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            Rectangle vBar = this.getVBarBounds(size);
            Rectangle hBar = this.getHBarBounds(size);
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
            mouseListener.onMouseDown(event.withX(event.x() + this.scrollX).withY(event.y() + this.scrollY), this.childSize);
        }
    }

    @Override
    public void onMouseUp(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT) && (this.vBarPressed || this.hBarPressed)) {
            this.vBarPressed = false;
            this.hBarPressed = false;
        } else {
            if (this.child instanceof MouseListener mouseListener) {
                mouseListener.onMouseUp(event.withX(event.x() + this.scrollX).withY(event.y() + this.scrollY), this.childSize);
            }
        }
    }

    @Override
    public void onMouseMove(final MouseMoveEvent event, final Size size) {
        Rectangle vBar = this.getVBarBounds(size);
        Rectangle hBar = this.getHBarBounds(size);
        this.vBarHovered = vBar != null && vBar.contains(event.x(), event.y());
        this.hBarHovered = hBar != null && hBar.contains(event.x(), event.y());
        if (this.vBarPressed && vBar != null) {
            float contentHeight = this.childSize.height();
            float maxScroll = contentHeight - size.height();
            float barHeight = vBar.height();
            float scrollableHeight = size.height() - barHeight;
            float dragDelta = event.y() - this.dragStartY;
            this.targetScrollY = MathUtils.clamp(this.initialScrollY + (dragDelta / scrollableHeight) * maxScroll, 0, maxScroll);
        } else if (this.hBarPressed && hBar != null) {
            float contentWidth = this.childSize.width();
            float maxScroll = contentWidth - size.width();
            float barWidth = hBar.width();
            float scrollableWidth = size.width() - barWidth;
            float dragDelta = event.x() - this.dragStartX;
            this.targetScrollX = MathUtils.clamp(this.initialScrollX + (dragDelta / scrollableWidth) * maxScroll, 0, maxScroll);
        } else {
            if (this.child instanceof MouseListener mouseListener) {
                boolean contains = new Rectangle(0, 0, size.width(), size.height()).contains(event.x(), event.y());
                if (contains && !this.childHovered) {
                    this.childHovered = true;
                    mouseListener.onMouseEnter();
                } else if (!contains && this.childHovered) {
                    this.childHovered = false;
                    mouseListener.onMouseLeave();
                }
                mouseListener.onMouseMove(event.withX(event.x() + this.scrollX).withY(event.y() + this.scrollY), this.childSize);
            }
        }
    }

    @Override
    public void onKeyDown(final KeyEvent event) {
        if (this.child instanceof KeyboardListener keyboardListener) {
            keyboardListener.onKeyDown(event);
        }
    }

    @Override
    public void onKeyUp(final KeyEvent event) {
        if (this.child instanceof KeyboardListener keyboardListener) {
            keyboardListener.onKeyUp(event);
        }
    }

    @Override
    public void onCharTyped(final CharEvent event) {
        if (this.child instanceof KeyboardListener keyboardListener) {
            keyboardListener.onCharTyped(event);
        }
    }

    @Override
    public void onMouseScroll(final MouseScrollEvent event, final Size size) {
        if (this.verticalScrolling && event.scrollY() != 0 && !this.vBarPressed) {
            float contentHeight = this.childSize.height();
            float maxScroll = Math.max(0, contentHeight - size.height());
            if (maxScroll > 0) {
                this.targetScrollY = MathUtils.clamp(this.targetScrollY - event.scrollY() * this.scrollSpeed.value(), 0, maxScroll);
            }
        }
        if (this.horizontalScrolling && event.scrollX() != 0 && !this.hBarPressed) {
            float contentWidth = this.childSize.width();
            float maxScroll = Math.max(0, contentWidth - size.width());
            if (maxScroll > 0) {
                this.targetScrollX = MathUtils.clamp(this.targetScrollX - event.scrollX() * this.scrollSpeed.value(), 0, maxScroll);
            }
        }
        if (this.child instanceof MouseListener mouseListener) {
            mouseListener.onMouseScroll(event.withX(event.x() + this.scrollX).withY(event.y() + this.scrollY), this.childSize);
        }
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        this.updateAnimation();

        renderer.scissor(0, 0, size.width(), size.height(), () -> {
            renderer.translate(-this.scrollX, -this.scrollY, () -> {
                if (this.child instanceof Renderable renderable) {
                    renderable.render(renderer, this.childSize);
                }
            });
        });

        this.renderBar(renderer, this.getVBarBounds(size), this.vBarHovered, this.vBarPressed);
        this.renderBar(renderer, this.getHBarBounds(size), this.hBarHovered, this.hBarPressed);
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
    private Rectangle getVBarBounds(final Size size) {
        if (!this.verticalScrolling) return null;
        float contentHeight = this.childSize.height();
        if (contentHeight <= size.height()) return null;

        float barWidth = this.barWidth.value();
        float barHeight = Math.max(20, (size.height() / contentHeight) * size.height());
        float maxScroll = contentHeight - size.height();
        float scrollPercentage = this.scrollY / maxScroll;
        float barY = scrollPercentage * (size.height() - barHeight);

        return new Rectangle(size.width() - barWidth, barY, barWidth, barHeight);
    }

    @Nullable
    private Rectangle getHBarBounds(final Size size) {
        if (!this.horizontalScrolling) return null;
        float contentWidth = this.childSize.width();
        if (contentWidth <= size.width()) return null;

        float barWidth = Math.max(20, (size.width() / contentWidth) * size.width());
        float barHeight = this.barWidth.value();
        float maxScroll = contentWidth - size.width();
        float scrollPercentage = this.scrollX / maxScroll;
        float barX = scrollPercentage * (size.width() - barWidth);

        return new Rectangle(barX, size.height() - barHeight, barWidth, barHeight);
    }

    @Override
    public void computeIdealSize() {
        this.child.computeIdealSize();
        this.idealSize = this.child.idealSize();
    }

    @Override
    public void computeLayout(final Size size) {
        Size childSize = new Size(
                MathUtils.clamp(this.child.idealSize().width(), this.child.minSize().width(), this.child.maxSize().width()),
                MathUtils.clamp(this.child.idealSize().height(), this.child.minSize().height(), this.child.maxSize().height())
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

}
