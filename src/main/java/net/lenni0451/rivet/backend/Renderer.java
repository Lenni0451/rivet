package net.lenni0451.rivet.backend;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.text.ShapedTextBuffer;
import org.joml.Matrix4f;

public interface Renderer {

    void filledRectangle(final Matrix4f positionMatrix, final float x, final float y, final float width, final float height, final Color color);

    void outlinedRectangle(final Matrix4f positionMatrix, final float x, final float y, final float width, final float height, final Color color, final float lineWidth);

    void filledRoundedRectangle(final Matrix4f positionMatrix, final float x, final float y, final float width, final float height, final float radius, final Color color);

    void outlinedRoundedRectangle(final Matrix4f positionMatrix, final float x, final float y, final float width, final float height, final float radius, final Color color, final float lineWidth);

    void text(final Matrix4f positionMatrix, final ShapedTextBuffer shapedTextBuffer, final float x, final float y, final boolean baselineAligned);

    void texture(final Matrix4f positionMatrix, final Texture texture, final float x, final float y, final float width, final float height);

    void beginBatch();

    void endBatch();

}
