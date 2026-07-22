package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.Sneaky;
import net.lenni0451.commons.animation.Animation;
import net.lenni0451.commons.animation.AnimationDirection;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.component.ParentContainer;
import net.lenni0451.rivet.component.impl.Arrow;
import net.lenni0451.rivet.input.mouse.ClickOn;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.layout.grid.GridAnchor;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridOptions;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.List;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class CollapsibleContainer extends ParentContainer {

    @Getter
    private final Component header;
    private final ClickableHeader clickableHeader;
    @Getter
    private final Component content;
    @Getter
    private boolean collapsed = true;
    @Getter
    private final ListenerList<Consumer<Boolean>> collapseChangeListener = new ListenerList<>();

    @Getter
    private final ThemeOption<Color> arrowColor = new ThemeOption<>(this, Theme.ARROW_COLOR);
    @Getter
    private final ThemeOption<Color> arrowDisabledColor = new ThemeOption<>(this, Theme.ARROW_DISABLED_COLOR);
    @Getter
    private final ThemeOption<Float> arrowLineWidth = new ThemeOption<>(this, Theme.ARROW_LINE_WIDTH);
    @Getter
    private final ThemeOption<Float> arrowSize = new ThemeOption<>(this, Theme.ARROW_SIZE);
    @Getter
    private final ThemeOption<ClickOn> clickOn = new ThemeOption<>(this, Theme.COLLAPSIBLE_CONTAINER_CLICK_ON);
    @Getter
    private final ThemeOption<AnimationConfig> collapseAnimationConfig = new ThemeOption<>(this, Theme.COLLAPSIBLE_CONTAINER_COLLAPSE_ANIMATION);

    private Animation collapseAnimation;
    private Size headerSize;
    private Size contentSize;

    public CollapsibleContainer(final Component header, final Component content) {
        this(header, h -> {}, content, c -> {});
    }

    public <H extends Component, C extends Component> CollapsibleContainer(final H header, final Consumer<H> headerInitializer, final C content, final Consumer<C> contentInitializer) {
        this.header = header;
        this.clickableHeader = new ClickableHeader(header);
        this.content = content;
        headerInitializer.accept(header);
        contentInitializer.accept(content);

        this.arrowSize.initListener().add(val -> this.requestLayoutRecalculation());
        this.collapseAnimationConfig.initListener().add(config -> {
            this.collapseAnimation = this.collapseAnimationConfig.value().create();
            if (this.collapsed) {
                this.collapseAnimation.finish(AnimationDirection.BACKWARDS);
            } else {
                this.collapseAnimation.finish(AnimationDirection.FORWARDS);
            }
        });
    }

    public final CollapsibleContainer collapsed(final boolean collapsed) {
        if (this.collapsed != collapsed) {
            this.collapsed = collapsed;
            if (collapsed) {
                this.mouseHandler().checkAndRemove(Sneaky.unsafeCast(this.content));
            }
            if (this.collapseAnimation != null) {
                this.collapseAnimation.runInDirection(collapsed ? AnimationDirection.BACKWARDS : AnimationDirection.FORWARDS);
            }
            this.collapseChangeListener.callVoid(l -> l.accept(collapsed));
            this.requestLayoutRecalculation();
        }
        return this;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        if (this.collapseAnimation.isRunning()) {
            this.requestLayoutRecalculation();
        }

        renderer.componentBounds(0, 0, this.headerSize.width(), this.headerSize.height(), () -> {
            this.clickableHeader.render(renderer, this.headerSize);
        });

        if (!this.collapsed || this.collapseAnimation.isRunning()) {
            float contentX = 0;
            float contentY = this.headerSize.height();
            float contentWidth = this.contentSize.width();
            float contentHeight = Math.min(this.contentSize.height(), size.height() - this.headerSize.height());
            renderer.translate(contentX, contentY, () -> {
                Runnable render = () -> {
                    this.content.render(renderer, new Size(contentWidth, contentHeight));
                };
                if (this.collapseAnimation.isRunning()) {
                    renderer.scissor(0, 0, contentWidth, contentHeight, render);
                } else {
                    renderer.componentBounds(0, 0, contentWidth, contentHeight, render);
                }
            });
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        Size idealHeaderSize = this.clickableHeader.computeIdealSize(constraints);
        if (this.collapsed && !this.collapseAnimation.isRunning()) {
            return idealHeaderSize;
        } else {
            Size contentConstraints = constraints.minus(0, idealHeaderSize.height());
            Size contentIdealSize = this.content.computeIdealSize(contentConstraints);
            return new Size(
                    Math.max(idealHeaderSize.width(), contentIdealSize.width()),
                    idealHeaderSize.height() + contentIdealSize.height() * this.collapseAnimation.getValue()
            );
        }
    }

    @Override
    public void computeLayout(final Size size) {
        Size idealHeaderSize = this.clickableHeader.computeIdealSize(size);
        this.headerSize = new Size(size.width(), idealHeaderSize.height());
        this.clickableHeader.computeLayout(this.headerSize);
        if (!this.collapsed || this.collapseAnimation.isRunning()) {
            float remainingHeight = Math.max(0, size.height() - idealHeaderSize.height());
            this.contentSize = new Size(size.width(), remainingHeight);
            this.content.computeLayout(this.contentSize);
        } else {
            this.contentSize = new Size(size.width(), 0);
        }
        this.updateChildPositions();
    }

    @Override
    public Size contentSize() {
        if (this.collapsed) {
            return this.headerSize;
        } else {
            return new Size(
                    this.headerSize.width(),
                    this.headerSize.height() + this.contentSize.height()
            );
        }
    }

    @Override
    public List<Component> children() {
        return List.of(this.clickableHeader, this.content);
    }

    @Override
    public Rectangle childBounds(final Component component) {
        if (component == this.clickableHeader) {
            return new Rectangle(this.headerSize);
        } else if (component == this.content) {
            return new Rectangle(0, this.headerSize.height(), this.contentSize);
        }
        return Rectangle.EMPTY;
    }

    private class ClickableHeader extends Container {
        private boolean hovered = false;

        private ClickableHeader(final Component header) {
            super(GridLayout.DEFAULT);

            Arrow arrow = new Arrow(() -> CollapsibleContainer.this.collapseAnimation.getValue());
            CollapsibleContainer.this.arrowColor.initListener().add(arrow.color()::set);
            CollapsibleContainer.this.arrowDisabledColor.initListener().add(arrow.disabledColor()::set);
            CollapsibleContainer.this.arrowLineWidth.initListener().add(arrow.lineWidth()::set);
            CollapsibleContainer.this.arrowSize.initListener().add(arrow.size()::set);

            this.addChild(arrow.layoutOptions(GridOptions.EMPTY.at(0, 0).withAnchor(GridAnchor.LEFT)));
            this.addChild(header.layoutOptions(GridOptions.EMPTY.at(1, 0).withWeightX(1).withFill(GridFill.HORIZONTAL)));
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
            if (!super.onComponentMouseDown(event, size)) {
                if (event.button().equals(MouseButton.LEFT)) {
                    if (CollapsibleContainer.this.clickOn.value().equals(ClickOn.DOWN) || CollapsibleContainer.this.clickOn.value().equals(ClickOn.BOTH)) {
                        CollapsibleContainer.this.collapsed(!CollapsibleContainer.this.collapsed);
                    }
                }
            }
            return true;
        }

        @Override
        protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
            if (!super.onComponentMouseUp(event, size)) {
                if (this.hovered && event.button().equals(MouseButton.LEFT)) {
                    if (CollapsibleContainer.this.clickOn.value().equals(ClickOn.UP) || CollapsibleContainer.this.clickOn.value().equals(ClickOn.BOTH)) {
                        CollapsibleContainer.this.collapsed(!CollapsibleContainer.this.collapsed);
                    }
                }
            }
            return true;
        }

        @Override
        protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
            super.onComponentMouseMove(event, size);
            return true;
        }
    }

}
