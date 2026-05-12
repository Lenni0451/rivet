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
public final class VerticalFlowLayout implements Layout {

    private final int horizontalGap;
    private final int verticalGap;

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        float currentWidth = 0;
        float currentHeight = 0;
        float totalWidth = 0;
        float totalHeight = 0;
        for (Component component : components) {
            float yGap = currentHeight > 0 ? this.verticalGap : 0;
            float componentWidth = this.widthOf(component);
            float componentHeight = this.heightOf(component);
            if (currentHeight > 0 && currentHeight + yGap + componentHeight > constraints.height()) {
                if (totalWidth > 0) totalWidth += this.horizontalGap;
                totalWidth += currentWidth;
                currentWidth = 0;

                totalHeight = Math.max(totalHeight, currentHeight);
                currentHeight = 0;
            }
            currentWidth = Math.max(currentWidth, componentWidth);
            currentHeight += yGap + componentHeight;
        }
        return new Size(totalWidth, totalHeight);
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
