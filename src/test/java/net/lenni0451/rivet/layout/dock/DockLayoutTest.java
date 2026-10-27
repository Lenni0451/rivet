package net.lenni0451.rivet.layout.dock;

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

class DockLayoutTest {

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
        final DockLayout layout = DockLayout.DEFAULT;
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
    void testSingleComponentAllPositions() {
        final DockLayout layout = DockLayout.DEFAULT;
        final Size containerSize = new Size(500, 400);

        // LEFT
        final Component cLeft = new TestComponent(120, 80).layoutOptions(DockPosition.LEFT);
        final Map<Component, Rectangle> boundsLeft = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(cLeft), boundsLeft::put);
        assertRectangleEquals(new Rectangle(0, 0, 120, 400), boundsLeft.get(cLeft));
        assertEquals(new Size(120, 80), layout.computeIdealSize(containerSize, List.of(cLeft)));

        // RIGHT
        final Component cRight = new TestComponent(140, 90).layoutOptions(DockPosition.RIGHT);
        final Map<Component, Rectangle> boundsRight = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(cRight), boundsRight::put);
        assertRectangleEquals(new Rectangle(360, 0, 140, 400), boundsRight.get(cRight));
        assertEquals(new Size(140, 90), layout.computeIdealSize(containerSize, List.of(cRight)));

        // TOP
        final Component cTop = new TestComponent(150, 70).layoutOptions(DockPosition.TOP);
        final Map<Component, Rectangle> boundsTop = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(cTop), boundsTop::put);
        assertRectangleEquals(new Rectangle(0, 0, 500, 70), boundsTop.get(cTop));
        assertEquals(new Size(150, 70), layout.computeIdealSize(containerSize, List.of(cTop)));

        // BOTTOM
        final Component cBottom = new TestComponent(160, 85).layoutOptions(DockPosition.BOTTOM);
        final Map<Component, Rectangle> boundsBottom = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(cBottom), boundsBottom::put);
        assertRectangleEquals(new Rectangle(0, 315, 500, 85), boundsBottom.get(cBottom));
        assertEquals(new Size(160, 85), layout.computeIdealSize(containerSize, List.of(cBottom)));

        // CENTER
        final Component cCenter = new TestComponent(200, 150).layoutOptions(DockPosition.CENTER);
        final Map<Component, Rectangle> boundsCenter = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(cCenter), boundsCenter::put);
        assertRectangleEquals(new Rectangle(0, 0, 500, 400), boundsCenter.get(cCenter));
        assertEquals(new Size(200, 150), layout.computeIdealSize(containerSize, List.of(cCenter)));
    }

    @Test
    void testSequentialDockingOrderSensitivity() {
        final DockLayout layout = DockLayout.DEFAULT;
        final Size containerSize = new Size(500, 400);

        // Case A: [TOP, LEFT] -> TOP takes full width, LEFT sits below TOP
        final Component topA = new TestComponent(100, 60).layoutOptions(DockPosition.TOP);
        final Component leftA = new TestComponent(80, 100).layoutOptions(DockPosition.LEFT);

        final Map<Component, Rectangle> boundsA = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(topA, leftA), boundsA::put);
        assertRectangleEquals(new Rectangle(0, 0, 500, 60), boundsA.get(topA));
        assertRectangleEquals(new Rectangle(0, 60, 80, 340), boundsA.get(leftA));
        assertEquals(new Size(100, 160), layout.computeIdealSize(new Size(1000, 1000), List.of(topA, leftA)));

        // Case B: [LEFT, TOP] -> LEFT takes full height, TOP sits to the right of LEFT
        final Component leftB = new TestComponent(80, 100).layoutOptions(DockPosition.LEFT);
        final Component topB = new TestComponent(100, 60).layoutOptions(DockPosition.TOP);

        final Map<Component, Rectangle> boundsB = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(leftB, topB), boundsB::put);
        assertRectangleEquals(new Rectangle(0, 0, 80, 400), boundsB.get(leftB));
        assertRectangleEquals(new Rectangle(80, 0, 420, 60), boundsB.get(topB));
        assertEquals(new Size(180, 100), layout.computeIdealSize(new Size(1000, 1000), List.of(leftB, topB)));
    }

    @Test
    void testMultipleComponentsSameEdge() {
        final DockLayout layout = DockLayout.DEFAULT;
        final Size containerSize = new Size(600, 400);

        // Multiple LEFT components stacking horizontally
        final Component left1 = new TestComponent(80, 50).layoutOptions(DockPosition.LEFT);
        final Component left2 = new TestComponent(120, 70).layoutOptions(DockPosition.LEFT);
        final Component left3 = new TestComponent(100, 60).layoutOptions(DockPosition.LEFT);

        final Map<Component, Rectangle> boundsLeft = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(left1, left2, left3), boundsLeft::put);
        assertRectangleEquals(new Rectangle(0, 0, 80, 400), boundsLeft.get(left1));
        assertRectangleEquals(new Rectangle(80, 0, 120, 400), boundsLeft.get(left2));
        assertRectangleEquals(new Rectangle(200, 0, 100, 400), boundsLeft.get(left3));

        // Multiple TOP components stacking vertically
        final Component top1 = new TestComponent(50, 60).layoutOptions(DockPosition.TOP);
        final Component top2 = new TestComponent(70, 80).layoutOptions(DockPosition.TOP);
        final Component top3 = new TestComponent(60, 40).layoutOptions(DockPosition.TOP);

        final Map<Component, Rectangle> boundsTop = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(top1, top2, top3), boundsTop::put);
        assertRectangleEquals(new Rectangle(0, 0, 600, 60), boundsTop.get(top1));
        assertRectangleEquals(new Rectangle(0, 60, 600, 80), boundsTop.get(top2));
        assertRectangleEquals(new Rectangle(0, 140, 600, 40), boundsTop.get(top3));

        // Multiple RIGHT components stacking inward from the right
        final Component right1 = new TestComponent(90, 50).layoutOptions(DockPosition.RIGHT);
        final Component right2 = new TestComponent(110, 50).layoutOptions(DockPosition.RIGHT);

        final Map<Component, Rectangle> boundsRight = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(right1, right2), boundsRight::put);
        assertRectangleEquals(new Rectangle(510, 0, 90, 400), boundsRight.get(right1));
        assertRectangleEquals(new Rectangle(400, 0, 110, 400), boundsRight.get(right2));

        // Multiple BOTTOM components stacking inward from the bottom
        final Component bottom1 = new TestComponent(50, 70).layoutOptions(DockPosition.BOTTOM);
        final Component bottom2 = new TestComponent(50, 50).layoutOptions(DockPosition.BOTTOM);

        final Map<Component, Rectangle> boundsBottom = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(bottom1, bottom2), boundsBottom::put);
        assertRectangleEquals(new Rectangle(0, 330, 600, 70), boundsBottom.get(bottom1));
        assertRectangleEquals(new Rectangle(0, 280, 600, 50), boundsBottom.get(bottom2));
    }

    @Test
    void testAllFivePositionsCanonical() {
        final DockLayout layout = DockLayout.DEFAULT;
        final Size containerSize = new Size(600, 500);

        final Component top = new TestComponent(100, 60).layoutOptions(DockPosition.TOP);
        final Component bottom = new TestComponent(120, 80).layoutOptions(DockPosition.BOTTOM);
        final Component left = new TestComponent(100, 50).layoutOptions(DockPosition.LEFT);
        final Component right = new TestComponent(120, 50).layoutOptions(DockPosition.RIGHT);
        final Component center = new TestComponent(200, 200).layoutOptions(DockPosition.CENTER);

        final List<Component> components = List.of(top, bottom, left, right, center);
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 600, 60), bounds.get(top));
        assertRectangleEquals(new Rectangle(0, 420, 600, 80), bounds.get(bottom));
        assertRectangleEquals(new Rectangle(0, 60, 100, 360), bounds.get(left));
        assertRectangleEquals(new Rectangle(480, 60, 120, 360), bounds.get(right));
        assertRectangleEquals(new Rectangle(100, 60, 380, 360), bounds.get(center));

        final Size ideal = layout.computeIdealSize(new Size(1000, 1000), components);
        assertEquals(new Size(420, 340), ideal);
    }

    @Test
    void testLayoutAndIdealSizeWithGap() {
        final DockLayout layout = new DockLayout(10f);
        final Size containerSize = new Size(600, 500);

        final Component top = new TestComponent(100, 60).layoutOptions(DockPosition.TOP);
        final Component bottom = new TestComponent(120, 80).layoutOptions(DockPosition.BOTTOM);
        final Component left = new TestComponent(100, 50).layoutOptions(DockPosition.LEFT);
        final Component right = new TestComponent(120, 50).layoutOptions(DockPosition.RIGHT);
        final Component center = new TestComponent(200, 200).layoutOptions(DockPosition.CENTER);

        final List<Component> components = List.of(top, bottom, left, right, center);
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 600, 60), bounds.get(top));
        assertRectangleEquals(new Rectangle(0, 420, 600, 80), bounds.get(bottom));
        assertRectangleEquals(new Rectangle(0, 70, 100, 340), bounds.get(left));
        assertRectangleEquals(new Rectangle(480, 70, 120, 340), bounds.get(right));
        assertRectangleEquals(new Rectangle(110, 70, 360, 340), bounds.get(center));

        // Ideal size with gap: (100 + 10 + 120 + 10 + 200 = 440, 60 + 10 + 80 + 10 + 200 = 360)
        final Size ideal = layout.computeIdealSize(new Size(1000, 1000), components);
        assertEquals(new Size(440, 360), ideal);

        // Trailing gap subtraction verification without CENTER
        final Component left1 = new TestComponent(80, 50).layoutOptions(DockPosition.LEFT);
        final Component left2 = new TestComponent(100, 60).layoutOptions(DockPosition.LEFT);
        final DockLayout layoutGap15 = new DockLayout(15f);
        final Size idealLefts = layoutGap15.computeIdealSize(new Size(1000, 1000), List.of(left1, left2));
        assertEquals(new Size(195, 60), idealLefts);
    }

    @Test
    void testCenterConsumesRemainingAreaAndPostCenterExhaustion() {
        final DockLayout layout = new DockLayout(10f);
        final Size containerSize = new Size(500, 400);

        final Component top = new TestComponent(100, 50).layoutOptions(DockPosition.TOP);
        final Component left = new TestComponent(80, 100).layoutOptions(DockPosition.LEFT);
        final Component center = new TestComponent(100, 100).layoutOptions(DockPosition.CENTER);

        // Components placed after CENTER
        final Component post1 = new TestComponent(60, 60).layoutOptions(DockPosition.RIGHT);
        final Component post2 = new TestComponent(40, 40)
                .minSize(30, 25)
                .layoutOptions(DockPosition.TOP);

        final List<Component> components = List.of(top, left, center, post1, post2);
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 500, 50), bounds.get(top));
        assertRectangleEquals(new Rectangle(0, 60, 80, 340), bounds.get(left));
        assertRectangleEquals(new Rectangle(90, 60, 410, 340), bounds.get(center));

        // post1 receives 0 remaining dimensions (500 - 0 = 500, 400, 0, 0)
        assertRectangleEquals(new Rectangle(500, 400, 0, 0), bounds.get(post1));

        // post2 receives 0 remaining dimensions clamped to minSize (30, 25)
        assertRectangleEquals(new Rectangle(500, 400, 30, 25), bounds.get(post2));
    }

    @Test
    void testSpaceExhaustionAndOverflow() {
        final DockLayout layout = DockLayout.DEFAULT;
        final Size containerSize = new Size(150, 100);

        // Horizontal space exhaustion
        final Component left1 = new TestComponent(100, 50).layoutOptions(DockPosition.LEFT);
        final Component left2 = new TestComponent(100, 50).layoutOptions(DockPosition.LEFT);
        final Component left3 = new TestComponent(50, 50)
                .minSize(20, 30)
                .layoutOptions(DockPosition.LEFT);

        final List<Component> horizontalComponents = List.of(left1, left2, left3);
        final Map<Component, Rectangle> horizontalBounds = new HashMap<>();
        layout.layoutComponents(containerSize, horizontalComponents, horizontalBounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 100, 100), horizontalBounds.get(left1));
        assertRectangleEquals(new Rectangle(100, 0, 50, 100), horizontalBounds.get(left2));
        assertRectangleEquals(new Rectangle(150, 0, 20, 100), horizontalBounds.get(left3));

        // Vertical space exhaustion
        final Component top1 = new TestComponent(50, 60).layoutOptions(DockPosition.TOP);
        final Component top2 = new TestComponent(50, 60).layoutOptions(DockPosition.TOP);
        final Component top3 = new TestComponent(50, 50)
                .minSize(25, 15)
                .layoutOptions(DockPosition.TOP);

        final List<Component> verticalComponents = List.of(top1, top2, top3);
        final Map<Component, Rectangle> verticalBounds = new HashMap<>();
        layout.layoutComponents(containerSize, verticalComponents, verticalBounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 150, 60), verticalBounds.get(top1));
        assertRectangleEquals(new Rectangle(0, 60, 150, 40), verticalBounds.get(top2));
        assertRectangleEquals(new Rectangle(0, 100, 150, 15), verticalBounds.get(top3));
    }

    @Test
    void testMissingAndNonDockPositionOptionsDefaultToCenter() {
        final DockLayout layout = DockLayout.DEFAULT;
        final Size containerSize = new Size(400, 300);

        // Child with null options
        final Component cNull = new TestComponent(100, 80);
        // Child with foreign options
        final Component cForeign = new TestComponent(50, 50).layoutOptions(new LayoutOptions() {});

        final List<Component> components = List.of(cNull, cForeign);
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        // cNull defaults to CENTER, taking the full area
        assertRectangleEquals(new Rectangle(0, 0, 400, 300), bounds.get(cNull));
        // cForeign arrives after CENTER, getting 0 remaining dimensions
        assertRectangleEquals(new Rectangle(400, 300, 0, 0), bounds.get(cForeign));
    }

    @Test
    void testMinMaxConstraintsOnDockedComponents() {
        final DockLayout layout = DockLayout.DEFAULT;
        final Size containerSize = new Size(500, 400);

        final Component left = new TestComponent(100, 100)
                .maxSize(80, 250)
                .layoutOptions(DockPosition.LEFT);

        final Component top = new TestComponent(100, 100)
                .minSize(200, 60)
                .maxSize(300, 80)
                .layoutOptions(DockPosition.TOP);

        final List<Component> components = List.of(left, top);
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 80, 250), bounds.get(left));
        assertRectangleEquals(new Rectangle(80, 0, 300, 80), bounds.get(top));
    }

    @Test
    void testContainerContractCompliance() {
        final DockLayout layout = new DockLayout(5f);
        final Container container = new Container(layout);

        final Component top = new TestComponent(100, 40).layoutOptions(DockPosition.TOP);
        final Component left = new TestComponent(60, 80).layoutOptions(DockPosition.LEFT);
        final Component center = new TestComponent(150, 100).layoutOptions(DockPosition.CENTER);

        container.add(top);
        container.add(left);
        container.add(center);

        assertDoesNotThrow(() -> container.computeLayout(new Size(500, 400)));
        assertRectangleEquals(new Rectangle(0, 0, 500, 40), container.childBounds(top));
        assertRectangleEquals(new Rectangle(0, 45, 60, 355), container.childBounds(left));
        assertRectangleEquals(new Rectangle(65, 45, 435, 355), container.childBounds(center));

        assertEquals(new Size(500, 400), container.contentSize());
        final Size expectedIdeal = layout.computeIdealSize(new Size(500, 400), List.of(top, left, center));
        assertEquals(expectedIdeal, container.computeIdealSize(new Size(500, 400)));
    }

    @Test
    void testRecordAndFluentWithMethods() {
        final DockLayout defaultLayout = DockLayout.DEFAULT;
        assertEquals(0f, defaultLayout.gap(), DELTA);

        final DockLayout noArgLayout = new DockLayout();
        assertEquals(0f, noArgLayout.gap(), DELTA);

        final DockLayout parameterized = new DockLayout(15f);
        assertEquals(15f, parameterized.gap(), DELTA);

        final DockLayout withGap = parameterized.withGap(25f);
        assertEquals(25f, withGap.gap(), DELTA);

        final DockLayout withGapBy = withGap.withGapBy(g -> g * 2f);
        assertEquals(50f, withGapBy.gap(), DELTA);

        assertEquals(5, DockPosition.values().length);
        assertEquals(DockPosition.LEFT, DockPosition.valueOf("LEFT"));
        assertEquals(DockPosition.RIGHT, DockPosition.valueOf("RIGHT"));
        assertEquals(DockPosition.TOP, DockPosition.valueOf("TOP"));
        assertEquals(DockPosition.BOTTOM, DockPosition.valueOf("BOTTOM"));
        assertEquals(DockPosition.CENTER, DockPosition.valueOf("CENTER"));
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
