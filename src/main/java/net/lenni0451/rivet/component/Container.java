package net.lenni0451.rivet.component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.input.mouse.*;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.*;

@Accessors(fluent = true, chain = true)
public class Container extends Component implements MouseListener, Renderable {

    @Getter
    private final Layout layout;
    private final List<Child> children = new ArrayList<>();
    private boolean mouseDown;
    private final ClickedComponent clickedComponent = new ClickedComponent();
    @Getter
    private Size contentSize = Size.EMPTY;

    public Container(final Rivet rivet, final Layout layout) {
        super(rivet);
        this.layout = layout;
    }

    public <T extends Component> T addChild(final T component) {
        this.removeChild(component);
        this.children.add(new Child(component));
        this.rivet.recalculateNextFrame();
        return component;
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

    @Override
    public void onMouseDown(final MouseButtonEvent event, final Size size) {
        this.mouseDown = true;
        for (Child child : this.children) {
            if (child.component instanceof MouseListener mouseListener) {
                if (child.bounds.contains(event.x(), event.y())) {
                    mouseListener.onMouseDown(event.withX(event.x() - child.bounds.x()).withY(event.y() - child.bounds.y()), child.bounds.size());
                    this.clickedComponent.down(child.component, event.button());
                    this.rivet.focusedComponent(child.component);
                }
            }
        }
    }

    @Override
    public void onMouseUp(final MouseButtonEvent event, final Size size) {
        for (Child child : this.children) {
            if (child.component instanceof MouseListener mouseListener) {
                if (this.clickedComponent.is(child.component)) {
                    mouseListener.onMouseUp(event.withX(event.x() - child.bounds.x()).withY(event.y() - child.bounds.y()), child.bounds.size());
                    this.clickedComponent.up(event.button());
                }
            }
        }
        this.mouseDown = false;
    }

    @Override
    public void onMouseMove(final MouseMoveEvent event, final Size size) {
        for (Child child : this.children) {
            if (child.component instanceof MouseListener mouseListener) {
                if (child.bounds.contains(event.x(), event.y()) && (!this.mouseDown || this.clickedComponent.is(child.component))) {
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
                if ((child.bounds.contains(event.x(), event.y()) && !this.mouseDown) || this.clickedComponent.is(child.component)) {
                    mouseListener.onMouseMove(event.withX(event.x() - child.bounds.x()).withY(event.y() - child.bounds.y()), child.bounds.size());
                }
            }
        }
    }

    @Override
    public void onMouseScroll(final MouseScrollEvent event, final Size size) {
        for (Child child : this.children) {
            if (child.component instanceof MouseListener mouseListener) {
                if (child.bounds.contains(event.x(), event.y())) {
                    mouseListener.onMouseScroll(event.withX(event.x() - child.bounds.x()).withY(event.y() - child.bounds.y()), child.bounds.size());
                }
            }
        }
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        for (Child child : this.children) {
            if (child.component instanceof Renderable renderable) {
                renderer.translate(child.bounds.x(), child.bounds.y(), () -> {
                    renderer.scissor(0, 0, child.bounds.width(), child.bounds.height(), () -> {
                        renderable.render(renderer, child.bounds.size());
                    });
                });
            }
        }
    }

    @Override
    public void computeIdealSize() {
        for (Child child : this.children) {
            child.component.computeIdealSize();
        }
        this.idealSize = this.layout.computeIdealSize(this.children.stream().map(child -> child.component).toList());
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


    @RequiredArgsConstructor
    private static final class Child {
        private final Component component;
        private Rectangle bounds = Rectangle.EMPTY;
        private boolean hovered = false;
    }

    private static class ClickedComponent {
        private Component component;
        private final Set<MouseButton> buttons = new HashSet<>();

        public boolean is(final Component component) {
            return this.component == component;
        }

        public void unset() {
            this.component = null;
            this.buttons.clear();
        }

        public void down(final Component component, final MouseButton button) {
            if (this.component != component) {
                this.component = component;
                this.buttons.clear();
            }
            this.buttons.add(button);
        }

        public void up(final MouseButton button) {
            this.buttons.remove(button);
            if (this.buttons.isEmpty()) {
                this.component = null;
            }
        }
    }

}
