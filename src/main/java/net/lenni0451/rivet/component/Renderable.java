package net.lenni0451.rivet.component;

import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.math.Rectangle;

public interface Renderable {

    void render(final Renderer renderer, final Rectangle bounds);

}
