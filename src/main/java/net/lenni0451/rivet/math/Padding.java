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
        left = Math.max(0, left);
        top = Math.max(0, top);
        right = Math.max(0, right);
        bottom = Math.max(0, bottom);
    }

    public float horizontal() {
        return this.left + this.right;
    }

    public float vertical() {
        return this.top + this.bottom;
    }

}
