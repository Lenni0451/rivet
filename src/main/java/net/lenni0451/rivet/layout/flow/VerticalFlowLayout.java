package net.lenni0451.rivet.layout.flow;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collection;
import java.util.function.BiConsumer;

@With
@WithBy
public record VerticalFlowLayout(int horizontalGap, int verticalGap) implements Layout {

    public static final VerticalFlowLayout DEFAULT = new VerticalFlowLayout();

    public VerticalFlowLayout() {
        this(0, 0);
    }

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        float x = 0;
        float y = 0;
        float maxWidth = 0;
        float totalWidth = 0;
        float totalHeight = 0;
        for (Component component : components) {
            Size idealSize = component.idealSize(constraints);
            float width = this.widthOf(component, idealSize);
            float height = this.heightOf(component, idealSize);
            if (y + height > constraints.height() && y > 0) {
                x += maxWidth + this.horizontalGap;
                y = 0;
                maxWidth = width;
            }
            totalWidth = Math.max(totalWidth, x + width);
            totalHeight = Math.max(totalHeight, y + height);
            y += height + this.verticalGap;
            maxWidth = Math.max(maxWidth, width);
        }
        return new Size(totalWidth, totalHeight);
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        float x = 0;
        float y = 0;
        float maxWidth = 0;
        for (Component component : components) {
            Size idealSize = component.idealSize(containerSize);
            float width = this.widthOf(component, idealSize);
            float height = this.heightOf(component, idealSize);
            if (y + height > containerSize.height() && y > 0) {
                x += maxWidth + this.horizontalGap;
                y = 0;
                maxWidth = width;
            }
            setBounds.accept(component, new Rectangle(x, y, width, height));
            y += height + this.verticalGap;
            maxWidth = Math.max(maxWidth, width);
        }
    }

}
