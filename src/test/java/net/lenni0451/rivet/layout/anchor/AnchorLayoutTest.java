package net.lenni0451.rivet.layout.anchor;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnchorLayoutTest {

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
        final AnchorLayout layout = AnchorLayout.INSTANCE;
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
    void testDefaultOptionsFallback() {
        final AnchorLayout layout = AnchorLayout.INSTANCE;
        final Component child = new TestComponent(120, 80);
        final Size containerSize = new Size(800, 600);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);

        assertEquals(1, bounds.size());
        assertRectangleEquals(new Rectangle(0, 0, 120, 80), bounds.get(child));
    }

    @Test
    void testPointAnchorsCornersAndCenter() {
        final AnchorLayout layout = AnchorLayout.INSTANCE;
        final Size containerSize = new Size(800, 600);

        final Component cTopLeft = new TestComponent(100, 50);
        cTopLeft.layoutOptions(AnchorOptions.EMPTY.point(0, 0).pivot(0, 0));

        final Component cTopRight = new TestComponent(100, 50);
        cTopRight.layoutOptions(AnchorOptions.EMPTY.point(1, 0).pivot(1, 0));

        final Component cBottomLeft = new TestComponent(100, 50);
        cBottomLeft.layoutOptions(AnchorOptions.EMPTY.point(0, 1).pivot(0, 1));

        final Component cBottomRight = new TestComponent(100, 50);
        cBottomRight.layoutOptions(AnchorOptions.EMPTY.point(1, 1).pivot(1, 1));

        final Component cCenter = new TestComponent(100, 50);
        cCenter.layoutOptions(AnchorOptions.EMPTY.point(0.5f, 0.5f).pivot(0.5f, 0.5f));

        final List<Component> components = List.of(cTopLeft, cTopRight, cBottomLeft, cBottomRight, cCenter);
        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 100, 50), bounds.get(cTopLeft));
        assertRectangleEquals(new Rectangle(700, 0, 100, 50), bounds.get(cTopRight));
        assertRectangleEquals(new Rectangle(0, 550, 100, 50), bounds.get(cBottomLeft));
        assertRectangleEquals(new Rectangle(700, 550, 100, 50), bounds.get(cBottomRight));
        assertRectangleEquals(new Rectangle(350, 275, 100, 50), bounds.get(cCenter));
    }

    @Test
    void testPointAnchorWithOffsets() {
        final AnchorLayout layout = AnchorLayout.INSTANCE;
        final Size containerSize = new Size(800, 600);

        final Component c1 = new TestComponent(100, 50);
        c1.layoutOptions(AnchorOptions.EMPTY.point(1, 1).pivot(1, 1).offset(10, 20, 30, 40));

        final Component c2 = new TestComponent(80, 40);
        c2.layoutOptions(AnchorOptions.EMPTY.point(0.5f, 0.5f).pivot(0.5f, 0.5f).offset(20, -10, 0, 0));

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(c1, c2), bounds::put);

        // c1: x = 800 + 10 - 30 - 100 = 680, y = 600 + 20 - 40 - 50 = 530
        assertRectangleEquals(new Rectangle(680, 530, 100, 50), bounds.get(c1));
        // c2: x = 400 + 20 - 0 - 40 = 380, y = 300 + (-10) - 0 - 20 = 270
        assertRectangleEquals(new Rectangle(380, 270, 80, 40), bounds.get(c2));
    }

    @Test
    void testStretchAnchorsFullAndInsets() {
        final AnchorLayout layout = AnchorLayout.INSTANCE;
        final Size containerSize = new Size(800, 600);

        final Component cFull = new TestComponent(100, 50);
        cFull.layoutOptions(AnchorOptions.EMPTY.anchor(0, 0, 1, 1));

        final Component cInsets = new TestComponent(100, 50);
        cInsets.layoutOptions(AnchorOptions.EMPTY.anchor(0, 0, 1, 1).offset(10, 20, 30, 40));

        final Component cPartial = new TestComponent(100, 50);
        cPartial.layoutOptions(AnchorOptions.EMPTY.anchor(0.2f, 0.25f, 0.8f, 0.75f));

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(cFull, cInsets, cPartial), bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 800, 600), bounds.get(cFull));
        assertRectangleEquals(new Rectangle(10, 20, 760, 540), bounds.get(cInsets));
        assertRectangleEquals(new Rectangle(160, 150, 480, 300), bounds.get(cPartial));
    }

    @Test
    void testHybridAnchors() {
        final AnchorLayout layout = AnchorLayout.INSTANCE;
        final Size containerSize = new Size(800, 600);

        // Stretch X, Point Y
        final Component cStretchXPointY = new TestComponent(100, 50);
        cStretchXPointY.layoutOptions(AnchorOptions.EMPTY.from(0, 0).to(1, 0).offset(15, 10, 25, 0).pivot(0, 0));

        // Point X, Stretch Y
        final Component cPointXStretchY = new TestComponent(100, 50);
        cPointXStretchY.layoutOptions(AnchorOptions.EMPTY.from(1, 0).to(1, 1).offset(0, 20, 10, 30).pivot(1, 0));

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(cStretchXPointY, cPointXStretchY), bounds::put);

        assertRectangleEquals(new Rectangle(15, 10, 760, 50), bounds.get(cStretchXPointY));
        assertRectangleEquals(new Rectangle(690, 20, 100, 550), bounds.get(cPointXStretchY));
    }

    @Test
    void testPivotSlackAlignmentWithMaxSize() {
        final AnchorLayout layout = AnchorLayout.INSTANCE;
        final Size containerSize = new Size(800, 600);

        final Component cLeft = new TestComponent(100, 50).maxSize(new Size(400, 200));
        cLeft.layoutOptions(AnchorOptions.EMPTY.anchor(0, 0, 1, 1).pivot(0, 0));

        final Component cCenter = new TestComponent(100, 50).maxSize(new Size(400, 200));
        cCenter.layoutOptions(AnchorOptions.EMPTY.anchor(0, 0, 1, 1).pivot(0.5f, 0.5f));

        final Component cRight = new TestComponent(100, 50).maxSize(new Size(400, 200));
        cRight.layoutOptions(AnchorOptions.EMPTY.anchor(0, 0, 1, 1).pivot(1.0f, 1.0f));

        final Component cAsymmetric = new TestComponent(100, 50).maxSize(new Size(400, 200));
        cAsymmetric.layoutOptions(AnchorOptions.EMPTY.anchor(0, 0, 1, 1).pivot(0.25f, 0.75f));

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(cLeft, cCenter, cRight, cAsymmetric), bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 400, 200), bounds.get(cLeft));
        assertRectangleEquals(new Rectangle(200, 200, 400, 200), bounds.get(cCenter));
        assertRectangleEquals(new Rectangle(400, 400, 400, 200), bounds.get(cRight));
        assertRectangleEquals(new Rectangle(100, 300, 400, 200), bounds.get(cAsymmetric));
    }

    @Test
    void testInvertedAnchors() {
        final AnchorLayout layout = AnchorLayout.INSTANCE;
        final Size containerSize = new Size(600, 400);

        final Component cInverted = new TestComponent(80, 50);
        cInverted.layoutOptions(AnchorOptions.EMPTY.from(0.75f, 0.6f).to(0.25f, 0.1f).offset(10, 5, 20, 15).pivot(0.5f, 0.5f));

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(cInverted), bounds::put);

        // minX = 450, maxX = 150 (point anchor). x = 450 + 10 - 20 - (80 * 0.5) = 400
        // minY = 240, maxY = 40 (point anchor). y = 240 + 5 - 15 - (50 * 0.5) = 205
        assertRectangleEquals(new Rectangle(400, 205, 80, 50), bounds.get(cInverted));
    }

    @Test
    void testStretchNegativeAvailableSpace() {
        final AnchorLayout layout = AnchorLayout.INSTANCE;
        final Size containerSize = new Size(200, 200);

        final Component cConstrained = new TestComponent(10, 10).minSize(new Size(50, 40));
        cConstrained.layoutOptions(AnchorOptions.EMPTY.anchor(0, 0, 1, 1).offset(120, 110, 100, 100).pivot(0.5f, 0.5f));

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(cConstrained), bounds::put);

        // availableWidth = 200 - 120 - 100 = -20 -> clamped to minSize 50. x = 0 + 120 + (-20 - 50) * 0.5 = 85
        // availableHeight = 200 - 110 - 100 = -10 -> clamped to minSize 40. y = 0 + 110 + (-10 - 40) * 0.5 = 85
        assertRectangleEquals(new Rectangle(85, 85, 50, 40), bounds.get(cConstrained));
    }

    @Test
    void testComputeIdealSizeInvariance() {
        final AnchorLayout layout = AnchorLayout.INSTANCE;

        final Component c1 = new TestComponent(100, 50);
        c1.layoutOptions(AnchorOptions.EMPTY.point(1, 1).offset(999, 999, 999, 999));

        final Component c2 = new TestComponent(150, 80);
        c2.layoutOptions(AnchorOptions.EMPTY.anchor(0, 0, 1, 1));

        final Component c3 = new TestComponent(40, 120);

        final Component c4 = new TestComponent(200, 30).maxSize(new Size(180, 30));
        final Component c5 = new TestComponent(30, 40).minSize(new Size(50, 130));

        final List<Component> components = List.of(c1, c2, c3, c4, c5);
        final Size ideal = layout.computeIdealSize(new Size(800, 600), components);

        // Widths: max(100, 150, 40, 180, 50) = 180
        // Heights: max(50, 80, 120, 30, 130) = 130
        assertEquals(new Size(180, 130), ideal);
    }

    @Test
    void testContainerContract() {
        final AnchorLayout layout = AnchorLayout.INSTANCE;
        final Container container = new Container(layout);

        final Component c1 = new TestComponent(50, 50);
        c1.layoutOptions(AnchorOptions.EMPTY.point(0.5f, 0.5f).pivot(0.5f, 0.5f));

        final Component c2 = new TestComponent(100, 100);
        c2.layoutOptions(AnchorOptions.EMPTY.anchor(0, 0, 1, 1));

        final Component c3 = new TestComponent(60, 40);

        container.add(c1);
        container.add(c2);
        container.add(c3);

        assertDoesNotThrow(() -> container.computeLayout(new Size(500, 400)));
        assertRectangleEquals(new Rectangle(225, 175, 50, 50), container.childBounds(c1));
        assertRectangleEquals(new Rectangle(0, 0, 500, 400), container.childBounds(c2));
        assertRectangleEquals(new Rectangle(0, 0, 60, 40), container.childBounds(c3));
    }

    @Test
    void testAnchorOptionsConstructorsAndFluentHelpers() {
        final AnchorOptions empty = AnchorOptions.EMPTY;
        assertEquals(0f, empty.anchorMinX(), DELTA);
        assertEquals(0f, empty.anchorMinY(), DELTA);
        assertEquals(0f, empty.anchorMaxX(), DELTA);
        assertEquals(0f, empty.anchorMaxY(), DELTA);
        assertEquals(0f, empty.offsetLeft(), DELTA);
        assertEquals(0f, empty.offsetTop(), DELTA);
        assertEquals(0f, empty.offsetRight(), DELTA);
        assertEquals(0f, empty.offsetBottom(), DELTA);
        assertEquals(0f, empty.pivotX(), DELTA);
        assertEquals(0f, empty.pivotY(), DELTA);

        final AnchorOptions fourArg = new AnchorOptions(0.1f, 0.2f, 0.8f, 0.9f);
        assertEquals(0.1f, fourArg.anchorMinX(), DELTA);
        assertEquals(0.2f, fourArg.anchorMinY(), DELTA);
        assertEquals(0.8f, fourArg.anchorMaxX(), DELTA);
        assertEquals(0.9f, fourArg.anchorMaxY(), DELTA);
        assertEquals(0f, fourArg.offsetLeft(), DELTA);

        final AnchorOptions configured = AnchorOptions.EMPTY
                .anchor(0.2f, 0.3f, 0.7f, 0.8f)
                .offset(10, 20, 30, 40)
                .pivot(0.5f, 0.6f);
        assertEquals(0.2f, configured.anchorMinX(), DELTA);
        assertEquals(0.3f, configured.anchorMinY(), DELTA);
        assertEquals(0.7f, configured.anchorMaxX(), DELTA);
        assertEquals(0.8f, configured.anchorMaxY(), DELTA);
        assertEquals(10f, configured.offsetLeft(), DELTA);
        assertEquals(20f, configured.offsetTop(), DELTA);
        assertEquals(30f, configured.offsetRight(), DELTA);
        assertEquals(40f, configured.offsetBottom(), DELTA);
        assertEquals(0.5f, configured.pivotX(), DELTA);
        assertEquals(0.6f, configured.pivotY(), DELTA);

        final AnchorOptions modified = configured
                .withAnchorMinX(0.1f)
                .withAnchorMinY(0.15f)
                .withAnchorMaxX(0.85f)
                .withAnchorMaxY(0.95f)
                .withOffsetLeft(5f)
                .withOffsetTop(6f)
                .withOffsetRight(7f)
                .withOffsetBottom(8f)
                .withPivotX(0.25f)
                .withPivotY(0.75f);
        assertEquals(0.1f, modified.anchorMinX(), DELTA);
        assertEquals(0.15f, modified.anchorMinY(), DELTA);
        assertEquals(0.85f, modified.anchorMaxX(), DELTA);
        assertEquals(0.95f, modified.anchorMaxY(), DELTA);
        assertEquals(5f, modified.offsetLeft(), DELTA);
        assertEquals(6f, modified.offsetTop(), DELTA);
        assertEquals(7f, modified.offsetRight(), DELTA);
        assertEquals(8f, modified.offsetBottom(), DELTA);
        assertEquals(0.25f, modified.pivotX(), DELTA);
        assertEquals(0.75f, modified.pivotY(), DELTA);
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
