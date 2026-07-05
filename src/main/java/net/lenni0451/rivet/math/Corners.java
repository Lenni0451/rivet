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
        topLeft = Math.max(0, topLeft);
        bottomLeft = Math.max(0, bottomLeft);
        bottomRight = Math.max(0, bottomRight);
        topRight = Math.max(0, topRight);
    }

}
