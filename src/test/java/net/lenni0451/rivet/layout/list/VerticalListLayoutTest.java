package net.lenni0451.rivet.layout.list;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VerticalListLayoutTest {

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
        final VerticalListLayout layout = VerticalListLayout.DEFAULT;
        final Size ideal = layout.computeIdealSize(new Size(400, 600), List.of());
        assertEquals(Size.EMPTY, ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(400, 600), List.of(), bounds::put);
        assertTrue(bounds.isEmpty());

        final Container container = new Container(layout);
        assertDoesNotThrow(() -> container.computeLayout(new Size(400, 600)));
        assertEquals(Size.EMPTY, container.contentSize());
    }

    @Test
    void testSingleComponent() {
        final VerticalListLayout layout = VerticalListLayout.DEFAULT;
        final Size containerSize = new Size(400, 600);
        final Component child = new TestComponent(60, 90);

        final Size ideal = layout.computeIdealSize(containerSize, List.of(child));
        assertEquals(new Size(60, 90), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);
        assertEquals(1, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 60, 90), bounds.get(child));

        final Container container = new Container(layout);
        container.add(child);
        container.computeLayout(containerSize);
        assertRectangleEquals(new Rectangle(0, 0, 60, 90), container.childBounds(child));
        assertEquals(new Size(60, 90), container.contentSize());
    }

    @Test
    void testMultipleComponentsWithoutGap() {
        final VerticalListLayout layout = new VerticalListLayout(0, false);
        final Size containerSize = new Size(500, 800);

        final Component c1 = new TestComponent(40, 50);
        final Component c2 = new TestComponent(70, 80);
        final Component c3 = new TestComponent(55, 120);
        final List<Component> list = List.of(c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(70, 250), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 40, 50), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 50, 70, 80), bounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 130, 55, 120), bounds.get(c3));

        final Container container = new Container(layout);
        container.add(c1).add(c2).add(c3);
        container.computeLayout(containerSize);
        assertEquals(new Size(70, 250), container.contentSize());
    }

    @Test
    void testMultipleComponentsWithGap() {
        final VerticalListLayout layout = new VerticalListLayout(15, false);
        final Size containerSize = new Size(500, 800);

        final Component c1 = new TestComponent(50, 60);
        final Component c2 = new TestComponent(80, 100);
        final Component c3 = new TestComponent(60, 80);
        final List<Component> list = List.of(c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(80, 270), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 50, 60), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 75, 80, 100), bounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 190, 60, 80), bounds.get(c3));

        final Container container = new Container(layout);
        container.add(c1).add(c2).add(c3);
        container.computeLayout(containerSize);
        assertEquals(new Size(80, 270), container.contentSize());
    }

    @Test
    void testFullWidthStretch() {
        final VerticalListLayout layout = new VerticalListLayout(10, true);
        final Size containerSize = new Size(300, 600);

        final Component c1 = new TestComponent(60, 70);
        final Component c2 = new TestComponent(80, 90);
        final List<Component> list = List.of(c1, c2);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(80, 170), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 300, 70), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 80, 300, 90), bounds.get(c2));
    }

    @Test
    void testFullWidthWithMinAndMaxConstraints() {
        final VerticalListLayout layout = new VerticalListLayout(0, true);
        final Size containerSize = new Size(250, 500);

        final Component c1 = new TestComponent(50, 100).maxSize(150, 200);
        final Component c2 = new TestComponent(50, 100).minSize(400, 50);
        final List<Component> list = List.of(c1, c2);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 150, 100), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 100, 400, 100), bounds.get(c2));
    }

    @Test
    void testPrimaryAxisMinAndMaxSizeClamping() {
        final VerticalListLayout layout = new VerticalListLayout(10, false);
        final Size containerSize = new Size(300, 500);

        final Component c1 = new TestComponent(40, 50).minSize(20, 80);
        final Component c2 = new TestComponent(60, 150).maxSize(80, 100);
        final List<Component> list = List.of(c1, c2);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(60, 190), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 40, 80), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 90, 60, 100), bounds.get(c2));
    }

    @Test
    void testConstrainedModeTruncation() {
        final VerticalListLayout layout = new VerticalListLayout(0, false, true);
        final Size containerSize = new Size(200, 180);

        final Component c1 = new TestComponent(50, 100);
        final Component c2 = new TestComponent(50, 120);
        final Component c3 = new TestComponent(50, 80);
        final List<Component> list = List.of(c1, c2, c3);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 50, 100), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 100, 50, 80), bounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 180, 50, 80), bounds.get(c3));
    }

    @Test
    void testConstrainedWithMinSizeClamping() {
        final VerticalListLayout layout = new VerticalListLayout(0, false, true);
        final Size containerSize = new Size(100, 150);

        final Component c1 = new TestComponent(40, 100);
        final Component c2 = new TestComponent(40, 100).minSize(20, 80);
        final List<Component> list = List.of(c1, c2);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 40, 100), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 100, 40, 80), bounds.get(c2));
    }

    @Test
    void testUnconstrainedOverflow() {
        final VerticalListLayout layout = new VerticalListLayout(10, false, false);
        final Size containerSize = new Size(100, 300);

        final Component c1 = new TestComponent(50, 200);
        final Component c2 = new TestComponent(50, 200);
        final Component c3 = new TestComponent(50, 200);
        final List<Component> list = List.of(c1, c2, c3);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 50, 200), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 210, 50, 200), bounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 420, 50, 200), bounds.get(c3));

        final Container container = new Container(layout);
        container.add(c1).add(c2).add(c3);
        assertDoesNotThrow(() -> container.computeLayout(containerSize));
        assertEquals(new Size(50, 620), container.contentSize());
    }

    @Test
    void testZeroHeightLeadComponentGapBehavior() {
        final VerticalListLayout layout = new VerticalListLayout(20, false);
        final Size containerSize = new Size(200, 400);

        final Component c0 = new TestComponent(50, 0);
        final Component c1 = new TestComponent(50, 80);
        final Component c2 = new TestComponent(50, 70);
        final List<Component> list = List.of(c0, c1, c2);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 50, 0), bounds.get(c0));
        assertRectangleEquals(new Rectangle(0, 0, 50, 80), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 100, 50, 70), bounds.get(c2));

        final Container container = new Container(layout);
        container.add(c0).add(c1).add(c2);
        container.computeLayout(containerSize);
        assertEquals(new Size(50, 170), container.contentSize());
    }

    @Test
    void testZeroContainerSize() {
        final VerticalListLayout standard = VerticalListLayout.DEFAULT;
        final VerticalListLayout fullWidth = VerticalListLayout.DEFAULT.withFullWidth(true);
        final Size containerSize = new Size(0, 0);

        final Component child = new TestComponent(50, 100);

        final Map<Component, Rectangle> standardBounds = new HashMap<>();
        standard.layoutComponents(containerSize, List.of(child), standardBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 50, 100), standardBounds.get(child));

        final Map<Component, Rectangle> fullWidthBounds = new HashMap<>();
        fullWidth.layoutComponents(containerSize, List.of(child), fullWidthBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 0, 100), fullWidthBounds.get(child));
    }

    @Test
    void testContainerContractCompliance() {
        final VerticalListLayout layout = new VerticalListLayout(10, true);
        final Container container = new Container(layout);

        final Component c1 = new TestComponent(30, 50);
        final Component c2 = new TestComponent(40, 60);
        final Component c3 = new TestComponent(20, 70);
        final Component c4 = new TestComponent(50, 80);
        container.add(c1).add(c2).add(c3).add(c4);

        final Size containerSize = new Size(250, 500);
        assertDoesNotThrow(() -> container.computeLayout(containerSize));

        assertRectangleEquals(new Rectangle(0, 0, 250, 50), container.childBounds(c1));
        assertRectangleEquals(new Rectangle(0, 60, 250, 60), container.childBounds(c2));
        assertRectangleEquals(new Rectangle(0, 130, 250, 70), container.childBounds(c3));
        assertRectangleEquals(new Rectangle(0, 210, 250, 80), container.childBounds(c4));
        assertEquals(new Size(250, 290), container.contentSize());
    }

    @Test
    void testRecordAndWitherMethods() {
        final VerticalListLayout defaultLayout = VerticalListLayout.DEFAULT;
        assertEquals(0, defaultLayout.gap());
        assertEquals(false, defaultLayout.fullWidth());
        assertEquals(false, defaultLayout.constrained());

        final VerticalListLayout noArg = new VerticalListLayout();
        assertEquals(defaultLayout, noArg);

        final VerticalListLayout twoArg = new VerticalListLayout(10, true);
        assertEquals(10, twoArg.gap());
        assertEquals(true, twoArg.fullWidth());
        assertEquals(false, twoArg.constrained());

        final VerticalListLayout threeArg = new VerticalListLayout(10, true, true);
        assertEquals(10, threeArg.gap());
        assertEquals(true, threeArg.fullWidth());
        assertEquals(true, threeArg.constrained());

        final VerticalListLayout withGap = threeArg.withGap(20);
        assertEquals(20, withGap.gap());
        assertEquals(true, withGap.fullWidth());
        assertEquals(true, withGap.constrained());

        final VerticalListLayout withFullWidth = threeArg.withFullWidth(false);
        assertEquals(10, withFullWidth.gap());
        assertEquals(false, withFullWidth.fullWidth());
        assertEquals(true, withFullWidth.constrained());

        final VerticalListLayout withConstrained = threeArg.withConstrained(false);
        assertEquals(10, withConstrained.gap());
        assertEquals(true, withConstrained.fullWidth());
        assertEquals(false, withConstrained.constrained());

        final VerticalListLayout withGapBy = threeArg.withGapBy(g -> g * 2);
        assertEquals(20, withGapBy.gap());

        final VerticalListLayout withFullWidthBy = threeArg.withFullWidthBy(b -> !b);
        assertEquals(false, withFullWidthBy.fullWidth());

        final VerticalListLayout withConstrainedBy = threeArg.withConstrainedBy(b -> !b);
        assertEquals(false, withConstrainedBy.constrained());

        assertEquals(threeArg, new VerticalListLayout(10, true, true));
        assertNotEquals(threeArg, withGap);
        assertEquals(threeArg.hashCode(), new VerticalListLayout(10, true, true).hashCode());
        assertTrue(threeArg.toString().contains("gap=10"));
    }

    private static class TestComponent extends Component {
        private final Size idealSize;

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
