package net.lenni0451.rivet.text.diagnostic;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.parser.ParserRegistry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnderlineStyleTest {

    private static final float DELTA = 1e-4F;

    private static List<Object[]> render(final UnderlineStyle style, final String method, final UnderlineStyle.Segment segment) {
        List<Object[]> calls = new ArrayList<>();
        Renderer renderer = (Renderer) Proxy.newProxyInstance(Renderer.class.getClassLoader(), new Class<?>[]{Renderer.class}, (proxy, invoked, args) -> {
            assertEquals(method, invoked.getName());
            calls.add(args);
            return null;
        });
        style.render(renderer, segment);
        return calls;
    }

    private static UnderlineStyle.Segment segment(final float x, final float width, final float visibleMinX, final float visibleMaxX) {
        return new UnderlineStyle.Segment(x, 40, width, 20, 1, Color.WHITE, visibleMinX, visibleMaxX);
    }

    private static void assertPointsInside(final Point[] points, final UnderlineStyle.Segment segment) {
        for (Point point : points) {
            assertTrue(point.x() >= segment.x() - DELTA && point.x() <= segment.maxX() + DELTA, "x outside of segment: " + point.x());
            assertTrue(point.y() >= segment.lineY() && point.y() <= segment.bottom(), "y outside of line: " + point.y());
        }
    }

    @Test
    void solidIsDrawnAtTheBottomOfTheLine() {
        List<Object[]> calls = render(UnderlineStyle.SOLID, "fillRect", segment(10, 30, 0, 100));
        assertEquals(1, calls.size());
        assertArrayEquals(new Object[]{10F, 59F, 30F, 1F, Color.WHITE}, calls.get(0));
    }

    @Test
    void dashesAreCutAtTheSegmentEnd() {
        List<Object[]> calls = render(UnderlineStyle.dashed(4, 2), "fillRect", segment(0, 8, 0, 100));
        assertEquals(2, calls.size());
        assertEquals(0F, calls.get(0)[0]);
        assertEquals(4F, calls.get(0)[2]);
        assertEquals(6F, calls.get(1)[0]);
        assertEquals(2F, calls.get(1)[2]);
    }

    @Test
    void dashesOutsideOfTheVisibleAreaAreSkipped() {
        List<Object[]> calls = render(UnderlineStyle.dashed(4, 2), "fillRect", segment(0, 60_000, 600, 606));
        assertEquals(1, calls.size());
        assertEquals(600F, calls.get(0)[0]);
    }

    @Test
    void curvesStayInsideOfTheSegment() {
        for (UnderlineStyle style : List.of(UnderlineStyle.WAVY, UnderlineStyle.ZIGZAG)) {
            UnderlineStyle.Segment segment = segment(10, 37, 0, 100);
            List<Object[]> calls = render(style, "polyLine", segment);
            assertEquals(1, calls.size());
            Point[] points = (Point[]) calls.get(0)[0];
            assertTrue(points.length >= 2);
            assertPointsInside(points, segment);
            assertEquals(segment.x(), points[0].x(), DELTA);
            assertEquals(segment.maxX(), points[points.length - 1].x(), DELTA);
        }
    }

    @Test
    void curvesOnlyGenerateVisiblePoints() {
        UnderlineStyle.Segment segment = segment(0, 1_000_000, 500, 600);
        Point[] points = (Point[]) render(UnderlineStyle.WAVY, "polyLine", segment).get(0)[0];
        assertTrue(points.length < 110, "Too many points: " + points.length);
        assertTrue(points[0].x() <= 500);
        assertTrue(points[points.length - 1].x() >= 600);
    }

    @Test
    void curvesKeepTheirPhaseWhenScrolling() {
        Point[] full = (Point[]) render(UnderlineStyle.WAVY, "polyLine", segment(0, 100, 0, 100)).get(0)[0];
        Point[] scrolled = (Point[]) render(UnderlineStyle.WAVY, "polyLine", segment(0, 100, 30, 100)).get(0)[0];
        int offset = full.length - scrolled.length;
        for (int i = 0; i < scrolled.length; i++) {
            assertEquals(full[i + offset].x(), scrolled[i].x(), DELTA);
            assertEquals(full[i + offset].y(), scrolled[i].y(), DELTA);
        }
    }

    @Test
    void shortCurvesHaveAtLeastTwoPoints() {
        Point[] points = (Point[]) render(UnderlineStyle.WAVY, "polyLine", segment(5, 0.5F, 0, 100)).get(0)[0];
        assertEquals(2, points.length);
    }

    @Test
    void parserRoundTrip() {
        ParserRegistry parsers = ParserRegistry.standard();
        for (String name : List.of("solid", "double", "dashed", "dotted", "wavy", "zigzag")) {
            UnderlineStyle style = parsers.parse(UnderlineStyle.class, name);
            assertEquals(name, parsers.format(UnderlineStyle.class, style));
        }
        assertThrows(IllegalArgumentException.class, () -> parsers.parse(UnderlineStyle.class, "unknown"));
    }

    @Test
    void diagnosticRejectsInvalidRanges() {
        assertThrows(IllegalArgumentException.class, () -> new TextDiagnostic(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> new TextDiagnostic(3, 2));
        assertDoesNotThrow(() -> new TextDiagnostic(2, 2));
    }

}
