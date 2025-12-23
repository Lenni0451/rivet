package net.lenni0451.rivet.layout.flow;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public class HorizontalFlowLayout implements Layout {

    private final int horizontalGap;
    private final int verticalGap;

    public HorizontalFlowLayout(final int horizontalGap, final int verticalGap) {
        this.horizontalGap = horizontalGap;
        this.verticalGap = verticalGap;
    }

    @Override
    public Size computeIdealSize(Collection<Component> components) {
        float width = 0;
        float height = 0;
        for (Component component : components) {
            width += this.widthOf(component);
            height = Math.max(height, this.heightOf(component));
        }
        width += (components.size() - 1) * this.horizontalGap;
        return new Size(width, height);
    }

    @Override
    public Map<Component, Rectangle> layoutComponents(Size containerSize, Collection<Component> components) {
        Map<Component, Rectangle> layout = new IdentityHashMap<>();
        float x = 0;
        float y = 0;
        float maxHeight = 0;
        for (Component component : components) {
            float width = this.widthOf(component);
            float height = this.heightOf(component);
            if (x + width > containerSize.width() && x > 0) {
                x = 0;
                y += maxHeight + this.verticalGap;
                maxHeight = height;
            }
            layout.put(component, new Rectangle(x, y, width, height));
            x += width + this.horizontalGap;
            maxHeight = Math.max(maxHeight, height);
        }
        return layout;
    }

}
