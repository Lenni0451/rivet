package net.lenni0451.rivet.backend.opengl;

import net.lenni0451.rivet.backend.ShapedTextBuffer;
import org.joml.primitives.Rectanglef;

public record ThinGLShapedTextBuffer(net.raphimc.thingl.text.shaper.ShapedTextBuffer shapedTextBuffer) implements ShapedTextBuffer {

    @Override
    public Rectanglef bounds() {
        return this.shapedTextBuffer.bounds();
    }

}
