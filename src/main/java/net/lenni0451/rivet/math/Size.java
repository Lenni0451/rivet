package net.lenni0451.rivet.math;

import lombok.With;
import lombok.experimental.WithBy;

@With
@WithBy
public record Size(float width, float height) {

    public static final Size EMPTY = new Size(0, 0);
    public static final Size INFINITE = new Size(Float.MAX_VALUE, Float.MAX_VALUE);

    public Size {
        width = Math.max(0, width);
        height = Math.max(0, height);
    }

    public Size plus(final float x, final float y) {
        return new Size(this.width + x, this.height + y);
    }

    public Size plus(final Padding padding) {
        return this.plus(padding.horizontal(), padding.vertical());
    }

    public Size minus(final float x, final float y) {
        return new Size(Math.max(this.width - x, 0), Math.max(this.height - y, 0));
    }

    public Size minus(final Padding padding) {
        return this.minus(padding.horizontal(), padding.vertical());
    }

    public Size max(final Size other) {
        return this.max(other.width, other.height);
    }

    public Size max(final float width, final float height) {
        return new Size(Math.max(this.width, width), Math.max(this.height, height));
    }

    public Size min(final Size other) {
        return this.min(other.width, other.height);
    }

    public Size min(final float width, final float height) {
        return new Size(Math.min(this.width, width), Math.min(this.height, height));
    }

}
