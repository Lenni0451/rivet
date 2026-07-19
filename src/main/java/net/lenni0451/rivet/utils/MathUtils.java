package net.lenni0451.rivet.utils;

import lombok.experimental.UtilityClass;
import net.lenni0451.rivet.math.Corners;

@UtilityClass
public class MathUtils {

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

    public static float lerp(final float start, final float end, final float progress) {
        return start + (end - start) * progress;
    }

    public static double snap(final double value, final double min, final double max, final double step) {
        if (step <= 0) {
            return net.lenni0451.commons.math.MathUtils.clamp(value, min, max);
        }
        double diff = value - min;
        double lowerStep = Math.floor(diff / step);
        double v1 = min + lowerStep * step;
        double v2 = min + (lowerStep + 1) * step;
        v2 = Math.min(v2, max);

        double snapped;
        if (value - v1 < v2 - value) {
            snapped = v1;
        } else {
            snapped = v2;
        }
        return Math.max(min, Math.min(max, snapped));
    }

}
