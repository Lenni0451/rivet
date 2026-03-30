package net.lenni0451.rivet.component;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseListener;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Container extends Component implements MouseListener, Renderable {

    private final Layout layout;
    private final List<Child> children = new ArrayList<>();

    public Container(final Rivet rivet, final Layout layout) {
        super(rivet);
        this.layout = layout;
    }

    public Layout getLayout() {
        return this.layout;
    }

    public <T extends Component> T addChild(final T component) {
        this.removeChild(component);
        this.children.add(new Child(component));
        this.rivet.recalculateNextFrame();
        return component;
    }

    public void removeChild(final Component component) {
        for (Iterator<Child> it = this.children.iterator(); it.hasNext(); ) {
            Child child = it.next();
            if (child.component == component) {
                if (this.rivet.getFocused() == component) {
                    this.rivet.setFocused(null);
                }
                this.rivet.recalculateNextFrame();
                it.remove();
                break;
            }
        }
    }

    public void clearChildren() {
        for (Child child : this.children) {
            if (this.rivet.getFocused() == child.component) {
                this.rivet.setFocused(null);
            }
        }
        this.children.clear();
        this.rivet.recalculateNextFrame();
    }

    @Override
    public void onMouseDown(final MouseButtonEvent event) {
        for (Child child : this.children) {
            if (child.component instanceof MouseListener mouseListener) {
                if (child.bounds.contains(event.x(), event.y())) {
                    mouseListener.onMouseDown(event.withX(event.x() - child.bounds.x()).withY(event.y() - child.bounds.y()));
                    this.rivet.setFocused(child.component);
                }
            }
        }
    }

    @Override
    public void onMouseUp(final MouseButtonEvent event) {
        for (Child child : this.children) {
            if (child.component instanceof MouseListener mouseListener) {
                if (child.bounds.contains(event.x(), event.y())) {
                    mouseListener.onMouseUp(event.withX(event.x() - child.bounds.x()).withY(event.y() - child.bounds.y()));
                }
            }
        }
    }

    @Override
    public void onMouseMove(final MouseMoveEvent event) {
        for (Child child : this.children) {
            if (child.component instanceof MouseListener mouseListener) {
                if (child.bounds.contains(event.x(), event.y())) {
                    if (!child.hovered) {
                        child.hovered = true;
                        mouseListener.onMouseEnter();
                    }
                    mouseListener.onMouseMove(event.withX(event.x() - child.bounds.x()).withY(event.y() - child.bounds.y()));
                } else {
                    if (child.hovered) {
                        child.hovered = false;
                        mouseListener.onMouseLeave();
                    }
                }
            }
        }
    }

    @Override
    public void onMouseScroll(final MouseScrollEvent event) {
        for (Child child : this.children) {
            if (child.component instanceof MouseListener mouseListener) {
                if (child.bounds.contains(event.x(), event.y())) {
                    mouseListener.onMouseScroll(event.withX(event.x() - child.bounds.x()).withY(event.y() - child.bounds.y()));
                }
            }
        }
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        for (Child child : this.children) {
            if (child.component instanceof Renderable renderable) {
                renderer.push();
                renderer.translate(child.bounds.x(), child.bounds.y());
                renderer.pushScissor(0, 0, child.bounds.width(), child.bounds.height());
                renderable.render(renderer, child.bounds.size());
                renderer.popScissor();
                renderer.pop();
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
        for (Child child : this.children) {
            Rectangle bounds = layout.get(child.component);
            if (bounds == null) throw new IllegalStateException("Layout did not provide bounds for all children!");

            child.bounds = bounds;
            child.component.computeLayout(bounds.size());
        }
    }


    @RequiredArgsConstructor
    private static final class Child {
        private final Component component;
        private Rectangle bounds = Rectangle.EMPTY;
        private boolean hovered = false;
    }

}
