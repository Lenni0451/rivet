package net.lenni0451.rivet.layout.fullsize;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FullSizeLayoutTest {

    private static final float DELTA = 1e-4f;

    private static void assertRectangleEquals(final Rectangle expected, final Rectangle actual, final float delta) {
        assertNotNull(actual, "Actual rectangle bounds must not be null");
        assertEquals(expected.x(), actual.x(), delta, "x mismatch");
        assertEquals(expected.y(), actual.y(), delta, "y mismatch");
        assertEquals(expected.width(), actual.width(), delta, "width mismatch");
        assertEquals(expected.height(), actual.height(), delta, "height mismatch");
    }

    private static void assertRectangleEquals(final Rectangle expected, final Rectangle actual) {
        assertRectangleEquals(expected, actual, DELTA);
    }

    @Test
    void testEmptyComponents() {
        final FullSizeLayout layout = FullSizeLayout.INSTANCE;
        final Size ideal = layout.computeIdealSize(new Size(800, 600), List.of());
        assertEquals(Size.EMPTY, ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(800, 600), List.of(), bounds::put);
        assertTrue(bounds.isEmpty());

        final Container container = new Container(layout);
        assertDoesNotThrow(() -> container.computeLayout(new Size(800, 600)));
        assertEquals(Size.EMPTY, container.contentSize());
    }

    @Test
    void testUnconstrainedFullExpansion() {
        final FullSizeLayout layout = FullSizeLayout.INSTANCE;
        final Component child = new TestComponent(100, 50);
        final Size containerSize = new Size(800, 600);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);

        assertEquals(1, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 800, 600), bounds.get(child));
    }

    @Test
    void testCenteringWithMaxSize() {
        final FullSizeLayout layout = FullSizeLayout.INSTANCE;
        final Component child = new TestComponent(100, 50).maxSize(new Size(400, 300));
        final Size containerSize = new Size(800, 600);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);

        // (800 - 400) / 2 = 200, (600 - 300) / 2 = 150
        assertRectangleEquals(new Rectangle(200, 150, 400, 300), bounds.get(child));
    }

    @Test
    void testCenteringOverflowWithMinSize() {
        final FullSizeLayout layout = FullSizeLayout.INSTANCE;
        final Component child = new TestComponent(100, 50).minSize(new Size(600, 500));
        final Size containerSize = new Size(400, 300);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);

        // (400 - 600) / 2 = -100, (300 - 500) / 2 = -100
        assertRectangleEquals(new Rectangle(-100, -100, 600, 500), bounds.get(child));
    }

    @Test
    void testSingleAxisConstraint() {
        final FullSizeLayout layout = FullSizeLayout.INSTANCE;
        final Size containerSize = new Size(800, 600);

        // Width constrained, height unconstrained
        final Component cWidthConstrained = new TestComponent(100, 50).maxSize(new Size(300, Float.MAX_VALUE));
        // Width unconstrained, height constrained
        final Component cHeightConstrained = new TestComponent(100, 50).maxSize(new Size(Float.MAX_VALUE, 200));

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(cWidthConstrained, cHeightConstrained), bounds::put);

        assertRectangleEquals(new Rectangle(250, 0, 300, 600), bounds.get(cWidthConstrained));
        assertRectangleEquals(new Rectangle(0, 200, 800, 200), bounds.get(cHeightConstrained));
    }

    @Test
    void testMultiChildOverlapAndCentering() {
        final FullSizeLayout layout = FullSizeLayout.INSTANCE;
        final Size containerSize = new Size(600, 400);

        final Component c1 = new TestComponent(10, 10);
        final Component c2 = new TestComponent(20, 20).maxSize(new Size(400, 200));
        final Component c3 = new TestComponent(30, 30).maxSize(new Size(200, 100));
        final Component c4 = new TestComponent(40, 40).minSize(new Size(800, 500));

        final List<Component> components = List.of(c1, c2, c3, c4);
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertEquals(4, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 600, 400), bounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 100, 400, 200), bounds.get(c2));
        assertRectangleEquals(new Rectangle(200, 150, 200, 100), bounds.get(c3));
        assertRectangleEquals(new Rectangle(-100, -50, 800, 500), bounds.get(c4));
    }

    @Test
    void testFractionalContainerDimensions() {
        final FullSizeLayout layout = FullSizeLayout.INSTANCE;
        final Component child = new TestComponent(10, 10).maxSize(new Size(50, 20));
        final Size containerSize = new Size(101, 51);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);

        // (101 - 50) / 2F = 25.5f, (51 - 20) / 2F = 15.5f
        assertRectangleEquals(new Rectangle(25.5f, 15.5f, 50, 20), bounds.get(child));
    }

    @Test
    void testZeroContainerSize() {
        final FullSizeLayout layout = FullSizeLayout.INSTANCE;
        final Component cUnconstrained = new TestComponent(50, 50);
        final Component cMin = new TestComponent(50, 50).minSize(new Size(100, 60));
        final Size containerSize = Size.EMPTY;

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(cUnconstrained, cMin), bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 0, 0), bounds.get(cUnconstrained));
        assertRectangleEquals(new Rectangle(-50, -30, 100, 60), bounds.get(cMin));
    }

    @Test
    void testComputeIdealSizeWithConstraints() {
        final FullSizeLayout layout = FullSizeLayout.INSTANCE;

        final Component c1 = new TestComponent(100, 50);
        final Component c2 = new TestComponent(150, 80).maxSize(new Size(120, 70));
        final Component c3 = new TestComponent(40, 30).minSize(new Size(60, 90));

        final Size ideal = layout.computeIdealSize(new Size(800, 600), List.of(c1, c2, c3));

        // c1: (100, 50)
        // c2: clamped to (120, 70)
        // c3: clamped to (60, 90)
        // max width = max(100, 120, 60) = 120
        // max height = max(50, 70, 90) = 90
        assertEquals(new Size(120, 90), ideal);
    }

    @Test
    void testContainerContract() {
        final FullSizeLayout layout = FullSizeLayout.INSTANCE;
        final Container container = new Container(layout);

        final Component c1 = new TestComponent(50, 50);
        final Component c2 = new TestComponent(100, 100).maxSize(new Size(80, 80));
        final Component c3 = new TestComponent(60, 40).minSize(new Size(600, 500));

        container.add(c1);
        container.add(c2);
        container.add(c3);

        assertDoesNotThrow(() -> container.computeLayout(new Size(500, 400)));
        assertRectangleEquals(new Rectangle(0, 0, 500, 400), container.childBounds(c1));
        assertRectangleEquals(new Rectangle(210, 160, 80, 80), container.childBounds(c2));
        assertRectangleEquals(new Rectangle(-50, -50, 600, 500), container.childBounds(c3));
        assertEquals(new Size(550, 450), container.contentSize());
        assertEquals(new Size(600, 500), container.computeIdealSize(new Size(500, 400)));
    }

    private static class TestComponent extends Component {
        private final Size idealSize;

        public TestComponent() {
            this(Size.EMPTY);
        }

        public TestComponent(final Size idealSize) {
            this.idealSize = idealSize;
        }

        public TestComponent(final float width, final float height) {
            this(new Size(width, height));
        }

        @Override
        public Size computeIdealSize(final Size constraints) {
            return this.idealSize;
        }
    }

}
