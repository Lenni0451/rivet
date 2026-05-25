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
        float width = 0;
        float height = 0;
        for (Component component : components) {
            Size idealSize = component.computeIdealSize(constraints);
            float componentWidth = this.widthOf(component, idealSize);
            float componentHeight = this.heightOf(component, idealSize);
            if (component.layoutOptions() instanceof AbsoluteLayoutOptions options) {
                if (options.width() != null) {
                    componentWidth = options.width();
                } else {
                    componentWidth += options.x();
                }
                if (options.height() != null) {
                    componentHeight = options.height();
                } else {
                    componentHeight += options.y();
                }
            }
            width = Math.max(width, componentWidth);
            height = Math.max(height, componentHeight);
        }
        return new Size(width, height);
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
                    width = options.width();
                }
                if (options.height() != null) {
                    height = options.height();
                }
            }
            setBounds.accept(component, new Rectangle(x, y, width, height));
        }
    }

}
