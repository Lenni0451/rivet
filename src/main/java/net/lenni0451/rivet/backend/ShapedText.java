package net.lenni0451.rivet.backend;

import net.lenni0451.rivet.math.Size;

public interface ShapedText {

    Size visualSize();

    Size logicalSize();

    float cursorPosition(final int index);

    int index(final float x);

}
