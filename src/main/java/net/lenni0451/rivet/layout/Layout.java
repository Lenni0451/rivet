package net.lenni0451.rivet.layout;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collection;
import java.util.Map;

public interface Layout {

    Size computeIdealSize(final Collection<Component> components);

    Map<Component, Rectangle> layoutComponents(final Size containerSize, final Collection<Component> components);

    default float widthOf(final Component component) {
        return Math.max(component.minSize().width(), Math.min(component.idealSize().width(), component.maxSize().width()));
    }

    default float heightOf(final Component component) {
        return Math.max(component.minSize().height(), Math.min(component.idealSize().height(), component.maxSize().height()));
    }

}
