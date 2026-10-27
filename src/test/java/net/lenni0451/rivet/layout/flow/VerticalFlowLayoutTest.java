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

class VerticalFlowLayoutTest {

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
        final VerticalFlowLayout layout = VerticalFlowLayout.DEFAULT;
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
        final VerticalFlowLayout layout = new VerticalFlowLayout(20, 10);
        final Size containerSize = new Size(400, 500);
        final Component child = new TestComponent(80, 120);

        final Size ideal = layout.computeIdealSize(containerSize, List.of(child));
        assertEquals(new Size(80, 120), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);
        assertEquals(1, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 80, 120), bounds.get(child));

        final Container container = new Container(layout);
        container.add(child);
        container.computeLayout(containerSize);
        assertRectangleEquals(new Rectangle(0, 0, 80, 120), container.childBounds(child));
        assertEquals(new Size(80, 120), container.contentSize());
    }

    @Test
    void testMultiChildSingleColumn() {
        final VerticalFlowLayout layout = new VerticalFlowLayout(15, 10);
        final Size containerSize = new Size(400, 600);

        final Component c1 = new TestComponent(40, 100);
        final Component c2 = new TestComponent(60, 150);
        final Component c3 = new TestComponent(50, 80);
        final List<Component> list = List.of(c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(60, 350), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 40, 100), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 110, 60, 150), bounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 270, 50, 80), bounds.get(c3));
    }

    @Test
    void testMultiChildWrappingAcrossColumns() {
        final VerticalFlowLayout layout = new VerticalFlowLayout(20, 10);
        final Size containerSize = new Size(500, 250);

        final Component c1 = new TestComponent(40, 100);
        final Component c2 = new TestComponent(50, 120);
        final Component c3 = new TestComponent(60, 80);
        final Component c4 = new TestComponent(30, 90);
        final List<Component> list = List.of(c1, c2, c3, c4);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(130, 230), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 40, 100), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 110, 50, 120), bounds.get(c2));
        assertRectangleEquals(new Rectangle(70, 0, 60, 80), bounds.get(c3));
        assertRectangleEquals(new Rectangle(70, 90, 30, 90), bounds.get(c4));
    }

    @Test
    void testMultiChildThreeColumnsDifferentWidths() {
        final VerticalFlowLayout layout = new VerticalFlowLayout(10, 5);
        final Size containerSize = new Size(300, 100);

        final Component c1 = new TestComponent(30, 60);
        final Component c2 = new TestComponent(20, 40);
        final Component c3 = new TestComponent(25, 50);
        final Component c4 = new TestComponent(35, 70);
        final List<Component> list = List.of(c1, c2, c3, c4);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(110, 95), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 30, 60), bounds.get(c1));
        assertRectangleEquals(new Rectangle(40, 0, 20, 40), bounds.get(c2));
        assertRectangleEquals(new Rectangle(40, 45, 25, 50), bounds.get(c3));
        assertRectangleEquals(new Rectangle(75, 0, 35, 70), bounds.get(c4));
    }

    @Test
    void testExactFitBoundary() {
        final VerticalFlowLayout layout = new VerticalFlowLayout(15, 10);
        final Size containerSize = new Size(300, 200);

        final Component c1 = new TestComponent(50, 95);
        final Component c2 = new TestComponent(60, 95);
        final List<Component> list = List.of(c1, c2);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(60, 200), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 50, 95), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 105, 60, 95), bounds.get(c2));
    }

    @Test
    void testOversizedFirstChildInColumn() {
        final VerticalFlowLayout layout = new VerticalFlowLayout(10, 10);
        final Size containerSize = new Size(300, 100);

        final Component c1 = new TestComponent(40, 150);
        final Component c2 = new TestComponent(30, 50);
        final List<Component> list = List.of(c1, c2);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(80, 150), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 40, 150), bounds.get(c1));
        assertRectangleEquals(new Rectangle(50, 0, 30, 50), bounds.get(c2));
    }

    @Test
    void testOversizedSubsequentChildForcesOwnColumn() {
        final VerticalFlowLayout layout = new VerticalFlowLayout(10, 10);
        final Size containerSize = new Size(300, 100);

        final Component c1 = new TestComponent(40, 60);
        final Component c2 = new TestComponent(50, 120);
        final Component c3 = new TestComponent(20, 40);
        final List<Component> list = List.of(c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(130, 120), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 40, 60), bounds.get(c1));
        assertRectangleEquals(new Rectangle(50, 0, 50, 120), bounds.get(c2));
        assertRectangleEquals(new Rectangle(110, 0, 20, 40), bounds.get(c3));
    }

    @Test
    void testZeroGapConfiguration() {
        final VerticalFlowLayout layout = new VerticalFlowLayout(0, 0);
        final Size containerSize = new Size(200, 100);

        final Component c1 = new TestComponent(30, 50);
        final Component c2 = new TestComponent(40, 50);
        final Component c3 = new TestComponent(20, 50);
        final List<Component> list = List.of(c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(60, 100), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 30, 50), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 50, 40, 50), bounds.get(c2));
        assertRectangleEquals(new Rectangle(40, 0, 20, 50), bounds.get(c3));
    }

    @Test
    void testMinMaxConstraintClamping() {
        final VerticalFlowLayout layout = VerticalFlowLayout.DEFAULT;
        final Size containerSize = new Size(400, 500);

        final Component c1 = new TestComponent(20, 30).minSize(50, 80);
        final Component c2 = new TestComponent(150, 200).maxSize(80, 100);
        final List<Component> list = List.of(c1, c2);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(80, 180), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 50, 80), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 80, 80, 100), bounds.get(c2));
    }

    @Test
    void testUnconstrainedContainerHeight() {
        final VerticalFlowLayout layout = new VerticalFlowLayout(10, 10);
        final Size containerSize = new Size(Float.MAX_VALUE, Float.MAX_VALUE);

        final Component c1 = new TestComponent(20, 100);
        final Component c2 = new TestComponent(50, 200);
        final Component c3 = new TestComponent(30, 300);
        final Component c4 = new TestComponent(40, 400);
        final Component c5 = new TestComponent(10, 500);
        final List<Component> list = List.of(c1, c2, c3, c4, c5);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(50, 1540), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 20, 100), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 110, 50, 200), bounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 320, 30, 300), bounds.get(c3));
        assertRectangleEquals(new Rectangle(0, 630, 40, 400), bounds.get(c4));
        assertRectangleEquals(new Rectangle(0, 1040, 10, 500), bounds.get(c5));
    }

    @Test
    void testZeroContainerHeightForcesSingleItemColumns() {
        final VerticalFlowLayout layout = new VerticalFlowLayout(15, 10);
        final Size containerSize = new Size(300, 0);

        final Component c1 = new TestComponent(20, 40);
        final Component c2 = new TestComponent(30, 50);
        final Component c3 = new TestComponent(25, 60);
        final List<Component> list = List.of(c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(105, 60), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 20, 40), bounds.get(c1));
        assertRectangleEquals(new Rectangle(35, 0, 30, 50), bounds.get(c2));
        assertRectangleEquals(new Rectangle(80, 0, 25, 60), bounds.get(c3));
    }

    @Test
    void testContainerContractAndContentSize() {
        final VerticalFlowLayout layout = new VerticalFlowLayout(10, 10);
        final Container container = new Container(layout);

        final Component c1 = new TestComponent(40, 60);
        final Component c2 = new TestComponent(40, 60);
        final Component c3 = new TestComponent(40, 60);
        container.add(c1).add(c2).add(c3);

        container.computeLayout(new Size(300, 100));

        assertRectangleEquals(new Rectangle(0, 0, 40, 60), container.childBounds(c1));
        assertRectangleEquals(new Rectangle(50, 0, 40, 60), container.childBounds(c2));
        assertRectangleEquals(new Rectangle(100, 0, 40, 60), container.childBounds(c3));
        assertEquals(new Size(140, 60), container.contentSize());
    }

    @Test
    void testRecordAndFluentWithMethods() {
        final VerticalFlowLayout defaultLayout = VerticalFlowLayout.DEFAULT;
        assertEquals(0, defaultLayout.horizontalGap());
        assertEquals(0, defaultLayout.verticalGap());

        final VerticalFlowLayout noArg = new VerticalFlowLayout();
        assertEquals(defaultLayout, noArg);

        final VerticalFlowLayout parameterized = new VerticalFlowLayout(20, 10);
        assertEquals(20, parameterized.horizontalGap());
        assertEquals(10, parameterized.verticalGap());

        final VerticalFlowLayout withH = parameterized.withHorizontalGap(30);
        assertEquals(30, withH.horizontalGap());
        assertEquals(10, withH.verticalGap());

        final VerticalFlowLayout withV = parameterized.withVerticalGap(40);
        assertEquals(20, withV.horizontalGap());
        assertEquals(40, withV.verticalGap());

        final VerticalFlowLayout withHBy = parameterized.withHorizontalGapBy(g -> g * 2);
        assertEquals(40, withHBy.horizontalGap());
        assertEquals(10, withHBy.verticalGap());

        final VerticalFlowLayout withVBy = parameterized.withVerticalGapBy(g -> g + 5);
        assertEquals(20, withVBy.horizontalGap());
        assertEquals(15, withVBy.verticalGap());

        assertEquals(parameterized, new VerticalFlowLayout(20, 10));
        assertNotEquals(parameterized, withH);
        assertEquals(parameterized.hashCode(), new VerticalFlowLayout(20, 10).hashCode());
        assertTrue(parameterized.toString().contains("horizontalGap=20"));
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
