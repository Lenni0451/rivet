package net.lenni0451.rivet.math;

import lombok.With;
import lombok.experimental.WithBy;

@With
@WithBy
public record Padding(float left, float top, float right, float bottom) {

    public static final Padding EMPTY = new Padding(0, 0, 0, 0);

    public Padding(final float all) {
        this(all, all, all, all);
    }

    public Padding {
        if (left < 0 || top < 0 || right < 0 || bottom < 0) {
            throw new IllegalArgumentException("Padding values (" + left + ", " + top + ", " + right + ", " + bottom + ") must be non-negative");
        }
    }

    public float horizontal() {
        return this.left + this.right;
    }

    public float vertical() {
        return this.top + this.bottom;
    }

}
