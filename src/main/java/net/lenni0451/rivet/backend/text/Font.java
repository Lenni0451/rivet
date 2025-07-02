package net.lenni0451.rivet.backend.text;

public interface Font {

    boolean hasGlyph(final int codePoint);

    int getSize();

    float getHeight();

    String getName();

    String getFamily();

}
