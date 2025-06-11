package net.lenni0451.rivet.container;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.MouseListener;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public abstract class Container extends Component implements MouseListener {

    private final Map<Component, Rectanglef> children = new IdentityHashMap<>();
    private final Set<Component> hoveredChildren = Collections.newSetFromMap(new IdentityHashMap<>());

    @Override
    public void onMouseEnter() {
    }

    @Override
    public void onMouseLeave() {
    }

    @Override
    public void onMouseDown(float mouseX, float mouseY, int button) {
        for (Map.Entry<Component, Rectanglef> entry : this.children.entrySet()) {
            Component child = entry.getKey();
            Rectanglef bounds = entry.getValue();
            if (child instanceof MouseListener mouseListener) {
                if (bounds.containsPoint(mouseX, mouseY)) {
                    mouseListener.onMouseDown(mouseX - bounds.minX, mouseY - bounds.minY, button);
                    this.rootContainer.setFocusedComponent(child);
                }
            }
        }
    }

    @Override
    public void onMouseUp(float mouseX, float mouseY, int button) {
        for (Map.Entry<Component, Rectanglef> entry : this.children.entrySet()) {
            Component child = entry.getKey();
            Rectanglef bounds = entry.getValue();
            if (child instanceof MouseListener mouseListener) {
                if (bounds.containsPoint(mouseX, mouseY)) {
                    mouseListener.onMouseUp(mouseX - bounds.minX, mouseY - bounds.minY, button);
                }
            }
        }
    }

    @Override
    public void onMouseMove(float mouseX, float mouseY) {
        Set<Component> freshlyHoveredChildren = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Map.Entry<Component, Rectanglef> entry : this.children.entrySet()) {
            Component child = entry.getKey();
            Rectanglef bounds = entry.getValue();
            if (child instanceof MouseListener mouseListener) {
                if (bounds.containsPoint(mouseX, mouseY)) {
                    if (!this.hoveredChildren.contains(mouseListener)) {
                        this.hoveredChildren.add(child);
                        freshlyHoveredChildren.add(child);
                        mouseListener.onMouseEnter();
                    }
                    mouseListener.onMouseMove(mouseX - bounds.minX, mouseY - bounds.minY);
                }
            }
        }
        this.hoveredChildren.removeIf(mouseListener -> {
            if (!freshlyHoveredChildren.contains(mouseListener)) {
                ((MouseListener) mouseListener).onMouseLeave();
                return true;
            }
            return false;
        });
    }

    protected void addChild(final Component child) {
        this.children.put(child, new Rectanglef());
        child.onAdded(this.rootContainer, this);
        this.layoutChildren(this.parent.getChildSize(this));
    }

    protected void removeChild(final Component child) {
        this.children.remove(child);
        if (this.hoveredChildren.remove(child)) {
            ((MouseListener) child).onMouseLeave();
        }
        this.layoutChildren(this.parent.getChildSize(this));
    }

    protected void clearChildren() {
        this.children.clear();
    }

    protected Rectanglef getChildBounds(final Component child) {
        return this.children.get(child);
    }

    protected Vector2f getChildPosition(final Component child) {
        Rectanglef bounds = this.children.get(child);
        if (bounds == null) return null;
        return new Vector2f(bounds.minX, bounds.minY);
    }

    protected Vector2f getChildSize(final Component child) {
        Rectanglef bounds = this.children.get(child);
        if (bounds == null) return null;
        return new Vector2f(bounds.lengthX(), bounds.lengthY());
    }

    protected void setChildBounds(final Component child, final float minX, final float minY, final float maxX, final float maxY) {
        Rectanglef bounds = this.children.get(child);
        if (bounds != null) {
            bounds.setMin(minX, minY).setMax(maxX, maxY);
        }
    }

    protected void setChildPosition(final Component child, final float x, final float y) {
        Rectanglef bounds = this.children.get(child);
        if (bounds != null) {
            float lengthX = bounds.lengthX();
            float lengthY = bounds.lengthY();
            bounds.minX = x;
            bounds.minY = y;
            bounds.maxX = x + lengthX;
            bounds.maxY = y + lengthY;
        }
    }

    protected void setChildSize(final Component child, final float width, final float height) {
        Rectanglef bounds = this.children.get(child);
        if (bounds != null) {
            bounds.maxX = bounds.minX + width;
            bounds.maxY = bounds.minY + height;
        }
    }

    protected abstract void layoutChildren(final Vector2f size);

}
