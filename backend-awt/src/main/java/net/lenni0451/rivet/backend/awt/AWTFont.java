package net.lenni0451.rivet.backend.awt;

import net.lenni0451.rivet.backend.Font;

public class AWTFont implements Font {

    final java.awt.Font font;

    public AWTFont(final java.awt.Font font) {
        this.font = font;
    }

    @Override
    public int getSize() {
        return this.font.getSize();
    }

}
