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

class HorizontalListLayoutTest {

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
        final HorizontalListLayout layout = HorizontalListLayout.DEFAULT;
        final Size ideal = layout.computeIdealSize(new Size(500, 300), List.of());
        assertEquals(Size.EMPTY, ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(500, 300), List.of(), bounds::put);
        assertTrue(bounds.isEmpty());

        final Container container = new Container(layout);
        assertDoesNotThrow(() -> container.computeLayout(new Size(500, 300)));
        assertEquals(Size.EMPTY, container.contentSize());
    }

    @Test
    void testSingleComponent() {
        final HorizontalListLayout layout = HorizontalListLayout.DEFAULT;
        final Size containerSize = new Size(500, 300);
        final Component child = new TestComponent(80, 40);

        final Size ideal = layout.computeIdealSize(containerSize, List.of(child));
        assertEquals(new Size(80, 40), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);
        assertEquals(1, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 80, 40), bounds.get(child));

        final Container container = new Container(layout);
        container.add(child);
        container.computeLayout(containerSize);
        assertRectangleEquals(new Rectangle(0, 0, 80, 40), container.childBounds(child));
        assertEquals(new Size(80, 40), container.contentSize());
    }

    @Test
    void testMultipleComponentsWithoutGap() {
        final HorizontalListLayout layout = new HorizontalListLayout(0, false);
        final Size containerSize = new Size(600, 400);

        final Component c1 = new TestComponent(50, 30);
        final Component c2 = new TestComponent(80, 60);
        final Component c3 = new TestComponent(120, 45);
        final List<Component> list = List.of(c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(250, 60), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 50, 30), bounds.get(c1));
        assertRectangleEquals(new Rectangle(50, 0, 80, 60), bounds.get(c2));
        assertRectangleEquals(new Rectangle(130, 0, 120, 45), bounds.get(c3));

        final Container container = new Container(layout);
        container.add(c1).add(c2).add(c3);
        container.computeLayout(containerSize);
        assertEquals(new Size(250, 60), container.contentSize());
    }

    @Test
    void testMultipleComponentsWithGap() {
        final HorizontalListLayout layout = new HorizontalListLayout(15, false);
        final Size containerSize = new Size(600, 400);

        final Component c1 = new TestComponent(60, 40);
        final Component c2 = new TestComponent(100, 50);
        final Component c3 = new TestComponent(80, 30);
        final List<Component> list = List.of(c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(270, 50), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 60, 40), bounds.get(c1));
        assertRectangleEquals(new Rectangle(75, 0, 100, 50), bounds.get(c2));
        assertRectangleEquals(new Rectangle(190, 0, 80, 30), bounds.get(c3));

        final Container container = new Container(layout);
        container.add(c1).add(c2).add(c3);
        container.computeLayout(containerSize);
        assertEquals(new Size(270, 50), container.contentSize());
    }

    @Test
    void testFullHeightStretch() {
        final HorizontalListLayout layout = new HorizontalListLayout(10, true);
        final Size containerSize = new Size(500, 200);

        final Component c1 = new TestComponent(70, 30);
        final Component c2 = new TestComponent(90, 50);
        final List<Component> list = List.of(c1, c2);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(170, 50), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 70, 200), bounds.get(c1));
        assertRectangleEquals(new Rectangle(80, 0, 90, 200), bounds.get(c2));
    }

    @Test
    void testFullHeightWithMinAndMaxConstraints() {
        final HorizontalListLayout layout = new HorizontalListLayout(0, true);
        final Size containerSize = new Size(500, 200);

        final Component c1 = new TestComponent(100, 50).maxSize(200, 120);
        final Component c2 = new TestComponent(100, 50).minSize(50, 350);
        final List<Component> list = List.of(c1, c2);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 120), bounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 0, 100, 350), bounds.get(c2));
    }

    @Test
    void testPrimaryAxisMinAndMaxSizeClamping() {
        final HorizontalListLayout layout = new HorizontalListLayout(10, false);
        final Size containerSize = new Size(500, 300);

        final Component c1 = new TestComponent(50, 40).minSize(80, 20);
        final Component c2 = new TestComponent(150, 60).maxSize(100, 80);
        final List<Component> list = List.of(c1, c2);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(190, 60), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 80, 40), bounds.get(c1));
        assertRectangleEquals(new Rectangle(90, 0, 100, 60), bounds.get(c2));
    }

    @Test
    void testConstrainedModeTruncation() {
        final HorizontalListLayout layout = new HorizontalListLayout(0, false, true);
        final Size containerSize = new Size(180, 200);

        final Component c1 = new TestComponent(100, 50);
        final Component c2 = new TestComponent(120, 50);
        final Component c3 = new TestComponent(80, 50);
        final List<Component> list = List.of(c1, c2, c3);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 50), bounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 0, 80, 50), bounds.get(c2));
        assertRectangleEquals(new Rectangle(180, 0, 80, 50), bounds.get(c3));
    }

    @Test
    void testConstrainedWithMinSizeClamping() {
        final HorizontalListLayout layout = new HorizontalListLayout(0, false, true);
        final Size containerSize = new Size(150, 100);

        final Component c1 = new TestComponent(100, 40);
        final Component c2 = new TestComponent(100, 40).minSize(80, 20);
        final List<Component> list = List.of(c1, c2);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 40), bounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 0, 80, 40), bounds.get(c2));
    }

    @Test
    void testUnconstrainedOverflow() {
        final HorizontalListLayout layout = new HorizontalListLayout(10, false, false);
        final Size containerSize = new Size(300, 100);

        final Component c1 = new TestComponent(200, 50);
        final Component c2 = new TestComponent(200, 50);
        final Component c3 = new TestComponent(200, 50);
        final List<Component> list = List.of(c1, c2, c3);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 200, 50), bounds.get(c1));
        assertRectangleEquals(new Rectangle(210, 0, 200, 50), bounds.get(c2));
        assertRectangleEquals(new Rectangle(420, 0, 200, 50), bounds.get(c3));

        final Container container = new Container(layout);
        container.add(c1).add(c2).add(c3);
        assertDoesNotThrow(() -> container.computeLayout(containerSize));
        assertEquals(new Size(620, 50), container.contentSize());
    }

    @Test
    void testZeroWidthLeadComponentGapBehavior() {
        final HorizontalListLayout layout = new HorizontalListLayout(20, false);
        final Size containerSize = new Size(400, 200);

        final Component c0 = new TestComponent(0, 50);
        final Component c1 = new TestComponent(80, 50);
        final Component c2 = new TestComponent(70, 50);
        final List<Component> list = List.of(c0, c1, c2);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 0, 50), bounds.get(c0));
        assertRectangleEquals(new Rectangle(0, 0, 80, 50), bounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 0, 70, 50), bounds.get(c2));

        final Container container = new Container(layout);
        container.add(c0).add(c1).add(c2);
        container.computeLayout(containerSize);
        assertEquals(new Size(170, 50), container.contentSize());
    }

    @Test
    void testZeroContainerSize() {
        final HorizontalListLayout standard = HorizontalListLayout.DEFAULT;
        final HorizontalListLayout fullHeight = HorizontalListLayout.DEFAULT.withFullHeight(true);
        final Size containerSize = new Size(0, 0);

        final Component child = new TestComponent(100, 50);

        final Map<Component, Rectangle> standardBounds = new HashMap<>();
        standard.layoutComponents(containerSize, List.of(child), standardBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 50), standardBounds.get(child));

        final Map<Component, Rectangle> fullHeightBounds = new HashMap<>();
        fullHeight.layoutComponents(containerSize, List.of(child), fullHeightBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 0), fullHeightBounds.get(child));
    }

    @Test
    void testContainerContractCompliance() {
        final HorizontalListLayout layout = new HorizontalListLayout(10, true);
        final Container container = new Container(layout);

        final Component c1 = new TestComponent(50, 30);
        final Component c2 = new TestComponent(60, 40);
        final Component c3 = new TestComponent(70, 20);
        final Component c4 = new TestComponent(80, 50);
        container.add(c1).add(c2).add(c3).add(c4);

        final Size containerSize = new Size(500, 250);
        assertDoesNotThrow(() -> container.computeLayout(containerSize));

        assertRectangleEquals(new Rectangle(0, 0, 50, 250), container.childBounds(c1));
        assertRectangleEquals(new Rectangle(60, 0, 60, 250), container.childBounds(c2));
        assertRectangleEquals(new Rectangle(130, 0, 70, 250), container.childBounds(c3));
        assertRectangleEquals(new Rectangle(210, 0, 80, 250), container.childBounds(c4));
        assertEquals(new Size(290, 250), container.contentSize());
    }

    @Test
    void testRecordAndWitherMethods() {
        final HorizontalListLayout defaultLayout = HorizontalListLayout.DEFAULT;
        assertEquals(0, defaultLayout.gap());
        assertEquals(false, defaultLayout.fullHeight());
        assertEquals(false, defaultLayout.constrained());

        final HorizontalListLayout noArg = new HorizontalListLayout();
        assertEquals(defaultLayout, noArg);

        final HorizontalListLayout twoArg = new HorizontalListLayout(10, true);
        assertEquals(10, twoArg.gap());
        assertEquals(true, twoArg.fullHeight());
        assertEquals(false, twoArg.constrained());

        final HorizontalListLayout threeArg = new HorizontalListLayout(10, true, true);
        assertEquals(10, threeArg.gap());
        assertEquals(true, threeArg.fullHeight());
        assertEquals(true, threeArg.constrained());

        final HorizontalListLayout withGap = threeArg.withGap(20);
        assertEquals(20, withGap.gap());
        assertEquals(true, withGap.fullHeight());
        assertEquals(true, withGap.constrained());

        final HorizontalListLayout withFullHeight = threeArg.withFullHeight(false);
        assertEquals(10, withFullHeight.gap());
        assertEquals(false, withFullHeight.fullHeight());
        assertEquals(true, withFullHeight.constrained());

        final HorizontalListLayout withConstrained = threeArg.withConstrained(false);
        assertEquals(10, withConstrained.gap());
        assertEquals(true, withConstrained.fullHeight());
        assertEquals(false, withConstrained.constrained());

        final HorizontalListLayout withGapBy = threeArg.withGapBy(g -> g * 2);
        assertEquals(20, withGapBy.gap());

        final HorizontalListLayout withFullHeightBy = threeArg.withFullHeightBy(b -> !b);
        assertEquals(false, withFullHeightBy.fullHeight());

        final HorizontalListLayout withConstrainedBy = threeArg.withConstrainedBy(b -> !b);
        assertEquals(false, withConstrainedBy.constrained());

        assertEquals(threeArg, new HorizontalListLayout(10, true, true));
        assertNotEquals(threeArg, withGap);
        assertEquals(threeArg.hashCode(), new HorizontalListLayout(10, true, true).hashCode());
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
