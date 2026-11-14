package net.lenni0451.rivet.math;

import lombok.With;
import lombok.experimental.WithBy;

@With
@WithBy
public record Transform2D(float m00, float m01, float m02, float m10, float m11, float m12) {

    public static final Transform2D IDENTITY = new Transform2D(1, 0, 0, 0, 1, 0);

    public Transform2D translate(final float x, final float y) {
        if (x == 0 && y == 0) return this;
        return new Transform2D(
                this.m00, this.m01, this.m02 + this.m00 * x + this.m01 * y,
                this.m10, this.m11, this.m12 + this.m10 * x + this.m11 * y
        );
    }

    public Transform2D scale(final float x, final float y) {
        if (x == 1 && y == 1) return this;
        return new Transform2D(
                this.m00 * x, this.m01 * y, this.m02,
                this.m10 * x, this.m11 * y, this.m12
        );
    }

    public Transform2D rotate(final float radians) {
        if (radians == 0) return this;
        final float cos = (float) Math.cos(radians);
        final float sin = (float) Math.sin(radians);
        return new Transform2D(
                this.m00 * cos + this.m01 * sin, -this.m00 * sin + this.m01 * cos, this.m02,
                this.m10 * cos + this.m11 * sin, -this.m10 * sin + this.m11 * cos, this.m12
        );
    }

    public float transformX(final float x, final float y) {
        return this.m00 * x + this.m01 * y + this.m02;
    }

    public float transformY(final float x, final float y) {
        return this.m10 * x + this.m11 * y + this.m12;
    }

    public boolean isAxisAligned() {
        return this.m01 == 0 && this.m10 == 0;
    }

    public float averageScale() {
        return (float) Math.sqrt(Math.abs(this.m00 * this.m11 - this.m01 * this.m10));
    }

    public Aabb transformBounds(final float minX, final float minY, final float maxX, final float maxY) {
        final float x1 = this.transformX(minX, minY);
        final float y1 = this.transformY(minX, minY);
        final float x2 = this.transformX(maxX, minY);
        final float y2 = this.transformY(maxX, minY);
        final float x3 = this.transformX(maxX, maxY);
        final float y3 = this.transformY(maxX, maxY);
        final float x4 = this.transformX(minX, maxY);
        final float y4 = this.transformY(minX, maxY);
        return new Aabb(
                Math.min(Math.min(x1, x2), Math.min(x3, x4)),
                Math.min(Math.min(y1, y2), Math.min(y3, y4)),
                Math.max(Math.max(x1, x2), Math.max(x3, x4)),
                Math.max(Math.max(y1, y2), Math.max(y3, y4))
        );
    }

    public Aabb transformBounds(final Rectangle rectangle) {
        return this.transformBounds(rectangle.x(), rectangle.y(), rectangle.maxX(), rectangle.maxY());
    }

}
