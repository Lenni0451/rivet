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
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.component.ParentContainer;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
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

@Accessors(fluent = true, chain = true, makeFinal = true)
public class ScrollContainer extends ParentContainer {

    @Getter
    private final Component child;
    @Getter
    private final boolean horizontalScrolling;
    @Getter
    private final boolean verticalScrolling;
    @Getter
    @Setter
    private boolean autoScroll;
    @Getter
    @Setter
    private float autoScrollThreshold = 0.1F;

    @Getter
    private final ThemeOption<Color> barColor = new ThemeOption<>(this, Theme.SCROLL_BAR_COLOR);
    @Getter
    private final ThemeOption<Color> barHoverColor = new ThemeOption<>(this, Theme.SCROLL_BAR_HOVER_COLOR);
    @Getter
    private final ThemeOption<Color> barClickColor = new ThemeOption<>(this, Theme.SCROLL_BAR_CLICK_COLOR);
    @Getter
    private final ThemeOption<Float> barWidth = new ThemeOption<>(this, Theme.SCROLL_BAR_WIDTH);
    @Getter
    private final ThemeOption<Float> barCornerRadius = new ThemeOption<>(this, Theme.SCROLL_BAR_CORNER_RADIUS);
    @Getter
    private final ThemeOption<Float> barOutlineWidth = new ThemeOption<>(this, Theme.SCROLL_BAR_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Color> barOutlineColor = new ThemeOption<>(this, Theme.SCROLL_BAR_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Float> scrollSpeed = new ThemeOption<>(this, Theme.SCROLL_SPEED);
    @Getter
    private final ThemeOption<Boolean> smoothScrolling = new ThemeOption<>(this, Theme.SCROLL_SMOOTH);
    @Getter
    private final ThemeOption<DynamicAnimationConfig> animationConfig = new ThemeOption<>(this, Theme.SCROLL_ANIMATION);
    @Getter
    private final ThemeOption<Long> nestedScrollTimeout = new ThemeOption<>(this, Theme.SCROLL_NESTED_SCROLL_TIMEOUT);
    @Getter
    private final ThemeOption<ScrollBarType> barType = new ThemeOption<>(this, Theme.SCROLL_BAR_TYPE);
    @Getter
    private final ThemeOption<Boolean> railClickJump = new ThemeOption<>(this, Theme.SCROLL_RAIL_CLICK_JUMP);
    @Getter
    private final ThemeOption<Color> railColor = new ThemeOption<>(this, Theme.SCROLL_RAIL_COLOR);
    @Getter
    private final ThemeOption<Color> railOutlineColor = new ThemeOption<>(this, Theme.SCROLL_RAIL_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Float> railOutlineWidth = new ThemeOption<>(this, Theme.SCROLL_RAIL_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Color> disabledBarColor = new ThemeOption<>(this, Theme.SCROLL_BAR_DISABLED_COLOR);
    @Getter
    private final ThemeOption<Color> disabledBarOutlineColor = new ThemeOption<>(this, Theme.SCROLL_BAR_DISABLED_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledRailColor = new ThemeOption<>(this, Theme.SCROLL_RAIL_DISABLED_COLOR);
    @Getter
    private final ThemeOption<Color> disabledRailOutlineColor = new ThemeOption<>(this, Theme.SCROLL_RAIL_DISABLED_OUTLINE_COLOR);

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

    private boolean hBarHovered;
    private boolean hBarPressed;
    private boolean hRailHovered;
    private boolean hRailPressed;
    private boolean vBarHovered;
    private boolean vBarPressed;
    private boolean vRailHovered;
    private boolean vRailPressed;
    private float dragStartX;
    private float dragStartY;
    private float initialScrollX;
    private float initialScrollY;
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
    }

    public float maxScrollX() {
        return Math.max(0, this.childSize.width() - this.visibleWidth(this.relativeBounds().size()));
    }

    public float maxScrollY() {
        return Math.max(0, this.childSize.height() - this.visibleHeight(this.relativeBounds().size()));
    }

    public ScrollContainer scrollX(final float scrollX) {
        return this.scrollX(scrollX, false);
    }

    public ScrollContainer scrollX(final float scrollX, final boolean immediate) {
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
            if (oldScrollX != this.scrollX) {
                if (this.rivet() != null) {
                    this.rivet().updateMouseState();
                }
                this.scrollListener.callVoid(c -> c.onScroll(this.scrollX, this.scrollY));
            }
        }
        return this;
    }

    public ScrollContainer scrollY(final float scrollY) {
        return this.scrollY(scrollY, false);
    }

    public ScrollContainer scrollY(final float scrollY, final boolean immediate) {
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
            if (oldScrollY != this.scrollY) {
                if (this.rivet() != null) {
                    this.rivet().updateMouseState();
                }
                this.scrollListener.callVoid(c -> c.onScroll(this.scrollX, this.scrollY));
            }
        }
        return this;
    }

    @Override
    protected ContainerMouseHandler<?> mouseHandler() {
        return this.mouseHandler;
    }

    @Override
    protected void onComponentAdded() {
        super.onComponentAdded();
        this.scrollXAnimation = this.animationConfig.value().create(this.scrollX);
        this.scrollYAnimation = this.animationConfig.value().create(this.scrollY);
    }

    @Override
    protected void onComponentRemoved() {
        super.onComponentRemoved();
        this.hBarHovered = false;
        this.hBarPressed = false;
        this.hRailHovered = false;
        this.hRailPressed = false;
        this.vBarHovered = false;
        this.vBarPressed = false;
        this.vRailHovered = false;
        this.vRailPressed = false;
    }

    @Override
    protected void onComponentDisabled() {
        super.onComponentDisabled();
        this.hBarHovered = false;
        this.hBarPressed = false;
        this.hRailHovered = false;
        this.hRailPressed = false;
        this.vBarHovered = false;
        this.vBarPressed = false;
        this.vRailHovered = false;
        this.vRailPressed = false;
    }

    @Override
    protected void onComponentThemeChanged() {
        super.onComponentThemeChanged();
        this.scrollXAnimation = this.animationConfig.value().create(this.scrollXAnimation.getValue());
        this.scrollYAnimation = this.animationConfig.value().create(this.scrollYAnimation.getValue());
    }

    @Override
    protected void onComponentMouseLeave() {
        super.onComponentMouseLeave();
        this.hBarHovered = false;
        this.hRailHovered = false;
        this.vBarHovered = false;
        this.vRailHovered = false;
    }

    @Override
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            Rectangle hThumb = this.getHThumbBounds(size);
            Rectangle hRail = this.getHScrollArea(size);
            Rectangle vThumb = this.getVThumbBounds(size);
            Rectangle vRail = this.getVScrollArea(size);
            if (hThumb != null && hThumb.contains(event.x(), event.y())) {
                this.hBarPressed = true;
                this.dragStartX = event.x();
                this.initialScrollX = this.targetScrollX;
                this.rivet().focusedComponent(this);
                return true;
            } else if (hRail != null && hRail.contains(event.x(), event.y())) {
                this.hRailPressed = true;
                float visibleWidth = this.visibleWidth(size);
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
                this.rivet().focusedComponent(this);
                return true;
            } else if (vThumb != null && vThumb.contains(event.x(), event.y())) {
                this.vBarPressed = true;
                this.dragStartY = event.y();
                this.initialScrollY = this.targetScrollY;
                this.rivet().focusedComponent(this);
                return true;
            } else if (vRail != null && vRail.contains(event.x(), event.y())) {
                this.vRailPressed = true;
                float visibleHeight = this.visibleHeight(size);
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
                this.rivet().focusedComponent(this);
                return true;
            }
        }
        return super.onComponentMouseDown(event, size);
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        Rectangle hThumb = this.getHThumbBounds(size);
        Rectangle hRail = this.getHScrollArea(size);
        Rectangle vThumb = this.getVThumbBounds(size);
        Rectangle vRail = this.getVScrollArea(size);
        boolean hThumbHovered = hThumb != null && hThumb.contains(event.x(), event.y());
        boolean hRailHovered = hRail != null && hRail.contains(event.x(), event.y());
        boolean vThumbHovered = vThumb != null && vThumb.contains(event.x(), event.y());
        boolean vRailHovered = vRail != null && vRail.contains(event.x(), event.y());
        boolean wasPressed = this.hBarPressed || this.hRailPressed || this.vBarPressed || this.vRailPressed;
        if (event.button().equals(MouseButton.LEFT)) {
            this.hBarPressed = false;
            this.hRailPressed = false;
            this.vBarPressed = false;
            this.vRailPressed = false;
        }
        if (hThumbHovered || hRailHovered || vThumbHovered || vRailHovered || wasPressed) {
            return true;
        }
        return super.onComponentMouseUp(event, size);
    }

    @Override
    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
        Rectangle hThumb = this.getHThumbBounds(size);
        Rectangle hRail = this.getHScrollArea(size);
        Rectangle vThumb = this.getVThumbBounds(size);
        Rectangle vRail = this.getVScrollArea(size);

        this.hBarHovered = hThumb != null && hThumb.contains(event.x(), event.y());
        this.hRailHovered = hRail != null && hRail.contains(event.x(), event.y());
        this.vBarHovered = vThumb != null && vThumb.contains(event.x(), event.y());
        this.vRailHovered = vRail != null && vRail.contains(event.x(), event.y());

        if (this.hBarPressed && hThumb != null) {
            float contentWidth = this.childSize.width();
            float visibleWidth = this.visibleWidth(size);
            float maxScroll = Math.max(0, contentWidth - visibleWidth);
            float barWidth = hThumb.width();
            float scrollableWidth = hRail.width() - barWidth;
            float dragDelta = event.x() - this.dragStartX;
            this.targetScrollX = MathUtils.clamp(this.initialScrollX + (dragDelta / scrollableWidth) * maxScroll, 0, maxScroll);
            return true;
        } else if (this.vBarPressed && vThumb != null) {
            float contentHeight = this.childSize.height();
            float visibleHeight = this.visibleHeight(size);
            float maxScroll = Math.max(0, contentHeight - visibleHeight);
            float barHeight = vThumb.height();
            float scrollableHeight = vRail.height() - barHeight;
            float dragDelta = event.y() - this.dragStartY;
            this.targetScrollY = MathUtils.clamp(this.initialScrollY + (dragDelta / scrollableHeight) * maxScroll, 0, maxScroll);
            return true;
        }
        return super.onComponentMouseMove(event, size);
    }

    @Override
    protected boolean onComponentMouseScroll(final MouseScrollEvent event, final Size size) {
        return this.nestedScrollCoordinator.handleScrolling(
                () -> {
                    if (this.hScrollVisible && (event.scrollX() != 0 || (!this.vScrollVisible && event.scrollY() != 0)) && !this.hBarPressed) {
                        float contentWidth = this.childSize.width();
                        float visibleWidth = this.visibleWidth(size);
                        float maxScroll = Math.max(0, contentWidth - visibleWidth);
                        if (maxScroll > 0) {
                            float scrollAmount = event.scrollX() == 0 ? event.scrollY() : event.scrollX();
                            this.targetScrollX = MathUtils.clamp(this.targetScrollX - scrollAmount * this.scrollSpeed.value(), 0, maxScroll);
                            return true;
                        }
                    }
                    if (this.vScrollVisible && event.scrollY() != 0 && !this.vBarPressed) {
                        float contentHeight = this.childSize.height();
                        float visibleHeight = this.visibleHeight(size);
                        float maxScroll = Math.max(0, contentHeight - visibleHeight);
                        if (maxScroll > 0) {
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
    public void render(final Renderer renderer, final Size size) {
        this.updateAnimation();
        renderer.scissor(0, 0, this.visibleWidth(size), this.visibleHeight(size), () -> {
            renderer.translate(-this.scrollX, -this.scrollY, () -> {
                this.child.render(renderer, this.childSize);
            });
        });
        this.renderHorizontalScrollbar(renderer, size);
        this.renderVerticalScrollbar(renderer, size);
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
        if (oldScrollX != this.scrollX || oldScrollY != this.scrollY) {
            if (this.rivet() != null) {
                this.rivet().updateMouseState();
            }
            this.updateChildPositions();
            this.scrollListener.callVoid(c -> c.onScroll(this.scrollX, this.scrollY));
        }
    }

    private void renderHorizontalScrollbar(final Renderer renderer, final Size size) {
        Rectangle rail = this.getHScrollArea(size);
        if (rail == null) return;
        if (this.barType.value() == ScrollBarType.NORMAL) {
            this.renderRail(renderer, rail, this.hRailHovered, this.hRailPressed);
        }
        this.renderThumb(renderer, this.getHThumbBounds(size), this.hBarHovered, this.hBarPressed);
    }

    private void renderVerticalScrollbar(final Renderer renderer, final Size size) {
        Rectangle rail = this.getVScrollArea(size);
        if (rail == null) return;
        if (this.barType.value() == ScrollBarType.NORMAL) {
            this.renderRail(renderer, rail, this.vRailHovered, this.vRailPressed);
        }
        this.renderThumb(renderer, this.getVThumbBounds(size), this.vBarHovered, this.vBarPressed);
    }

    private void renderCorner(final Renderer renderer, final Size size) {
        if (this.barType.value() == ScrollBarType.NORMAL && this.vScrollVisible && this.hScrollVisible) {
            Color color = this.disabled() ? this.disabledRailColor.value() : this.railColor.value();
            float barWidth = this.barWidth.value();
            renderer.fillRect(size.width() - barWidth, size.height() - barWidth, barWidth, barWidth, color);
        }
    }

    private void renderRail(final Renderer renderer, final Rectangle bounds, final boolean hovered, final boolean pressed) {
        Color color = this.disabled() ? this.disabledRailColor.value() : this.railColor.value();
        renderer.fillRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), color);
        if (this.railOutlineWidth.value() > 0) {
            Color outlineColor = this.disabled() ? this.disabledRailOutlineColor.value() : this.railOutlineColor.value();
            renderer.outlineRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), this.railOutlineWidth.value(), outlineColor);
        }
    }

    private void renderThumb(final Renderer renderer, @Nullable final Rectangle bounds, final boolean hovered, final boolean pressed) {
        if (bounds == null) return;

        Color color;
        Color outlineColor;
        if (this.disabled()) {
            color = this.disabledBarColor.value();
            outlineColor = this.disabledBarOutlineColor.value();
        } else {
            if (pressed) color = this.barClickColor.value();
            else if (hovered) color = this.barHoverColor.value();
            else color = this.barColor.value();
            outlineColor = this.barOutlineColor.value();
        }

        renderer.optimizedFillRoundedRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), this.barCornerRadius.value(), color);
        if (this.barOutlineWidth.value() > 0) {
            renderer.optimizedOutlineRoundedRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), this.barCornerRadius.value(), this.barOutlineWidth.value(), outlineColor);
        }
    }

    private float visibleWidth(final Size size) {
        return size.width() - (this.vScrollVisible && this.barType.value() == ScrollBarType.NORMAL ? this.barWidth.value() : 0);
    }

    private float visibleHeight(final Size size) {
        return size.height() - (this.hScrollVisible && this.barType.value() == ScrollBarType.NORMAL ? this.barWidth.value() : 0);
    }

    @Nullable
    private Rectangle getHScrollArea(final Size size) {
        if (!this.hScrollVisible) return null;
        float barWidth = this.barWidth.value();
        return new Rectangle(0, size.height() - barWidth, size.width() - (this.vScrollVisible && this.barType.value() == ScrollBarType.NORMAL ? barWidth : 0), barWidth);
    }

    @Nullable
    private Rectangle getHThumbBounds(final Size size) {
        Rectangle rail = this.getHScrollArea(size);
        if (rail == null) return null;
        float contentWidth = this.childSize.width();
        float visibleWidth = this.visibleWidth(size);
        float thumbWidth = Math.max(20, (visibleWidth / contentWidth) * rail.width());
        float maxScroll = Math.max(1, contentWidth - visibleWidth);
        float scrollPercentage = this.scrollX / maxScroll;
        float thumbX = rail.x() + scrollPercentage * (rail.width() - thumbWidth);
        return new Rectangle(thumbX, rail.y(), thumbWidth, rail.height());
    }

    @Nullable
    private Rectangle getVScrollArea(final Size size) {
        if (!this.vScrollVisible) return null;
        float barWidth = this.barWidth.value();
        return new Rectangle(size.width() - barWidth, 0, barWidth, size.height() - (this.hScrollVisible && this.barType.value() == ScrollBarType.NORMAL ? barWidth : 0));
    }

    @Nullable
    private Rectangle getVThumbBounds(final Size size) {
        Rectangle rail = this.getVScrollArea(size);
        if (rail == null) return null;
        float contentHeight = this.childSize.height();
        float visibleHeight = this.visibleHeight(size);
        float thumbHeight = Math.max(20, (visibleHeight / contentHeight) * rail.height());
        float maxScroll = Math.max(1, contentHeight - visibleHeight);
        float scrollPercentage = this.scrollY / maxScroll;
        float thumbY = rail.y() + scrollPercentage * (rail.height() - thumbHeight);
        return new Rectangle(rail.x(), thumbY, rail.width(), thumbHeight);
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return this.child.computeIdealSize(new Size(
                this.horizontalScrolling ? Float.MAX_VALUE : constraints.width(),
                this.verticalScrolling ? Float.MAX_VALUE : constraints.height()
        ));
    }

    @Override
    public void computeLayout(final Size size) {
        float previousMaxScrollX = this.childSize.width() - this.visibleWidth(size);
        float previousMaxScrollY = this.childSize.height() - this.visibleHeight(size);
        float availableWidth = size.width();
        float availableHeight = size.height();
        Size idealChildSize = this.child.computeIdealSize(new Size(
                this.horizontalScrolling ? Float.MAX_VALUE : size.width(),
                this.verticalScrolling ? Float.MAX_VALUE : size.height()
        ));

        this.hScrollVisible = false;
        this.vScrollVisible = false;
        if (this.barType.value() == ScrollBarType.NORMAL) {
            this.hScrollVisible = this.horizontalScrolling && idealChildSize.width() > (this.vScrollVisible ? availableWidth - this.barWidth.value() : availableWidth);
            this.vScrollVisible = this.verticalScrolling && idealChildSize.height() > availableHeight;
            if (this.hScrollVisible && !this.vScrollVisible) {
                this.vScrollVisible = this.verticalScrolling && idealChildSize.height() > (availableHeight - this.barWidth.value());
            }
            if (this.vScrollVisible && !this.hScrollVisible) {
                this.hScrollVisible = this.horizontalScrolling && idealChildSize.width() > (availableWidth - this.barWidth.value());
            }

            if (this.hScrollVisible) availableHeight -= this.barWidth.value();
            if (this.vScrollVisible) availableWidth -= this.barWidth.value();
        }

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

        float oldScrollX = this.scrollX;
        float oldScrollY = this.scrollY;
        { // Horizontal scroll bar
            float contentWidth = childSize.width();
            if (this.barType.value() == ScrollBarType.FLOATING) {
                this.hScrollVisible = this.horizontalScrolling && contentWidth > availableWidth;
            }
            float maxScrollX = Math.max(0, contentWidth - availableWidth);
            this.targetScrollX = MathUtils.clamp(this.targetScrollX, 0, maxScrollX);
            this.scrollX = MathUtils.clamp(this.scrollX, 0, maxScrollX);
        }
        { // Vertical scroll bar
            float contentHeight = childSize.height();
            if (this.barType.value() == ScrollBarType.FLOATING) {
                this.vScrollVisible = this.verticalScrolling && contentHeight > availableHeight;
            }
            float maxScrollY = Math.max(0, contentHeight - availableHeight);
            if (this.autoScroll && previousMaxScrollY - this.targetScrollY <= availableHeight * this.autoScrollThreshold) {
                this.targetScrollY = maxScrollY;
            } else {
                this.targetScrollY = MathUtils.clamp(this.targetScrollY, 0, maxScrollY);
            }
            this.scrollY = MathUtils.clamp(this.scrollY, 0, maxScrollY);
        }
        if (oldScrollX != this.scrollX || oldScrollY != this.scrollY) {
            this.scrollListener.callVoid(c -> c.onScroll(this.scrollX, this.scrollY));
        }
        this.updateChildPositions();
    }

    @Override
    public Size contentSize() {
        return Size.EMPTY;
    }

    @Override
    public List<Component> children() {
        return List.of(this.child);
    }

    @Override
    public Rectangle childBounds(final Component component) {
        if (component == this.child) {
            return new Rectangle(-this.scrollX, -this.scrollY, this.childSize);
        }
        return Rectangle.EMPTY;
    }


    public enum ScrollBarType {
        FLOATING, NORMAL
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
            return new Rectangle(
                    -ScrollContainer.this.scrollX,
                    -ScrollContainer.this.scrollY,
                    ScrollContainer.this.childSize.width(),
                    ScrollContainer.this.childSize.height()
            );
        }

        @Override
        protected List<Component> elementsAt(final float x, final float y, final Size containerBounds) {
            if (x < 0 || x >= containerBounds.width() || y < 0 || y >= containerBounds.height()) return List.of();
            Rectangle hThumb = ScrollContainer.this.getHThumbBounds(containerBounds);
            Rectangle hRail = ScrollContainer.this.getHScrollArea(containerBounds);
            Rectangle vThumb = ScrollContainer.this.getVThumbBounds(containerBounds);
            Rectangle vRail = ScrollContainer.this.getVScrollArea(containerBounds);
            boolean componentHovered = (hThumb == null || !hThumb.contains(x, y))
                    && (hRail == null || !hRail.contains(x, y))
                    && (vThumb == null || !vThumb.contains(x, y))
                    && (vRail == null || !vRail.contains(x, y));
            return componentHovered ? List.of(ScrollContainer.this.child) : List.of();
        }
    }

}
