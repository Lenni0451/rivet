package net.lenni0451.rivet.layout.list;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class HorizontalListLayout implements Layout {

    private final int gap;
    private final boolean fullHeight;

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        float width = 0;
        float height = 0;
        for (Component component : components) {
            width += this.widthOf(component);
            height = Math.max(height, this.heightOf(component));
        }
        if (!components.isEmpty()) {
            width += this.gap * (components.size() - 1);
        }
        return new Size(width, this.fullHeight ? constraints.height() : height);
    }

    @Override
    public Map<Component, Rectangle> layoutComponents(final Size containerSize, final Collection<Component> components) {
        Map<Component, Rectangle> layout = new IdentityHashMap<>();
        float x = 0;
        for (Component component : components) {
            float width = this.widthOf(component);
            float height = this.fullHeight ? this.heightOf(component, containerSize.height()) : this.heightOf(component);
            if (x > 0) x += this.gap;
            layout.put(component, new Rectangle(x, 0, width, height));
            x += width;
        }
        return layout;
    }

}
