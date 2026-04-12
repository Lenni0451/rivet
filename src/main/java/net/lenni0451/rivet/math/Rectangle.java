package net.lenni0451.rivet.math;

import lombok.With;
import lombok.experimental.WithBy;

@With
@WithBy
public record Rectangle(float x, float y, float width, float height) {

    public static final Rectangle EMPTY = new Rectangle(0, 0, 0, 0);

    public Size size() {
        return new Size(this.width, this.height);
    }

    public boolean contains(final float x, final float y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }

}
