package net.lenni0451.rivet.math;

import lombok.With;
import lombok.experimental.WithBy;

@With
@WithBy
public record Corners(float topLeft, float bottomLeft, float bottomRight, float topRight) {

    public static final Corners EMPTY = new Corners(0, 0, 0, 0);

    public Corners(final float all) {
        this(all, all, all, all);
    }

    public Corners {
        if (topLeft < 0 || bottomLeft < 0 || bottomRight < 0 || topRight < 0) {
            throw new IllegalArgumentException("Corner values (" + topLeft + ", " + bottomLeft + ", " + bottomRight + ", " + topRight + ") must be non-negative");
        }
    }

}
