package net.lenni0451.rivet.component.container;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContainerTest {

    private static final Size SIZE = new Size(100, 100);

    @Test
    void testChildBoundsFollowChildren() {
        Container container = new Container(VerticalListLayout.DEFAULT);
        Component first = new FixedComponent();
        Component second = new FixedComponent();
        container.add(first).add(second);
        container.layout(SIZE);
        assertEquals(new Rectangle(0, 0, 10, 10), container.childBounds(first));
        assertEquals(new Rectangle(0, 10, 10, 10), container.childBounds(second));

        container.remove(first);
        assertEquals(Rectangle.EMPTY, container.childBounds(first));
        container.layout(SIZE);
        assertEquals(new Rectangle(0, 0, 10, 10), container.childBounds(second));

        container.add(first);
        container.layout(SIZE);
        assertEquals(List.of(second, first), container.children());
        assertEquals(new Rectangle(0, 10, 10, 10), container.childBounds(first));

        container.clear();
        assertEquals(Rectangle.EMPTY, container.childBounds(first));
        assertEquals(Rectangle.EMPTY, container.childBounds(second));
    }

    @Test
    void testReAddingMovesChildToTheEnd() {
        Container container = new Container(VerticalListLayout.DEFAULT);
        Component first = new FixedComponent();
        Component second = new FixedComponent();
        container.add(first).add(second).add(first);
        container.layout(SIZE);

        assertEquals(List.of(second, first), container.children());
        assertEquals(new Rectangle(0, 10, 10, 10), container.childBounds(first));
    }


    private static final class FixedComponent extends Component {
        @Override
        protected Size idealSizeInternal(final Size constraints) {
            return new Size(10, 10);
        }
    }

}
