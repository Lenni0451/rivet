package net.lenni0451.rivet.backend.opengl;

import net.lenni0451.rivet.backend.Font;

public class ThinGLFont implements Font {

    final net.raphimc.thingl.text.font.Font font;

    public ThinGLFont(final net.raphimc.thingl.text.font.Font font) {
        this.font = font;
    }

    @Override
    public int getSize() {
        return this.font.getSize();
    }

}
