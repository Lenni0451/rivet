package net.lenni0451.rivet.layout.absolute;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AbsoluteLayoutTest {

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
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;
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
    void testSingleComponentWithoutOptions() {
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;
        final Component child = new TestComponent(100, 50);
        final List<Component> components = List.of(child);

        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);
        assertEquals(new Size(100, 50), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(800, 600), components, bounds::put);
        assertEquals(1, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 100, 50), bounds.get(child));
    }

    @Test
    void testSingleComponentWithCoordinatesAndDimensions() {
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;
        final Component child = new TestComponent(50, 30);
        child.layoutOptions(new AbsoluteOptions(20, 35, 120f, 80f));
        final List<Component> components = List.of(child);

        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);
        assertEquals(new Size(140, 115), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(500, 500), components, bounds::put);
        assertEquals(1, bounds.size());
        assertRectangleEquals(new Rectangle(20, 35, 120, 80), bounds.get(child));
    }

    @Test
    void testSingleComponentWithCoordinatesOnly() {
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;
        final Component child = new TestComponent(100, 60);
        child.layoutOptions(new AbsoluteOptions(40, 50));
        final List<Component> components = List.of(child);

        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);
        assertEquals(new Size(140, 110), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(500, 500), components, bounds::put);
        assertEquals(1, bounds.size());
        assertRectangleEquals(new Rectangle(40, 50, 100, 60), bounds.get(child));
    }

    @Test
    void testNegativeCoordinates() {
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;

        // Partially negative coordinates: (x + width > 0, y + height > 0)
        final Component child1 = new TestComponent(80, 70);
        child1.layoutOptions(new AbsoluteOptions(-20, -30, 80f, 70f));
        final Size ideal1 = layout.computeIdealSize(new Size(800, 600), List.of(child1));
        assertEquals(new Size(60, 40), ideal1);

        final Map<Component, Rectangle> bounds1 = new HashMap<>();
        layout.layoutComponents(new Size(500, 500), List.of(child1), bounds1::put);
        assertRectangleEquals(new Rectangle(-20, -30, 80, 70), bounds1.get(child1));

        // Strongly negative coordinates: (x + width < 0, y + height < 0)
        final Component child2 = new TestComponent(40, 30);
        child2.layoutOptions(new AbsoluteOptions(-100, -80, 40f, 30f));
        final Size ideal2 = layout.computeIdealSize(new Size(800, 600), List.of(child2));
        assertEquals(Size.EMPTY, ideal2);

        final Map<Component, Rectangle> bounds2 = new HashMap<>();
        layout.layoutComponents(new Size(500, 500), List.of(child2), bounds2::put);
        assertRectangleEquals(new Rectangle(-100, -80, 40, 30), bounds2.get(child2));
    }

    @Test
    void testNegativeDimensionsFillContainerInLayout() {
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;
        final Component child = new TestComponent(100, 60);
        child.layoutOptions(new AbsoluteOptions(15, 25, -1f, -1f));
        final List<Component> components = List.of(child);

        // computeIdealSize falls back to component ideal size when options width/height are < 0
        final Size ideal = layout.computeIdealSize(new Size(500, 400), components);
        assertEquals(new Size(115, 85), ideal);

        // layoutComponents fills container width & height when options width/height are < 0
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(500, 400), components, bounds::put);
        assertEquals(1, bounds.size());
        assertRectangleEquals(new Rectangle(15, 25, 500, 400), bounds.get(child));
    }

    @Test
    void testMixedNegativeAndExplicitDimensions() {
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;

        // Negative width, explicit height
        final Component child1 = new TestComponent(80, 50);
        child1.layoutOptions(new AbsoluteOptions(0, 10, -1f, 75f));
        final Size ideal1 = layout.computeIdealSize(new Size(600, 300), List.of(child1));
        assertEquals(new Size(80, 85), ideal1);

        final Map<Component, Rectangle> bounds1 = new HashMap<>();
        layout.layoutComponents(new Size(600, 300), List.of(child1), bounds1::put);
        assertRectangleEquals(new Rectangle(0, 10, 600, 75), bounds1.get(child1));

        // Explicit width, negative height
        final Component child2 = new TestComponent(80, 50);
        child2.layoutOptions(new AbsoluteOptions(20, 0, 150f, -1f));
        final Size ideal2 = layout.computeIdealSize(new Size(600, 300), List.of(child2));
        assertEquals(new Size(170, 50), ideal2);

        final Map<Component, Rectangle> bounds2 = new HashMap<>();
        layout.layoutComponents(new Size(600, 300), List.of(child2), bounds2::put);
        assertRectangleEquals(new Rectangle(20, 0, 150, 300), bounds2.get(child2));
    }

    @Test
    void testMinSizeClamping() {
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;
        final Component child = new TestComponent(30, 20);
        child.minSize(80, 60);
        child.layoutOptions(new AbsoluteOptions(10, 10, 40f, 30f));
        final List<Component> components = List.of(child);

        // Clamped to minSize: widthOf(child, 40) -> 80 + 10 = 90; heightOf(child, 30) -> 60 + 10 = 70
        final Size ideal = layout.computeIdealSize(new Size(500, 500), components);
        assertEquals(new Size(90, 70), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(500, 500), components, bounds::put);
        assertRectangleEquals(new Rectangle(10, 10, 80, 60), bounds.get(child));
    }

    @Test
    void testMaxSizeClamping() {
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;
        final Component child = new TestComponent(200, 200);
        child.maxSize(150, 120);
        child.layoutOptions(new AbsoluteOptions(5, 5, 300f, 250f));
        final List<Component> components = List.of(child);

        // Clamped to maxSize: widthOf(child, 300) -> 150 + 5 = 155; heightOf(child, 250) -> 120 + 5 = 125
        final Size ideal = layout.computeIdealSize(new Size(500, 500), components);
        assertEquals(new Size(155, 125), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(500, 500), components, bounds::put);
        assertRectangleEquals(new Rectangle(5, 5, 150, 120), bounds.get(child));

        // MaxSize clamping on container fill (-1f)
        final Component child2 = new TestComponent(50, 50);
        child2.maxSize(200, 150);
        child2.layoutOptions(new AbsoluteOptions(0, 0, -1f, -1f));
        final Map<Component, Rectangle> bounds2 = new HashMap<>();
        layout.layoutComponents(new Size(800, 600), List.of(child2), bounds2::put);
        assertRectangleEquals(new Rectangle(0, 0, 200, 150), bounds2.get(child2));
    }

    @Test
    void testMultipleComponentsBoundingBox() {
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;
        final Component c1 = new TestComponent(100, 50);
        c1.layoutOptions(new AbsoluteOptions(10, 20, 100f, 50f)); // right: 110, bottom: 70

        final Component c2 = new TestComponent(80, 60);
        c2.layoutOptions(new AbsoluteOptions(150, 40, 80f, 60f)); // right: 230, bottom: 100

        final Component c3 = new TestComponent(50, 120);
        c3.layoutOptions(new AbsoluteOptions(30, 180, 50f, 120f)); // right: 80, bottom: 300

        final List<Component> components = List.of(c1, c2, c3);
        final Size ideal = layout.computeIdealSize(new Size(500, 500), components);
        assertEquals(new Size(230, 300), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(500, 500), components, bounds::put);
        assertEquals(3, bounds.size());
        assertRectangleEquals(new Rectangle(10, 20, 100, 50), bounds.get(c1));
        assertRectangleEquals(new Rectangle(150, 40, 80, 60), bounds.get(c2));
        assertRectangleEquals(new Rectangle(30, 180, 50, 120), bounds.get(c3));
    }

    @Test
    void testMultipleOverlappingComponents() {
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;
        final Component c1 = new TestComponent(100, 100);
        c1.layoutOptions(new AbsoluteOptions(0, 0, 100f, 100f));

        final Component c2 = new TestComponent(100, 100);
        c2.layoutOptions(new AbsoluteOptions(50, 50, 100f, 100f));

        final List<Component> components = List.of(c1, c2);
        final Size ideal = layout.computeIdealSize(new Size(500, 500), components);
        assertEquals(new Size(150, 150), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(500, 500), components, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 100), bounds.get(c1));
        assertRectangleEquals(new Rectangle(50, 50, 100, 100), bounds.get(c2));
    }

    @Test
    void testZeroContainerSize() {
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;
        final Component child = new TestComponent(100, 60);
        child.layoutOptions(new AbsoluteOptions(10, 10, -1f, -1f));

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(Size.EMPTY, List.of(child), bounds::put);
        assertRectangleEquals(new Rectangle(10, 10, 0, 0), bounds.get(child));
    }

    @Test
    void testContainerContractCompliance() {
        final AbsoluteLayout layout = AbsoluteLayout.INSTANCE;
        final Container container = new Container(layout);

        final Component c1 = new TestComponent(100, 50);
        c1.layoutOptions(new AbsoluteOptions(10, 20, 100f, 50f));

        final Component c2 = new TestComponent(60, 40);
        c2.layoutOptions(new AbsoluteOptions(50, 80, 120f, 70f));

        container.add(c1);
        container.add(c2);

        assertDoesNotThrow(() -> container.computeLayout(new Size(500, 500)));
        assertRectangleEquals(new Rectangle(10, 20, 100, 50), container.childBounds(c1));
        assertRectangleEquals(new Rectangle(50, 80, 120, 70), container.childBounds(c2));
        assertEquals(new Size(170, 150), container.contentSize());
        assertEquals(new Size(170, 150), container.computeIdealSize(new Size(500, 500)));
    }

    @Test
    void testAbsoluteOptionsConstructorsAndFluentHelpers() {
        final AbsoluteOptions empty = AbsoluteOptions.EMPTY;
        assertEquals(0f, empty.x(), DELTA);
        assertEquals(0f, empty.y(), DELTA);
        assertNull(empty.width());
        assertNull(empty.height());

        final AbsoluteOptions fromPoint = new AbsoluteOptions(15f, 25f);
        assertEquals(15f, fromPoint.x(), DELTA);
        assertEquals(25f, fromPoint.y(), DELTA);
        assertNull(fromPoint.width());
        assertNull(fromPoint.height());

        final AbsoluteOptions fromRect = new AbsoluteOptions(new Rectangle(5, 10, 50, 60));
        assertEquals(5f, fromRect.x(), DELTA);
        assertEquals(10f, fromRect.y(), DELTA);
        assertEquals(50f, fromRect.width(), DELTA);
        assertEquals(60f, fromRect.height(), DELTA);

        final AbsoluteOptions configured = AbsoluteOptions.EMPTY.at(30, 40).sized(100, 200);
        assertEquals(30f, configured.x(), DELTA);
        assertEquals(40f, configured.y(), DELTA);
        assertEquals(100f, configured.width(), DELTA);
        assertEquals(200f, configured.height(), DELTA);

        final AbsoluteOptions modified = configured.withX(1).withY(2).withWidth(3f).withHeight(4f);
        assertEquals(1f, modified.x(), DELTA);
        assertEquals(2f, modified.y(), DELTA);
        assertEquals(3f, modified.width(), DELTA);
        assertEquals(4f, modified.height(), DELTA);
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
