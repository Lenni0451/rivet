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

}
