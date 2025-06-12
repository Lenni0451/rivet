package net.lenni0451.rivet.backend;

public interface Font {

    boolean hasGlyph(final int codePoint);

    int getSize();

}
