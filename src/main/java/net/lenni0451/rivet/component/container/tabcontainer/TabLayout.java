package net.lenni0451.rivet.component.container.tabcontainer;

import lombok.RequiredArgsConstructor;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
class TabLayout implements Layout {

    private final TabContainer tabContainer;

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        if (components.isEmpty()) return Size.EMPTY;

        float width = 0;
        float height = 0;
        if (this.tabContainer.tabSameSize().get()) {
            Size maxSize = Size.EMPTY;
            for (Component component : components) {
                Size idealSize = component.idealSize(constraints);
                maxSize = maxSize.max(new Size(this.widthOf(component, idealSize), this.heightOf(component, idealSize)));
            }
            width = maxSize.width() * components.size();
            height = maxSize.height();
        } else {
            for (Component component : components) {
                Size idealSize = component.idealSize(constraints);
                width += this.widthOf(component, idealSize);
                height = Math.max(height, this.heightOf(component, idealSize));
            }
        }
        return new Size(
                width + this.tabContainer.tabGap().get() * (components.size() - 1),
                height + this.tabContainer.tabVerticalGap().get() * 2
        );
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        if (components.isEmpty()) return;

        boolean sameSize = this.tabContainer.tabSameSize().get();
        float verticalGap = this.tabContainer.tabVerticalGap().get();
        float tabGap = this.tabContainer.tabGap().get();
        Map<Component, Size> idealSizes = new IdentityHashMap<>();
        Size maxSize = Size.EMPTY;
        for (Component component : components) {
            Size idealSize = this.sizeOf(component, component.idealSize(containerSize));
            idealSizes.put(component, idealSize);
            if (sameSize) {
                maxSize = maxSize.max(idealSize);
            }
        }

        if (sameSize) {
            maxSize = maxSize.withHeight(MathUtils.clamp(maxSize.height(), 0, containerSize.height() - verticalGap * 2));
        }
        float totalWidth = 0;
        for (Component component : components) {
            totalWidth += this.widthOf(component, sameSize ? maxSize.width() : idealSizes.get(component).width()) + tabGap;
        }
        totalWidth -= tabGap;

        float startX = switch (this.tabContainer.tabAlignment().get()) {
            case LEFT -> 0;
            case CENTER -> (containerSize.width() - totalWidth) / 2;
            case RIGHT -> containerSize.width() - totalWidth;
        };
        if (startX < 0) startX = 0;

        for (Component component : components) {
            float width = this.widthOf(component, sameSize ? maxSize.width() : idealSizes.get(component).width());
            float height = this.heightOf(component, sameSize ? maxSize.height() : idealSizes.get(component).height());
            setBounds.accept(component, new Rectangle(startX, verticalGap, width, height));
            startX += width + tabGap;
        }
    }

}
