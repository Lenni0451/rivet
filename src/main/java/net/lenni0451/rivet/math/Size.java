package net.lenni0451.rivet.math;

import lombok.With;
import lombok.experimental.WithBy;

@With
@WithBy
public record Size(float width, float height) {

    public static final Size EMPTY = new Size(0, 0);

    public Size scale(final float x, final float y) {
        return new Size(this.width * x, this.height * y);
    }

}
