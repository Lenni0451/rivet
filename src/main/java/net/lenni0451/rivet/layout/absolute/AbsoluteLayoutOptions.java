package net.lenni0451.rivet.layout.absolute;

import lombok.With;
import net.lenni0451.rivet.layout.LayoutOptions;

import javax.annotation.Nullable;

@With
public record AbsoluteLayoutOptions(float x, float y, @Nullable Float width, @Nullable Float height) implements LayoutOptions {

    public AbsoluteLayoutOptions(final float x, final float y) {
        this(x, y, null, null);
    }

}
