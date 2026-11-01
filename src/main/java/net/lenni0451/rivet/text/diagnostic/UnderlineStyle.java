package net.lenni0451.rivet.text.diagnostic;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.math.Point;

import java.util.function.DoubleUnaryOperator;

@FunctionalInterface
public interface UnderlineStyle {

    UnderlineStyle SOLID = (renderer, segment) -> renderer.fillRect(segment.x(), segment.bottom() - segment.thickness(), segment.width(), segment.thickness(), segment.color());
    UnderlineStyle DOUBLE = (renderer, segment) -> {
        renderer.fillRect(segment.x(), segment.bottom() - segment.thickness(), segment.width(), segment.thickness(), segment.color());
        renderer.fillRect(segment.x(), segment.bottom() - segment.thickness() * 3F, segment.width(), segment.thickness(), segment.color());
    };
    UnderlineStyle DASHED = dashed(4F, 2F);
    UnderlineStyle DOTTED = dashed(1F, 2F);
    UnderlineStyle WAVY = wavy(1.5F, 6F);
    UnderlineStyle ZIGZAG = zigzag(1.5F, 6F);

    /**
     * @param dash The length of a dash as a multiple of the line thickness
     * @param gap  The gap between two dashes as a multiple of the line thickness
     */
    static UnderlineStyle dashed(final float dash, final float gap) {
        return (renderer, segment) -> {
            float dashLength = dash * segment.thickness();
            float period = dashLength + gap * segment.thickness();
            if (period <= 0) return;

            float y = segment.bottom() - segment.thickness();
            float end = Math.min(segment.maxX(), segment.visibleMaxX());
            int index = Math.max(0, (int) Math.floor((segment.visibleMinX() - segment.x()) / period));
            for (float x = segment.x() + index * period; x < end; x = segment.x() + ++index * period) {
                renderer.fillRect(x, y, Math.min(dashLength, segment.maxX() - x), segment.thickness(), segment.color());
            }
        };
    }

    /**
     * @param amplitude  The height of the wave above and below its center as a multiple of the line thickness
     * @param wavelength The length of one wave as a multiple of the line thickness
     */
    static UnderlineStyle wavy(final float amplitude, final float wavelength) {
        return (renderer, segment) -> {
            float height = amplitude * segment.thickness();
            float length = wavelength * segment.thickness();
            renderCurve(renderer, segment, height, length / 6F, offset -> height * Math.sin(offset / length * Math.PI * 2));
        };
    }

    /**
     * @param amplitude  The height of the line above and below its center as a multiple of the line thickness
     * @param wavelength The length of one up and down movement as a multiple of the line thickness
     */
    static UnderlineStyle zigzag(final float amplitude, final float wavelength) {
        return (renderer, segment) -> {
            float height = amplitude * segment.thickness();
            float length = wavelength * segment.thickness();
            renderCurve(renderer, segment, height, length / 2F, offset -> {
                double phase = offset / length;
                return height * (4 * Math.abs(phase - Math.floor(phase + 0.5)) - 1);
            });
        };
    }

    private static void renderCurve(final Renderer renderer, final Segment segment, final float amplitude, final float step, final DoubleUnaryOperator curve) {
        if (step <= 0) return;

        // The curve is anchored to the start of the segment, so that scrolling doesn't shift its phase
        float centerY = segment.bottom() - segment.thickness() / 2F - amplitude;
        float maxOffset = segment.width();
        int first = Math.max(0, (int) Math.floor((segment.visibleMinX() - segment.x()) / step));
        int last = Math.min((int) Math.ceil(maxOffset / step), (int) Math.ceil((segment.visibleMaxX() - segment.x()) / step));
        if (last <= first) return;

        Point[] points = new Point[last - first + 1];
        for (int i = 0; i < points.length; i++) {
            float offset = Math.min(maxOffset, (first + i) * step);
            points[i] = new Point(segment.x() + offset, centerY + (float) curve.applyAsDouble(offset));
        }
        renderer.polyLine(points, segment.thickness(), segment.color());
    }


    void render(final Renderer renderer, final Segment segment);


    /**
     * The part of a diagnostic on a single line.
     *
     * @param x           The start of the diagnostic on this line
     * @param lineY       The top of the line
     * @param width       The width of the diagnostic on this line
     * @param lineHeight  The height of the line
     * @param thickness   The line thickness from the theme
     * @param color       The resolved color of the diagnostic
     * @param visibleMinX The start of the visible area. Everything before can be skipped
     * @param visibleMaxX The end of the visible area. Everything after can be skipped
     */
    record Segment(float x, float lineY, float width, float lineHeight, float thickness, Color color, float visibleMinX, float visibleMaxX) {

        public float maxX() {
            return this.x + this.width;
        }

        public float bottom() {
            return this.lineY + this.lineHeight;
        }

    }

}
