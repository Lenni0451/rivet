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
public record VerticalListLayout(int gap, boolean fullWidth, boolean constrained) implements Layout {

    public static final VerticalListLayout DEFAULT = new VerticalListLayout();

    public VerticalListLayout() {
        this(0, false);
    }

    public VerticalListLayout(final int gap, final boolean fullWidth) {
        this(gap, fullWidth, false);
    }

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        float width = 0;
        float height = 0;
        for (Component component : components) {
            Size idealSize = component.computeIdealSize(constraints);
            width = Math.max(width, this.widthOf(component, idealSize));
            height += this.heightOf(component, idealSize);
        }
        if (!components.isEmpty()) {
            height += this.gap * (components.size() - 1);
        }
        return new Size(width, height);
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        float y = 0;
        float leftoverHeight = containerSize.height();
        for (Component component : components) {
            Size idealSize = component.computeIdealSize(containerSize);
            float width = this.fullWidth ? this.widthOf(component, containerSize.width()) : this.widthOf(component, idealSize);
            float height = this.heightOf(component, idealSize);
            if (this.constrained && leftoverHeight > 0) {
                if (height > leftoverHeight) {
                    height = leftoverHeight;
                }
                leftoverHeight -= height;
            }
            if (y != 0) y += this.gap;
            setBounds.accept(component, new Rectangle(0, y, width, height));
            y += this.heightOf(component, idealSize);
        }
    }

}
