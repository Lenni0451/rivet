package net.lenni0451.rivet.backend.opengl;

import net.lenni0451.rivet.backend.ShapedTextBuffer;

public record ThinGLShapedTextBuffer(net.raphimc.thingl.text.shaper.ShapedTextBuffer shapedTextBuffer) implements ShapedTextBuffer {
}
