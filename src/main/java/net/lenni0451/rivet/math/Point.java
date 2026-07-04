package net.lenni0451.rivet.math;

import lombok.With;
import lombok.experimental.WithBy;

@With
@WithBy
public record Point(float x, float y) {

    public Point min(final Point other) {
        if (other.x >= this.x && other.y >= this.y) return this;
        return new Point(Math.min(this.x, other.x), Math.min(this.y, other.y));
    }

    public Point max(final Point point) {
        if (point.x <= this.x && point.y <= this.y) return this;
        return new Point(Math.max(this.x, point.x), Math.max(this.y, point.y));
    }

}
