package net.lenni0451.rivet.component;

import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.List;

public interface Parent {

    void requestLayoutRecalculation();

    Size contentSize();

    List<Component> children();

    Rectangle absoluteBounds();

    Rectangle childBounds(final Component component);

}
