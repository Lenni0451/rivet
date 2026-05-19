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
public class VerticalListLayout implements Layout {

    private final int gap;
    private final boolean fullWidth;

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        float width = 0;
        float height = 0;
        for (Component component : components) {
            width = Math.max(width, this.widthOf(component));
            height += this.heightOf(component);
        }
        if (!components.isEmpty()) {
            height += this.gap * (components.size() - 1);
        }
        return new Size(this.fullWidth ? constraints.width() : width, height);
    }

    @Override
    public Map<Component, Rectangle> layoutComponents(final Size containerSize, final Collection<Component> components) {
        Map<Component, Rectangle> layout = new IdentityHashMap<>();
        float y = 0;
        for (Component component : components) {
            float width = this.fullWidth ? this.widthOf(component, containerSize.width()) : this.widthOf(component);
            float height = this.heightOf(component);
            if (y != 0) y += this.gap;
            layout.put(component, new Rectangle(0, y, width, height));
            y += this.heightOf(component);
        }
        return layout;
    }

}
