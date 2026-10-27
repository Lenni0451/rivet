package net.lenni0451.rivet.layout.grid;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridLayoutTest {

    private static void assertRectangleEquals(final Rectangle expected, final Rectangle actual, final float delta) {
        assertEquals(expected.x(), actual.x(), delta, "x mismatch");
        assertEquals(expected.y(), actual.y(), delta, "y mismatch");
        assertEquals(expected.width(), actual.width(), delta, "width mismatch");
        assertEquals(expected.height(), actual.height(), delta, "height mismatch");
    }

    @Test
    void testEmptyGrid() {
        final GridLayout layout = new GridLayout();
        final Size ideal = layout.computeIdealSize(new Size(800, 600), List.of());
        assertEquals(Size.EMPTY, ideal);
    }

    @Test
    void testSingleComponentIdealSize() {
        final GridLayout layout = new GridLayout(10, 10);
        final Component component = new TestComponent(new Size(50, 30));
        component.layoutOptions(new GridOptions(0, 0));

        final Size ideal = layout.computeIdealSize(new Size(800, 600), List.of(component));
        assertEquals(new Size(50, 30), ideal);
    }

    @Test
    void testBasicLayoutPositioning() {
        final GridLayout layout = new GridLayout(10, 20);

        final Component component1 = new TestComponent(new Size(100, 50));
        component1.layoutOptions(new GridOptions(0, 0));

        final Component component2 = new TestComponent(new Size(150, 80));
        component2.layoutOptions(new GridOptions(1, 0));

        final Component component3 = new TestComponent(new Size(80, 60));
        component3.layoutOptions(new GridOptions(0, 1));

        final List<Component> components = List.of(component1, component2, component3);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        // Expected columnWidths: column0 = max(100, 80) = 100, column1 = 150
        // Expected rowHeights: row0 = max(50, 80) = 80, row1 = 60
        // Total Ideal Width = 100 + 150 + 10 (horizontalGap) = 260
        // Total Ideal Height = 80 + 60 + 20 (verticalGap) = 160
        assertEquals(new Size(260, 160), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(ideal, components, bounds::put);

        // Column offsets: column0 = 0, column1 = 100 + 10 = 110
        // Row offsets: row0 = 0, row1 = 80 + 20 = 100
        // component1: column 0, row 0. Cell: (0, 0, 100, 80). Ideal size: (100, 50). Anchor CENTER.
        // x = 0 + (100 - 100)/2 = 0. y = 0 + (80 - 50)/2 = 15. Width = 100, Height = 50.
        assertRectangleEquals(new Rectangle(0, 15, 100, 50), bounds.get(component1), 1e-4f);

        // component2: column 1, row 0. Cell: (110, 0, 150, 80). Ideal size: (150, 80). Anchor CENTER.
        // x = 110 + (150 - 150)/2 = 110. y = 0 + (80 - 80)/2 = 0. Width = 150, Height = 80.
        assertRectangleEquals(new Rectangle(110, 0, 150, 80), bounds.get(component2), 1e-4f);

        // component3: column 0, row 1. Cell: (0, 100, 100, 60). Ideal size: (80, 60). Anchor CENTER.
        // x = 0 + (100 - 80)/2 = 10. y = 100 + (60 - 60)/2 = 100. Width = 80, Height = 60.
        assertRectangleEquals(new Rectangle(10, 100, 80, 60), bounds.get(component3), 1e-4f);
    }

    @Test
    void testSpanningComponents() {
        final GridLayout layout = new GridLayout(10, 10);

        final Component component1 = new TestComponent(new Size(50, 50));
        component1.layoutOptions(new GridOptions(0, 0));

        final Component component2 = new TestComponent(new Size(50, 50));
        component2.layoutOptions(new GridOptions(1, 0));

        // Spans column 0 & 1, row 1.
        final Component spanningComponent = new TestComponent(new Size(150, 40));
        spanningComponent.layoutOptions(new GridOptions(0, 1, 2, 1, 0, 0, GridAnchor.CENTER, GridFill.NONE, Padding.EMPTY));

        final List<Component> components = List.of(component1, component2, spanningComponent);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        // 1st pass: non-spanning columns: column0 = 50, column1 = 50. Total non-spanning width = 100 + 10 = 110.
        // 2nd pass: spanning: spanningComponent needs 150. Current is 110.
        // Difference = 40. Distribute 20 to each column.
        // Final columnWidths: column0 = 70, column1 = 70.
        // RowHeights: row0 = 50, row1 = 40.
        // Ideal Size: Width = 70 + 70 + 10 = 150. Height = 50 + 40 + 10 = 100.
        assertEquals(new Size(150, 100), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(ideal, components, bounds::put);

        // spanningComponent cell: starts at column0 = 0. Ends at column2 - gap = columnWidth[0] + columnWidth[1] + gap = 70 + 70 + 10 = 150.
        // Cell rect: (0, 60, 150, 40). Component: (150, 40).
        assertRectangleEquals(new Rectangle(0, 60, 150, 40), bounds.get(spanningComponent), 1e-4f);
    }

    @Test
    void testHomogeneousRowsAndColumns() {
        final GridLayout layout = new GridLayout(10, 10, true, true, false, false);

        final Component component1 = new TestComponent(new Size(50, 30));
        component1.layoutOptions(new GridOptions(0, 0));

        final Component component2 = new TestComponent(new Size(100, 80));
        component2.layoutOptions(new GridOptions(1, 1));

        final List<Component> components = List.of(component1, component2);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        // Without homogeneity: column0=50, column1=100; row0=30, row1=80.
        // With homogeneity: column0=100, column1=100; row0=80, row1=80.
        // Ideal Width = 100 + 100 + 10 = 210.
        // Ideal Height = 80 + 80 + 10 = 170.
        assertEquals(new Size(210, 170), ideal);
    }

    @Test
    void testWeightsDistribution() {
        // Not homogeneous, no shrink, growing enabled
        final GridLayout layout = new GridLayout(10, 10);

        final Component component1 = new TestComponent(new Size(100, 50));
        component1.layoutOptions(new GridOptions(0, 0).withWeightX(1.0f).withWeightY(1.0f));

        final Component component2 = new TestComponent(new Size(100, 50));
        component2.layoutOptions(new GridOptions(1, 0).withWeightX(3.0f).withWeightY(0.0f));

        final List<Component> components = List.of(component1, component2);
        // Ideal is Width = 100 + 100 + 10 = 210, Height = 50.
        // Let's layout inside container of Width = 310, Height = 150.
        // Extra width = 100. weightX total = 4.0. column0 gets 25, column1 gets 75.
        // Final column0 = 125, column1 = 175.
        // Extra height = 100. weightY total = 1.0. row0 gets 100.
        // Final row0 = 150.
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(310, 150), components, bounds::put);

        // column0 offset = 0, column1 offset = 125 + 10 = 135.
        // component1: Cell is (0, 0, 125, 150). Ideal component size: (100, 50). Anchor CENTER.
        // x = (125 - 100)/2 = 12.5. y = (150 - 50)/2 = 50.
        assertRectangleEquals(new Rectangle(12.5f, 50f, 100, 50), bounds.get(component1), 1e-4f);

        // component2: Cell is (135, 0, 175, 150). Ideal component size: (100, 50). Anchor CENTER.
        // x = 135 + (175 - 100)/2 = 172.5. y = (150 - 50)/2 = 50.
        assertRectangleEquals(new Rectangle(172.5f, 50f, 100, 50), bounds.get(component2), 1e-4f);
    }

    @Test
    void testShrinking() {
        final GridLayout layout = new GridLayout(10, 10, false, false, true, true);

        final Component component1 = new TestComponent(new Size(100, 100));
        component1.layoutOptions(new GridOptions(0, 0));

        final Component component2 = new TestComponent(new Size(100, 100));
        component2.layoutOptions(new GridOptions(1, 0));

        final List<Component> components = List.of(component1, component2);
        // Ideal size = 100 + 100 + 10 = 210 (Width), 100 (Height).
        // Let's layout inside container of Width = 110, Height = 60.
        // Horizontal gaps = 10. Available width = 110 - 10 = 100.
        // Original widths sum = 200. Scale = 100/200 = 0.5.
        // columnWidths become 50 each.
        // Vertical gaps = 0. Available height = 60 - 0 = 60.
        // Original height sum = 100. Scale = 60/100 = 0.6.
        // Row heights become 60.
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(110, 60), components, bounds::put);

        // component1: Cell is (0, 0, 50, 60). Ideal is (100, 100).
        // Component gets clamped to cell size: max width = 50, max height = 60.
        // x = 0, y = 0, width = 50, height = 60.
        assertRectangleEquals(new Rectangle(0, 0, 50, 60), bounds.get(component1), 1e-4f);

        // component2: Cell is (50 + 10 = 60, 0, 50, 60).
        // x = 60, y = 0, width = 50, height = 60.
        assertRectangleEquals(new Rectangle(60, 0, 50, 60), bounds.get(component2), 1e-4f);
    }

    @Test
    void testPaddingAndAlignment() {
        final GridLayout layout = new GridLayout(0, 0);

        final Component component = new TestComponent(new Size(40, 30));
        component.layoutOptions(new GridOptions(0, 0)
                .withPadding(new Padding(20, 10, 20, 10))
                .withAnchor(GridAnchor.BOTTOM_RIGHT)
                .withWeightX(1.0f)
                .withWeightY(1.0f));

        final List<Component> components = List.of(component);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        // Ideal component width = 40 + padding (20 left + 20 right) = 80.
        // Ideal component height = 30 + padding (10 top + 10 bottom) = 50.
        assertEquals(new Size(80, 50), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        // Layout in larger size: 100 x 80.
        // column0 gets 100, row0 gets 80.
        // cellWidth = 100, cellHeight = 80.
        // availableWidth = 100 - 40 (horizontal padding) = 60.
        // availableHeight = 80 - 20 (vertical padding) = 60.
        // Component ideal width = 40, height = 30.
        // Anchor BOTTOM_RIGHT:
        // x = cellX + padding.left() + (availableWidth - width) = 0 + 20 + (60 - 40) = 40.
        // y = cellY + padding.top() + (availableHeight - height) = 0 + 10 + (60 - 30) = 40.
        layout.layoutComponents(new Size(100, 80), components, bounds::put);

        assertRectangleEquals(new Rectangle(40, 40, 40, 30), bounds.get(component), 1e-4f);
    }

    @Test
    void testEmptyRowsAndColumnsCompression() {
        final GridLayout layout = new GridLayout(10, 20);

        final Component component1 = new TestComponent(new Size(100, 50));
        component1.layoutOptions(new GridOptions(1, 1));

        final Component component2 = new TestComponent(new Size(150, 80));
        component2.layoutOptions(new GridOptions(2, 2));

        final List<Component> components = List.of(component1, component2);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        assertEquals(new Size(260, 150), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(ideal, components, bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 100, 50), bounds.get(component1), 1e-4f);
        assertRectangleEquals(new Rectangle(110, 70, 150, 80), bounds.get(component2), 1e-4f);
    }

    @Test
    void testWeightedRowsShrinkOnShortage() {
        final GridLayout layout = new GridLayout(0, 0);

        final Component component1 = new TestComponent(new Size(100, 100));
        component1.layoutOptions(new GridOptions(0, 0, 1, 1, 0.0f, 0.0f, GridAnchor.CENTER, GridFill.BOTH, Padding.EMPTY));

        final Component component2 = new TestComponent(new Size(100, 300));
        component2.layoutOptions(new GridOptions(0, 1, 1, 1, 0.0f, 1.0f, GridAnchor.CENTER, GridFill.BOTH, Padding.EMPTY));

        final List<Component> components = List.of(component1, component2);

        final Size containerSize = new Size(100, 200);
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 100, 100), bounds.get(component1), 1e-4f);
        assertRectangleEquals(new Rectangle(0, 100, 100, 100), bounds.get(component2), 1e-4f);
    }

    @Test
    void testSpanningComponentExpandsWeightedColumn() {
        final GridLayout layout = new GridLayout(5, 5);

        final Component topTextField = new TestComponent(new Size(100, 20));
        topTextField.layoutOptions(new GridOptions(1, 0).withWeightX(1.0f).withFill(GridFill.HORIZONTAL));

        final Component bottomSizer = new TestComponent(new Size(500, 20));
        bottomSizer.layoutOptions(new GridOptions(0, 1).withWeightX(1.0f).withFill(GridFill.HORIZONTAL).withColumnSpan(2));

        final List<Component> components = List.of(topTextField, bottomSizer);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        assertEquals(500, ideal.width(), 1e-4f);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(ideal, components, bounds::put);

        assertRectangleEquals(new Rectangle(5, 0, 495, 20), bounds.get(topTextField), 1e-4f);
        assertRectangleEquals(new Rectangle(0, 25, 500, 20), bounds.get(bottomSizer), 1e-4f);
    }

    @Test
    void testSpanningComponentDoesNotExpandUnweightedLabelColumn() {
        final GridLayout layout = new GridLayout(5, 5);

        final Component label = new TestComponent(new Size(40, 20));
        label.layoutOptions(new GridOptions(0, 1).withAnchor(GridAnchor.LEFT));

        final Component textField = new TestComponent(new Size(100, 20));
        textField.layoutOptions(new GridOptions(1, 1).withAnchor(GridAnchor.LEFT));

        final Component sizer = new TestComponent(new Size(500, 20));
        sizer.layoutOptions(new GridOptions(0, 0).withWeightX(1.0f).withFill(GridFill.HORIZONTAL).withColumnSpan(2));

        final List<Component> components = List.of(label, textField, sizer);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        assertEquals(500, ideal.width(), 1e-4f);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(ideal, components, bounds::put);

        assertRectangleEquals(new Rectangle(0, 25, 40, 20), bounds.get(label), 1e-4f);
        assertRectangleEquals(new Rectangle(45, 25, 100, 20), bounds.get(textField), 1e-4f);
        assertRectangleEquals(new Rectangle(0, 0, 500, 20), bounds.get(sizer), 1e-4f);
    }

    @Test
    void testHomogeneousColumnsWithSpanningComponent() {
        final GridLayout layout = new GridLayout(10, 10, true, false, false, false);

        final Component component1 = new TestComponent(new Size(60, 20));
        component1.layoutOptions(new GridOptions(0, 0));

        final Component component2 = new TestComponent(new Size(20, 20));
        component2.layoutOptions(new GridOptions(1, 0));

        final Component spanning = new TestComponent(new Size(150, 20));
        spanning.layoutOptions(new GridOptions(0, 1).withColumnSpan(2));

        final List<Component> components = List.of(component1, component2, spanning);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        // Spanning component needs 150 across 2 columns and 1 gap of 10 -> 70 per column.
        assertEquals(new Size(150, 50), ideal);
    }

    @Test
    void testMultipleSpanningComponentsOrderIndependence() {
        final GridLayout layout = new GridLayout(5, 5);

        final Component label = new TestComponent(new Size(40, 20));
        label.layoutOptions(new GridOptions(0, 2).withAnchor(GridAnchor.LEFT));

        final Component textField = new TestComponent(new Size(100, 20));
        textField.layoutOptions(new GridOptions(1, 2).withAnchor(GridAnchor.LEFT));

        final Component unweightedHeader = new TestComponent(new Size(300, 20));
        unweightedHeader.layoutOptions(new GridOptions(0, 0).withFill(GridFill.HORIZONTAL).withColumnSpan(2));

        final Component weightedSizer = new TestComponent(new Size(500, 20));
        weightedSizer.layoutOptions(new GridOptions(0, 1).withWeightX(1.0f).withFill(GridFill.HORIZONTAL).withColumnSpan(2));

        final List<Component> components = List.of(label, textField, unweightedHeader, weightedSizer);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        assertEquals(500, ideal.width(), 1e-4f);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(ideal, components, bounds::put);

        // Unweighted label column must stay at 40 even though unweightedHeader came before weightedSizer
        assertRectangleEquals(new Rectangle(0, 50, 40, 20), bounds.get(label), 1e-4f);
        assertRectangleEquals(new Rectangle(45, 50, 100, 20), bounds.get(textField), 1e-4f);
    }

    @Test
    void testWeightedRowsShrinkRedistributesAfterClampingToZero() {
        final GridLayout layout = new GridLayout(0, 0);

        final Component fixedRow = new TestComponent(new Size(100, 100));
        fixedRow.layoutOptions(new GridOptions(0, 0).withFill(GridFill.BOTH));

        final Component smallWeightedRow = new TestComponent(new Size(100, 20));
        smallWeightedRow.layoutOptions(new GridOptions(0, 1).withWeightY(1.0f).withFill(GridFill.BOTH));

        final Component largeWeightedRow = new TestComponent(new Size(100, 300));
        largeWeightedRow.layoutOptions(new GridOptions(0, 2).withWeightY(1.0f).withFill(GridFill.BOTH));

        final List<Component> components = List.of(fixedRow, smallWeightedRow, largeWeightedRow);
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(100, 200), components, bounds::put);

        // Total ideal height is 420, container is 200 (shortage of 220).
        // smallWeightedRow clamps at 0 (absorbing 20), largeWeightedRow absorbs the remaining 200 -> height 100.
        assertRectangleEquals(new Rectangle(0, 0, 100, 100), bounds.get(fixedRow), 1e-4f);
        assertRectangleEquals(new Rectangle(0, 100, 100, 0), bounds.get(smallWeightedRow), 1e-4f);
        assertRectangleEquals(new Rectangle(0, 100, 100, 100), bounds.get(largeWeightedRow), 1e-4f);
    }

    @Test
    void testNegativeRowAndColumnIndices() {
        final GridLayout layout = new GridLayout(10, 10);

        final Component existing = new TestComponent(new Size(100, 50));
        existing.layoutOptions(new GridOptions(0, 0));

        final Component prependedTop = new TestComponent(new Size(100, 30));
        prependedTop.layoutOptions(new GridOptions(0, -1));

        final Component prependedLeft = new TestComponent(new Size(40, 50));
        prependedLeft.layoutOptions(new GridOptions(-2, 0));

        final List<Component> components = List.of(existing, prependedTop, prependedLeft);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        // Columns: -2 (width 40), 0 (width 100) + gap 10 = 150
        // Rows: -1 (height 30), 0 (height 50) + gap 10 = 90
        assertEquals(new Size(150, 90), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(ideal, components, bounds::put);

        assertRectangleEquals(new Rectangle(50, 0, 100, 30), bounds.get(prependedTop), 1e-4f);
        assertRectangleEquals(new Rectangle(0, 40, 40, 50), bounds.get(prependedLeft), 1e-4f);
        assertRectangleEquals(new Rectangle(50, 40, 100, 50), bounds.get(existing), 1e-4f);
    }

    private static class TestComponent extends Component {
        private final Size idealSize;

        public TestComponent(final Size idealSize) {
            this.idealSize = idealSize;
        }

        @Override
        public Size computeIdealSize(final Size constraints) {
            return this.idealSize;
        }
    }

}
