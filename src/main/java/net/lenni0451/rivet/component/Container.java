package net.lenni0451.rivet.component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.input.ClickedElement;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseListener;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public class Container extends Component implements MouseListener, Renderable {

    @Getter
    private final Layout layout;
    private final List<Child> children = new ArrayList<>();
    private boolean mouseDown;
    private final ClickedElement<Component> clickedComponent = new ClickedElement<>();
    @Getter
    private Size contentSize = Size.EMPTY;

    public Container(final Rivet rivet, final Layout layout) {
        super(rivet);
        this.layout = layout;
    }

    public Container addChild(final Component component) {
        return this.addChild(component, c -> {});
    }

    public <E extends Component> Container addChild(final E component, final Consumer<E> initializer) {
        initializer.accept(component);
        this.removeChild(component);
        this.children.add(new Child(component));
        this.rivet.recalculateNextFrame();
        return this;
    }

    public List<Component> children() {
        return this.children.stream().map(c -> c.component).toList();
    }

    // Can return Bounds.EMPTY if the component isn't layouted yet or is not a child of this container
    public Rectangle childBounds(final Component component) {
        for (Child child : this.children) {
            if (child.component == component) {
                return child.bounds;
            }
        }
        return Rectangle.EMPTY;
    }

    public boolean removeChild(final Component component) {
        if (this.clickedComponent.is(component)) {
            this.clickedComponent.unset();
        }
        for (Iterator<Child> it = this.children.iterator(); it.hasNext(); ) {
            Child child = it.next();
            if (child.component == component) {
                if (this.rivet.focusedComponent() == component) {
                    this.rivet.focusedComponent(null);
                }
                it.remove();
                this.rivet.recalculateNextFrame();
                return true;
            }
        }
        return false;
    }

    public void clearChildren() {
        this.clickedComponent.unset();
        for (Child child : this.children) {
            if (this.rivet.focusedComponent() == child.component) {
                this.rivet.focusedComponent(null);
            }
        }
        this.children.clear();
        this.rivet.recalculateNextFrame();
    }

    public boolean intercepts(final float x, final float y) {
        return this.findChildAt(x, y) != null;
    }

    @Override
    public void onMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
        this.mouseDown = true;
        Child child = this.findChildAt(event.x(), event.y());
        if (child != null) {
            this.rivet.focusedComponent(child.component);
            if (child.component instanceof MouseListener mouseListener) {
                mouseListener.onMouseDown(event.withX(event.x() - child.bounds.x()).withY(event.y() - child.bounds.y()), child.bounds.add(bounds.x(), bounds.y()));
            }
            this.clickedComponent.down(child.component, event.button());
        }
    }

    @Override
    public void onMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
        for (Child child : this.children) {
            if (this.clickedComponent.is(child.component)) {
                if (child.component instanceof MouseListener mouseListener) {
                    mouseListener.onMouseUp(event.withX(event.x() - child.bounds.x()).withY(event.y() - child.bounds.y()), child.bounds.add(bounds.x(), bounds.y()));
                }
                this.clickedComponent.up(event.button());
            }
        }
        this.mouseDown = false;
    }

    @Override
    public void onMouseMove(final MouseMoveEvent event, final Rectangle bounds) {
        Child topChild = this.findChildAt(event.x(), event.y());
        for (Child child : this.children) {
            if (child.component instanceof MouseListener mouseListener) {
                boolean isTop = child == topChild;
                boolean isDragged = this.clickedComponent.is(child.component);

                if (isTop && (!this.mouseDown || isDragged)) {
                    if (!child.hovered) {
                        child.hovered = true;
                        mouseListener.onMouseEnter();
                    }
                } else {
                    if (child.hovered) {
                        child.hovered = false;
                        mouseListener.onMouseLeave();
                    }
                }

                if ((isTop && !this.mouseDown) || isDragged) {
                    mouseListener.onMouseMove(event.withX(event.x() - child.bounds.x()).withY(event.y() - child.bounds.y()), child.bounds.add(bounds.x(), bounds.y()));
                }
            }
        }
    }

    @Override
    public boolean onMouseScroll(final MouseScrollEvent event, final Rectangle bounds) {
        Child child = this.findChildAt(event.x(), event.y());
        if (child != null && child.component instanceof MouseListener mouseListener) {
            return mouseListener.onMouseScroll(event.withX(event.x() - child.bounds.x()).withY(event.y() - child.bounds.y()), child.bounds.add(bounds.x(), bounds.y()));
        }
        return false;
    }

    @Override
    public void render(final Renderer renderer, final Rectangle bounds) {
        for (Child child : this.children) {
            if (child.component instanceof Renderable renderable) {
                renderer.translate(child.bounds.x(), child.bounds.y(), () -> {
                    renderer.componentBounds(0, 0, child.bounds.width(), child.bounds.height(), () -> {
                        renderable.render(renderer, child.bounds.add(bounds.x(), bounds.y()));
                    });
                });
            }
        }
    }

    @Override
    public void computeIdealSize(final Size constraints) {
        for (Child child : this.children) {
            child.component.computeIdealSize(constraints);
        }
        this.idealSize = this.layout.computeIdealSize(constraints, this.children.stream().map(child -> child.component).toList());
    }

    @Override
    public void computeLayout(final Size size) {
        Map<Component, Rectangle> layout = this.layout.layoutComponents(size, this.children.stream().map(child -> child.component).toList());
        Size contentSize = Size.EMPTY;
        for (Child child : this.children) {
            Rectangle bounds = layout.get(child.component);
            if (bounds == null) throw new IllegalStateException("Layout did not provide bounds for all children!");

            child.bounds = bounds;
            child.component.computeLayout(bounds.size());
            contentSize = new Size(
                    Math.max(size.width(), bounds.x() + bounds.width()),
                    Math.max(size.height(), bounds.y() + bounds.height())
            );
        }
        this.contentSize = contentSize;
    }

    @Nullable
    private Child findChildAt(final float x, final float y) {
        for (int i = this.children.size() - 1; i >= 0; i--) {
            Child child = this.children.get(i);
            if (child.component.interactive() && child.bounds.contains(x, y)) {
                return child;
            }
        }
        return null;
    }


    @RequiredArgsConstructor
    private static final class Child {
        private final Component component;
        private Rectangle bounds = Rectangle.EMPTY;
        private boolean hovered = false;
    }

}
