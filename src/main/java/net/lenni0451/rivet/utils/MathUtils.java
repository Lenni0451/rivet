package net.lenni0451.rivet.utils;

import lombok.experimental.UtilityClass;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

@UtilityClass
public class MathUtils {

    public static final float EPSILON = 0.001F;

    public static boolean isGreaterThan(final float a, final float b) {
        return a - b > EPSILON;
    }

    public static boolean isGreaterThan(final float a, final float b, final float epsilon) {
        return a - b > epsilon;
    }

    public static float roundMin(final float value, final float min) {
        return Math.max(Math.round(value), min);
    }

    public static Corners clampCorners(final float width, final float height, float rtl, float rbl, float rbr, float rtr) {
        if (rtl == rbl && rtl == rbr && rtl == rtr) {
            float maxRadius = Math.min(width, height) / 2F;
            float radius = Math.min(rtl, maxRadius);
            return new Corners(radius, radius, radius, radius);
        } else {
            rtl = Math.min(rtl, Math.min(width, height));
            rtr = Math.min(rtr, Math.min(width - rtl, height));
            rbl = Math.min(rbl, Math.min(width, height - rtl));
            rbr = Math.min(rbr, Math.min(width - rbl, height - rtr));
            return new Corners(rtl, rbl, rbr, rtr);
        }
    }

    public static double snap(final double value, final double min, final double max, final double step) {
        return snap(value, min, max, step, false);
    }

    public static double snap(final double value, final double min, final double max, final double step, final boolean zeroAligned) {
        if (step <= 0) {
            return net.lenni0451.commons.math.MathUtils.clamp(value, min, max);
        }
        double base = zeroAligned ? 0 : min;
        double diff = value - base;
        double lowerStep = Math.floor(diff / step);
        double v1 = Math.max(min, base + lowerStep * step);
        double v2 = Math.min(max, base + (lowerStep + 1) * step);

        double snapped;
        if (value - v1 < v2 - value) {
            snapped = v1;
        } else {
            snapped = v2;
        }
        return Math.max(min, Math.min(max, snapped));
    }

    public static Rectangle relativizeVisibleArea(final Rectangle visibleArea, final Rectangle bounds) {
        return relativizeVisibleArea(visibleArea, bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    public static Rectangle relativizeVisibleArea(final Rectangle visibleArea, final float x, final float y, final Size size) {
        return relativizeVisibleArea(visibleArea, x, y, size.width(), size.height());
    }

    public static Rectangle relativizeVisibleArea(final Rectangle visibleArea, final float x, final float y, final float width, final float height) {
        Rectangle intersection = visibleArea.intersection(new Rectangle(x, y, width, height));
        if (intersection.equals(Rectangle.EMPTY)) return Rectangle.EMPTY;
        return new Rectangle(intersection.x() - x, intersection.y() - y, intersection.width(), intersection.height());
    }

    public static Rectangle rotateVisibleArea(final Rectangle visibleArea, final float degrees) {
        return rotateVisibleArea(visibleArea, degrees, 0, 0);
    }

    public static Rectangle rotateVisibleArea(final Rectangle visibleArea, final float degrees, final float pivotX, final float pivotY) {
        if (visibleArea.equals(Rectangle.EMPTY) || degrees % 360F == 0) return visibleArea;
        double rad = Math.toRadians(-degrees);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        float[] xs = {visibleArea.x(), visibleArea.maxX(), visibleArea.maxX(), visibleArea.x()};
        float[] ys = {visibleArea.y(), visibleArea.y(), visibleArea.maxY(), visibleArea.maxY()};

        for (int i = 0; i < 4; i++) {
            float dx = xs[i] - pivotX;
            float dy = ys[i] - pivotY;
            float rx = pivotX + (dx * cos - dy * sin);
            float ry = pivotY + (dx * sin + dy * cos);
            if (rx < minX) minX = rx;
            if (rx > maxX) maxX = rx;
            if (ry < minY) minY = ry;
            if (ry > maxY) maxY = ry;
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

}
