package net.lenni0451.rivet.container;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.MouseListener;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.math.impl.ExtendedVector2f;
import net.lenni0451.rivet.renderer.Renderer;
import org.joml.Matrix4fStack;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public abstract class Container extends Component implements Renderable, MouseListener {

    private final Map<Component, Rectanglef> children = new IdentityHashMap<>();
    private final Set<Component> hoveredChildren = Collections.newSetFromMap(new IdentityHashMap<>());

    @Override
    public void render(final Renderer renderer, final Matrix4fStack positionMatrix, final Size size) {
        for (Map.Entry<Component, Rectanglef> entry : this.children.entrySet()) {
            final Component child = entry.getKey();
            final Rectanglef bounds = entry.getValue();
            if (child instanceof Renderable renderable) {
                positionMatrix.pushMatrix();
                positionMatrix.translate(bounds.minX, bounds.minY, 0F);
                renderable.render(renderer, positionMatrix, new ExtendedVector2f(bounds.lengthX(), bounds.lengthY()));
                positionMatrix.popMatrix();
            }
        }
    }

    @Override
    public void onMouseDown(float mouseX, float mouseY, int button) {
        for (Map.Entry<Component, Rectanglef> entry : this.children.entrySet()) {
            Component child = entry.getKey();
            Rectanglef bounds = entry.getValue();
            if (child instanceof MouseListener mouseListener) {
                if (bounds.containsPoint(mouseX, mouseY)) {
                    mouseListener.onMouseDown(mouseX - bounds.minX, mouseY - bounds.minY, button);
                    this.rivet.setFocusedComponent(child);
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

    protected Set<Component> getChildren() {
        return Collections.unmodifiableSet(this.children.keySet());
    }

    protected void addChild(final Component child) {
        this.children.put(child, new Rectanglef());
        if (this.rivet != null) {
            child.onAdded(this.rivet, this);
            this.relayoutChildren();
        }
    }

    protected void removeChild(final Component child) {
        this.children.remove(child);
        if (this.hoveredChildren.remove(child)) {
            ((MouseListener) child).onMouseLeave();
        }
        if (this.rivet != null) {
            this.relayoutChildren();
        }
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

    protected void setChildBounds(final Component child, final float x, final float y, final float width, final float height) {
        Rectanglef bounds = this.children.get(child);
        if (bounds != null) {
            bounds.setMin(x, y).setMax(x + width, y + height);
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

    protected void relayoutChildren() {
        if (this.parent == null) {
            this.layoutChildren(this.rivet.getSize());
        } else {
            this.layoutChildren(this.parent.getChildSize(this));
        }
        this.computePreferredSize();
    }

    protected abstract void layoutChildren(final Vector2f size);

    @Override
    public void onAdded(Rivet rivet, Container parent) {
        super.onAdded(rivet, parent);
        for (Component child : this.children.keySet()) {
            child.onAdded(rivet, this);
        }
        this.relayoutChildren();
    }

    @Override
    protected void computePreferredSize() {
        if (this.rivet == null) return;
        this.computePreferredSize0();
        if (this.parent != null) {
            this.parent.relayoutChildren();
        }
    }

    protected abstract void computePreferredSize0();

}
