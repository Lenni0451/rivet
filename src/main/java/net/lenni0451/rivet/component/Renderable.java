package net.lenni0451.rivet.component;

import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.math.Size;

public interface Renderable {

    void render(final Renderer renderer, final Size size);

}
