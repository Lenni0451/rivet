package net.lenni0451.rivet.component;

import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.math.Size;
import org.joml.Matrix4fStack;

public interface Renderable {

    void render(final Renderer renderer, final Matrix4fStack positionMatrix, final Size size);

}
