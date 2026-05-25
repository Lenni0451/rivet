package net.lenni0451.rivet.layout.fullsize;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collection;
import java.util.function.BiConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FullSizeLayout implements Layout {

    public static final FullSizeLayout INSTANCE = new FullSizeLayout();

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        float width = 0;
        float height = 0;
        for (Component component : components) {
            Size idealSize = component.computeIdealSize(constraints);
            width = Math.max(width, this.widthOf(component, idealSize));
            height = Math.max(height, this.heightOf(component, idealSize));
        }
        return new Size(
                Math.min(width, constraints.width()),
                Math.min(height, constraints.height())
        );
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        for (Component component : components) {
            float width = Math.min(containerSize.width(), component.maxSize().width());
            float height = Math.min(containerSize.height(), component.maxSize().height());
            setBounds.accept(component, new Rectangle((containerSize.width() - width) / 2F, (containerSize.height() - height) / 2F, width, height));
        }
    }

}
