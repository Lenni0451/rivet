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
            this.triggerLayoutChange();
        }
    }

    public void setSize(final Component component, final float width, final float height) {
        Rectanglef bounds = this.childParameters.get(component);
        if (bounds != null) {
            bounds.setMax(bounds.minX + width, bounds.minY + height);
            this.triggerLayoutChange();
        }
    }

    @Override
    protected void computePreferredSize() {
        float maxWidth = 0;
        float maxHeight = 0;
        for (Map.Entry<Component, Rectanglef> entry : this.childParameters.entrySet()) {
            Size preferredSize = entry.getKey().getActualPreferredSize();
            if (Float.isNaN(entry.getValue().maxX)) {
                maxWidth = Math.max(maxWidth, entry.getValue().minX + preferredSize.width());
            } else {
                maxWidth = Math.max(maxWidth, entry.getValue().maxX);
            }
            if (Float.isNaN(entry.getValue().maxY)) {
                maxHeight = Math.max(maxHeight, entry.getValue().minY + preferredSize.height());
            } else {
                maxHeight = Math.max(maxHeight, entry.getValue().maxY);
            }
        }
        this.preferredSize.set(maxWidth, maxHeight);
    }

    @Override
    protected void computeLayout0(Vector2f size) {
        for (Map.Entry<Component, Rectanglef> entry : this.childParameters.entrySet()) {
            Size preferredSize = entry.getKey().getActualPreferredSize();
            Rectanglef bounds = entry.getValue();
            this.setChildBounds(entry.getKey(),
                    bounds.minX,
                    bounds.minY,
                    Float.isNaN(bounds.maxX) ? preferredSize.width() : bounds.lengthX(),
                    Float.isNaN(bounds.maxY) ? preferredSize.height() : bounds.lengthY()
            );
        }
    }

}
