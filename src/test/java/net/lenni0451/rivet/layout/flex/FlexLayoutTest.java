package net.lenni0451.rivet.layout.flex;

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

class FlexLayoutTest {

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
        final FlexLayout layout = FlexLayout.DEFAULT;
        final Size ideal = layout.computeIdealSize(new Size(800, 600), List.of());
        assertEquals(Size.EMPTY, ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(new Size(800, 600), List.of(), bounds::put);
        assertTrue(bounds.isEmpty());

        final Container container = new Container(layout);
        assertDoesNotThrow(() -> container.computeLayout(new Size(800, 600)));
        assertEquals(Size.EMPTY, container.contentSize());

        // Also test with non-zero gaps
        final FlexLayout gappedLayout = layout.withRowGap(20).withColumnGap(15);
        assertEquals(Size.EMPTY, gappedLayout.computeIdealSize(new Size(800, 600), List.of()));
        final Map<Component, Rectangle> gappedBounds = new HashMap<>();
        gappedLayout.layoutComponents(new Size(800, 600), List.of(), gappedBounds::put);
        assertTrue(gappedBounds.isEmpty());
    }

    @Test
    void testSingleChildDefaultOptions() {
        final FlexLayout layout = FlexLayout.DEFAULT;
        final Size containerSize = new Size(500, 400);

        final Component child = new TestComponent(120, 80);
        final Size ideal = layout.computeIdealSize(containerSize, List.of(child));
        assertEquals(new Size(120, 80), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 120, 80), bounds.get(child));

        // Column direction
        final FlexLayout colLayout = layout.withDirection(FlexDirection.COLUMN);
        final Size colIdeal = colLayout.computeIdealSize(containerSize, List.of(child));
        assertEquals(new Size(120, 80), colIdeal);

        final Map<Component, Rectangle> colBounds = new HashMap<>();
        colLayout.layoutComponents(containerSize, List.of(child), colBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 120, 80), colBounds.get(child));

        // Container contract
        final Container container = new Container(layout);
        container.add(child);
        container.computeLayout(containerSize);
        assertRectangleEquals(new Rectangle(0, 0, 120, 80), container.childBounds(child));
        assertEquals(new Size(120, 80), container.contentSize());
    }

    @Test
    void testSingleChildStretchAndGrow() {
        final FlexLayout layout = FlexLayout.DEFAULT.withAlignItems(FlexAlignItems.STRETCH);
        final Size containerSize = new Size(500, 400);

        final Component child = new TestComponent(120, 80)
                .layoutOptions(FlexOptions.DEFAULT.withGrow(1));

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(child), bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 500, 400), bounds.get(child));

        // In COLUMN direction: height grows to 400, width stretches to 500
        final FlexLayout colLayout = layout.withDirection(FlexDirection.COLUMN);
        final Map<Component, Rectangle> colBounds = new HashMap<>();
        colLayout.layoutComponents(containerSize, List.of(child), colBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 500, 400), colBounds.get(child));
    }

    @Test
    void testComponentOrdering() {
        final FlexLayout layout = FlexLayout.DEFAULT.withColumnGap(10);
        final Size containerSize = new Size(500, 300);

        final Component c1 = new TestComponent(80, 50).layoutOptions(FlexOptions.DEFAULT.withOrder(2));
        final Component c2 = new TestComponent(60, 50).layoutOptions(FlexOptions.DEFAULT.withOrder(-1));
        final Component c3 = new TestComponent(70, 50).layoutOptions(FlexOptions.DEFAULT.withOrder(1));

        // Add in random order: C1, C2, C3 -> Expected order: C2 (-1), C3 (1), C1 (2)
        final List<Component> components = List.of(c1, c2, c3);
        final Size ideal = layout.computeIdealSize(containerSize, components);
        // Ideal main = 60 + 10 + 70 + 10 + 80 = 230, cross = 50
        assertEquals(new Size(230, 50), ideal);

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, components, bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 60, 50), bounds.get(c2));
        assertRectangleEquals(new Rectangle(70, 0, 70, 50), bounds.get(c3));
        assertRectangleEquals(new Rectangle(150, 0, 80, 50), bounds.get(c1));

        // Stable sort for ties: C_A and C_B both have order 0
        final Component ca = new TestComponent(50, 40).layoutOptions(FlexOptions.DEFAULT.withOrder(0));
        final Component cb = new TestComponent(50, 40).layoutOptions(FlexOptions.DEFAULT.withOrder(0));
        final Map<Component, Rectangle> tieBounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(ca, cb), tieBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 50, 40), tieBounds.get(ca));
        assertRectangleEquals(new Rectangle(60, 0, 50, 40), tieBounds.get(cb));

        // Fallback for component with non-FlexOptions (e.g. AbsoluteOptions) defaults to order 0
        final Component cNone = new TestComponent(40, 50).layoutOptions(new AbsoluteOptions(0, 0));
        final Map<Component, Rectangle> mixedBounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(c1, cNone, c2), mixedBounds::put);
        // C2 (-1) first at 0, CNone (0) second at 60 + 10 = 70, C1 (2) third at 70 + 40 + 10 = 120
        assertRectangleEquals(new Rectangle(0, 0, 60, 50), mixedBounds.get(c2));
        assertRectangleEquals(new Rectangle(70, 0, 40, 50), mixedBounds.get(cNone));
        assertRectangleEquals(new Rectangle(120, 0, 80, 50), mixedBounds.get(c1));
    }

    @Test
    void testFlexDirections() {
        final Size containerSize = new Size(500, 300);
        final Component c1 = new TestComponent(100, 50);
        final Component c2 = new TestComponent(150, 60);
        final List<Component> components = List.of(c1, c2);

        // ROW
        final FlexLayout rowLayout = FlexLayout.DEFAULT.withDirection(FlexDirection.ROW);
        assertEquals(new Size(250, 60), rowLayout.computeIdealSize(containerSize, components));
        final Map<Component, Rectangle> rowBounds = new HashMap<>();
        rowLayout.layoutComponents(containerSize, components, rowBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 50), rowBounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 0, 150, 60), rowBounds.get(c2));

        // ROW_REVERSE: items reversed along main axis
        final FlexLayout rowRevLayout = FlexLayout.DEFAULT.withDirection(FlexDirection.ROW_REVERSE);
        assertEquals(new Size(250, 60), rowRevLayout.computeIdealSize(containerSize, components));
        final Map<Component, Rectangle> rowRevBounds = new HashMap<>();
        rowRevLayout.layoutComponents(containerSize, components, rowRevBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 150, 60), rowRevBounds.get(c2));
        assertRectangleEquals(new Rectangle(150, 0, 100, 50), rowRevBounds.get(c1));

        // COLUMN: main axis Y, cross axis X
        final FlexLayout colLayout = FlexLayout.DEFAULT.withDirection(FlexDirection.COLUMN);
        assertEquals(new Size(150, 110), colLayout.computeIdealSize(containerSize, components));
        final Map<Component, Rectangle> colBounds = new HashMap<>();
        colLayout.layoutComponents(containerSize, components, colBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 50), colBounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 50, 150, 60), colBounds.get(c2));

        // COLUMN_REVERSE: items reversed along Y axis
        final FlexLayout colRevLayout = FlexLayout.DEFAULT.withDirection(FlexDirection.COLUMN_REVERSE);
        assertEquals(new Size(150, 110), colRevLayout.computeIdealSize(containerSize, components));
        final Map<Component, Rectangle> colRevBounds = new HashMap<>();
        colRevLayout.layoutComponents(containerSize, components, colRevBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 150, 60), colRevBounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 60, 100, 50), colRevBounds.get(c1));
    }

    @Test
    void testFlexWrapModes() {
        final Size containerSize = new Size(500, 400);
        final Component c1 = new TestComponent(200, 50);
        final Component c2 = new TestComponent(200, 60);
        final Component c3 = new TestComponent(200, 70);
        final List<Component> components = List.of(c1, c2, c3);

        // NO_WRAP: All 3 components stay on 1 line. With default shrink = 1, they shrink to fit 500 (166.6667 each)
        final FlexLayout noWrapLayout = FlexLayout.DEFAULT.withWrap(FlexWrap.NO_WRAP);
        assertEquals(new Size(600, 70), noWrapLayout.computeIdealSize(containerSize, components));
        final Map<Component, Rectangle> noWrapBounds = new HashMap<>();
        noWrapLayout.layoutComponents(containerSize, components, noWrapBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 166.6667f, 50), noWrapBounds.get(c1));
        assertRectangleEquals(new Rectangle(166.6667f, 0, 166.6667f, 60), noWrapBounds.get(c2));
        assertRectangleEquals(new Rectangle(333.3333f, 0, 166.6667f, 70), noWrapBounds.get(c3));

        // When shrink = 0, NO_WRAP items overflow container bounds without shrinking
        final Component ns1 = new TestComponent(200, 50).layoutOptions(FlexOptions.DEFAULT.withShrink(0));
        final Component ns2 = new TestComponent(200, 60).layoutOptions(FlexOptions.DEFAULT.withShrink(0));
        final Component ns3 = new TestComponent(200, 70).layoutOptions(FlexOptions.DEFAULT.withShrink(0));
        final Map<Component, Rectangle> noShrinkBounds = new HashMap<>();
        noWrapLayout.layoutComponents(containerSize, List.of(ns1, ns2, ns3), noShrinkBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 200, 50), noShrinkBounds.get(ns1));
        assertRectangleEquals(new Rectangle(200, 0, 200, 60), noShrinkBounds.get(ns2));
        assertRectangleEquals(new Rectangle(400, 0, 200, 70), noShrinkBounds.get(ns3));

        // WRAP: 500 width fits C1 (200) + C2 (200) = 400. C3 (200) wraps to line 2.
        final FlexLayout wrapLayout = FlexLayout.DEFAULT.withWrap(FlexWrap.WRAP).withRowGap(15);
        assertEquals(new Size(400, 145), wrapLayout.computeIdealSize(containerSize, components));
        final Map<Component, Rectangle> wrapBounds = new HashMap<>();
        wrapLayout.layoutComponents(containerSize, components, wrapBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 200, 50), wrapBounds.get(c1));
        assertRectangleEquals(new Rectangle(200, 0, 200, 60), wrapBounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 75, 200, 70), wrapBounds.get(c3));

        // WRAP_REVERSE: Lines reversed along cross axis. Line 2 at y = 0, Line 1 at y = 70 + 15 = 85.
        final FlexLayout wrapRevLayout = FlexLayout.DEFAULT.withWrap(FlexWrap.WRAP_REVERSE).withRowGap(15);
        assertEquals(new Size(400, 145), wrapRevLayout.computeIdealSize(containerSize, components));
        final Map<Component, Rectangle> wrapRevBounds = new HashMap<>();
        wrapRevLayout.layoutComponents(containerSize, components, wrapRevBounds::put);
        assertRectangleEquals(new Rectangle(0, 85, 200, 50), wrapRevBounds.get(c1));
        assertRectangleEquals(new Rectangle(200, 85, 200, 60), wrapRevBounds.get(c2));
        assertRectangleEquals(new Rectangle(0, 0, 200, 70), wrapRevBounds.get(c3));

        // Oversized first item in WRAP: 600px width in 500px container wraps gracefully to line 1 without loop.
        // With default shrink = 1, it shrinks to fit container main dimension (500).
        final Component oversizedDefault = new TestComponent(600, 50);
        final Map<Component, Rectangle> overDefaultBounds = new HashMap<>();
        wrapLayout.layoutComponents(containerSize, List.of(oversizedDefault), overDefaultBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 500, 50), overDefaultBounds.get(oversizedDefault));

        // With shrink = 0, it overflows the container main dimension (600).
        final Component oversizedNoShrink = new TestComponent(600, 50).layoutOptions(FlexOptions.DEFAULT.withShrink(0));
        final Map<Component, Rectangle> overNoShrinkBounds = new HashMap<>();
        wrapLayout.layoutComponents(containerSize, List.of(oversizedNoShrink), overNoShrinkBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 600, 50), overNoShrinkBounds.get(oversizedNoShrink));
    }

    @Test
    void testJustifyContentRow() {
        final Size containerSize = new Size(500, 100);
        final Component c1 = new TestComponent(100, 40);
        final Component c2 = new TestComponent(100, 40);
        final List<Component> components = List.of(c1, c2);

        // START: pos = 0
        final FlexLayout startLayout = FlexLayout.DEFAULT.withJustifyContent(FlexJustify.START);
        final Map<Component, Rectangle> startBounds = new HashMap<>();
        startLayout.layoutComponents(containerSize, components, startBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 40), startBounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 0, 100, 40), startBounds.get(c2));

        // END: pos = 500 - 200 = 300
        final FlexLayout endLayout = FlexLayout.DEFAULT.withJustifyContent(FlexJustify.END);
        final Map<Component, Rectangle> endBounds = new HashMap<>();
        endLayout.layoutComponents(containerSize, components, endBounds::put);
        assertRectangleEquals(new Rectangle(300, 0, 100, 40), endBounds.get(c1));
        assertRectangleEquals(new Rectangle(400, 0, 100, 40), endBounds.get(c2));

        // CENTER: pos = 300 / 2 = 150
        final FlexLayout centerLayout = FlexLayout.DEFAULT.withJustifyContent(FlexJustify.CENTER);
        final Map<Component, Rectangle> centerBounds = new HashMap<>();
        centerLayout.layoutComponents(containerSize, components, centerBounds::put);
        assertRectangleEquals(new Rectangle(150, 0, 100, 40), centerBounds.get(c1));
        assertRectangleEquals(new Rectangle(250, 0, 100, 40), centerBounds.get(c2));

        // SPACE_BETWEEN: spacing = 300 / 1 = 300 -> C1 at 0, C2 at 400
        final FlexLayout spaceBetweenLayout = FlexLayout.DEFAULT.withJustifyContent(FlexJustify.SPACE_BETWEEN);
        final Map<Component, Rectangle> sbBounds = new HashMap<>();
        spaceBetweenLayout.layoutComponents(containerSize, components, sbBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 40), sbBounds.get(c1));
        assertRectangleEquals(new Rectangle(400, 0, 100, 40), sbBounds.get(c2));

        // SPACE_AROUND: spacing = 300 / 2 = 150, pos = 75 -> C1 at 75, C2 at 325
        final FlexLayout spaceAroundLayout = FlexLayout.DEFAULT.withJustifyContent(FlexJustify.SPACE_AROUND);
        final Map<Component, Rectangle> saBounds = new HashMap<>();
        spaceAroundLayout.layoutComponents(containerSize, components, saBounds::put);
        assertRectangleEquals(new Rectangle(75, 0, 100, 40), saBounds.get(c1));
        assertRectangleEquals(new Rectangle(325, 0, 100, 40), saBounds.get(c2));

        // SPACE_EVENLY: spacing = 300 / 3 = 100, pos = 100 -> C1 at 100, C2 at 300
        final FlexLayout spaceEvenlyLayout = FlexLayout.DEFAULT.withJustifyContent(FlexJustify.SPACE_EVENLY);
        final Map<Component, Rectangle> seBounds = new HashMap<>();
        spaceEvenlyLayout.layoutComponents(containerSize, components, seBounds::put);
        assertRectangleEquals(new Rectangle(100, 0, 100, 40), seBounds.get(c1));
        assertRectangleEquals(new Rectangle(300, 0, 100, 40), seBounds.get(c2));

        // Single child edge cases for space distributions
        final Component single = new TestComponent(100, 40);
        final Map<Component, Rectangle> sbSingle = new HashMap<>();
        spaceBetweenLayout.layoutComponents(containerSize, List.of(single), sbSingle::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 40), sbSingle.get(single));

        final Map<Component, Rectangle> saSingle = new HashMap<>();
        spaceAroundLayout.layoutComponents(containerSize, List.of(single), saSingle::put);
        // spacing = 400 / 1 = 400, pos = 200 -> at 200
        assertRectangleEquals(new Rectangle(200, 0, 100, 40), saSingle.get(single));

        final Map<Component, Rectangle> seSingle = new HashMap<>();
        spaceEvenlyLayout.layoutComponents(containerSize, List.of(single), seSingle::put);
        // spacing = 400 / 2 = 200, pos = 200 -> at 200
        assertRectangleEquals(new Rectangle(200, 0, 100, 40), seSingle.get(single));

        // Overconstrained main axis (free < 0, shrink = 0): clamps free to 0, packs at START
        final Component large1 = new TestComponent(300, 40).layoutOptions(FlexOptions.DEFAULT.withShrink(0));
        final Component large2 = new TestComponent(300, 40).layoutOptions(FlexOptions.DEFAULT.withShrink(0));
        final Map<Component, Rectangle> overSb = new HashMap<>();
        spaceBetweenLayout.layoutComponents(containerSize, List.of(large1, large2), overSb::put);
        assertRectangleEquals(new Rectangle(0, 0, 300, 40), overSb.get(large1));
        assertRectangleEquals(new Rectangle(300, 0, 300, 40), overSb.get(large2));
    }

    @Test
    void testJustifyContentWithGaps() {
        final Size containerSize = new Size(500, 100);
        final Component c1 = new TestComponent(100, 40);
        final Component c2 = new TestComponent(100, 40);
        final List<Component> components = List.of(c1, c2);

        // Gap = 20. Used = 100 + 20 + 100 = 220. Free = 280.
        // START: C1 at 0, C2 at 120
        final FlexLayout start = FlexLayout.DEFAULT.withColumnGap(20).withJustifyContent(FlexJustify.START);
        final Map<Component, Rectangle> startBounds = new HashMap<>();
        start.layoutComponents(containerSize, components, startBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 40), startBounds.get(c1));
        assertRectangleEquals(new Rectangle(120, 0, 100, 40), startBounds.get(c2));

        // END: pos = 280 -> C1 at 280, C2 at 280 + 100 + 20 = 400
        final FlexLayout end = FlexLayout.DEFAULT.withColumnGap(20).withJustifyContent(FlexJustify.END);
        final Map<Component, Rectangle> endBounds = new HashMap<>();
        end.layoutComponents(containerSize, components, endBounds::put);
        assertRectangleEquals(new Rectangle(280, 0, 100, 40), endBounds.get(c1));
        assertRectangleEquals(new Rectangle(400, 0, 100, 40), endBounds.get(c2));

        // CENTER: pos = 140 -> C1 at 140, C2 at 260
        final FlexLayout center = FlexLayout.DEFAULT.withColumnGap(20).withJustifyContent(FlexJustify.CENTER);
        final Map<Component, Rectangle> centerBounds = new HashMap<>();
        center.layoutComponents(containerSize, components, centerBounds::put);
        assertRectangleEquals(new Rectangle(140, 0, 100, 40), centerBounds.get(c1));
        assertRectangleEquals(new Rectangle(260, 0, 100, 40), centerBounds.get(c2));

        // SPACE_BETWEEN: spacing = 20 + 280 = 300 -> C1 at 0, C2 at 400
        final FlexLayout sb = FlexLayout.DEFAULT.withColumnGap(20).withJustifyContent(FlexJustify.SPACE_BETWEEN);
        final Map<Component, Rectangle> sbBounds = new HashMap<>();
        sb.layoutComponents(containerSize, components, sbBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 40), sbBounds.get(c1));
        assertRectangleEquals(new Rectangle(400, 0, 100, 40), sbBounds.get(c2));

        // SPACE_AROUND: spacing = 20 + 280/2 = 160, pos = 80 -> C1 at 80, C2 at 340
        final FlexLayout sa = FlexLayout.DEFAULT.withColumnGap(20).withJustifyContent(FlexJustify.SPACE_AROUND);
        final Map<Component, Rectangle> saBounds = new HashMap<>();
        sa.layoutComponents(containerSize, components, saBounds::put);
        assertRectangleEquals(new Rectangle(80, 0, 100, 40), saBounds.get(c1));
        assertRectangleEquals(new Rectangle(340, 0, 100, 40), saBounds.get(c2));

        // SPACE_EVENLY: spacing = 20 + 280/3 = 113.3333f, pos = 113.3333f -> C1 at 113.3333f, C2 at 326.6667f
        final FlexLayout se = FlexLayout.DEFAULT.withColumnGap(20).withJustifyContent(FlexJustify.SPACE_EVENLY);
        final Map<Component, Rectangle> seBounds = new HashMap<>();
        se.layoutComponents(containerSize, components, seBounds::put);
        assertRectangleEquals(new Rectangle(113.3333f, 0, 100, 40), seBounds.get(c1));
        assertRectangleEquals(new Rectangle(326.6667f, 0, 100, 40), seBounds.get(c2));
    }

    @Test
    void testAlignItemsRow() {
        final Size containerSize = new Size(400, 300);
        final Component c1 = new TestComponent(100, 60);
        final Component c2 = new TestComponent(100, 100);
        final List<Component> components = List.of(c1, c2);

        // START: crossPos = 0
        final FlexLayout start = FlexLayout.DEFAULT.withAlignItems(FlexAlignItems.START);
        final Map<Component, Rectangle> startBounds = new HashMap<>();
        start.layoutComponents(containerSize, components, startBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 60), startBounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 0, 100, 100), startBounds.get(c2));

        // END: crossPos = 300 - height -> C1 at 240, C2 at 200
        final FlexLayout end = FlexLayout.DEFAULT.withAlignItems(FlexAlignItems.END);
        final Map<Component, Rectangle> endBounds = new HashMap<>();
        end.layoutComponents(containerSize, components, endBounds::put);
        assertRectangleEquals(new Rectangle(0, 240, 100, 60), endBounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 200, 100, 100), endBounds.get(c2));

        // CENTER: crossPos = (300 - height) / 2 -> C1 at 120, C2 at 100
        final FlexLayout center = FlexLayout.DEFAULT.withAlignItems(FlexAlignItems.CENTER);
        final Map<Component, Rectangle> centerBounds = new HashMap<>();
        center.layoutComponents(containerSize, components, centerBounds::put);
        assertRectangleEquals(new Rectangle(0, 120, 100, 60), centerBounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 100, 100, 100), centerBounds.get(c2));

        // STRETCH: crossPos = 0, height = 300
        final FlexLayout stretch = FlexLayout.DEFAULT.withAlignItems(FlexAlignItems.STRETCH);
        final Map<Component, Rectangle> stretchBounds = new HashMap<>();
        stretch.layoutComponents(containerSize, components, stretchBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 300), stretchBounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 0, 100, 300), stretchBounds.get(c2));
    }

    @Test
    void testAlignSelfOverrides() {
        final FlexLayout layout = FlexLayout.DEFAULT.withAlignItems(FlexAlignItems.START);
        final Size containerSize = new Size(400, 300);

        final Component c1 = new TestComponent(80, 50).layoutOptions(FlexOptions.DEFAULT.withAlign(FlexAlignSelf.AUTO));
        final Component c2 = new TestComponent(80, 50).layoutOptions(FlexOptions.DEFAULT.withAlign(FlexAlignSelf.CENTER));
        final Component c3 = new TestComponent(80, 50).layoutOptions(FlexOptions.DEFAULT.withAlign(FlexAlignSelf.END));
        final Component c4 = new TestComponent(80, 50).layoutOptions(FlexOptions.DEFAULT.withAlign(FlexAlignSelf.STRETCH));

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(c1, c2, c3, c4), bounds::put);

        assertRectangleEquals(new Rectangle(0, 0, 80, 50), bounds.get(c1));
        assertRectangleEquals(new Rectangle(80, 125, 80, 50), bounds.get(c2));
        assertRectangleEquals(new Rectangle(160, 250, 80, 50), bounds.get(c3));
        assertRectangleEquals(new Rectangle(240, 0, 80, 300), bounds.get(c4));
    }

    @Test
    void testAlignContentMultiLine() {
        // Multi-line WRAP. Container height 400, width 200.
        // Each child width = 200, so each wraps to its own line.
        final Size containerSize = new Size(200, 400);
        final Component c1 = new TestComponent(200, 50);
        final Component c2 = new TestComponent(200, 50);
        final List<Component> components = List.of(c1, c2);

        // rowGap = 20. Used = 50 + 20 + 50 = 120. Free = 280.
        // START: Line 1 at 0, Line 2 at 70
        final FlexLayout start = FlexLayout.DEFAULT.withWrap(FlexWrap.WRAP).withRowGap(20).withAlignContent(FlexAlignContent.START);
        final Map<Component, Rectangle> startBounds = new HashMap<>();
        start.layoutComponents(containerSize, components, startBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 200, 50), startBounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 70, 200, 50), startBounds.get(c2));

        // END: pos = 280 -> Line 1 at 280, Line 2 at 350
        final FlexLayout end = start.withAlignContent(FlexAlignContent.END);
        final Map<Component, Rectangle> endBounds = new HashMap<>();
        end.layoutComponents(containerSize, components, endBounds::put);
        assertRectangleEquals(new Rectangle(0, 280, 200, 50), endBounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 350, 200, 50), endBounds.get(c2));

        // CENTER: pos = 140 -> Line 1 at 140, Line 2 at 210
        final FlexLayout center = start.withAlignContent(FlexAlignContent.CENTER);
        final Map<Component, Rectangle> centerBounds = new HashMap<>();
        center.layoutComponents(containerSize, components, centerBounds::put);
        assertRectangleEquals(new Rectangle(0, 140, 200, 50), centerBounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 210, 200, 50), centerBounds.get(c2));

        // SPACE_BETWEEN: spacing = 20 + 280 = 300 -> Line 1 at 0, Line 2 at 350
        final FlexLayout sb = start.withAlignContent(FlexAlignContent.SPACE_BETWEEN);
        final Map<Component, Rectangle> sbBounds = new HashMap<>();
        sb.layoutComponents(containerSize, components, sbBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 200, 50), sbBounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 350, 200, 50), sbBounds.get(c2));

        // SPACE_AROUND: spacing = 20 + 280/2 = 160, pos = 80 -> Line 1 at 80, Line 2 at 290
        final FlexLayout sa = start.withAlignContent(FlexAlignContent.SPACE_AROUND);
        final Map<Component, Rectangle> saBounds = new HashMap<>();
        sa.layoutComponents(containerSize, components, saBounds::put);
        assertRectangleEquals(new Rectangle(0, 80, 200, 50), saBounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 290, 200, 50), saBounds.get(c2));

        // SPACE_EVENLY: spacing = 20 + 280/3 = 113.3333f, pos = 113.3333f -> Line 1 at 113.3333f, Line 2 at 276.6667f
        final FlexLayout se = start.withAlignContent(FlexAlignContent.SPACE_EVENLY);
        final Map<Component, Rectangle> seBounds = new HashMap<>();
        se.layoutComponents(containerSize, components, seBounds::put);
        assertRectangleEquals(new Rectangle(0, 113.3333f, 200, 50), seBounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 276.6667f, 200, 50), seBounds.get(c2));

        // STRETCH: extra 280 / 2 = 140 share added to each line height -> Line 1 height 190 at y = 0, Line 2 height 190 at y = 210
        // (with alignItems = STRETCH, items also expand to line cross size)
        final FlexLayout stretch = start.withAlignContent(FlexAlignContent.STRETCH).withAlignItems(FlexAlignItems.STRETCH);
        final Map<Component, Rectangle> stretchBounds = new HashMap<>();
        stretch.layoutComponents(containerSize, components, stretchBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 200, 190), stretchBounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 210, 200, 190), stretchBounds.get(c2));
    }

    @Test
    void testFlexGrowPositiveFreeSpace() {
        final Size containerSize = new Size(500, 100);
        final Component c1 = new TestComponent(100, 40);
        final Component c2 = new TestComponent(100, 40);

        // Equal grow (1, 1): free = 300, totalGrow = 2 -> each gets 150 -> widths = 250
        c1.layoutOptions(FlexOptions.DEFAULT.withGrow(1));
        c2.layoutOptions(FlexOptions.DEFAULT.withGrow(1));
        final Map<Component, Rectangle> equalBounds = new HashMap<>();
        FlexLayout.DEFAULT.layoutComponents(containerSize, List.of(c1, c2), equalBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 250, 40), equalBounds.get(c1));
        assertRectangleEquals(new Rectangle(250, 0, 250, 40), equalBounds.get(c2));

        // Unequal grow (1, 2): free = 300, totalGrow = 3 -> C1 gets 100 (width 200), C2 gets 200 (width 300)
        c1.layoutOptions(FlexOptions.DEFAULT.withGrow(1));
        c2.layoutOptions(FlexOptions.DEFAULT.withGrow(2));
        final Map<Component, Rectangle> unequalBounds = new HashMap<>();
        FlexLayout.DEFAULT.layoutComponents(containerSize, List.of(c1, c2), unequalBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 200, 40), unequalBounds.get(c1));
        assertRectangleEquals(new Rectangle(200, 0, 300, 40), unequalBounds.get(c2));

        // Zero grow (0, 0): line unmodified -> widths remain 100
        c1.layoutOptions(FlexOptions.DEFAULT.withGrow(0));
        c2.layoutOptions(FlexOptions.DEFAULT.withGrow(0));
        final Map<Component, Rectangle> zeroBounds = new HashMap<>();
        FlexLayout.DEFAULT.layoutComponents(containerSize, List.of(c1, c2), zeroBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 40), zeroBounds.get(c1));
        assertRectangleEquals(new Rectangle(100, 0, 100, 40), zeroBounds.get(c2));

        // One item grow = 1, other grow = 0: C1 gets all 300 (width 400), C2 gets 0 (width 100)
        c1.layoutOptions(FlexOptions.DEFAULT.withGrow(1));
        c2.layoutOptions(FlexOptions.DEFAULT.withGrow(0));
        final Map<Component, Rectangle> oneBounds = new HashMap<>();
        FlexLayout.DEFAULT.layoutComponents(containerSize, List.of(c1, c2), oneBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 400, 40), oneBounds.get(c1));
        assertRectangleEquals(new Rectangle(400, 0, 100, 40), oneBounds.get(c2));

        // Column direction: grow applies along Y axis
        final FlexLayout colLayout = FlexLayout.DEFAULT.withDirection(FlexDirection.COLUMN);
        final Map<Component, Rectangle> colBounds = new HashMap<>();
        c1.layoutOptions(FlexOptions.DEFAULT.withGrow(1));
        c2.layoutOptions(FlexOptions.DEFAULT.withGrow(1));
        // Container height 500, items height 40 each. Free = 500 - 80 = 420. Each gets 210 -> height 250.
        colLayout.layoutComponents(new Size(100, 500), List.of(c1, c2), colBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 250), colBounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 250, 100, 250), colBounds.get(c2));
    }

    @Test
    void testFlexShrinkOverflowSpace() {
        final Size containerSize = new Size(300, 100);
        final Component c1 = new TestComponent(200, 40);
        final Component c2 = new TestComponent(200, 40);

        // Equal shrink (1, 1): overflow = -100.
        // Total weighted = 200*1 + 200*1 = 400. Each loses 100 * (200/400) = 50 -> widths = 150
        c1.layoutOptions(FlexOptions.DEFAULT.withShrink(1));
        c2.layoutOptions(FlexOptions.DEFAULT.withShrink(1));
        final Map<Component, Rectangle> equalBounds = new HashMap<>();
        FlexLayout.DEFAULT.layoutComponents(containerSize, List.of(c1, c2), equalBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 150, 40), equalBounds.get(c1));
        assertRectangleEquals(new Rectangle(150, 0, 150, 40), equalBounds.get(c2));

        // Unequal base & shrink:
        // C1: base 100, shrink 2 -> weighted = 200
        // C2: base 200, shrink 1 -> weighted = 200
        // Container width 200. Total main = 300. Free = -100.
        // Total weighted = 400.
        // C1 loses 100 * (200/400) = 50 -> width = 50
        // C2 loses 100 * (200/400) = 50 -> width = 150
        final Component u1 = new TestComponent(100, 40).layoutOptions(FlexOptions.DEFAULT.withShrink(2));
        final Component u2 = new TestComponent(200, 40).layoutOptions(FlexOptions.DEFAULT.withShrink(1));
        final Map<Component, Rectangle> unequalBounds = new HashMap<>();
        FlexLayout.DEFAULT.layoutComponents(new Size(200, 100), List.of(u1, u2), unequalBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 50, 40), unequalBounds.get(u1));
        assertRectangleEquals(new Rectangle(50, 0, 150, 40), unequalBounds.get(u2));

        // Zero shrink on one item: C1 shrink 0, C2 shrink 1
        // Container 300, items 200 each. Free = -100.
        // Total weighted = 0 + 200*1 = 200.
        // C1 loses 0 -> width = 200
        // C2 loses 100 * (200/200) = 100 -> width = 100
        c1.layoutOptions(FlexOptions.DEFAULT.withShrink(0));
        c2.layoutOptions(FlexOptions.DEFAULT.withShrink(1));
        final Map<Component, Rectangle> zeroBounds = new HashMap<>();
        FlexLayout.DEFAULT.layoutComponents(containerSize, List.of(c1, c2), zeroBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 200, 40), zeroBounds.get(c1));
        assertRectangleEquals(new Rectangle(200, 0, 100, 40), zeroBounds.get(c2));

        // Column direction: shrink applies along Y axis
        final FlexLayout colLayout = FlexLayout.DEFAULT.withDirection(FlexDirection.COLUMN);
        final Component col1 = new TestComponent(100, 200).layoutOptions(FlexOptions.DEFAULT.withShrink(1));
        final Component col2 = new TestComponent(100, 200).layoutOptions(FlexOptions.DEFAULT.withShrink(1));
        final Map<Component, Rectangle> colBounds = new HashMap<>();
        colLayout.layoutComponents(new Size(100, 300), List.of(col1, col2), colBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 150), colBounds.get(col1));
        assertRectangleEquals(new Rectangle(0, 150, 100, 150), colBounds.get(col2));
    }

    @Test
    void testFlexBasisOverridesIdealSize() {
        final Size containerSize = new Size(500, 100);
        final Component c1 = new TestComponent(50, 40).layoutOptions(FlexOptions.DEFAULT.withBasis(120));
        final Component c2 = new TestComponent(60, 40).layoutOptions(FlexOptions.DEFAULT.withBasis(0));
        final Component c3 = new TestComponent(70, 40).layoutOptions(FlexOptions.DEFAULT.withBasis(-10));

        final FlexLayout layout = FlexLayout.DEFAULT.withColumnGap(10);
        // Ideal main = 120 + 10 + 60 + 10 + 70 = 270
        assertEquals(new Size(270, 40), layout.computeIdealSize(containerSize, List.of(c1, c2, c3)));

        final Map<Component, Rectangle> bounds = new HashMap<>();
        layout.layoutComponents(containerSize, List.of(c1, c2, c3), bounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 120, 40), bounds.get(c1));
        assertRectangleEquals(new Rectangle(130, 0, 60, 40), bounds.get(c2));
        assertRectangleEquals(new Rectangle(200, 0, 70, 40), bounds.get(c3));

        // Grow starting from basis
        final Component g1 = new TestComponent(50, 40).layoutOptions(FlexOptions.DEFAULT.withBasis(100).withGrow(1));
        final Component g2 = new TestComponent(50, 40).layoutOptions(FlexOptions.DEFAULT.withBasis(100).withGrow(1));
        // Container width 400. Total basis = 200. Free = 200. Each gets 100 -> widths = 200
        final Map<Component, Rectangle> growBounds = new HashMap<>();
        FlexLayout.DEFAULT.layoutComponents(new Size(400, 100), List.of(g1, g2), growBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 200, 40), growBounds.get(g1));
        assertRectangleEquals(new Rectangle(200, 0, 200, 40), growBounds.get(g2));
    }

    @Test
    void testRowAndColumnGaps() {
        // In ROW: columnGap is between items, rowGap is between lines
        final FlexLayout rowLayout = FlexLayout.DEFAULT
                .withWrap(FlexWrap.WRAP)
                .withColumnGap(10)
                .withRowGap(20);

        final Component r1 = new TestComponent(100, 50);
        final Component r2 = new TestComponent(100, 50);
        final Component r3 = new TestComponent(100, 50);
        final Component r4 = new TestComponent(100, 50);
        final List<Component> rowList = List.of(r1, r2, r3, r4);

        // Container width 250: R1 (100) + gap (10) + R2 (100) = 210 <= 250 -> Line 1
        // R3 and R4 wrap to Line 2
        // Ideal size: main = max(210, 210) = 210, cross = 50 + 20 + 50 = 120
        assertEquals(new Size(210, 120), rowLayout.computeIdealSize(new Size(250, 300), rowList));

        final Map<Component, Rectangle> rowBounds = new HashMap<>();
        rowLayout.layoutComponents(new Size(250, 300), rowList, rowBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 100, 50), rowBounds.get(r1));
        assertRectangleEquals(new Rectangle(110, 0, 100, 50), rowBounds.get(r2));
        assertRectangleEquals(new Rectangle(0, 70, 100, 50), rowBounds.get(r3));
        assertRectangleEquals(new Rectangle(110, 70, 100, 50), rowBounds.get(r4));

        // In COLUMN: rowGap is between items, columnGap is between lines
        final FlexLayout colLayout = FlexLayout.DEFAULT
                .withDirection(FlexDirection.COLUMN)
                .withWrap(FlexWrap.WRAP)
                .withRowGap(15)
                .withColumnGap(25);

        final Component c1 = new TestComponent(60, 80);
        final Component c2 = new TestComponent(60, 80);
        final Component c3 = new TestComponent(60, 80);
        final Component c4 = new TestComponent(60, 80);
        final List<Component> colList = List.of(c1, c2, c3, c4);

        // Container height 200: C1 (80) + gap (15) + C2 (80) = 175 <= 200 -> Column 1
        // C3 and C4 wrap to Column 2
        // Ideal size: totalMain = 175 (Y), totalCross = 60 + 25 + 60 = 145 (X) -> Size(145, 175)
        assertEquals(new Size(145, 175), colLayout.computeIdealSize(new Size(300, 200), colList));

        final Map<Component, Rectangle> colBounds = new HashMap<>();
        colLayout.layoutComponents(new Size(300, 200), colList, colBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 60, 80), colBounds.get(c1));
        assertRectangleEquals(new Rectangle(0, 95, 60, 80), colBounds.get(c2));
        assertRectangleEquals(new Rectangle(85, 0, 60, 80), colBounds.get(c3));
        assertRectangleEquals(new Rectangle(85, 95, 60, 80), colBounds.get(c4));
    }

    @Test
    void testMinMaxClampingMainAndCross() {
        // Main axis grow clamping with maxSize
        final Component cGrowMax = new TestComponent(50, 40)
                .layoutOptions(FlexOptions.DEFAULT.withGrow(1));
        cGrowMax.maxSize(new Size(80, 100));

        final Component cGrowOther = new TestComponent(50, 40)
                .layoutOptions(FlexOptions.DEFAULT.withGrow(1));

        // Container width 300. Free = 200, share = 100.
        // cGrowMax proposed = 150, clamped to 80.
        // cGrowOther proposed = 150.
        final Map<Component, Rectangle> growBounds = new HashMap<>();
        FlexLayout.DEFAULT.layoutComponents(new Size(300, 100), List.of(cGrowMax, cGrowOther), growBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 80, 40), growBounds.get(cGrowMax));
        assertRectangleEquals(new Rectangle(80, 0, 150, 40), growBounds.get(cGrowOther));

        // Main axis shrink clamping with minSize
        final Component cShrinkMin = new TestComponent(100, 40)
                .layoutOptions(FlexOptions.DEFAULT.withShrink(1));
        cShrinkMin.minSize(new Size(85, 0));

        final Component cShrinkOther = new TestComponent(100, 40)
                .layoutOptions(FlexOptions.DEFAULT.withShrink(1));

        // Container width 150. Free = -50. Reduction = 25 per item.
        // cShrinkMin proposed = 75, clamped to 85.
        // cShrinkOther proposed = 75.
        final Map<Component, Rectangle> shrinkBounds = new HashMap<>();
        FlexLayout.DEFAULT.layoutComponents(new Size(150, 100), List.of(cShrinkMin, cShrinkOther), shrinkBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 85, 40), shrinkBounds.get(cShrinkMin));
        assertRectangleEquals(new Rectangle(85, 0, 75, 40), shrinkBounds.get(cShrinkOther));

        // Cross axis stretch clamping with maxSize
        final Component cStretchMax = new TestComponent(80, 50);
        cStretchMax.maxSize(new Size(Float.MAX_VALUE, 150));

        final FlexLayout stretchLayout = FlexLayout.DEFAULT.withAlignItems(FlexAlignItems.STRETCH);
        final Map<Component, Rectangle> stretchBounds = new HashMap<>();
        stretchLayout.layoutComponents(new Size(400, 400), List.of(cStretchMax), stretchBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 80, 150), stretchBounds.get(cStretchMax));

        // Basis clamping with maxSize
        final Component cBasisMax = new TestComponent(50, 40)
                .layoutOptions(FlexOptions.DEFAULT.withBasis(500));
        cBasisMax.maxSize(new Size(300, Float.MAX_VALUE));

        final Map<Component, Rectangle> basisBounds = new HashMap<>();
        FlexLayout.DEFAULT.layoutComponents(new Size(600, 100), List.of(cBasisMax), basisBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 300, 40), basisBounds.get(cBasisMax));
    }

    @Test
    void testContainerContractCompliance() {
        final FlexLayout layout = FlexLayout.DEFAULT
                .withColumnGap(10)
                .withRowGap(15)
                .withWrap(FlexWrap.WRAP);

        final Container container = new Container(layout);
        final Component c1 = new TestComponent(100, 50).layoutOptions(FlexOptions.DEFAULT.withOrder(2));
        final Component c2 = new TestComponent(120, 60).layoutOptions(FlexOptions.DEFAULT.withOrder(1));
        final Component c3 = new TestComponent(80, 40).layoutOptions(FlexOptions.DEFAULT.withOrder(3));

        container.add(c1);
        container.add(c2);
        container.add(c3);

        final Size containerSize = new Size(300, 400);
        assertDoesNotThrow(() -> container.computeLayout(containerSize));

        // Verify bounds exist for every child
        assertNotNull(container.childBounds(c1));
        assertNotNull(container.childBounds(c2));
        assertNotNull(container.childBounds(c3));

        // Verify container contentSize matches layout.computeIdealSize
        assertEquals(layout.computeIdealSize(containerSize, List.of(c1, c2, c3)), container.contentSize());
    }

    @Test
    void testFlexOptionsConstructorsWithersAndDefaults() {
        // Defaults
        final FlexOptions def = FlexOptions.DEFAULT;
        assertEquals(0, def.order());
        assertEquals(0, def.grow());
        assertEquals(1, def.shrink());
        assertEquals(0, def.basis());
        assertEquals(FlexAlignSelf.AUTO, def.align());

        // Canonical constructor
        final FlexOptions custom = new FlexOptions(3, 2, 0, 150, FlexAlignSelf.CENTER);
        assertEquals(3, custom.order());
        assertEquals(2, custom.grow());
        assertEquals(0, custom.shrink());
        assertEquals(150, custom.basis());
        assertEquals(FlexAlignSelf.CENTER, custom.align());

        // Withers
        final FlexOptions modified = def
                .withOrder(3)
                .withGrow(2)
                .withShrink(0)
                .withBasis(150)
                .withAlign(FlexAlignSelf.CENTER);
        assertEquals(custom, modified);
        assertEquals(custom.hashCode(), modified.hashCode());
        assertEquals(custom.toString(), modified.toString());

        // WithBy
        final FlexOptions byModified = def
                .withOrderBy(o -> o + 3)
                .withGrowBy(g -> g + 2)
                .withShrinkBy(s -> s - 1)
                .withBasisBy(b -> b + 150)
                .withAlignBy(a -> FlexAlignSelf.CENTER);
        assertEquals(custom, byModified);
    }

    @Test
    void testFlexLayoutConstructorsWithersAndDefaults() {
        // Defaults
        final FlexLayout def = FlexLayout.DEFAULT;
        assertEquals(FlexDirection.ROW, def.direction());
        assertEquals(FlexWrap.NO_WRAP, def.wrap());
        assertEquals(FlexJustify.START, def.justifyContent());
        assertEquals(FlexAlignItems.START, def.alignItems());
        assertEquals(FlexAlignContent.START, def.alignContent());
        assertEquals(0, def.rowGap());
        assertEquals(0, def.columnGap());

        // Canonical constructor
        final FlexLayout custom = new FlexLayout(
                FlexDirection.COLUMN_REVERSE,
                FlexWrap.WRAP_REVERSE,
                FlexJustify.SPACE_EVENLY,
                FlexAlignItems.CENTER,
                FlexAlignContent.SPACE_AROUND,
                25,
                30
        );

        // Withers
        final FlexLayout modified = def
                .withDirection(FlexDirection.COLUMN_REVERSE)
                .withWrap(FlexWrap.WRAP_REVERSE)
                .withJustifyContent(FlexJustify.SPACE_EVENLY)
                .withAlignItems(FlexAlignItems.CENTER)
                .withAlignContent(FlexAlignContent.SPACE_AROUND)
                .withRowGap(25)
                .withColumnGap(30);

        assertEquals(custom, modified);
        assertEquals(custom.hashCode(), modified.hashCode());
        assertEquals(custom.toString(), modified.toString());

        // WithBy
        final FlexLayout byModified = def
                .withDirectionBy(d -> FlexDirection.COLUMN_REVERSE)
                .withWrapBy(w -> FlexWrap.WRAP_REVERSE)
                .withJustifyContentBy(j -> FlexJustify.SPACE_EVENLY)
                .withAlignItemsBy(a -> FlexAlignItems.CENTER)
                .withAlignContentBy(c -> FlexAlignContent.SPACE_AROUND)
                .withRowGapBy(r -> r + 25)
                .withColumnGapBy(c -> c + 30);
        assertEquals(custom, byModified);
    }

    @Test
    void testEnumCoverage() {
        // FlexDirection
        assertEquals(4, FlexDirection.values().length);
        assertEquals(FlexDirection.ROW, FlexDirection.valueOf("ROW"));
        assertEquals(FlexDirection.ROW_REVERSE, FlexDirection.valueOf("ROW_REVERSE"));
        assertEquals(FlexDirection.COLUMN, FlexDirection.valueOf("COLUMN"));
        assertEquals(FlexDirection.COLUMN_REVERSE, FlexDirection.valueOf("COLUMN_REVERSE"));

        // FlexWrap
        assertEquals(3, FlexWrap.values().length);
        assertEquals(FlexWrap.NO_WRAP, FlexWrap.valueOf("NO_WRAP"));
        assertEquals(FlexWrap.WRAP, FlexWrap.valueOf("WRAP"));
        assertEquals(FlexWrap.WRAP_REVERSE, FlexWrap.valueOf("WRAP_REVERSE"));

        // FlexJustify
        assertEquals(6, FlexJustify.values().length);
        assertEquals(FlexJustify.START, FlexJustify.valueOf("START"));
        assertEquals(FlexJustify.CENTER, FlexJustify.valueOf("CENTER"));
        assertEquals(FlexJustify.END, FlexJustify.valueOf("END"));
        assertEquals(FlexJustify.SPACE_BETWEEN, FlexJustify.valueOf("SPACE_BETWEEN"));
        assertEquals(FlexJustify.SPACE_AROUND, FlexJustify.valueOf("SPACE_AROUND"));
        assertEquals(FlexJustify.SPACE_EVENLY, FlexJustify.valueOf("SPACE_EVENLY"));

        // FlexAlignItems
        assertEquals(4, FlexAlignItems.values().length);
        assertEquals(FlexAlignItems.START, FlexAlignItems.valueOf("START"));
        assertEquals(FlexAlignItems.END, FlexAlignItems.valueOf("END"));
        assertEquals(FlexAlignItems.CENTER, FlexAlignItems.valueOf("CENTER"));
        assertEquals(FlexAlignItems.STRETCH, FlexAlignItems.valueOf("STRETCH"));

        // FlexAlignSelf
        assertEquals(5, FlexAlignSelf.values().length);
        assertEquals(FlexAlignSelf.AUTO, FlexAlignSelf.valueOf("AUTO"));
        assertEquals(FlexAlignSelf.START, FlexAlignSelf.valueOf("START"));
        assertEquals(FlexAlignSelf.END, FlexAlignSelf.valueOf("END"));
        assertEquals(FlexAlignSelf.CENTER, FlexAlignSelf.valueOf("CENTER"));
        assertEquals(FlexAlignSelf.STRETCH, FlexAlignSelf.valueOf("STRETCH"));

        // FlexAlignContent
        assertEquals(7, FlexAlignContent.values().length);
        assertEquals(FlexAlignContent.START, FlexAlignContent.valueOf("START"));
        assertEquals(FlexAlignContent.CENTER, FlexAlignContent.valueOf("CENTER"));
        assertEquals(FlexAlignContent.END, FlexAlignContent.valueOf("END"));
        assertEquals(FlexAlignContent.SPACE_BETWEEN, FlexAlignContent.valueOf("SPACE_BETWEEN"));
        assertEquals(FlexAlignContent.SPACE_AROUND, FlexAlignContent.valueOf("SPACE_AROUND"));
        assertEquals(FlexAlignContent.SPACE_EVENLY, FlexAlignContent.valueOf("SPACE_EVENLY"));
        assertEquals(FlexAlignContent.STRETCH, FlexAlignContent.valueOf("STRETCH"));
    }

    @Test
    void testEdgeCasesAndSpecialInputs() {
        final FlexLayout layout = FlexLayout.DEFAULT.withRowGap(10).withColumnGap(10);

        // Zero container size
        final Component c1 = new TestComponent(100, 50);
        final Map<Component, Rectangle> zeroBounds = new HashMap<>();
        assertDoesNotThrow(() -> layout.layoutComponents(Size.EMPTY, List.of(c1), zeroBounds::put));
        // With shrink=1, in 0x0 container, main axis (width) shrinks to 0, cross axis (height) remains natural 50
        assertRectangleEquals(new Rectangle(0, 0, 0, 50), zeroBounds.get(c1));

        // When alignItems is STRETCH, cross axis stretches to crossAvailable (0)
        final FlexLayout zeroStretchLayout = layout.withAlignItems(FlexAlignItems.STRETCH);
        final Map<Component, Rectangle> zeroStretchBounds = new HashMap<>();
        zeroStretchLayout.layoutComponents(Size.EMPTY, List.of(c1), zeroStretchBounds::put);
        assertRectangleEquals(new Rectangle(0, 0, 0, 0), zeroStretchBounds.get(c1));

        // Enormous gaps larger than container size
        final FlexLayout hugeGaps = FlexLayout.DEFAULT.withColumnGap(1000).withRowGap(1000);
        final Component c2 = new TestComponent(50, 40);
        final Map<Component, Rectangle> hugeBounds = new HashMap<>();
        assertDoesNotThrow(() -> hugeGaps.layoutComponents(new Size(100, 100), List.of(c2), hugeBounds::put));
        assertRectangleEquals(new Rectangle(0, 0, 50, 40), hugeBounds.get(c2));

        // Overconstrained distribute with SPACE_AROUND / SPACE_EVENLY
        // When used > available, free clamps to 0. pos starts at gap/2 and gap respectively.
        final FlexLayout saOver = FlexLayout.DEFAULT.withJustifyContent(FlexJustify.SPACE_AROUND).withColumnGap(20);
        final Component o1 = new TestComponent(100, 40).layoutOptions(FlexOptions.DEFAULT.withShrink(0));
        final Component o2 = new TestComponent(100, 40).layoutOptions(FlexOptions.DEFAULT.withShrink(0));
        // used = 100 + 20 + 100 = 220 > container width 150. free = 0.
        // spacing = 20 + 0 = 20. pos = 10.
        final Map<Component, Rectangle> saOverBounds = new HashMap<>();
        saOver.layoutComponents(new Size(150, 100), List.of(o1, o2), saOverBounds::put);
        assertRectangleEquals(new Rectangle(10, 0, 100, 40), saOverBounds.get(o1));
        assertRectangleEquals(new Rectangle(130, 0, 100, 40), saOverBounds.get(o2));
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
