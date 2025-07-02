package net.lenni0451.rivet.backend.opengl;

import net.lenni0451.rivet.backend.Font;

public record ThinGLFont(net.raphimc.thingl.text.font.Font font) implements Font {

    public ThinGLFont(final net.raphimc.thingl.text.font.Font font) {
        this.font = font;
        ThinGLBackend.CLEANER.register(this, font::free);
    }

    @Override
    public boolean hasGlyph(final int codePoint) {
        return this.font.getGlyphByCodePoint(codePoint).glyphIndex() != 0;
    }

    @Override
    public int getSize() {
        return this.font.getSize();
    }

    @Override
    public float getHeight() {
        return this.font.getHeight();
    }

    @Override
    public String getName() {
        return this.font.getPostscriptName();
    }

    @Override
    public String getFamily() {
        return this.font.getFamily();
    }

}
