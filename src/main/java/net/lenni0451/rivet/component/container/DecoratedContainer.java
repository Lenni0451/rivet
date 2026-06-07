package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.dragdrop.DragOverEvent;
import net.lenni0451.rivet.dragdrop.DropEvent;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.utils.ContainerMouseHandler;

import java.util.List;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class DecoratedContainer extends Component implements Parent {

    @Getter
    private final Component background;
    @Getter
    private final Component child;
    private final MouseHandler mouseHandler = new MouseHandler();
    @Getter
    @Setter
    private Padding innerPadding;

    public DecoratedContainer(final Component background, final Component child) {
        this(background, c -> {}, child, c -> {});
    }

    public <B extends Component, C extends Component> DecoratedContainer(final B background, final Consumer<B> backgroundInitializer, final C child, final Consumer<C> childInitializer) {
        this.background = background;
        backgroundInitializer.accept(background);
        this.child = child;
        childInitializer.accept(child);

        this.innerPadding = Padding.EMPTY;
    }

    @Override
    protected void onComponentAdded() {
        this.background.setRivet(this.rivet(), this);
        this.child.setRivet(this.rivet(), this);
    }

    @Override
    protected void onComponentRemoved() {
        this.background.setRivet(null, null);
        this.child.setRivet(null, null);
        this.mouseHandler.unsafeClear();
    }

    @Override
    protected void onComponentDisabled() {
        this.background.disabled(true);
        this.child.disabled(true);
        this.mouseHandler.unsafeClear();
    }

    @Override
    protected void onComponentEnabled() {
        this.background.disabled(false);
        this.child.disabled(false);
    }

    @Override
    protected void onComponentMouseLeave() {
        this.mouseHandler.onMouseLeave();
    }

    @Override
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
        return this.mouseHandler.onMouseDown(this.rivet(), event, bounds).handled();
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
        return this.mouseHandler.onMouseUp(this.rivet(), event, bounds).handled();
    }

    @Override
    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Rectangle bounds) {
        return this.mouseHandler.onMouseMove(event, bounds).handled();
    }

    @Override
    protected boolean onComponentMouseScroll(final MouseScrollEvent event, final Rectangle bounds) {
        return this.mouseHandler.onMouseScroll(event, bounds).handled();
    }

    @Override
    protected boolean onComponentDrop(final DropEvent event, final Rectangle bounds) {
        return this.mouseHandler.onDrop(event, bounds).handled();
    }

    @Override
    protected boolean onComponentDragOver(final DragOverEvent event, final Rectangle bounds) {
        return this.mouseHandler.onDragOver(event, bounds).handled();
    }

    @Override
    protected void onComponentDragLeave() {
        this.mouseHandler.onDragLeave();
    }

    @Override
    public void onThemeChanged() {
        this.background.onThemeChanged();
        this.child.onThemeChanged();
    }

    @Override
    public void render(final Renderer renderer, final Rectangle bounds) {
        this.background.render(renderer, bounds);
        float width = bounds.width() - this.innerPadding.horizontal();
        float height = bounds.height() - this.innerPadding.vertical();
        renderer.translate(this.innerPadding.left(), this.innerPadding.top(), () -> {
            renderer.componentBounds(0, 0, width, height, () -> {
                this.child.render(renderer, new Rectangle(
                        bounds.x() + this.innerPadding.left(), bounds.y() + this.innerPadding.top(),
                        width, height
                ));
            });
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return this.child.computeIdealSize(constraints.minus(this.innerPadding.horizontal(), this.innerPadding.vertical()))
                .plus(this.innerPadding.horizontal(), this.innerPadding.vertical());
    }

    @Override
    public void computeLayout(final Size size) {
        this.child.computeLayout(size.minus(this.innerPadding.horizontal(), this.innerPadding.vertical()));
    }

    @Override
    public void requestLayoutRecalculation() {
        if (this.parent() != null) this.parent().requestLayoutRecalculation();
    }

    @Override
    public Size contentSize() {
        if (this.child instanceof Parent parent) {
            return parent.contentSize().plus(this.innerPadding.horizontal(), this.innerPadding.vertical());
        }
        return Size.EMPTY;
    }

    @Override
    public List<Component> children() {
        return List.of(this.background, this.child);
    }


    private class MouseHandler extends ContainerMouseHandler<Component> {
        @Override
        protected Component map(final Component element) {
            return element;
        }

        @Override
        protected Rectangle relativeBounds(final Rectangle containerBounds, final Component element) {
            if (element == DecoratedContainer.this.child) {
                Padding padding = DecoratedContainer.this.innerPadding;
                return new Rectangle(
                        padding.left(), padding.top(),
                        containerBounds.width() - padding.horizontal(), containerBounds.height() - padding.vertical()
                );
            } else {
                return new Rectangle(0, 0, containerBounds.width(), containerBounds.height());
            }
        }

        @Override
        protected List<Component> elementsAt(final float x, final float y, final Rectangle containerBounds) {
            Padding padding = DecoratedContainer.this.innerPadding;
            if (x >= padding.left() && x < containerBounds.width() - padding.right() && y >= padding.top() && y < containerBounds.height() - padding.bottom()) {
                return List.of(DecoratedContainer.this.child, DecoratedContainer.this.background);
            } else {
                return List.of(DecoratedContainer.this.background);
            }
        }
    }

}
