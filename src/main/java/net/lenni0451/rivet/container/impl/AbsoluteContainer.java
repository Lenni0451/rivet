package net.lenni0451.rivet.container.impl;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.container.Container;
import net.lenni0451.rivet.math.Size;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;

public class AbsoluteContainer extends Container {

    public void add(final Component component, final float x, final float y) {
        this.addChild(component);
        this.setChildPosition(component, x, y);
        Size actualPreferredSize = component.getActualPreferredSize();
        this.setChildBounds(component, x, y, actualPreferredSize.width(), actualPreferredSize.height());
        this.computePreferredSize();
    }

    public void remove(final Component component) {
        this.removeChild(component);
        this.computePreferredSize();
    }

    public void setPosition(final Component component, final float x, final float y) {
        this.setChildPosition(component, x, y);
        this.computePreferredSize();
    }

    public void setSize(final Component component, final float width, final float height) {
        this.setChildSize(component, width, height);
        this.computePreferredSize();
    }

    @Override
    protected void layoutChildren(final Vector2f size) {
    }

    @Override
    public void computePreferredSize0() {
        float width = 0;
        float height = 0;
        for (Component child : this.getChildren()) {
            Rectanglef bounds = this.getChildBounds(child);
            width = Math.max(width, bounds.minX + bounds.maxX);
            height = Math.max(height, bounds.minY + bounds.maxY);
        }
        this.preferredSize.set(width, height);
    }

}
