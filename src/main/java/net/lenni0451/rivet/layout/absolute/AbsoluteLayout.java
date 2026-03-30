package net.lenni0451.rivet.layout.absolute;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AbsoluteLayout implements Layout {

    public static final AbsoluteLayout INSTANCE = new AbsoluteLayout();

    @Override
    public Size computeIdealSize(final Collection<Component> components) {
        float width = 0;
        float height = 0;
        for (Component component : components) {
            float componentWidth = this.widthOf(component);
            float componentHeight = this.heightOf(component);
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
    public Map<Component, Rectangle> layoutComponents(final Size containerSize, final Collection<Component> components) {
        Map<Component, Rectangle> layout = new IdentityHashMap<>();
        for (Component component : components) {
            float x = 0;
            float y = 0;
            float width = this.widthOf(component);
            float height = this.heightOf(component);
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
            layout.put(component, new Rectangle(x, y, width, height));
        }
        return layout;
    }

}
