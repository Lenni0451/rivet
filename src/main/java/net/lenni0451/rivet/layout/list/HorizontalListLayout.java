package net.lenni0451.rivet.layout.list;

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
public record HorizontalListLayout(int gap, boolean fullHeight, boolean constrained) implements Layout {

    public static final HorizontalListLayout DEFAULT = new HorizontalListLayout();

    public HorizontalListLayout() {
        this(0, false);
    }

    public HorizontalListLayout(final int gap, final boolean fullHeight) {
        this(gap, fullHeight, false);
    }

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        float width = 0;
        float height = 0;
        for (Component component : components) {
            Size idealSize = component.computeIdealSize(constraints);
            if (width > 0) width += this.gap;
            width += this.widthOf(component, idealSize);
            height = Math.max(height, this.heightOf(component, idealSize));
        }
        return new Size(width, height);
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        float x = 0;
        float leftoverWidth = containerSize.width();
        for (Component component : components) {
            Size idealSize = component.computeIdealSize(containerSize);
            float width = this.widthOf(component, idealSize);
            float height = this.fullHeight ? this.heightOf(component, containerSize.height()) : this.heightOf(component, idealSize);
            float gap = x > 0 ? this.gap : 0;
            if (this.constrained) {
                leftoverWidth -= gap;
                if (leftoverWidth > 0 && width > leftoverWidth) {
                    width = this.widthOf(component, leftoverWidth);
                }
                leftoverWidth -= width;
            }
            x += gap;
            setBounds.accept(component, new Rectangle(x, 0, width, height));
            x += width;
        }
    }

}
