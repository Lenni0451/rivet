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
        final Component c = new TestComponent(new Size(50, 30));
        c.layoutOptions(new GridOptions(0, 0));

        final Size ideal = layout.computeIdealSize(new Size(800, 600), List.of(c));
        assertEquals(new Size(50, 30), ideal);
    }

    @Test
    void testBasicLayoutPositioning() {
        final GridLayout layout = new GridLayout(10, 20);

        final Component c1 = new TestComponent(new Size(100, 50));
        c1.layoutOptions(new GridOptions(0, 0));

        final Component c2 = new TestComponent(new Size(150, 80));
        c2.layoutOptions(new GridOptions(1, 0));

        final Component c3 = new TestComponent(new Size(80, 60));
        c3.layoutOptions(new GridOptions(0, 1));

        final List<Component> components = List.of(c1, c2, c3);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        // Expected colWidths: col0 = max(100, 80) = 100, col1 = 150
        // Expected rowHeights: row0 = max(50, 80) = 80, row1 = 60
        // Total Ideal Width = 100 + 150 + 10 (horizontalGap) = 260
        // Total Ideal Height = 80 + 60 + 20 (verticalGap) = 160
        assertEquals(new Size(260, 160), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(ideal, components, bounds::put);

        // Column offsets: col0 = 0, col1 = 100 + 10 = 110
        // Row offsets: row0 = 0, row1 = 80 + 20 = 100
        // c1: col 0, row 0. Cell: (0, 0, 100, 80). Ideal size: (100, 50). Anchor CENTER.
        // x = 0 + (100 - 100)/2 = 0. y = 0 + (80 - 50)/2 = 15. Width = 100, Height = 50.
        assertRectangleEquals(new Rectangle(0, 15, 100, 50), bounds.get(c1), 1e-4f);

        // c2: col 1, row 0. Cell: (110, 0, 150, 80). Ideal size: (150, 80). Anchor CENTER.
        // x = 110 + (150 - 150)/2 = 110. y = 0 + (80 - 80)/2 = 0. Width = 150, Height = 80.
        assertRectangleEquals(new Rectangle(110, 0, 150, 80), bounds.get(c2), 1e-4f);

        // c3: col 0, row 1. Cell: (0, 100, 100, 60). Ideal size: (80, 60). Anchor CENTER.
        // x = 0 + (100 - 80)/2 = 10. y = 100 + (60 - 60)/2 = 100. Width = 80, Height = 60.
        assertRectangleEquals(new Rectangle(10, 100, 80, 60), bounds.get(c3), 1e-4f);
    }

    @Test
    void testSpanningComponents() {
        final GridLayout layout = new GridLayout(10, 10);

        final Component c1 = new TestComponent(new Size(50, 50));
        c1.layoutOptions(new GridOptions(0, 0));

        final Component c2 = new TestComponent(new Size(50, 50));
        c2.layoutOptions(new GridOptions(1, 0));

        // Spans column 0 & 1, row 1.
        final Component cSpanning = new TestComponent(new Size(150, 40));
        cSpanning.layoutOptions(new GridOptions(0, 1, 2, 1, 0, 0, GridAnchor.CENTER, GridFill.NONE, Padding.EMPTY));

        final List<Component> components = List.of(c1, c2, cSpanning);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        // 1st pass: non-spanning columns: col0 = 50, col1 = 50. Total non-spanning width = 100 + 10 = 110.
        // 2nd pass: spanning: cSpanning needs 150. Current is 110.
        // Diff = 40. Distribute 20 to each column.
        // Final colWidths: col0 = 70, col1 = 70.
        // RowHeights: row0 = 50, row1 = 40.
        // Ideal Size: Width = 70 + 70 + 10 = 150. Height = 50 + 40 + 10 = 100.
        assertEquals(new Size(150, 100), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(ideal, components, bounds::put);

        // cSpanning cell: starts at col0 = 0. Ends at col2 - gap = colWidth[0] + colWidth[1] + gap = 70 + 70 + 10 = 150.
        // Cell rect: (0, 60, 150, 40). Component: (150, 40).
        assertRectangleEquals(new Rectangle(0, 60, 150, 40), bounds.get(cSpanning), 1e-4f);
    }

    @Test
    void testHomogeneousRowsAndColumns() {
        final GridLayout layout = new GridLayout(10, 10, true, true, false, false);

        final Component c1 = new TestComponent(new Size(50, 30));
        c1.layoutOptions(new GridOptions(0, 0));

        final Component c2 = new TestComponent(new Size(100, 80));
        c2.layoutOptions(new GridOptions(1, 1));

        final List<Component> components = List.of(c1, c2);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        // Without homogeneity: col0=50, col1=100; row0=30, row1=80.
        // With homogeneity: col0=100, col1=100; row0=80, row1=80.
        // Ideal Width = 100 + 100 + 10 = 210.
        // Ideal Height = 80 + 80 + 10 = 170.
        assertEquals(new Size(210, 170), ideal);
    }

    @Test
    void testWeightsDistribution() {
        // Not homogeneous, no shrink, growing enabled
        final GridLayout layout = new GridLayout(10, 10);

        final Component c1 = new TestComponent(new Size(100, 50));
        c1.layoutOptions(new GridOptions(0, 0).withWeightX(1.0f).withWeightY(1.0f));

        final Component c2 = new TestComponent(new Size(100, 50));
        c2.layoutOptions(new GridOptions(1, 0).withWeightX(3.0f).withWeightY(0.0f));

        final List<Component> components = List.of(c1, c2);
        // Ideal is Width = 100 + 100 + 10 = 210, Height = 50.
        // Let's layout inside container of Width = 310, Height = 150.
        // Extra width = 100. weightX total = 4.0. col0 gets 25, col1 gets 75.
        // Final col0 = 125, col1 = 175.
        // Extra height = 100. weightY total = 1.0. row0 gets 100.
        // Final row0 = 150.
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(310, 150), components, bounds::put);

        // col0 offset = 0, col1 offset = 125 + 10 = 135.
        // c1: Cell is (0, 0, 125, 150). Ideal component size: (100, 50). Anchor CENTER.
        // x = (125 - 100)/2 = 12.5. y = (150 - 50)/2 = 50.
        assertRectangleEquals(new Rectangle(12.5f, 50f, 100, 50), bounds.get(c1), 1e-4f);

        // c2: Cell is (135, 0, 175, 150). Ideal component size: (100, 50). Anchor CENTER.
        // x = 135 + (175 - 100)/2 = 172.5. y = (150 - 50)/2 = 50.
        assertRectangleEquals(new Rectangle(172.5f, 50f, 100, 50), bounds.get(c2), 1e-4f);
    }

    @Test
    void testShrinking() {
        final GridLayout layout = new GridLayout(10, 10, false, false, true, true);

        final Component c1 = new TestComponent(new Size(100, 100));
        c1.layoutOptions(new GridOptions(0, 0));

        final Component c2 = new TestComponent(new Size(100, 100));
        c2.layoutOptions(new GridOptions(1, 0));

        final List<Component> components = List.of(c1, c2);
        // Ideal size = 100 + 100 + 10 = 210 (Width), 100 (Height).
        // Let's layout inside container of Width = 110, Height = 60.
        // Horizontal gaps = 10. Available width = 110 - 10 = 100.
        // Original widths sum = 200. Scale = 100/200 = 0.5.
        // ColWidths become 50 each.
        // Vertical gaps = 0. Available height = 60 - 0 = 60.
        // Original height sum = 100. Scale = 60/100 = 0.6.
        // Row heights become 60.
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(110, 60), components, bounds::put);

        System.out.println("testShrinking - c1 bounds: " + bounds.get(c1));
        System.out.println("testShrinking - c2 bounds: " + bounds.get(c2));

        // c1: Cell is (0, 0, 50, 60). Ideal is (100, 100).
        // Component gets clamped to cell size: max width = 50, max height = 60.
        // x = 0, y = 0, width = 50, height = 60.
        assertRectangleEquals(new Rectangle(0, 0, 50, 60), bounds.get(c1), 1e-4f);

        // c2: Cell is (50 + 10 = 60, 0, 50, 60).
        // x = 60, y = 0, width = 50, height = 60.
        assertRectangleEquals(new Rectangle(60, 0, 50, 60), bounds.get(c2), 1e-4f);
    }

    @Test
    void testPaddingAndAlignment() {
        final GridLayout layout = new GridLayout(0, 0);

        final Component c = new TestComponent(new Size(40, 30));
        c.layoutOptions(new GridOptions(0, 0)
                .withPadding(new Padding(20, 10, 20, 10))
                .withAnchor(GridAnchor.BOTTOM_RIGHT)
                .withWeightX(1.0f)
                .withWeightY(1.0f));

        final List<Component> components = List.of(c);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        // Ideal component width = 40 + padding (20 left + 20 right) = 80.
        // Ideal component height = 30 + padding (10 top + 10 bottom) = 50.
        assertEquals(new Size(80, 50), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        // Layout in larger size: 100 x 80.
        // Col0 gets 100, Row0 gets 80.
        // cellWidth = 100, cellHeight = 80.
        // availableWidth = 100 - 40 (horizontal padding) = 60.
        // availableHeight = 80 - 20 (vertical padding) = 60.
        // Component ideal width = 40, height = 30.
        // Anchor BOTTOM_RIGHT:
        // x = cellX + padding.left() + (availableWidth - width) = 0 + 20 + (60 - 40) = 40.
        // y = cellY + padding.top() + (availableHeight - height) = 0 + 10 + (60 - 30) = 40.
        layout.layoutComponents(new Size(100, 80), components, bounds::put);

        System.out.println("testPaddingAndAlignment - c bounds: " + bounds.get(c));
        assertRectangleEquals(new Rectangle(40, 40, 40, 30), bounds.get(c), 1e-4f);
    }

    @Test
    void testEmptyRowsAndColumnsCompression() {
        final GridLayout layout = new GridLayout(10, 20);

        final Component c1 = new TestComponent(new Size(100, 50));
        c1.layoutOptions(new GridOptions(1, 1));

        final Component c2 = new TestComponent(new Size(150, 80));
        c2.layoutOptions(new GridOptions(2, 2));

        final List<Component> components = List.of(c1, c2);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        assertEquals(new Size(260, 150), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(ideal, components, bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 100, 50), bounds.get(c1), 1e-4f);
        assertRectangleEquals(new Rectangle(110, 70, 150, 80), bounds.get(c2), 1e-4f);
    }

    @Test
    void testWeightedRowsShrinkOnShortage() {
        final GridLayout layout = new GridLayout(0, 0);

        final Component c1 = new TestComponent(new Size(100, 100));
        c1.layoutOptions(new GridOptions(0, 0, 1, 1, 0.0f, 0.0f, GridAnchor.CENTER, GridFill.BOTH, Padding.EMPTY));

        final Component c2 = new TestComponent(new Size(100, 300));
        c2.layoutOptions(new GridOptions(0, 1, 1, 1, 0.0f, 1.0f, GridAnchor.CENTER, GridFill.BOTH, Padding.EMPTY));

        final List<Component> components = List.of(c1, c2);

        final Size containerSize = new Size(100, 200);
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 100, 100), bounds.get(c1), 1e-4f);
        assertRectangleEquals(new Rectangle(0, 100, 100, 100), bounds.get(c2), 1e-4f);
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
