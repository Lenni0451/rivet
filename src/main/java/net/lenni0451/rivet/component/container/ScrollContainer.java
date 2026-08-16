package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.animation.DynamicAnimation;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.animation.DynamicAnimationConfig;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.component.ParentContainer;
import net.lenni0451.rivet.component.impl.ScrollBar;
import net.lenni0451.rivet.component.impl.ScrollBar.ScrollBarType;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import net.lenni0451.rivet.utils.ContainerMouseHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static net.lenni0451.rivet.utils.MathUtils.EPSILON;
import static net.lenni0451.rivet.utils.MathUtils.isGreaterThan;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class ScrollContainer extends ParentContainer {

    @Getter
    private final Component child;
    @Getter
    private final boolean horizontalScrolling;
    @Getter
    private final boolean verticalScrolling;
    @Getter
    private final ScrollBar hScrollBar;
    @Getter
    private final ScrollBar vScrollBar;
    @Getter
    @Setter
    private boolean autoScroll;
    @Getter
    @Setter
    private float autoScrollThreshold = 0.1F;
    @Getter
    @Setter
    private boolean presentInfiniteSize = true;

    @Getter
    private final ThemeOption<Float> scrollSpeed = new ThemeOption<>(this, Theme.ScrollContainer.SPEED);
    @Getter
    private final ThemeOption<Boolean> smoothScrolling = new ThemeOption<>(this, Theme.ScrollContainer.SMOOTH);
    @Getter
    private final ThemeOption<DynamicAnimationConfig> animationConfig = new ThemeOption<>(this, Theme.ScrollContainer.ANIMATION);
    @Getter
    private final ThemeOption<Long> nestedScrollTimeout = new ThemeOption<>(this, Theme.ScrollContainer.NESTED_SCROLL_TIMEOUT);

    private Size childSize = Size.EMPTY;
    private final MouseHandler mouseHandler = new MouseHandler();

    private final NestedScrollCoordinator nestedScrollCoordinator = new NestedScrollCoordinator();
    @Getter
    private final ListenerList<ScrollListener> scrollListener = new ListenerList<>();
    @Getter
    private float scrollX;
    @Getter
    private float scrollY;
    private float targetScrollX;
    private float targetScrollY;
    private DynamicAnimation scrollXAnimation;
    private DynamicAnimation scrollYAnimation;

    private boolean vScrollVisible;
    private boolean hScrollVisible;

    public ScrollContainer(final Component child) {
        this(child, false, true);
    }

    public <C extends Component> ScrollContainer(final C child, final Consumer<C> initializer) {
        this(child, initializer, false, true);
    }

    public ScrollContainer(final Component child, final boolean horizontalScrolling, final boolean verticalScrolling) {
        this(child, c -> {}, horizontalScrolling, verticalScrolling);
    }

    public <C extends Component> ScrollContainer(final C child, final Consumer<C> initializer, final boolean horizontalScrolling, final boolean verticalScrolling) {
        this.child = child;
        initializer.accept(child);
        this.horizontalScrolling = horizontalScrolling;
        this.verticalScrolling = verticalScrolling;
        this.hScrollBar = new ScrollBar(ScrollBar.Orientation.HORIZONTAL);
        this.vScrollBar = new ScrollBar(ScrollBar.Orientation.VERTICAL);

        this.hScrollBar.scrollListener().add(this::scrollX);
        this.vScrollBar.scrollListener().add(this::scrollY);
    }

    public final float maxScrollX() {
        float maxScroll = this.childSize.width() - this.visibleWidth(this.relativeBounds().size());
        return maxScroll <= EPSILON ? 0 : maxScroll;
    }

    public final float maxScrollY() {
        float maxScroll = this.childSize.height() - this.visibleHeight(this.relativeBounds().size());
        return maxScroll <= EPSILON ? 0 : maxScroll;
    }

    public final ScrollContainer scrollX(final float scrollX) {
        return this.scrollX(scrollX, false);
    }

    public final ScrollContainer scrollX(final float scrollX, final boolean immediate) {
        this.targetScrollX = MathUtils.clamp(scrollX, 0, this.maxScrollX());
        if (this.scrollXAnimation != null) {
            this.scrollXAnimation.setTarget(this.targetScrollX);
            if (immediate) {
                this.scrollXAnimation.finish();
            }
        }
        if (immediate) {
            float oldScrollX = this.scrollX;
            this.scrollX = this.targetScrollX;
            this.hScrollBar.scroll(this.scrollX, false);
            if (oldScrollX != this.scrollX) {
                if (this.rivet() != null) {
                    this.rivet().updateMouseState();
                }
                this.scrollListener.call(c -> c.onScroll(this.scrollX, this.scrollY));
            }
        }
        return this;
    }

    public final ScrollContainer scrollY(final float scrollY) {
        return this.scrollY(scrollY, false);
    }

    public final ScrollContainer scrollY(final float scrollY, final boolean immediate) {
        this.targetScrollY = MathUtils.clamp(scrollY, 0, this.maxScrollY());
        if (this.scrollYAnimation != null) {
            this.scrollYAnimation.setTarget(this.targetScrollY);
            if (immediate) {
                this.scrollYAnimation.finish();
            }
        }
        if (immediate) {
            float oldScrollY = this.scrollY;
            this.scrollY = this.targetScrollY;
            this.vScrollBar.scroll(this.scrollY, false);
            if (oldScrollY != this.scrollY) {
                if (this.rivet() != null) {
                    this.rivet().updateMouseState();
                }
                this.scrollListener.call(c -> c.onScroll(this.scrollX, this.scrollY));
            }
        }
        return this;
    }

    @Override
    protected ContainerMouseHandler<?> mouseHandler() {
        return this.mouseHandler;
    }

    @Override
    protected void onAddedInternal() {
        super.onAddedInternal();
        this.scrollXAnimation = this.animationConfig.value().create(this.scrollX);
        this.scrollYAnimation = this.animationConfig.value().create(this.scrollY);
    }

    @Override
    protected void onThemeChangedInternal() {
        super.onThemeChangedInternal();
        this.scrollXAnimation = this.animationConfig.value().create(this.scrollXAnimation.getValue());
        this.scrollYAnimation = this.animationConfig.value().create(this.scrollYAnimation.getValue());
    }

    @Override
    protected boolean onMouseScrollInternal(final MouseScrollEvent event, final Size size) {
        return this.nestedScrollCoordinator.handleScrolling(
                () -> {
                    if (this.hScrollVisible && (event.scrollX() != 0 || (!this.vScrollVisible && event.scrollY() != 0)) && !this.hScrollBar.barPressed()) {
                        float contentWidth = this.childSize.width();
                        float visibleWidth = this.visibleWidth(size);
                        float maxScroll = Math.max(0, contentWidth - visibleWidth);
                        if (maxScroll > EPSILON) {
                            float scrollAmount = event.scrollX() == 0 ? event.scrollY() : event.scrollX();
                            this.targetScrollX = MathUtils.clamp(this.targetScrollX - scrollAmount * this.scrollSpeed.value(), 0, maxScroll);
                            return true;
                        }
                    }
                    if (this.vScrollVisible && event.scrollY() != 0 && !this.vScrollBar.barPressed()) {
                        float contentHeight = this.childSize.height();
                        float visibleHeight = this.visibleHeight(size);
                        float maxScroll = Math.max(0, contentHeight - visibleHeight);
                        if (maxScroll > EPSILON) {
                            this.targetScrollY = MathUtils.clamp(this.targetScrollY - event.scrollY() * this.scrollSpeed.value(), 0, maxScroll);
                            return true;
                        }
                    }
                    return false;
                },
                () -> this.child.onMouseScroll(
                        event.withX(event.x() + this.scrollX).withY(event.y() + this.scrollY),
                        this.childSize
                )
        );
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size) {
        this.updateAnimation();
        renderer.scissor(0, 0, this.visibleWidth(size), this.visibleHeight(size), () -> {
            renderer.translate(-this.scrollX, -this.scrollY, () -> {
                this.child.render(renderer, this.childSize);
            });
        });
        if (this.hScrollVisible) {
            Rectangle area = this.getHScrollArea(size);
            if (area != null) {
                renderer.translate(area.x(), area.y(), () -> {
                    this.hScrollBar.render(renderer, area.size());
                });
            }
        }
        if (this.vScrollVisible) {
            Rectangle area = this.getVScrollArea(size);
            if (area != null) {
                renderer.translate(area.x(), area.y(), () -> {
                    this.vScrollBar.render(renderer, area.size());
                });
            }
        }
        this.renderCorner(renderer, size);
    }

    private void updateAnimation() {
        if (this.smoothScrolling.value()) {
            this.scrollXAnimation.setTarget(this.targetScrollX);
            this.scrollYAnimation.setTarget(this.targetScrollY);
        } else {
            this.scrollXAnimation.setTarget(this.targetScrollX).finish();
            this.scrollYAnimation.setTarget(this.targetScrollY).finish();
        }
        float oldScrollX = this.scrollX;
        float oldScrollY = this.scrollY;
        this.scrollX = this.scrollXAnimation.getValue();
        this.scrollY = this.scrollYAnimation.getValue();
        this.hScrollBar.scroll(this.scrollX, false);
        this.vScrollBar.scroll(this.scrollY, false);
        if (oldScrollX != this.scrollX || oldScrollY != this.scrollY) {
            if (this.rivet() != null) {
                this.rivet().updateMouseState();
            }
            this.scrollListener.call(c -> c.onScroll(this.scrollX, this.scrollY));
        }
    }

    private void renderCorner(final Renderer renderer, final Size size) {
        if (this.vScrollVisible && this.hScrollVisible && this.vScrollBar.barType().value() == ScrollBarType.NORMAL && this.hScrollBar.barType().value() == ScrollBarType.NORMAL) {
            Color color = this.disabled() ? this.vScrollBar.disabledRailColor().value() : this.vScrollBar.railColor().value();
            float vBarWidth = this.vScrollBar.barWidth().value();
            float hBarHeight = this.hScrollBar.barWidth().value();
            renderer.fillRect(size.width() - vBarWidth, size.height() - hBarHeight, vBarWidth, hBarHeight, color);
        }
    }

    private float visibleWidth(final Size size) {
        return size.width() - (this.vScrollVisible && this.vScrollBar.barType().value() == ScrollBarType.NORMAL ? this.vScrollBar.barWidth().value() : 0);
    }

    private float visibleHeight(final Size size) {
        return size.height() - (this.hScrollVisible && this.hScrollBar.barType().value() == ScrollBarType.NORMAL ? this.hScrollBar.barWidth().value() : 0);
    }

    @Nullable
    private Rectangle getHScrollArea(final Size size) {
        if (!this.hScrollVisible) return null;
        float barHeight = this.hScrollBar.barWidth().value();
        float vBarWidth = this.vScrollBar.barWidth().value();
        return new Rectangle(
                0,
                size.height() - barHeight,
                size.width() - (this.vScrollVisible && this.vScrollBar.barType().value() == ScrollBarType.NORMAL ? vBarWidth : 0),
                barHeight
        );
    }

    @Nullable
    private Rectangle getVScrollArea(final Size size) {
        if (!this.vScrollVisible) return null;
        float barWidth = this.vScrollBar.barWidth().value();
        float hBarHeight = this.hScrollBar.barWidth().value();
        return new Rectangle(
                size.width() - barWidth,
                0,
                barWidth,
                size.height() - (this.hScrollVisible && this.hScrollBar.barType().value() == ScrollBarType.NORMAL ? hBarHeight : 0)
        );
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return this.child.computeIdealSize(new Size(
                this.horizontalScrolling && this.presentInfiniteSize ? Float.MAX_VALUE : constraints.width(),
                this.verticalScrolling && this.presentInfiniteSize ? Float.MAX_VALUE : constraints.height()
        )).clamp(this.child);
    }

    @Override
    public void computeLayout(final Size size) {
        // float previousMaxScrollX = this.childSize.width() - this.visibleWidth(size);
        float previousMaxScrollY = this.childSize.height() - this.visibleHeight(size);

        this.hScrollVisible = false;
        this.vScrollVisible = false;

        boolean hBarNormal = this.hScrollBar.barType().value() == ScrollBarType.NORMAL;
        boolean vBarNormal = this.vScrollBar.barType().value() == ScrollBarType.NORMAL;

        float availableWidth;
        float availableHeight;

        boolean checkAgain;
        int iterations = 0;
        do {
            checkAgain = false;
            iterations++;

            availableWidth = size.width();
            availableHeight = size.height();
            if (this.hScrollVisible && hBarNormal) availableHeight -= this.hScrollBar.barWidth().value();
            if (this.vScrollVisible && vBarNormal) availableWidth -= this.vScrollBar.barWidth().value();

            Size idealChildSize = this.child.computeIdealSize(new Size(
                    this.horizontalScrolling && this.presentInfiniteSize ? Float.MAX_VALUE : availableWidth,
                    this.verticalScrolling && this.presentInfiniteSize ? Float.MAX_VALUE : availableHeight
            ));

            Size childSize = new Size(
                    MathUtils.clamp(this.horizontalScrolling ? Math.max(idealChildSize.width(), availableWidth) : availableWidth, this.child.minSize().width(), this.child.maxSize().width()),
                    MathUtils.clamp(this.verticalScrolling ? Math.max(idealChildSize.height(), availableHeight) : availableHeight, this.child.minSize().height(), this.child.maxSize().height())
            );
            this.child.computeLayout(childSize);
            if (this.child instanceof Parent parent) {
                Size parentContentSize = parent.contentSize();
                if (!parentContentSize.equals(Size.EMPTY)) {
                    childSize = new Size(
                            this.horizontalScrolling ? MathUtils.clamp(Math.max(childSize.width(), parentContentSize.width()), this.child.minSize().width(), this.child.maxSize().width()) : childSize.width(),
                            this.verticalScrolling ? MathUtils.clamp(Math.max(childSize.height(), parentContentSize.height()), this.child.minSize().height(), this.child.maxSize().height()) : childSize.height()
                    );
                }
            }
            this.childSize = childSize;

            if (hBarNormal || vBarNormal) {
                boolean newHScrollVisible = this.horizontalScrolling && isGreaterThan(this.childSize.width(), availableWidth);
                boolean newVScrollVisible = this.verticalScrolling && isGreaterThan(this.childSize.height(), availableHeight);

                if (newHScrollVisible && !newVScrollVisible && hBarNormal) {
                    newVScrollVisible = this.verticalScrolling && isGreaterThan(this.childSize.height(), availableHeight - this.hScrollBar.barWidth().value());
                }
                if (newVScrollVisible && !newHScrollVisible && vBarNormal) {
                    newHScrollVisible = this.horizontalScrolling && isGreaterThan(this.childSize.width(), availableWidth - this.vScrollBar.barWidth().value());
                }

                if (newHScrollVisible != this.hScrollVisible || newVScrollVisible != this.vScrollVisible) {
                    this.hScrollVisible = newHScrollVisible;
                    this.vScrollVisible = newVScrollVisible;
                    checkAgain = true;
                }
            }
        } while (checkAgain && iterations < 3);

        float oldScrollX = this.scrollX;
        float oldScrollY = this.scrollY;
        { // Horizontal scroll bar
            float contentWidth = this.childSize.width();
            if (!hBarNormal) {
                this.hScrollVisible = this.horizontalScrolling && isGreaterThan(contentWidth, availableWidth);
            }
            float maxScrollX = Math.max(0, contentWidth - availableWidth);
            if (maxScrollX <= EPSILON) maxScrollX = 0;
            this.targetScrollX = MathUtils.clamp(this.targetScrollX, 0, maxScrollX);
            this.scrollX = MathUtils.clamp(this.scrollX, 0, maxScrollX);
        }
        { // Vertical scroll bar
            float contentHeight = this.childSize.height();
            if (!vBarNormal) {
                this.vScrollVisible = this.verticalScrolling && isGreaterThan(contentHeight, availableHeight);
            }
            float maxScrollY = Math.max(0, contentHeight - availableHeight);
            if (maxScrollY <= EPSILON) maxScrollY = 0;
            if (this.autoScroll && previousMaxScrollY - this.targetScrollY <= availableHeight * this.autoScrollThreshold) {
                this.targetScrollY = maxScrollY;
            } else {
                this.targetScrollY = MathUtils.clamp(this.targetScrollY, 0, maxScrollY);
            }
            this.scrollY = MathUtils.clamp(this.scrollY, 0, maxScrollY);
        }

        this.hScrollBar.contentSize(this.childSize.width());
        this.hScrollBar.visibleSize(availableWidth);
        this.hScrollBar.scroll(this.scrollX, false);
        Rectangle hArea = this.getHScrollArea(size);
        if (hArea != null) {
            this.hScrollBar.computeLayout(hArea.size());
        }

        this.vScrollBar.contentSize(this.childSize.height());
        this.vScrollBar.visibleSize(availableHeight);
        this.vScrollBar.scroll(this.scrollY, false);
        Rectangle vArea = this.getVScrollArea(size);
        if (vArea != null) {
            this.vScrollBar.computeLayout(vArea.size());
        }

        if (oldScrollX != this.scrollX || oldScrollY != this.scrollY) {
            this.scrollListener.call(c -> c.onScroll(this.scrollX, this.scrollY));
        }
    }

    @Override
    public Size contentSize() {
        return Size.EMPTY;
    }

    @Override
    public List<Component> children() {
        return List.of(this.child, this.hScrollBar, this.vScrollBar);
    }

    @Override
    public Rectangle childBounds(final Component component) {
        if (component == this.child) {
            return new Rectangle(-this.scrollX, -this.scrollY, this.childSize);
        } else if (component == this.hScrollBar && this.hScrollVisible) {
            Rectangle area = this.getHScrollArea(this.relativeBounds().size());
            return area != null ? area : Rectangle.EMPTY;
        } else if (component == this.vScrollBar && this.vScrollVisible) {
            Rectangle area = this.getVScrollArea(this.relativeBounds().size());
            return area != null ? area : Rectangle.EMPTY;
        }
        return Rectangle.EMPTY;
    }


    @FunctionalInterface
    public interface ScrollListener {
        void onScroll(final float scrollX, final float scrollY);
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

    private class MouseHandler extends ContainerMouseHandler<Component> {
        @Override
        protected Component map(final Component element) {
            return element;
        }

        @Override
        protected Rectangle relativeBounds(final Size containerBounds, final Component element) {
            if (element == ScrollContainer.this.child) {
                return new Rectangle(
                        -ScrollContainer.this.scrollX,
                        -ScrollContainer.this.scrollY,
                        ScrollContainer.this.childSize.width(),
                        ScrollContainer.this.childSize.height()
                );
            } else if (element == ScrollContainer.this.hScrollBar) {
                Rectangle area = ScrollContainer.this.getHScrollArea(containerBounds);
                return area != null ? area : Rectangle.EMPTY;
            } else if (element == ScrollContainer.this.vScrollBar) {
                Rectangle area = ScrollContainer.this.getVScrollArea(containerBounds);
                return area != null ? area : Rectangle.EMPTY;
            }
            return Rectangle.EMPTY;
        }

        @Override
        protected List<Component> elementsAt(final float x, final float y, final Size containerBounds) {
            if (x >= 0 && x < containerBounds.width() && y >= 0 && y < containerBounds.height()) {
                if (ScrollContainer.this.hScrollVisible) {
                    Rectangle area = ScrollContainer.this.getHScrollArea(containerBounds);
                    if (area != null && area.contains(x, y)) {
                        return List.of(ScrollContainer.this.hScrollBar);
                    }
                }
                if (ScrollContainer.this.vScrollVisible) {
                    Rectangle area = ScrollContainer.this.getVScrollArea(containerBounds);
                    if (area != null && area.contains(x, y)) {
                        return List.of(ScrollContainer.this.vScrollBar);
                    }
                }
                if (x < ScrollContainer.this.visibleWidth(containerBounds) && y < ScrollContainer.this.visibleHeight(containerBounds)) {
                    return List.of(ScrollContainer.this.child);
                }
            }
            return List.of();
        }
    }

}
