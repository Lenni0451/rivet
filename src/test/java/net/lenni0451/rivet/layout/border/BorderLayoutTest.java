package net.lenni0451.rivet.layout.border;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.layout.absolute.AbsoluteOptions;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BorderLayoutTest {

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
        final BorderLayout layout = BorderLayout.DEFAULT;
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
    void testSingleChildPositions() {
        final BorderLayout layout = BorderLayout.DEFAULT;
        final Size containerSize = new Size(800, 600);

        // TOP
        final Component topChild = new TestComponent(200, 50).layoutOptions(BorderPosition.TOP);
        final Map<Component, Rectangle> topBounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(topChild), topBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 800, 50), topBounds.get(topChild));
        assertEquals(new Size(200, 50), layout.computeIdealSize(containerSize, List.of(topChild)));

        // BOTTOM
        final Component bottomChild = new TestComponent(200, 60).layoutOptions(BorderPosition.BOTTOM);
        final Map<Component, Rectangle> bottomBounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(bottomChild), bottomBounds::put);
        assertRectangleEquals(new Rectangle(0, 540, 800, 60), bottomBounds.get(bottomChild));
        assertEquals(new Size(200, 60), layout.computeIdealSize(containerSize, List.of(bottomChild)));

        // LEFT
        final Component leftChild = new TestComponent(150, 200).layoutOptions(BorderPosition.LEFT);
        final Map<Component, Rectangle> leftBounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(leftChild), leftBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 150, 600), leftBounds.get(leftChild));
        assertEquals(new Size(150, 200), layout.computeIdealSize(containerSize, List.of(leftChild)));

        // RIGHT
        final Component rightChild = new TestComponent(120, 250).layoutOptions(BorderPosition.RIGHT);
        final Map<Component, Rectangle> rightBounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(rightChild), rightBounds::put);
        assertRectangleEquals(new Rectangle(680, 0, 120, 600), rightBounds.get(rightChild));
        assertEquals(new Size(120, 250), layout.computeIdealSize(containerSize, List.of(rightChild)));

        // CENTER
        final Component centerChild = new TestComponent(300, 200).layoutOptions(BorderPosition.CENTER);
        final Map<Component, Rectangle> centerBounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(centerChild), centerBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 800, 600), centerBounds.get(centerChild));
        assertEquals(new Size(300, 200), layout.computeIdealSize(containerSize, List.of(centerChild)));
    }

    @Test
    void testDefaultCornerPrioritiesAllRegions() {
        final BorderLayout layout = BorderLayout.DEFAULT;
        final Size containerSize = new Size(800, 600);

        final Component top = new TestComponent(200, 50).layoutOptions(BorderPosition.TOP);
        final Component bottom = new TestComponent(200, 60).layoutOptions(BorderPosition.BOTTOM);
        final Component left = new TestComponent(80, 100).layoutOptions(BorderPosition.LEFT);
        final Component right = new TestComponent(90, 120).layoutOptions(BorderPosition.RIGHT);
        final Component center = new TestComponent(300, 200).layoutOptions(BorderPosition.CENTER);
        final List<Component> components = List.of(top, bottom, left, right, center);

        final Size ideal = layout.computeIdealSize(containerSize, components);
        assertEquals(new Size(470, 310), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertEquals(5, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 800, 50), bounds.get(top));
        assertRectangleEquals(new Rectangle(0, 540, 800, 60), bounds.get(bottom));
        assertRectangleEquals(new Rectangle(0, 50, 80, 490), bounds.get(left));
        assertRectangleEquals(new Rectangle(710, 50, 90, 490), bounds.get(right));
        assertRectangleEquals(new Rectangle(80, 50, 630, 490), bounds.get(center));
    }

    @Test
    void testVerticalCornerPrioritiesAllRegions() {
        final BorderLayout layout = new BorderLayout(
                TopLeftPriority.LEFT,
                BottomLeftPriority.LEFT,
                BottomRightPriority.RIGHT,
                TopRightPriority.RIGHT
        );
        final Size containerSize = new Size(800, 600);

        final Component top = new TestComponent(200, 50).layoutOptions(BorderPosition.TOP);
        final Component bottom = new TestComponent(200, 60).layoutOptions(BorderPosition.BOTTOM);
        final Component left = new TestComponent(80, 100).layoutOptions(BorderPosition.LEFT);
        final Component right = new TestComponent(90, 120).layoutOptions(BorderPosition.RIGHT);
        final Component center = new TestComponent(300, 200).layoutOptions(BorderPosition.CENTER);
        final List<Component> components = List.of(top, bottom, left, right, center);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertEquals(5, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 80, 600), bounds.get(left));
        assertRectangleEquals(new Rectangle(710, 0, 90, 600), bounds.get(right));
        assertRectangleEquals(new Rectangle(80, 0, 630, 50), bounds.get(top));
        assertRectangleEquals(new Rectangle(80, 540, 630, 60), bounds.get(bottom));
        assertRectangleEquals(new Rectangle(80, 50, 630, 490), bounds.get(center));
    }

    @Test
    void testHybridCornerPriorities() {
        final BorderLayout layout = new BorderLayout(
                TopLeftPriority.LEFT,
                BottomLeftPriority.BOTTOM,
                BottomRightPriority.BOTTOM,
                TopRightPriority.TOP
        );
        final Size containerSize = new Size(800, 600);

        final Component top = new TestComponent(300, 40).layoutOptions(BorderPosition.TOP);
        final Component bottom = new TestComponent(350, 50).layoutOptions(BorderPosition.BOTTOM);
        final Component left = new TestComponent(100, 200).layoutOptions(BorderPosition.LEFT);
        final Component right = new TestComponent(120, 180).layoutOptions(BorderPosition.RIGHT);
        final Component center = new TestComponent(250, 220).layoutOptions(BorderPosition.CENTER);
        final List<Component> components = List.of(top, bottom, left, right, center);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertEquals(5, bounds.size());
        assertRectangleEquals(new Rectangle(100, 0, 700, 40), bounds.get(top));
        assertRectangleEquals(new Rectangle(0, 0, 100, 550), bounds.get(left));
        assertRectangleEquals(new Rectangle(0, 550, 800, 50), bounds.get(bottom));
        assertRectangleEquals(new Rectangle(680, 40, 120, 510), bounds.get(right));
        assertRectangleEquals(new Rectangle(100, 40, 580, 510), bounds.get(center));

        assertEquals(new Size(470, 310), layout.computeIdealSize(containerSize, components));
    }

    @Test
    void testPinwheelCornerPriorities() {
        final BorderLayout layout = new BorderLayout(
                TopLeftPriority.TOP,
                BottomLeftPriority.LEFT,
                BottomRightPriority.BOTTOM,
                TopRightPriority.RIGHT
        );
        final Size containerSize = new Size(800, 600);

        final Component top = new TestComponent(200, 40).layoutOptions(BorderPosition.TOP);
        final Component right = new TestComponent(120, 200).layoutOptions(BorderPosition.RIGHT);
        final Component bottom = new TestComponent(200, 50).layoutOptions(BorderPosition.BOTTOM);
        final Component left = new TestComponent(100, 200).layoutOptions(BorderPosition.LEFT);
        final Component center = new TestComponent(200, 200).layoutOptions(BorderPosition.CENTER);
        final List<Component> components = List.of(top, right, bottom, left, center);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertEquals(5, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 680, 40), bounds.get(top));
        assertRectangleEquals(new Rectangle(680, 0, 120, 550), bounds.get(right));
        assertRectangleEquals(new Rectangle(100, 550, 700, 50), bounds.get(bottom));
        assertRectangleEquals(new Rectangle(0, 40, 100, 560), bounds.get(left));
        assertRectangleEquals(new Rectangle(100, 40, 580, 510), bounds.get(center));
    }

    @Test
    void testMultipleComponentsInSamePosition() {
        final BorderLayout layout = BorderLayout.DEFAULT;
        final Size containerSize = new Size(800, 600);

        final Component top1 = new TestComponent(100, 40).layoutOptions(BorderPosition.TOP);
        final Component top2 = new TestComponent(150, 70).layoutOptions(BorderPosition.TOP);
        final Component left1 = new TestComponent(80, 100).layoutOptions(BorderPosition.LEFT);
        final Component left2 = new TestComponent(120, 80).layoutOptions(BorderPosition.LEFT);
        final Component center1 = new TestComponent(200, 150).layoutOptions(BorderPosition.CENTER);
        final Component center2 = new TestComponent(250, 180).layoutOptions(BorderPosition.CENTER);
        final List<Component> components = List.of(top1, top2, left1, left2, center1, center2);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertEquals(6, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 800, 70), bounds.get(top1));
        assertRectangleEquals(new Rectangle(0, 0, 800, 70), bounds.get(top2));
        assertRectangleEquals(new Rectangle(0, 70, 120, 530), bounds.get(left1));
        assertRectangleEquals(new Rectangle(0, 70, 120, 530), bounds.get(left2));
        assertRectangleEquals(new Rectangle(120, 70, 680, 530), bounds.get(center1));
        assertRectangleEquals(new Rectangle(120, 70, 680, 530), bounds.get(center2));
    }

    @Test
    void testMissingOrInvalidOptionsDefaultToCenter() {
        final BorderLayout layout = BorderLayout.DEFAULT;
        final Size containerSize = new Size(800, 600);

        final Component unassigned = new TestComponent(100, 50);
        final Component center = new TestComponent(120, 60).layoutOptions(BorderPosition.CENTER);
        final Component invalidOptions = new TestComponent(140, 70).layoutOptions(AbsoluteOptions.EMPTY);
        final List<Component> components = List.of(unassigned, center, invalidOptions);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertEquals(3, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 800, 600), bounds.get(unassigned));
        assertRectangleEquals(new Rectangle(0, 0, 800, 600), bounds.get(center));
        assertRectangleEquals(new Rectangle(0, 0, 800, 600), bounds.get(invalidOptions));

        final Size ideal = layout.computeIdealSize(containerSize, components);
        assertEquals(new Size(140, 70), ideal);
    }

    @Test
    void testComputeIdealSizeWithCornerPriorities() {
        final Component top = new TestComponent(500, 50).layoutOptions(BorderPosition.TOP);
        final Component bottom = new TestComponent(200, 60).layoutOptions(BorderPosition.BOTTOM);
        final Component left = new TestComponent(80, 100).layoutOptions(BorderPosition.LEFT);
        final Component right = new TestComponent(90, 120).layoutOptions(BorderPosition.RIGHT);
        final Component center = new TestComponent(300, 200).layoutOptions(BorderPosition.CENTER);
        final List<Component> components = List.of(top, bottom, left, right, center);

        final BorderLayout defaultLayout = BorderLayout.DEFAULT;
        assertEquals(new Size(500, 310), defaultLayout.computeIdealSize(new Size(1000, 1000), components));

        final BorderLayout verticalLayout = new BorderLayout(
                TopLeftPriority.LEFT,
                BottomLeftPriority.LEFT,
                BottomRightPriority.RIGHT,
                TopRightPriority.RIGHT
        );
        assertEquals(new Size(670, 310), verticalLayout.computeIdealSize(new Size(1000, 1000), components));
    }

    @Test
    void testContainerSmallerThanBorderStrips() {
        final BorderLayout layout = BorderLayout.DEFAULT;
        final Size smallContainer = new Size(100, 80);

        final Component left = new TestComponent(60, 20).layoutOptions(BorderPosition.LEFT);
        final Component right = new TestComponent(70, 20).layoutOptions(BorderPosition.RIGHT);
        final Component top = new TestComponent(20, 50).layoutOptions(BorderPosition.TOP);
        final Component bottom = new TestComponent(20, 40).layoutOptions(BorderPosition.BOTTOM);
        final Component center = new TestComponent(30, 30).layoutOptions(BorderPosition.CENTER);
        final List<Component> components = List.of(left, right, top, bottom, center);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(smallContainer, components, bounds::put);

        assertEquals(5, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 100, 50), bounds.get(top));
        assertRectangleEquals(new Rectangle(0, 40, 100, 40), bounds.get(bottom));
        assertRectangleEquals(new Rectangle(0, 50, 60, 0), bounds.get(left));
        assertRectangleEquals(new Rectangle(30, 50, 70, 0), bounds.get(right));
        assertRectangleEquals(new Rectangle(60, 50, 0, 0), bounds.get(center));

        final Component clampedCenter = new TestComponent(30, 30)
                .minSize(25, 15)
                .layoutOptions(BorderPosition.CENTER);
        final Map<Component, Rectangle> clampedBounds = new HashMap<>();
        layout.layoutComponents(smallContainer, List.of(left, right, top, bottom, clampedCenter), clampedBounds::put);
        assertRectangleEquals(new Rectangle(60, 50, 25, 15), clampedBounds.get(clampedCenter));
    }

    @Test
    void testMinAndMaxSizeClamping() {
        final BorderLayout layout = BorderLayout.DEFAULT;
        final Size containerSize = new Size(1000, 800);

        final Component top = new TestComponent(200, 30)
                .maxSize(400, 30)
                .layoutOptions(BorderPosition.TOP);
        final Component left = new TestComponent(100, 200).layoutOptions(BorderPosition.LEFT);
        final Component right = new TestComponent(100, 200).layoutOptions(BorderPosition.RIGHT);
        final Component bottom = new TestComponent(200, 50).layoutOptions(BorderPosition.BOTTOM);
        final Component center = new TestComponent(200, 200)
                .maxSize(300, 250)
                .layoutOptions(BorderPosition.CENTER);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(top, left, right, bottom, center), bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 400, 30), bounds.get(top));
        assertRectangleEquals(new Rectangle(100, 30, 300, 250), bounds.get(center));
    }

    @Test
    void testContainerContractCompliance() {
        final BorderLayout layout = BorderLayout.DEFAULT;
        final Container container = new Container(layout);

        final Component top = new TestComponent(200, 50).layoutOptions(BorderPosition.TOP);
        final Component bottom = new TestComponent(200, 60).layoutOptions(BorderPosition.BOTTOM);
        final Component left = new TestComponent(80, 100).layoutOptions(BorderPosition.LEFT);
        final Component right = new TestComponent(90, 120).layoutOptions(BorderPosition.RIGHT);
        final Component center = new TestComponent(300, 200).layoutOptions(BorderPosition.CENTER);
        final Component extraCenter = new TestComponent(100, 80).layoutOptions(BorderPosition.CENTER);

        container.add(top);
        container.add(bottom);
        container.add(left);
        container.add(right);
        container.add(center);
        container.add(extraCenter);

        assertDoesNotThrow(() -> container.computeLayout(new Size(800, 600)));

        assertRectangleEquals(new Rectangle(0, 0, 800, 50), container.childBounds(top));
        assertRectangleEquals(new Rectangle(0, 540, 800, 60), container.childBounds(bottom));
        assertRectangleEquals(new Rectangle(0, 50, 80, 490), container.childBounds(left));
        assertRectangleEquals(new Rectangle(710, 50, 90, 490), container.childBounds(right));
        assertRectangleEquals(new Rectangle(80, 50, 630, 490), container.childBounds(center));
        assertRectangleEquals(new Rectangle(80, 50, 630, 490), container.childBounds(extraCenter));

        assertEquals(new Size(800, 600), container.contentSize());
        assertEquals(new Size(470, 310), container.computeIdealSize(new Size(800, 600)));
    }

    @Test
    void testBorderLayoutRecordAndWithMethods() {
        final BorderLayout def = BorderLayout.DEFAULT;
        assertEquals(TopLeftPriority.TOP, def.topLeftPriority());
        assertEquals(BottomLeftPriority.BOTTOM, def.bottomLeftPriority());
        assertEquals(BottomRightPriority.BOTTOM, def.bottomRightPriority());
        assertEquals(TopRightPriority.TOP, def.topRightPriority());

        final BorderLayout withTL = def.withTopLeftPriority(TopLeftPriority.LEFT);
        assertEquals(TopLeftPriority.LEFT, withTL.topLeftPriority());
        assertEquals(BottomLeftPriority.BOTTOM, withTL.bottomLeftPriority());
        assertNotEquals(def, withTL);

        final BorderLayout withBL = def.withBottomLeftPriority(BottomLeftPriority.LEFT);
        assertEquals(BottomLeftPriority.LEFT, withBL.bottomLeftPriority());

        final BorderLayout withBR = def.withBottomRightPriority(BottomRightPriority.RIGHT);
        assertEquals(BottomRightPriority.RIGHT, withBR.bottomRightPriority());

        final BorderLayout withTR = def.withTopRightPriority(TopRightPriority.RIGHT);
        assertEquals(TopRightPriority.RIGHT, withTR.topRightPriority());

        final BorderLayout custom = new BorderLayout(
                TopLeftPriority.LEFT,
                BottomLeftPriority.LEFT,
                BottomRightPriority.RIGHT,
                TopRightPriority.RIGHT
        );
        assertEquals(custom, def.withTopLeftPriority(TopLeftPriority.LEFT)
                .withBottomLeftPriority(BottomLeftPriority.LEFT)
                .withBottomRightPriority(BottomRightPriority.RIGHT)
                .withTopRightPriority(TopRightPriority.RIGHT));
        assertEquals(custom.hashCode(), custom.hashCode());
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
