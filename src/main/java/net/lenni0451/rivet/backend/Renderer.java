package net.lenni0451.rivet.backend;

import net.lenni0451.commons.color.Color;

public interface Renderer {

    void push();

    void translate(final float x, final float y);

    default void scale(final float xy) {
        this.scale(xy, xy);
    }

    void scale(final float x, final float y);

    void pop();


    void fillRect(final float x, final float y, final float width, final float height, final Color color);

}
