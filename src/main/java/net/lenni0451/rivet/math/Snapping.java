package net.lenni0451.rivet.math;

import lombok.experimental.UtilityClass;
import net.lenni0451.rivet.Rivet;

@UtilityClass
public final class Snapping {

    public static float snap(final Rivet rivet, final float value) {
        if (!rivet.snapToInteger()) return value;
        return Math.round(value);
    }

    public static Rectangle snap(final Rivet rivet, final Rectangle rect) {
        if (!rivet.snapToInteger()) return rect;
        return new Rectangle(
                Math.round(rect.x()),
                Math.round(rect.y()),
                Math.round(rect.width()),
                Math.round(rect.height())
        );
    }

}
