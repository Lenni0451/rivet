package net.lenni0451.rivet.component.container.tabcontainer;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

class TabLayout implements Layout {

    TabAlignment alignment = TabAlignment.LEFT;
    boolean sameSize = false;
    float verticalGap = 0;
    float tabGap = 0;

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        if (components.isEmpty()) return Size.EMPTY;

        float width = 0;
        float height = 0;
        if (this.sameSize) {
            Size maxSize = Size.EMPTY;
            for (Component component : components) {
                maxSize = maxSize.max(component.computeIdealSize(constraints));
            }
            width = maxSize.width() * components.size();
            height = maxSize.height();
        } else {
            for (Component component : components) {
                Size idealSize = component.computeIdealSize(constraints);
                width += idealSize.width();
                height = Math.max(height, idealSize.height());
            }
        }
        return new Size(
                width + this.tabGap * (components.size() - 1),
                height + this.verticalGap * 2
        );
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        if (components.isEmpty()) return;

        Map<Component, Size> idealSizes = new IdentityHashMap<>();
        for (Component component : components) {
            idealSizes.put(component, component.computeIdealSize(containerSize));
        }

        Size maxSize = null;
        if (this.sameSize) {
            maxSize = idealSizes.values().stream().reduce(Size.EMPTY, Size::max);
            maxSize = maxSize.withHeightBy(height -> Math.min(containerSize.height() - this.verticalGap * 2, height + this.verticalGap * 2));
        }
        float totalWidth = 0;
        for (Component component : components) {
            Size size = maxSize == null ? idealSizes.get(component) : maxSize;
            totalWidth += size.width() + this.tabGap;
        }
        totalWidth -= this.tabGap;

        float startX = switch (this.alignment) {
            case LEFT -> 0;
            case CENTER -> (containerSize.width() - totalWidth) / 2;
            case RIGHT -> containerSize.width() - totalWidth;
        };
        if (startX < 0) startX = 0;

        for (Component component : components) {
            Size size = maxSize == null ? idealSizes.get(component) : maxSize;
            setBounds.accept(component, new Rectangle(startX, this.verticalGap, size));
            startX += size.width() + this.tabGap;
        }
    }

}
