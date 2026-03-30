package net.lenni0451.rivet.component;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseListener;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class Container extends Component implements MouseListener, Renderable {

    private final Layout layout;
    private final Map<Component, Rectangle> children = new IdentityHashMap<>();
    private final Set<Component> hoveredChildren = Collections.newSetFromMap(new IdentityHashMap<>());

    public Container(final Rivet rivet, final Layout layout) {
        super(rivet);
        this.layout = layout;
    }

    public Layout getLayout() {
        return this.layout;
    }

    public <T extends Component> T addChild(final T component) {
        this.children.put(component, Rectangle.EMPTY);
        this.rivet.recalculateNextFrame();
        return component;
    }

    public void removeChild(final Component component) {
        Rectangle bounds = this.children.remove(component);
        if (bounds != null) {
            this.hoveredChildren.remove(component);
            if (this.rivet.getFocused() == component) {
                this.rivet.setFocused(null);
            }
            this.rivet.recalculateNextFrame();
        }
    }

    public void clearChildren() {
        for (Component child : this.children.keySet()) {
            if (this.rivet.getFocused() == child) {
                this.rivet.setFocused(null);
            }
        }
        this.hoveredChildren.clear();
        this.children.clear();
        this.rivet.recalculateNextFrame();
    }

    @Override
    public void onMouseDown(final MouseButtonEvent event) {
        for (Map.Entry<Component, Rectangle> entry : this.children.entrySet()) {
            Component child = entry.getKey();
            Rectangle bounds = entry.getValue();
            if (child instanceof MouseListener mouseListener) {
                if (bounds.contains(event.x(), event.y())) {
                    mouseListener.onMouseDown(event.withX(event.x() - bounds.x()).withY(event.y() - bounds.y()));
                    this.rivet.setFocused(child);
                }
            }
        }
    }

    @Override
    public void onMouseUp(final MouseButtonEvent event) {
        for (Map.Entry<Component, Rectangle> entry : this.children.entrySet()) {
            Component child = entry.getKey();
            Rectangle bounds = entry.getValue();
            if (child instanceof MouseListener mouseListener) {
                if (bounds.contains(event.x(), event.y())) {
                    mouseListener.onMouseUp(event.withX(event.x() - bounds.x()).withY(event.y() - bounds.y()));
                }
            }
        }
    }

    @Override
    public void onMouseMove(final MouseMoveEvent event) {
        Set<Component> currentlyHovered = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Map.Entry<Component, Rectangle> entry : this.children.entrySet()) {
            Component child = entry.getKey();
            Rectangle bounds = entry.getValue();
            if (child instanceof MouseListener mouseListener) {
                if (bounds.contains(event.x(), event.y())) {
                    currentlyHovered.add(child);
                    if (!this.hoveredChildren.contains(child)) {
                        this.hoveredChildren.add(child);
                        mouseListener.onMouseEnter();
                    }
                    mouseListener.onMouseMove(event.withX(event.x() - bounds.x()).withY(event.y() - bounds.y()));
                }
            }
        }
        this.hoveredChildren.removeIf(child -> {
            if (!currentlyHovered.contains(child)) {
                ((MouseListener) child).onMouseLeave();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onMouseScroll(final MouseScrollEvent event) {
        for (Map.Entry<Component, Rectangle> entry : this.children.entrySet()) {
            Component child = entry.getKey();
            Rectangle bounds = entry.getValue();
            if (child instanceof MouseListener mouseListener) {
                if (bounds.contains(event.x(), event.y())) {
                    mouseListener.onMouseScroll(event.withX(event.x() - bounds.x()).withY(event.y() - bounds.y()));
                }
            }
        }
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        for (Map.Entry<Component, Rectangle> entry : this.children.entrySet()) {
            Component child = entry.getKey();
            Rectangle bounds = entry.getValue();
            if (child instanceof Renderable renderable) {
                renderer.push();
                renderer.translate(bounds.x(), bounds.y());
                renderer.pushScissor(0, 0, bounds.width(), bounds.height());
                renderable.render(renderer, bounds.size());
                renderer.popScissor();
                renderer.pop();
            }
        }
    }

    @Override
    public void computeIdealSize() {
        for (Component child : this.children.keySet()) {
            child.computeIdealSize();
        }
        this.idealSize = this.layout.computeIdealSize(this.children.keySet());
    }

    @Override
    public void computeLayout(final Size size) {
        Map<Component, Rectangle> layout = this.layout.layoutComponents(size, this.children.keySet());
        for (Map.Entry<Component, Rectangle> entry : this.children.entrySet()) {
            Rectangle bounds = layout.get(entry.getKey());
            if (bounds == null) throw new IllegalStateException("Layout did not provide bounds for all children!");

            entry.setValue(bounds);
            entry.getKey().computeLayout(bounds.size());
        }
    }

}
