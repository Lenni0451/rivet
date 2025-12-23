package net.lenni0451.rivet.backend;

import net.lenni0451.commons.color.Color;

public interface Renderer {

    void push();

    void translate(final float x, final float y);

    void pushScissor(final float x, final float y, final float width, final float height);

    void popScissor();

    default void scale(final float xy) {
        this.scale(xy, xy);
    }

    void scale(final float x, final float y);

    void pop();


    void fillRect(final float x, final float y, final float width, final float height, final Color color);

    void renderText(final ShapedText shapedText, final float x, final float y, final HorizontalOrigin horizontalOrigin, final VerticalOrigin verticalOrigin);


    enum VerticalOrigin {
        BASELINE,
        LOGICAL_TOP,
        LOGICAL_CENTER,
        LOGICAL_BOTTOM,
        VISUAL_TOP,
        VISUAL_CENTER,
        VISUAL_BOTTOM,
    }

    enum HorizontalOrigin {
        LOGICAL_LEFT,
        VISUAL_LEFT,
        VISUAL_CENTER,
        VISUAL_RIGHT,
    }

}
