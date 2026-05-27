package net.lenni0451.rivet.layout.anchor;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.layout.LayoutOptions;

@With
@WithBy
public record AnchorLayoutOptions(
        float anchorMinX, float anchorMaxX, float anchorMinY, float anchorMaxY,
        float offsetLeft, float offsetTop, float offsetRight, float offsetBottom,
        float pivotX, float pivotY
) implements LayoutOptions {

    public static final AnchorLayoutOptions EMPTY = new AnchorLayoutOptions(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

    public static AnchorLayoutOptions point(final float x, final float y) {
        return new AnchorLayoutOptions(x, x, y, y, 0, 0, 0, 0, 0, 0);
    }


    public AnchorLayoutOptions(final float anchorMinX, final float anchorMaxX, final float anchorMinY, final float anchorMaxY) {
        this(anchorMinX, anchorMaxX, anchorMinY, anchorMaxY, 0, 0, 0, 0, 0, 0);
    }

    public AnchorLayoutOptions from(final float x, final float y) {
        return this.withAnchorMinX(x).withAnchorMinY(y);
    }

    public AnchorLayoutOptions to(final float x, final float y) {
        return this.withAnchorMaxX(x).withAnchorMaxY(y);
    }

}
