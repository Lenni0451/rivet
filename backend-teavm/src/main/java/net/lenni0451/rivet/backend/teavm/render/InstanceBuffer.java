package net.lenni0451.rivet.backend.teavm.render;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.math.Transform2D;

import java.util.Arrays;

/**
 * CPU side storage for all instances of a frame.<br>
 * Every primitive (shape, glyph, image, triangle) is a single instance of the same layout,
 * which allows drawing a whole layer with one instanced draw call.
 * <pre>
 * floats (per instance):
 *   0..3   matrix (m00, m10, m01, m11)
 *   4..7   translation x, translation y, mode, outline width
 *   8..11  rect (x, y, width, height) in local space
 *   12..15 radii (tl, bl, br, tr) | uv (u0, v0, u1, v1) | triangle p2 (x, y, -, -)
 *   16..19 triangle p0 (x, y), p1 (x, y)
 * ints (per instance):
 *   0..3   colors (top left, bottom left, bottom right, top right) as packed RGBA bytes
 * </pre>
 */
@Getter
@Accessors(fluent = true, chain = true, makeFinal = true)
public final class InstanceBuffer {

    public static final int FLOATS_PER_INSTANCE = 20;
    public static final int COLORS_PER_INSTANCE = 4;

    public static final int MODE_SHAPE = 0;
    public static final int MODE_TEXTURE = 1;
    public static final int MODE_TRIANGLE_AA = 2;
    public static final int MODE_TRIANGLE = 3;
    public static final int MODE_RECT = 4;

    private float[] floats = new float[FLOATS_PER_INSTANCE * 1024];
    private int[] colors = new int[COLORS_PER_INSTANCE * 1024];
    private int count;

    public static int pack(final Color color) {
        return color.toABGR(); // Little endian -> bytes in memory are R, G, B, A
    }

    public void reset() {
        this.count = 0;
    }

    /**
     * Add a rounded rectangle shape. Plain rectangles and circles are special cases of this.
     */
    public void shape(final Transform2D m, final float x, final float y, final float width, final float height,
                      final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth,
                      final int ctl, final int cbl, final int cbr, final int ctr) {
        float maxRadius = Math.max(0, Math.min(width, height) / 2F);
        int f = this.begin(m.m00(), m.m10(), m.m01(), m.m11(), m.m02(), m.m12(), MODE_SHAPE, outlineWidth, x, y, width, height, ctl, cbl, cbr, ctr);
        this.floats[f + 12] = clamp(rtl, maxRadius);
        this.floats[f + 13] = clamp(rbl, maxRadius);
        this.floats[f + 14] = clamp(rbr, maxRadius);
        this.floats[f + 15] = clamp(rtr, maxRadius);
    }

    /**
     * Add a filled rectangle without anti aliasing.<br>
     * Hard edges make sure that adjacent rectangles (e.g. stacked gradients) do not produce visible seams.
     */
    public void rect(final Transform2D m, final float x, final float y, final float width, final float height,
                     final int ctl, final int cbl, final int cbr, final int ctr) {
        this.begin(m.m00(), m.m10(), m.m01(), m.m11(), m.m02(), m.m12(), MODE_RECT, 0, x, y, width, height, ctl, cbl, cbr, ctr);
    }

    /**
     * Add a rectangle that is rotated around its origin.<br>
     * Used for lines: The rect starts at (x, y) and extends along the given direction.
     */
    public void rotatedRect(final Transform2D m, final float x, final float y, final float cos, final float sin,
                            final float length, final float thickness, final int color) {
        float n00 = m.m00() * cos + m.m01() * sin;
        float n01 = -m.m00() * sin + m.m01() * cos;
        float n10 = m.m10() * cos + m.m11() * sin;
        float n11 = -m.m10() * sin + m.m11() * cos;
        int f = this.begin(n00, n10, n01, n11, m.transformX(x, y), m.transformY(x, y), MODE_SHAPE, 0,
                0, -thickness / 2F, length, thickness, color, color, color, color);
        this.floats[f + 12] = 0;
        this.floats[f + 13] = 0;
        this.floats[f + 14] = 0;
        this.floats[f + 15] = 0;
    }

    public void texture(final Transform2D m, final float x, final float y, final float width, final float height,
                        final float u0, final float v0, final float u1, final float v1, final int color) {
        int f = this.begin(m.m00(), m.m10(), m.m01(), m.m11(), m.m02(), m.m12(), MODE_TEXTURE, 0, x, y, width, height, color, color, color, color);
        this.floats[f + 12] = u0;
        this.floats[f + 13] = v0;
        this.floats[f + 14] = u1;
        this.floats[f + 15] = v1;
    }

    public void triangle(final Transform2D m, final float x1, final float y1, final float x2, final float y2, final float x3, final float y3,
                         final boolean antiAliased, final int color) {
        // The distance to a triangle without an area is 0 everywhere, which would fill its whole bounding box
        if ((x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1) == 0) return;
        float minX = Math.min(x1, Math.min(x2, x3));
        float minY = Math.min(y1, Math.min(y2, y3));
        float maxX = Math.max(x1, Math.max(x2, x3));
        float maxY = Math.max(y1, Math.max(y2, y3));
        int f = this.begin(m.m00(), m.m10(), m.m01(), m.m11(), m.m02(), m.m12(), antiAliased ? MODE_TRIANGLE_AA : MODE_TRIANGLE, 0,
                minX, minY, maxX - minX, maxY - minY, color, color, color, color);
        this.floats[f + 12] = x3;
        this.floats[f + 13] = y3;
        this.floats[f + 14] = 0;
        this.floats[f + 15] = 0;
        this.floats[f + 16] = x1;
        this.floats[f + 17] = y1;
        this.floats[f + 18] = x2;
        this.floats[f + 19] = y2;
    }

    private int begin(final float m00, final float m10, final float m01, final float m11, final float tx, final float ty,
                      final int mode, final float outlineWidth, final float x, final float y, final float width, final float height,
                      final int ctl, final int cbl, final int cbr, final int ctr) {
        if ((this.count + 1) * FLOATS_PER_INSTANCE > this.floats.length) {
            this.floats = Arrays.copyOf(this.floats, this.floats.length * 2);
            this.colors = Arrays.copyOf(this.colors, this.colors.length * 2);
        }
        int f = this.count * FLOATS_PER_INSTANCE;
        float[] floats = this.floats;
        floats[f] = m00;
        floats[f + 1] = m10;
        floats[f + 2] = m01;
        floats[f + 3] = m11;
        floats[f + 4] = tx;
        floats[f + 5] = ty;
        floats[f + 6] = mode;
        floats[f + 7] = outlineWidth;
        floats[f + 8] = x;
        floats[f + 9] = y;
        floats[f + 10] = width;
        floats[f + 11] = height;
        int c = this.count * COLORS_PER_INSTANCE;
        this.colors[c] = ctl;
        this.colors[c + 1] = cbl;
        this.colors[c + 2] = cbr;
        this.colors[c + 3] = ctr;
        this.count++;
        return f;
    }

    private static float clamp(final float radius, final float max) {
        return Math.max(0, Math.min(radius, max));
    }

}
