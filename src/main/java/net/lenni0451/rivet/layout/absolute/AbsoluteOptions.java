package net.lenni0451.rivet.layout.absolute;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.layout.LayoutOptions;
import net.lenni0451.rivet.math.Rectangle;

import javax.annotation.Nullable;

@With
@WithBy
public record AbsoluteOptions(float x, float y, @Nullable Float width, @Nullable Float height) implements LayoutOptions {

    public static final AbsoluteOptions EMPTY = new AbsoluteOptions(0, 0, null, null);

    public AbsoluteOptions(final float x, final float y) {
        this(x, y, null, null);
    }

    public AbsoluteOptions(final Rectangle bounds) {
        this(bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    public AbsoluteOptions at(final float x, final float y) {
        return this.withX(x).withY(y);
    }

    public AbsoluteOptions sized(final float width, final float height) {
        return this.withWidth(width).withHeight(height);
    }

}
