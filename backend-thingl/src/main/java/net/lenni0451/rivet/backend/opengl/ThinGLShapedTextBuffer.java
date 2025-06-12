package net.lenni0451.rivet.backend.opengl;

import net.lenni0451.rivet.backend.ShapedTextBuffer;

public class ThinGLShapedTextBuffer implements ShapedTextBuffer {

    final net.raphimc.thingl.text.shaper.ShapedTextBuffer shapedTextBuffer;

    public ThinGLShapedTextBuffer(final net.raphimc.thingl.text.shaper.ShapedTextBuffer shapedTextBuffer) {
        this.shapedTextBuffer = shapedTextBuffer;
    }

}
