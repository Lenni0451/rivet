package net.lenni0451.rivet.layout.tile;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.layout.LayoutOptions;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TileLayoutTest {

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
    void testEmptyComponentsZeroGaps() {
        final TileLayout layout = new TileLayout(3, 2);
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
    void testEmptyComponentsNonZeroGaps() {
        final TileLayout layout = new TileLayout(3, 2, 10, 15);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), List.of());
        assertEquals(new Size(20, 15), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(800, 600), List.of(), bounds::put);
        assertTrue(bounds.isEmpty());
    }

    @Test
    void testSingleComponentExplicitAtOrigin() {
        final TileLayout layout = new TileLayout(2, 2, 10, 20);
        final Size containerSize = new Size(210, 120);
        final Component child = new TestComponent(80, 50).layoutOptions(TileOptions.at(0, 0));

        final Size ideal = layout.computeIdealSize(containerSize, List.of(child));
        assertEquals(new Size(170, 120), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);
        assertEquals(1, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 100, 50), bounds.get(child));

        final Container container = new Container(layout);
        container.add(child);
        container.computeLayout(containerSize);
        assertRectangleEquals(new Rectangle(0, 0, 100, 50), container.childBounds(child));
    }

    @Test
    void testSingleComponentAutoPlaced() {
        final TileLayout layout = new TileLayout(2, 2, 10, 20);
        final Size containerSize = new Size(210, 120);
        final Component child = new TestComponent(80, 50);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);
        assertEquals(1, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 100, 50), bounds.get(child));
    }

    @Test
    void testFullUniformGridBasicPositioning() {
        final TileLayout layout = new TileLayout(2, 2, 10, 20);
        final Size containerSize = new Size(210, 120);

        final Component c0 = new TestComponent(40, 20).layoutOptions(TileOptions.at(0, 0));
        final Component c1 = new TestComponent(60, 30).layoutOptions(TileOptions.at(1, 0));
        final Component c2 = new TestComponent(50, 40).layoutOptions(TileOptions.at(0, 1));
        final Component c3 = new TestComponent(30, 25).layoutOptions(TileOptions.at(1, 1));
        final List<Component> list = List.of(c0, c1, c2, c3);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(130, 100), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 50), bounds.get(c0));
        assertRectangleEquals(new Rectangle(110, 0, 100, 50), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 70, 100, 50), bounds.get(c2));
        assertRectangleEquals(new Rectangle(110, 70, 100, 50), bounds.get(c3));
    }

    @Test
    void testAutoPlacementInRowMajorOrder() {
        final TileLayout layout = new TileLayout(2, 2, 10, 20);
        final Size containerSize = new Size(210, 120);

        final Component c0 = new TestComponent(40, 20);
        final Component c1 = new TestComponent(60, 30);
        final Component c2 = new TestComponent(50, 40);
        final Component c3 = new TestComponent(30, 25);
        final List<Component> list = List.of(c0, c1, c2, c3);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 50), bounds.get(c0));
        assertRectangleEquals(new Rectangle(110, 0, 100, 50), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 70, 100, 50), bounds.get(c2));
        assertRectangleEquals(new Rectangle(110, 70, 100, 50), bounds.get(c3));
    }

    @Test
    void testMixedExplicitAndAutoPlacement() {
        final TileLayout layout = new TileLayout(2, 2, 10, 10);
        final Size containerSize = new Size(210, 210);

        final Component c0 = new TestComponent(50, 50);
        final Component c1 = new TestComponent(50, 50).layoutOptions(TileOptions.at(1, 0));
        final Component c2 = new TestComponent(50, 50);
        final Component c3 = new TestComponent(50, 50).layoutOptions(TileOptions.at(1, 1));
        final List<Component> list = List.of(c0, c1, c2, c3);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 100), bounds.get(c0));
        assertRectangleEquals(new Rectangle(110, 0, 100, 100), bounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 110, 100, 100), bounds.get(c2));
        assertRectangleEquals(new Rectangle(110, 110, 100, 100), bounds.get(c3));
    }

    @Test
    void testCellCollisionFallbackToAutoPlacement() {
        final TileLayout layout = new TileLayout(2, 2, 0, 0);
        final Size containerSize = new Size(200, 200);

        final Component c1 = new TestComponent(50, 50).layoutOptions(TileOptions.at(0, 0));
        final Component c2 = new TestComponent(50, 50).layoutOptions(TileOptions.at(0, 0));
        final Component c3 = new TestComponent(50, 50).layoutOptions(TileOptions.at(1, 1));
        final List<Component> list = List.of(c1, c2, c3);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 100), bounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 0, 100, 100), bounds.get(c2));
        assertRectangleEquals(new Rectangle(100, 100, 100, 100), bounds.get(c3));
    }

    @Test
    void testOutOfBoundsCoordinatesFallbackToAutoPlacement() {
        final TileLayout layout = new TileLayout(2, 2, 0, 0);
        final Size containerSize = new Size(200, 200);

        final Component cNegCol = new TestComponent(50, 50).layoutOptions(TileOptions.at(-1, 0));
        final Component cNegRow = new TestComponent(50, 50).layoutOptions(TileOptions.at(0, -1));
        final Component cLargeCol = new TestComponent(50, 50).layoutOptions(TileOptions.at(5, 0));
        final Component cLargeRow = new TestComponent(50, 50).layoutOptions(TileOptions.at(0, 5));
        final List<Component> list = List.of(cNegCol, cNegRow, cLargeCol, cLargeRow);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 100), bounds.get(cNegCol));
        assertRectangleEquals(new Rectangle(100, 0, 100, 100), bounds.get(cNegRow));
        assertRectangleEquals(new Rectangle(0, 100, 100, 100), bounds.get(cLargeCol));
        assertRectangleEquals(new Rectangle(100, 100, 100, 100), bounds.get(cLargeRow));
    }

    @Test
    void testIncompatibleLayoutOptionsFallbackToAutoPlacement() {
        final TileLayout layout = new TileLayout(2, 2, 0, 0);
        final Size containerSize = new Size(200, 200);

        final Component cForeign = new TestComponent(50, 50).layoutOptions(new LayoutOptions() {});
        final Component cNull = new TestComponent(50, 50);
        final List<Component> list = List.of(cForeign, cNull);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 100), bounds.get(cForeign));
        assertRectangleEquals(new Rectangle(100, 0, 100, 100), bounds.get(cNull));
    }

    @Test
    void testDynamicGridExpansionWhenComponentsExceedCapacity() {
        final TileLayout layout = new TileLayout(2, 2, 10, 10);
        final Size containerSize = new Size(320, 210);

        final Component c0 = new TestComponent(50, 40);
        final Component c1 = new TestComponent(50, 40);
        final Component c2 = new TestComponent(50, 40);
        final Component c3 = new TestComponent(50, 40);
        final Component c4 = new TestComponent(50, 40);
        final List<Component> list = List.of(c0, c1, c2, c3, c4);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(170, 90), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 100), bounds.get(c0));
        assertRectangleEquals(new Rectangle(110, 0, 100, 100), bounds.get(c1));
        assertRectangleEquals(new Rectangle(220, 0, 100, 100), bounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 110, 100, 100), bounds.get(c3));
        assertRectangleEquals(new Rectangle(110, 110, 100, 100), bounds.get(c4));
    }

    @Test
    void testExplicitPlacementInExpandedGrid() {
        final TileLayout layout = new TileLayout(2, 2, 0, 0);
        final Size containerSize = new Size(300, 200);

        final Component c0 = new TestComponent(50, 50).layoutOptions(TileOptions.at(2, 0));
        final Component c1 = new TestComponent(50, 50);
        final Component c2 = new TestComponent(50, 50);
        final Component c3 = new TestComponent(50, 50);
        final Component c4 = new TestComponent(50, 50);
        final List<Component> list = List.of(c0, c1, c2, c3, c4);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(200, 0, 100, 100), bounds.get(c0));
        assertRectangleEquals(new Rectangle(0, 0, 100, 100), bounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 0, 100, 100), bounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 100, 100, 100), bounds.get(c3));
        assertRectangleEquals(new Rectangle(100, 100, 100, 100), bounds.get(c4));
    }

    @Test
    void testMinAndMaxSizeClampingOnCells() {
        final TileLayout layout = new TileLayout(2, 2, 0, 0);
        final Size containerSize = new Size(200, 200);

        final Component cMax = new TestComponent(80, 80).maxSize(60, 40).layoutOptions(TileOptions.at(0, 0));
        final Component cMin = new TestComponent(80, 80).minSize(130, 140).layoutOptions(TileOptions.at(1, 0));
        final Component cNormal = new TestComponent(50, 50).layoutOptions(TileOptions.at(0, 1));
        final List<Component> list = List.of(cMax, cMin, cNormal);

        final Size ideal = layout.computeIdealSize(containerSize, list);
        assertEquals(new Size(260, 280), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 60, 40), bounds.get(cMax));
        assertRectangleEquals(new Rectangle(100, 0, 130, 140), bounds.get(cMin));
        assertRectangleEquals(new Rectangle(0, 100, 100, 100), bounds.get(cNormal));
    }

    @Test
    void testContainerContractCompliance() {
        final TileLayout layout = new TileLayout(2, 2, 10, 10);
        final Container container = new Container(layout);

        final Component c0 = new TestComponent(50, 50);
        final Component c1 = new TestComponent(50, 50).layoutOptions(TileOptions.at(1, 0));
        final Component c2 = new TestComponent(50, 50);
        final Component c3 = new TestComponent(50, 50).layoutOptions(TileOptions.at(1, 1));
        container.add(c0).add(c1).add(c2).add(c3);

        final Size containerSize = new Size(210, 210);
        assertDoesNotThrow(() -> container.computeLayout(containerSize));

        assertRectangleEquals(new Rectangle(0, 0, 100, 100), container.childBounds(c0));
        assertRectangleEquals(new Rectangle(110, 0, 100, 100), container.childBounds(c1));
        assertRectangleEquals(new Rectangle(0, 110, 100, 100), container.childBounds(c2));
        assertRectangleEquals(new Rectangle(110, 110, 100, 100), container.childBounds(c3));
        assertEquals(new Size(210, 210), container.contentSize());
        assertEquals(layout.computeIdealSize(containerSize, List.of(c0, c1, c2, c3)), container.computeIdealSize(containerSize));
    }

    @Test
    void testRecordConstructorsGettersAndWithers() {
        final TileLayout twoArg = new TileLayout(3, 2);
        assertEquals(3, twoArg.columns());
        assertEquals(2, twoArg.rows());
        assertEquals(0, twoArg.horizontalGap());
        assertEquals(0, twoArg.verticalGap());

        final TileLayout fourArg = new TileLayout(3, 2, 10, 15);
        assertEquals(3, fourArg.columns());
        assertEquals(2, fourArg.rows());
        assertEquals(10, fourArg.horizontalGap());
        assertEquals(15, fourArg.verticalGap());

        final TileLayout withCols = fourArg.withColumns(4);
        assertEquals(4, withCols.columns());
        assertEquals(2, withCols.rows());

        final TileLayout withRows = fourArg.withRows(3);
        assertEquals(3, withRows.rows());
        assertEquals(3, withRows.columns());

        final TileLayout withH = fourArg.withHorizontalGap(12);
        assertEquals(12, withH.horizontalGap());

        final TileLayout withV = fourArg.withVerticalGap(18);
        assertEquals(18, withV.verticalGap());

        final TileLayout withColsBy = fourArg.withColumnsBy(c -> c + 1);
        assertEquals(4, withColsBy.columns());

        final TileLayout withRowsBy = fourArg.withRowsBy(r -> r + 1);
        assertEquals(3, withRowsBy.rows());

        final TileLayout withHBy = fourArg.withHorizontalGapBy(g -> g * 2);
        assertEquals(20, withHBy.horizontalGap());

        final TileLayout withVBy = fourArg.withVerticalGapBy(g -> g * 2);
        assertEquals(30, withVBy.verticalGap());

        assertEquals(fourArg, new TileLayout(3, 2, 10, 15));
        assertNotEquals(fourArg, withCols);
        assertEquals(fourArg.hashCode(), new TileLayout(3, 2, 10, 15).hashCode());
        assertTrue(fourArg.toString().contains("columns=3"));

        // TileOptions
        final TileOptions options = TileOptions.at(1, 2);
        assertEquals(1, options.column());
        assertEquals(2, options.row());
        assertEquals(options, new TileOptions(1, 2));

        final TileOptions withCol = options.withColumn(3);
        assertEquals(3, withCol.column());
        assertEquals(2, withCol.row());

        final TileOptions withRow = options.withRow(4);
        assertEquals(1, withRow.column());
        assertEquals(4, withRow.row());

        final TileOptions withColBy = options.withColumnBy(c -> c + 1);
        assertEquals(2, withColBy.column());

        final TileOptions withRowBy = options.withRowBy(r -> r + 1);
        assertEquals(3, withRowBy.row());

        assertNotEquals(options, withCol);
        assertEquals(options.hashCode(), new TileOptions(1, 2).hashCode());
        assertTrue(options.toString().contains("column=1"));
    }

    @Test
    void testOutOfOrderChildrenAddition() {
        final TileLayout layout = new TileLayout(2, 2, 0, 0);
        final Size containerSize = new Size(200, 200);

        final Component cBottomRight = new TestComponent(50, 50).layoutOptions(TileOptions.at(1, 1));
        final Component cTopLeft = new TestComponent(50, 50).layoutOptions(TileOptions.at(0, 0));
        final Component cTopRight = new TestComponent(50, 50).layoutOptions(TileOptions.at(1, 0));
        final Component cBottomLeft = new TestComponent(50, 50).layoutOptions(TileOptions.at(0, 1));
        final List<Component> list = List.of(cBottomRight, cTopLeft, cTopRight, cBottomLeft);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, list, bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 100), bounds.get(cTopLeft));
        assertRectangleEquals(new Rectangle(100, 0, 100, 100), bounds.get(cTopRight));
        assertRectangleEquals(new Rectangle(0, 100, 100, 100), bounds.get(cBottomLeft));
        assertRectangleEquals(new Rectangle(100, 100, 100, 100), bounds.get(cBottomRight));
    }

    @Test
    void testZeroGapVsNonZeroGapComparison() {
        final TileLayout zeroGap = new TileLayout(2, 2, 0, 0);
        final TileLayout withGap = new TileLayout(2, 2, 10, 10);

        final Component c0 = new TestComponent(50, 50).layoutOptions(TileOptions.at(0, 0));
        final Component c1 = new TestComponent(50, 50).layoutOptions(TileOptions.at(1, 1));
        final List<Component> list = List.of(c0, c1);

        final Map<Component, Rectangle> zeroBounds = new HashMap<>();
        zeroGap.layoutComponents(new Size(200, 200), list, zeroBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 100), zeroBounds.get(c0));
        assertRectangleEquals(new Rectangle(100, 100, 100, 100), zeroBounds.get(c1));

        final Map<Component, Rectangle> gapBounds = new HashMap<>();
        withGap.layoutComponents(new Size(210, 210), list, gapBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 100), gapBounds.get(c0));
        assertRectangleEquals(new Rectangle(110, 110, 100, 100), gapBounds.get(c1));
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
