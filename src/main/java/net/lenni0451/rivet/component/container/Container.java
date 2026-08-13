package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.component.ParentContainer;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.event.listener.VoidListener;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.utils.ContainerMouseHandler;

import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public class Container extends ParentContainer {

    @Getter
    private final Layout layout;
    private final List<Child> children = new ArrayList<>();
    private final MouseHandler mouseHandler = new MouseHandler();
    @Getter
    private final ListenerList<VoidListener<Component>> addChildListener = new ListenerList<>();
    @Getter
    private final ListenerList<Consumer<Component>> removeChildListener = new ListenerList<>();
    @Getter
    private final ListenerList<Runnable> clearChildrenListener = new ListenerList<>();
    @Getter
    private final ListenerList<Runnable> childChangedListener = new ListenerList<>();
    @Getter
    private Size contentSize = Size.EMPTY;

    public final Container add(final Component component) {
        return this.add(component, c -> {});
    }

    public final <E extends Component> Container add(final E component, final Consumer<E> initializer) {
        initializer.accept(component);
        this.addChildListener.callVoid((l, c) -> l.accept(c, component), () -> {
            this.remove(component);
            this.children.add(new Child(component));
            if (this.rivet() != null) {
                component.setRivet(this.rivet(), this);
                this.requestLayoutRecalculation();
            }
            this.childChangedListener.call(Runnable::run);
        });
        return this;
    }

    public final Container sort(final Comparator<Component> comparator) {
        this.children.sort((child1, child2) -> comparator.compare(child1.component, child2.component));
        if (this.rivet() != null) this.rivet().recalculateNextFrame();
        return this;
    }

    @Override
    public final List<Component> children() {
        return this.children.stream().map(c -> c.component).toList();
    }

    // Can return Bounds.EMPTY if the component isn't layouted yet or is not a child of this container
    @Override
    public final Rectangle childBounds(final Component component) {
        for (Child child : this.children) {
            if (child.component == component) {
                return child.bounds;
            }
        }
        return Rectangle.EMPTY;
    }

    public final boolean remove(final Component component) {
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
                this.removeChildListener.call(l -> l.accept(component));
                this.childChangedListener.call(Runnable::run);
                return true;
            }
        }
        return false;
    }

    public final Container clear() {
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
        this.clearChildrenListener.call(Runnable::run);
        this.childChangedListener.call(Runnable::run);
        return this;
    }

    @Override
    protected final ContainerMouseHandler<?> mouseHandler() {
        return this.mouseHandler;
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size) {
        Size screenSize = this.rivet().scaledSize();
        for (Child child : this.children) {
            float childX = child.bounds.x() + renderer.xOffset();
            float childY = child.bounds.y() + renderer.yOffset();
            boolean skip = false;
            if ((childX < 0 && childX + child.bounds.width() < 0) || childX > screenSize.width()) {
                skip = true;
            }
            if ((childY < 0 && childY + child.bounds.height() < 0) || childY > screenSize.height()) {
                skip = true;
            }
            if (skip) {
                child.component.updatePosition(childX, childY, child.bounds.width(), child.bounds.height());
            } else {
                renderer.translate(child.bounds.x(), child.bounds.y(), () -> {
                    renderer.componentBounds(0, 0, child.bounds.width(), child.bounds.height(), () -> {
                        child.component.render(renderer, child.bounds.size());
                    });
                });
            }
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return this.layout.computeIdealSize(constraints, this.children.stream().map(child -> child.component).toList());
    }

    @Override
    public void computeLayout(final Size size) {
        Map<Component, Rectangle> newBounds = new IdentityHashMap<>();
        this.layout.layoutComponents(size, this.children.stream().map(child -> child.component).toList(), newBounds::put);
        if (newBounds.size() != this.children.size()) {
            throw new IllegalStateException("Layout '" + this.layout.getClass().getSimpleName() + "' did not provide bounds for all children (" + newBounds.size() + " provided, but " + this.children.size() + " expected)");
        }

        Size contentSize = Size.EMPTY;
        for (Child child : this.children) {
            Rectangle newChildBounds = newBounds.get(child.component);
            if (newChildBounds == null) {
                throw new IllegalStateException("Layout '" + this.layout.getClass().getSimpleName() + "' did not provide bounds for child '" + child.component.getClass().getSimpleName() + "'");
            }
            child.bounds = newChildBounds;
            child.component.computeLayout(child.bounds.size());
            float childWidth = child.bounds.width();
            float childHeight = child.bounds.height();
            if (child.component instanceof Parent parent) {
                Size parentContentSize = parent.contentSize();
                if (!parentContentSize.equals(Size.EMPTY)) {
                    childWidth = Math.max(childWidth, parentContentSize.width());
                    childHeight = Math.max(childHeight, parentContentSize.height());
                }
            }
            contentSize = contentSize.max(
                    child.bounds.x() + childWidth,
                    child.bounds.y() + childHeight
            );
        }
        this.contentSize = contentSize;
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
        protected Rectangle relativeBounds(final Size containerBounds, final Child element) {
            return element.bounds;
        }

        @Override
        protected List<Child> elementsAt(final float x, final float y, final Size containerBounds) {
            if (x < 0 || x >= containerBounds.width() || y < 0 || y >= containerBounds.height()) return List.of();
            List<Child> elements = new ArrayList<>();
            for (int i = Container.this.children.size() - 1; i >= 0; i--) {
                Child child = Container.this.children.get(i);
                if (child.bounds.contains(x, y)) {
                    elements.add(child);
                }
            }
            return elements;
        }
    }

}
