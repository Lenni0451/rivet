package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.dragdrop.DragOverEvent;
import net.lenni0451.rivet.dragdrop.DropEvent;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.math.Snapping;
import net.lenni0451.rivet.utils.ContainerMouseHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public class Container extends Component implements Parent {

    @Getter
    private final Layout layout;
    private final List<Child> children = new ArrayList<>();
    private final MouseHandler mouseHandler = new MouseHandler();
    @Getter
    private Size contentSize = Size.EMPTY;

    public final Container addChild(final Component component) {
        return this.addChild(component, c -> {});
    }

    public final <E extends Component> Container addChild(final E component, final Consumer<E> initializer) {
        initializer.accept(component);
        this.removeChild(component);
        this.children.add(new Child(component));
        if (this.rivet() != null) {
            component.setRivet(this.rivet(), this);
            this.requestLayoutRecalculation();
        }
        return this;
    }

    public final Container sortChildren(final Comparator<Component> comparator) {
        this.children.sort((child1, child2) -> comparator.compare(child1.component, child2.component));
        if (this.rivet() != null) this.rivet().recalculateNextFrame();
        return this;
    }

    @Override
    public final List<Component> children() {
        return this.children.stream().map(c -> c.component).toList();
    }

    // Can return Bounds.EMPTY if the component isn't layouted yet or is not a child of this container
    public final Rectangle childBounds(final Component component) {
        for (Child child : this.children) {
            if (child.component == component) {
                return child.bounds;
            }
        }
        return Rectangle.EMPTY;
    }

    public final boolean removeChild(final Component component) {
        for (Iterator<Child> it = this.children.iterator(); it.hasNext(); ) {
            Child child = it.next();
            if (child.component == component) {
                it.remove();
                this.mouseHandler.checkAndRemove(child);
                if (this.rivet() != null && this.rivet().focusedComponent() == component) {
                    this.rivet().focusedComponent(null);
                }
                if (this.rivet() != null) {
                    component.setRivet(null, null);
                    this.requestLayoutRecalculation();
                }
                return true;
            }
        }
        return false;
    }

    public final Container clearChildren() {
        this.mouseHandler.clear();
        if (this.rivet() != null) {
            for (Child child : this.children) {
                if (this.rivet().focusedComponent() == child.component) {
                    this.rivet().focusedComponent(null);
                }
                child.component.setRivet(null, null);
            }
        }
        this.children.clear();
        if (this.rivet() != null) {
            this.requestLayoutRecalculation();
        }
        return this;
    }

    public final boolean intercepts(final float x, final float y) {
        return this.mouseHandler.elementAt(x, y, null) != null; //TODO: Get rid of this
    }

    @Override
    protected void onComponentAdded() {
        for (Child child : this.children) {
            child.component.setRivet(this.rivet(), this);
        }
    }

    @Override
    protected void onComponentRemoved() {
        for (Child child : this.children) {
            child.component.setRivet(null, null);
        }
        this.mouseHandler.unsafeClear();
    }

    @Override
    protected void onComponentDisabled() {
        for (Child child : this.children) {
            child.component.disabled(true);
        }
        this.mouseHandler.unsafeClear();
    }

    @Override
    protected void onComponentEnabled() {
        for (Child child : this.children) {
            child.component.disabled(false);
        }
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
        this.children.forEach(c -> c.component.onThemeChanged());
    }

    @Override
    public void render(final Renderer renderer, final Rectangle bounds) {
        for (Child child : this.children) {
            renderer.translate(child.bounds.x(), child.bounds.y(), () -> {
                renderer.componentBounds(0, 0, child.bounds.width(), child.bounds.height(), () -> {
                    child.component.render(renderer, child.bounds.add(bounds.x(), bounds.y()));
                });
            });
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return this.layout.computeIdealSize(constraints, this.children.stream().map(child -> child.component).toList());
    }

    @Override
    public void computeLayout(final Size size) {
        Map<Component, Rectangle> newBounds = new IdentityHashMap<>();
        this.layout.layoutComponents(size, this.children.stream().map(child -> child.component).toList(), (component, bounds) -> {
            newBounds.put(component, Snapping.snap(this.rivet(), bounds));
        });
        if (newBounds.size() != this.children.size()) {
            throw new IllegalStateException("Layout '" + this.layout.getClass().getSimpleName() + "' did not provide bounds for all children (" + newBounds.size() + " provided, but " + this.children.size() + " expected)");
        }

        Size contentSize = Size.EMPTY;
        for (Child child : this.children) {
            child.bounds = newBounds.get(child.component);
            child.component.computeLayout(child.bounds.size());
            contentSize = contentSize.max(
                    child.bounds.x() + child.bounds.width(),
                    child.bounds.y() + child.bounds.height()
            );
        }
        this.contentSize = contentSize;
    }

    @Override
    public void requestLayoutRecalculation() {
        if (this.parent() != null) this.parent().requestLayoutRecalculation();
    }


    @RequiredArgsConstructor
    private static final class Child {
        private final Component component;
        private Rectangle bounds = Rectangle.EMPTY;
    }

    private class MouseHandler extends ContainerMouseHandler<Child> {
        @Override
        protected Component map(final Child element) {
            return element.component;
        }

        @Override
        protected Rectangle relativeBounds(final Rectangle containerBounds, final Child element) {
            return element.bounds;
        }

        @Override
        @Nullable
        protected Child elementAt(final float x, final float y, final Rectangle containerBounds) {
            for (int i = Container.this.children.size() - 1; i >= 0; i--) {
                Child child = Container.this.children.get(i);
                if (child.component.interactive() && child.bounds.contains(x, y)) {
                    return child;
                }
            }
            return null;
        }

        @Override
        protected List<Child> allElementsAt(final float x, final float y, final Rectangle containerBounds) {
            List<Child> elements = new ArrayList<>();
            for (int i = Container.this.children.size() - 1; i >= 0; i--) {
                Child child = Container.this.children.get(i);
                if (child.component.interactive() && child.bounds.contains(x, y)) {
                    elements.add(child);
                }
            }
            return elements;
        }
    }

}
