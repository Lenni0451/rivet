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
public class HorizontalFlowLayout implements Layout {

    private final int horizontalGap;
    private final int verticalGap;

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        float currentWidth = 0;
        float currentHeight = 0;
        float totalWidth = 0;
        float totalHeight = 0;
        for (Component component : components) {
            float xGap = currentWidth > 0 ? this.horizontalGap : 0;
            float componentWidth = this.widthOf(component);
            float componentHeight = this.heightOf(component);
            if (currentWidth > 0 && currentWidth + xGap + componentWidth > constraints.width()) {
                totalWidth = Math.max(totalWidth, currentWidth);
                currentWidth = 0;

                if (totalHeight > 0) totalHeight += this.verticalGap;
                totalHeight += currentHeight;
                currentHeight = 0;
            }
            currentWidth += xGap + componentWidth;
            currentHeight = Math.max(currentHeight, componentHeight);
        }
        return new Size(totalWidth, totalHeight);
    }

    @Override
    public Map<Component, Rectangle> layoutComponents(final Size containerSize, final Collection<Component> components) {
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
