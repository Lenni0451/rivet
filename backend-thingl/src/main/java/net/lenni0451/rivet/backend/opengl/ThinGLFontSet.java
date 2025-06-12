package net.lenni0451.rivet.backend.opengl;

import net.lenni0451.rivet.backend.Font;
import net.lenni0451.rivet.backend.FontSet;

public class ThinGLFontSet implements FontSet {

    final net.raphimc.thingl.text.font.FontSet fontSet;
    private final ThinGLFont mainFont;

    public ThinGLFontSet(final net.raphimc.thingl.text.font.FontSet fontSet) {
        this.fontSet = fontSet;
        this.mainFont = new ThinGLFont(this.fontSet.getMainFont());
    }

    @Override
    public Font getMainFont() {
        return this.mainFont;
    }

    @Override
    public Font getFont(final int codePoint) {
        return new ThinGLFont(this.fontSet.getFont(codePoint));
    }

}
