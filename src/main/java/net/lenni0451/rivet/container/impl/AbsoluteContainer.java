package net.lenni0451.rivet.container.impl;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.container.Container;
import net.lenni0451.rivet.math.Size;
import org.joml.Vector2f;
import org.joml.primitives.Rectanglef;

import java.util.IdentityHashMap;
import java.util.Map;

public class AbsoluteContainer extends Container {

    private final Map<Component, Rectanglef> childParameters = new IdentityHashMap<>();

    public void add(final Component component, final float x, final float y) {
        this.childParameters.put(component, new Rectanglef(x, y, Float.NaN, Float.NaN));
        this.addChild(component);
    }

    public void remove(final Component component) {
        this.childParameters.remove(component);
        this.removeChild(component);
    }

    public void setPosition(final Component component, final float x, final float y) {
        Rectanglef bounds = this.childParameters.get(component);
        if (bounds != null) {
            float width = bounds.lengthX();
            float height = bounds.lengthY();
            bounds.setMin(x, y).setMax(x + width, y + height);
            this.relayoutChildren();
        }
    }

    public void setSize(final Component component, final float width, final float height) {
        Rectanglef bounds = this.childParameters.get(component);
        if (bounds != null) {
            bounds.setMax(bounds.minX + width, bounds.minY + height);
            this.relayoutChildren();
        }
    }

    @Override
    protected void layoutChildren(final Vector2f size) {
        for (Component child : this.getChildren()) {
            Rectanglef parameter = this.childParameters.get(child);
            Size preferredSize = child.getActualPreferredSize();
            float width = Float.isNaN(parameter.maxX) ? preferredSize.width() : parameter.lengthX();
            float height = Float.isNaN(parameter.maxY) ? preferredSize.height() : parameter.lengthY();
            this.setChildBounds(child, parameter.minX, parameter.minY, width, height);
        }
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
