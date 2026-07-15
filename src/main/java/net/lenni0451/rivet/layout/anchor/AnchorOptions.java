package net.lenni0451.rivet.layout.anchor;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.layout.LayoutOptions;

@With
@WithBy
public record AnchorOptions(
        float anchorMinX, float anchorMinY, float anchorMaxX, float anchorMaxY,
        float offsetLeft, float offsetTop, float offsetRight, float offsetBottom,
        float pivotX, float pivotY
) implements LayoutOptions {

    public static final AnchorOptions EMPTY = new AnchorOptions(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

    public AnchorOptions(final float anchorMinX, final float anchorMinY, final float anchorMaxX, final float anchorMaxY) {
        this(anchorMinX, anchorMinY, anchorMaxX, anchorMaxY, 0, 0, 0, 0, 0, 0);
    }

    public AnchorOptions point(final float x, final float y) {
        return this.anchor(x, y, x, y);
    }

    public AnchorOptions from(final float x, final float y) {
        return this.withAnchorMinX(x).withAnchorMinY(y);
    }

    public AnchorOptions to(final float x, final float y) {
        return this.withAnchorMaxX(x).withAnchorMaxY(y);
    }

    public AnchorOptions anchor(final float minX, final float minY, final float maxX, final float maxY) {
        return this.from(minX, minY).to(maxX, maxY);
    }

    public AnchorOptions offset(final float left, final float top, final float right, final float bottom) {
        return this.withOffsetLeft(left).withOffsetTop(top).withOffsetRight(right).withOffsetBottom(bottom);
    }

    public AnchorOptions pivot(final float x, final float y) {
        return this.withPivotX(x).withPivotY(y);
    }

}
