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


    void fillCircle(final float x, final float y, final float radius, final Color color);

    void outlineCircle(final float x, final float y, final float radius, final float outlineWidth, final Color color);

    void fillRect(final float x, final float y, final float width, final float height, final Color color);

    void outlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color);

    void fillRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final Color color);

    void outlineRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final float outlineWidth, final Color color);

    default void fillOptimizedRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final Color color) {
        float maxRadius = Math.min(width, height) / 2F;
        float radius = Math.min(cornerRadius, maxRadius);
        if (radius <= 0) {
            this.fillRect(x, y, width, height, color);
        } else if (width == height && radius == maxRadius) {
            this.fillCircle(x + radius, y + radius, radius, color);
        } else {
            this.fillRoundedRect(x, y, width, height, radius, color);
        }
    }

    default void outlineOptimizedRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final float outlineWidth, final Color color) {
        float maxRadius = Math.min(width, height) / 2F;
        float radius = Math.min(cornerRadius, maxRadius);
        if (radius <= 0) {
            this.outlineRect(x, y, width, height, outlineWidth, color);
        } else if (width == height && radius == maxRadius) {
            this.outlineCircle(x + radius, y + radius, radius, outlineWidth, color);
        } else {
            this.outlineRoundedRect(x, y, width, height, radius, outlineWidth, color);
        }
    }

    void renderText(final ShapedText shapedText, final float x, final float y, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin);

}
