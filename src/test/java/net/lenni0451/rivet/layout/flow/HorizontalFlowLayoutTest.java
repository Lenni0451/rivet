package net.lenni0451.rivet.layout.flow;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HorizontalFlowLayoutTest {

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
        final HorizontalFlowLayout layout = HorizontalFlowLayout.DEFAULT;
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
    void testSingleChild() {
        final HorizontalFlowLayout layout = new HorizontalFlowLayout(10, 20);
        final Size containerSize = new Size(500, 400);
        final Component child = new TestComponent(120, 80);

        final Size ideal = layout.computeIdealSize(containerSize, List.of(child));
        assertEquals(new Size(120, 80), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);
        assertEquals(1, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 120, 80), bounds.get(child));

        final Container container = new Container(layout);
        container.add(child);
        container.computeLayout(containerSize);
        assertRectangleEquals(new Rectangle(0, 0, 120, 80), container.childBounds(child));
        assertEquals(new Size(120, 80), container.contentSize());
    }

    @Test
    void testMultiChildSingleRow() {
        final HorizontalFlowLayout layout = new HorizontalFlowLayout(10, 15);
        final Size containerSize = new Size(600, 400);

        final Component c1 = new TestComponent(100, 40);
        final Component c2 = new TestComponent(150, 60);
        final Component c3 = new TestComponent(80, 50);
        final List<Component> list = List.of(c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(350, 60), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 40), bounds.get(c1));
        assertRectangleEquals(new Rectangle(110, 0, 150, 60), bounds.get(c2));
        assertRectangleEquals(new Rectangle(270, 0, 80, 50), bounds.get(c3));
    }

    @Test
    void testMultiChildWrappingAcrossRows() {
        final HorizontalFlowLayout layout = new HorizontalFlowLayout(10, 20);
        final Size containerSize = new Size(250, 500);

        final Component c1 = new TestComponent(100, 40);
        final Component c2 = new TestComponent(120, 50);
        final Component c3 = new TestComponent(80, 60);
        final Component c4 = new TestComponent(90, 30);
        final List<Component> list = List.of(c1, c2, c3, c4);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(230, 130), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 40), bounds.get(c1));
        assertRectangleEquals(new Rectangle(110, 0, 120, 50), bounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 70, 80, 60), bounds.get(c3));
        assertRectangleEquals(new Rectangle(90, 70, 90, 30), bounds.get(c4));
    }

    @Test
    void testMultiChildThreeRowsDifferentHeights() {
        final HorizontalFlowLayout layout = new HorizontalFlowLayout(5, 10);
        final Size containerSize = new Size(100, 300);

        final Component c1 = new TestComponent(60, 30);
        final Component c2 = new TestComponent(40, 20);
        final Component c3 = new TestComponent(50, 25);
        final Component c4 = new TestComponent(70, 35);
        final List<Component> list = List.of(c1, c2, c3, c4);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(95, 110), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 60, 30), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 40, 40, 20), bounds.get(c2));
        assertRectangleEquals(new Rectangle(45, 40, 50, 25), bounds.get(c3));
        assertRectangleEquals(new Rectangle(0, 75, 70, 35), bounds.get(c4));
    }

    @Test
    void testExactFitBoundary() {
        final HorizontalFlowLayout layout = new HorizontalFlowLayout(10, 15);
        final Size containerSize = new Size(200, 300);

        final Component c1 = new TestComponent(95, 50);
        final Component c2 = new TestComponent(95, 60);
        final List<Component> list = List.of(c1, c2);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(200, 60), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 95, 50), bounds.get(c1));
        assertRectangleEquals(new Rectangle(105, 0, 95, 60), bounds.get(c2));
    }

    @Test
    void testOversizedFirstChildInRow() {
        final HorizontalFlowLayout layout = new HorizontalFlowLayout(10, 10);
        final Size containerSize = new Size(100, 300);

        final Component c1 = new TestComponent(150, 40);
        final Component c2 = new TestComponent(50, 30);
        final List<Component> list = List.of(c1, c2);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(150, 80), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 150, 40), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 50, 50, 30), bounds.get(c2));
    }

    @Test
    void testOversizedSubsequentChildForcesOwnRow() {
        final HorizontalFlowLayout layout = new HorizontalFlowLayout(10, 10);
        final Size containerSize = new Size(100, 300);

        final Component c1 = new TestComponent(60, 40);
        final Component c2 = new TestComponent(120, 50);
        final Component c3 = new TestComponent(40, 20);
        final List<Component> list = List.of(c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(120, 130), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 60, 40), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 50, 120, 50), bounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 110, 40, 20), bounds.get(c3));
    }

    @Test
    void testZeroGapConfiguration() {
        final HorizontalFlowLayout layout = new HorizontalFlowLayout(0, 0);
        final Size containerSize = new Size(100, 200);

        final Component c1 = new TestComponent(50, 30);
        final Component c2 = new TestComponent(50, 40);
        final Component c3 = new TestComponent(50, 20);
        final List<Component> list = List.of(c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(100, 60), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 50, 30), bounds.get(c1));
        assertRectangleEquals(new Rectangle(50, 0, 50, 40), bounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 40, 50, 20), bounds.get(c3));
    }

    @Test
    void testMinMaxConstraintClamping() {
        final HorizontalFlowLayout layout = HorizontalFlowLayout.DEFAULT;
        final Size containerSize = new Size(500, 400);

        final Component c1 = new TestComponent(30, 20).minSize(80, 50);
        final Component c2 = new TestComponent(200, 150).maxSize(100, 80);
        final List<Component> list = List.of(c1, c2);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(180, 80), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 80, 50), bounds.get(c1));
        assertRectangleEquals(new Rectangle(80, 0, 100, 80), bounds.get(c2));
    }

    @Test
    void testUnconstrainedContainerWidth() {
        final HorizontalFlowLayout layout = new HorizontalFlowLayout(10, 10);
        final Size containerSize = new Size(Float.MAX_VALUE, Float.MAX_VALUE);

        final Component c1 = new TestComponent(100, 20);
        final Component c2 = new TestComponent(200, 50);
        final Component c3 = new TestComponent(300, 30);
        final Component c4 = new TestComponent(400, 40);
        final Component c5 = new TestComponent(500, 10);
        final List<Component> list = List.of(c1, c2, c3, c4, c5);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(1540, 50), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 20), bounds.get(c1));
        assertRectangleEquals(new Rectangle(110, 0, 200, 50), bounds.get(c2));
        assertRectangleEquals(new Rectangle(320, 0, 300, 30), bounds.get(c3));
        assertRectangleEquals(new Rectangle(630, 0, 400, 40), bounds.get(c4));
        assertRectangleEquals(new Rectangle(1040, 0, 500, 10), bounds.get(c5));
    }

    @Test
    void testZeroContainerWidthForcesSingleItemRows() {
        final HorizontalFlowLayout layout = new HorizontalFlowLayout(10, 15);
        final Size containerSize = new Size(0, 300);

        final Component c1 = new TestComponent(40, 20);
        final Component c2 = new TestComponent(50, 30);
        final Component c3 = new TestComponent(60, 25);
        final List<Component> list = List.of(c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(60, 105), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 40, 20), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 35, 50, 30), bounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 80, 60, 25), bounds.get(c3));
    }

    @Test
    void testContainerContractAndContentSize() {
        final HorizontalFlowLayout layout = new HorizontalFlowLayout(10, 10);
        final Container container = new Container(layout);

        final Component c1 = new TestComponent(60, 40);
        final Component c2 = new TestComponent(60, 40);
        final Component c3 = new TestComponent(60, 40);
        container.add(c1).add(c2).add(c3);

        container.computeLayout(new Size(100, 300));

        assertRectangleEquals(new Rectangle(0, 0, 60, 40), container.childBounds(c1));
        assertRectangleEquals(new Rectangle(0, 50, 60, 40), container.childBounds(c2));
        assertRectangleEquals(new Rectangle(0, 100, 60, 40), container.childBounds(c3));
        assertEquals(new Size(60, 140), container.contentSize());
    }

    @Test
    void testRecordAndFluentWithMethods() {
        final HorizontalFlowLayout defaultLayout = HorizontalFlowLayout.DEFAULT;
        assertEquals(0, defaultLayout.horizontalGap());
        assertEquals(0, defaultLayout.verticalGap());

        final HorizontalFlowLayout noArg = new HorizontalFlowLayout();
        assertEquals(defaultLayout, noArg);

        final HorizontalFlowLayout parameterized = new HorizontalFlowLayout(10, 20);
        assertEquals(10, parameterized.horizontalGap());
        assertEquals(20, parameterized.verticalGap());

        final HorizontalFlowLayout withH = parameterized.withHorizontalGap(30);
        assertEquals(30, withH.horizontalGap());
        assertEquals(20, withH.verticalGap());

        final HorizontalFlowLayout withV = parameterized.withVerticalGap(40);
        assertEquals(10, withV.horizontalGap());
        assertEquals(40, withV.verticalGap());

        final HorizontalFlowLayout withHBy = parameterized.withHorizontalGapBy(g -> g + 5);
        assertEquals(15, withHBy.horizontalGap());
        assertEquals(20, withHBy.verticalGap());

        final HorizontalFlowLayout withVBy = parameterized.withVerticalGapBy(g -> g * 2);
        assertEquals(10, withVBy.horizontalGap());
        assertEquals(40, withVBy.verticalGap());

        assertEquals(parameterized, new HorizontalFlowLayout(10, 20));
        assertNotEquals(parameterized, withH);
        assertEquals(parameterized.hashCode(), new HorizontalFlowLayout(10, 20).hashCode());
        assertTrue(parameterized.toString().contains("horizontalGap=10"));
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
