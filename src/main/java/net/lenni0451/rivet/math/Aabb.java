package net.lenni0451.rivet.math;

import lombok.With;
import lombok.experimental.WithBy;

@With
@WithBy
public record Aabb(float minX, float minY, float maxX, float maxY) {

    public static final Aabb EMPTY = new Aabb(Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    public static final Aabb FULL = new Aabb(-Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

    public boolean isEmpty() {
        return Float.isNaN(this.minX);
    }

    public boolean isFull() {
        return this.equals(FULL);
    }

    public Aabb intersect(final Aabb other) {
        final float minX = Math.max(this.minX, other.minX);
        final float minY = Math.max(this.minY, other.minY);
        final float maxX = Math.min(this.maxX, other.maxX);
        final float maxY = Math.min(this.maxY, other.maxY);
        // Inverted bounds must not be returned, they would turn into the gap between both bounds once their corners are transformed
        if (!(minX < maxX && minY < maxY)) return EMPTY;
        return new Aabb(minX, minY, maxX, maxY);
    }

    public Aabb union(final Aabb other) {
        if (other.isEmpty()) return this;
        if (this.isEmpty()) return other;
        return new Aabb(
                Math.min(this.minX, other.minX),
                Math.min(this.minY, other.minY),
                Math.max(this.maxX, other.maxX),
                Math.max(this.maxY, other.maxY)
        );
    }

    public boolean intersects(final Aabb other) {
        return this.minX < other.maxX && this.maxX > other.minX && this.minY < other.maxY && this.maxY > other.minY;
    }

}
