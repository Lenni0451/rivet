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

    default void updateChildPositions() {
        Rectangle parentBounds = this.absoluteBounds();
        for (Component child : this.children()) {
            Rectangle relative = this.childBounds(child);
            Rectangle absolute = new Rectangle(
                    parentBounds.x() + relative.x(),
                    parentBounds.y() + relative.y(),
                    relative.width(),
                    relative.height()
            );
            child.updatePosition(absolute);
            if (child instanceof Parent childParent) {
                childParent.updateChildPositions();
            }
        }
    }

}
