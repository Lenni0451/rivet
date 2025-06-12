package net.lenni0451.rivet.backend;

import net.lenni0451.commons.color.Color;
import org.joml.Matrix4f;

public interface Renderer {

    void filledRectangle(final Matrix4f positionMatrix, final float x, final float y, final float width, final float height, final Color color);

    void text(final Matrix4f positionMatrix, final ShapedTextBuffer shapedTextBuffer, final float x, final float y);

    void beginBatch();

    void endBatch();

}
