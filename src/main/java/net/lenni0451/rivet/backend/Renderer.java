package net.lenni0451.rivet.backend;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.text.TextOrigin;

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

    void outlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color);

    void fillRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final Color color);

    void outlineRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final float outlineWidth, final Color color);

    void renderText(final ShapedText shapedText, final float x, final float y, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin);

}
