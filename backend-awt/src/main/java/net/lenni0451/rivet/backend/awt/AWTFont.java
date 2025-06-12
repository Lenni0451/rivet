package net.lenni0451.rivet.backend.awt;

import net.lenni0451.rivet.backend.Font;

public record AWTFont(java.awt.Font font) implements Font {

    @Override
    public boolean hasGlyph(int codePoint) {
        return this.font.canDisplay(codePoint);
    }

    @Override
    public int getSize() {
        return this.font.getSize();
    }

}
