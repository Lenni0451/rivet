package net.lenni0451.rivet.backend.text;

import net.lenni0451.rivet.math.Point;

public interface ShapedText extends Shaped {

    Point cursorPosition(final int index);

    int index(final float x, final float y);

}

