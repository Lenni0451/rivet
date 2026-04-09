package net.lenni0451.rivet.layout.flow;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class VerticalFlowLayout implements Layout {

    private final int horizontalGap;
    private final int verticalGap;

    @Override
    public Size computeIdealSize(final Collection<Component> components) {
        float width = 0;
        float height = 0;
        for (Component component : components) {
            width = Math.max(width, this.widthOf(component));
            height += this.heightOf(component);
        }
        height += (components.size() - 1) * this.verticalGap;
        return new Size(width, height);
    }

    @Override
    public Map<Component, Rectangle> layoutComponents(final Size containerSize, final Collection<Component> components) {
        Map<Component, Rectangle> layout = new IdentityHashMap<>();
        float x = 0;
        float y = 0;
        float maxWidth = 0;
        for (Component component : components) {
            float width = this.widthOf(component);
            float height = this.heightOf(component);
            if (y + height > containerSize.height() && y > 0) {
                x += maxWidth + this.horizontalGap;
                y = 0;
                maxWidth = width;
            }
            layout.put(component, new Rectangle(x, y, width, height));
            y += height + this.verticalGap;
            maxWidth = Math.max(maxWidth, width);
        }
        return layout;
    }

}
