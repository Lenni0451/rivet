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

}
