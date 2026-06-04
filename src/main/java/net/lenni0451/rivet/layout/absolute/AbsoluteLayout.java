package net.lenni0451.rivet.layout.absolute;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collection;
import java.util.function.BiConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AbsoluteLayout implements Layout {

    public static final AbsoluteLayout INSTANCE = new AbsoluteLayout();

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        Size size = Size.EMPTY;
        for (Component component : components) {
            Size idealSize = component.computeIdealSize(constraints);
            float width = this.widthOf(component, idealSize);
            float height = this.heightOf(component, idealSize);
            if (component.layoutOptions() instanceof AbsoluteLayoutOptions options) {
                if (options.width() != null && options.width() >= 0) width = options.width();
                width += options.x();

                if (options.height() != null && options.height() >= 0) height = options.height();
                height += options.y();
            }
            size = size.max(width, height);
        }
        return size;
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        for (Component component : components) {
            Size idealSize = component.computeIdealSize(containerSize);
            float x = 0;
            float y = 0;
            float width = this.widthOf(component, idealSize);
            float height = this.heightOf(component, idealSize);
            if (component.layoutOptions() instanceof AbsoluteLayoutOptions options) {
                x = options.x();
                y = options.y();
                if (options.width() != null) {
                    if (options.width() >= 0) {
                        width = options.width();
                    } else {
                        width = containerSize.width();
                    }
                }
                if (options.height() != null) {
                    if (options.height() >= 0) {
                        height = options.height();
                    } else {
                        height = containerSize.height();
                    }
                }
            }
            setBounds.accept(component, new Rectangle(x, y, width, height));
        }
    }

}
