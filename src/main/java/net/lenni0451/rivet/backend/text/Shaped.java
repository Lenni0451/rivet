package net.lenni0451.rivet.backend.text;

import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.text.model.TextOrigin;

public interface Shaped {

    Rectangle visualBounds();

    Rectangle logicalBounds();

    default float anchorOffset(final TextOrigin.Horizontal horizontalOrigin) {
        return switch (horizontalOrigin) {
            case LOGICAL_LEFT -> -this.logicalBounds().x();
            case VISUAL_LEFT -> -this.visualBounds().x();
            case VISUAL_CENTER -> {
                Rectangle visualBounds = this.visualBounds();
                yield -(visualBounds.x() + visualBounds.maxX()) / 2F;
            }
            case VISUAL_RIGHT -> -this.visualBounds().maxX();
        };
    }

    default float anchorOffset(final TextOrigin.Vertical verticalOrigin) {
        return switch (verticalOrigin) {
            case BASELINE -> 0;
            case LOGICAL_TOP -> -this.logicalBounds().y();
            case LOGICAL_CENTER -> {
                Rectangle logicalBounds = this.logicalBounds();
                yield -(logicalBounds.y() + logicalBounds.maxY()) / 2F;
            }
            case LOGICAL_BOTTOM -> -this.logicalBounds().maxY();
            case VISUAL_TOP -> -this.visualBounds().y();
            case VISUAL_CENTER -> {
                Rectangle visualBounds = this.visualBounds();
                yield -(visualBounds.y() + visualBounds.maxY()) / 2F;
            }
            case VISUAL_BOTTOM -> -this.visualBounds().maxY();
        };
    }

    default float alignAnchor(final float anchor, final TextOrigin.Horizontal horizontalOrigin) {
        return anchor + this.anchorOffset(horizontalOrigin);
    }

    default float alignAnchor(final float anchor, final TextOrigin.Vertical verticalOrigin) {
        return anchor + this.anchorOffset(verticalOrigin);
    }

    default float alignAnchorTo(final float x, final TextOrigin.Horizontal from, final TextOrigin.Horizontal to) {
        if (from.equals(to)) return x;
        return this.alignAnchor(x, from) - this.anchorOffset(to);
    }

    default float alignAnchorTo(final float y, final TextOrigin.Vertical from, final TextOrigin.Vertical to) {
        if (from.equals(to)) return y;
        return this.alignAnchor(y, from) - this.anchorOffset(to);
    }

}
