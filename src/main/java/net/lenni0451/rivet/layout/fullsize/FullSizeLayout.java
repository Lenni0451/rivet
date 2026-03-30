package net.lenni0451.rivet.layout.fullsize;

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
public class FullSizeLayout implements Layout {

    public static final FullSizeLayout INSTANCE = new FullSizeLayout();

    @Override
    public Size computeIdealSize(final Collection<Component> components) {
        float width = 0;
        float height = 0;
        for (Component component : components) {
            width = Math.max(width, component.idealSize().width());
            height = Math.max(height, component.idealSize().height());
        }
        return new Size(width, height);
    }

    @Override
    public Map<Component, Rectangle> layoutComponents(final Size containerSize, final Collection<Component> components) {
        Map<Component, Rectangle> map = new IdentityHashMap<>();
        for (Component component : components) {
            float width = Math.min(containerSize.width(), component.maxSize().width());
            float height = Math.min(containerSize.height(), component.maxSize().height());
            map.put(component, new Rectangle((containerSize.width() - width) / 2F, (containerSize.height() - height) / 2F, width, height));
        }
        return map;
    }

}
