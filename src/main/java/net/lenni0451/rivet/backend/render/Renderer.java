package net.lenni0451.rivet.backend.render;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.backend.text.ShapedTextBlock;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.text.model.TextOrigin;

import java.util.function.Consumer;

public interface Renderer {

    void translate(final float x, final float y, final Runnable renderer);

    void componentBounds(final float x, final float y, final float width, final float height, final Runnable renderer);

    void scissor(final float x, final float y, final float width, final float height, final Runnable renderer);

    default void scale(final float xy, final Runnable renderer) {
        this.scale(xy, xy, renderer);
    }

    void scale(final float x, final float y, final Runnable renderer);

    default void rotate(final float degrees, final float pivotX, final float pivotY, final Runnable renderer) {
        this.translate(pivotX, pivotY, () -> this.rotate(degrees, () -> this.translate(-pivotX, -pivotY, renderer)));
    }

    void rotate(final float degrees, final Runnable renderer);

    void stencil(final Consumer<Renderer> maskRenderer, final Runnable renderer);

    void inverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer);

    void custom(final ModifierCommand.Custom command, final Runnable renderer);


    void fillCircle(final float x, final float y, final float radius, final Color color);

    void outlineCircle(final float x, final float y, final float radius, final float outlineWidth, final Color color);

    void fillTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3, final Color color);

    void fillRect(final float x, final float y, final float width, final float height, final Color color);

    void outlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color);

    default void fillRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final Color color) {
        this.fillRoundedRect(x, y, width, height, cornerRadius, cornerRadius, cornerRadius, cornerRadius, color);
    }

    default void fillRoundedRect(final float x, final float y, final float width, final float height, final Corners corners, final Color color) {
        this.fillRoundedRect(x, y, width, height, corners.topLeft(), corners.bottomLeft(), corners.bottomRight(), corners.topRight(), color);
    }

    void fillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color);

    default void outlineRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final float outlineWidth, final Color color) {
        this.outlineRoundedRect(x, y, width, height, cornerRadius, cornerRadius, cornerRadius, cornerRadius, outlineWidth, color);
    }

    default void outlineRoundedRect(final float x, final float y, final float width, final float height, final Corners corners, final float outlineWidth, final Color color) {
        this.outlineRoundedRect(x, y, width, height, corners.topLeft(), corners.bottomLeft(), corners.bottomRight(), corners.topRight(), outlineWidth, color);
    }

    void outlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color);

    void fillPolygon(final Point[] points, final Color color);

    void line(final float x1, final float y1, final float x2, final float y2, final float width, final Color color);

    void polyLine(final Point[] points, final float width, final Color color);

    void fillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr);

    void text(final ShapedText shapedText, final float anchorX, final float anchorY, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin);

    default void text(final ShapedTextBlock shapedTextBlock, final float anchorX, final float anchorY, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin, final ShapedTextBlock.LineAlignment lineAlignment) {
        float blockLeft = shapedTextBlock.alignAnchorTo(anchorX, horizontalOrigin, TextOrigin.Horizontal.LOGICAL_LEFT);
        float currentY = shapedTextBlock.alignAnchorTo(anchorY, verticalOrigin, TextOrigin.Vertical.BASELINE);
        float blockWidth = shapedTextBlock.visualBounds().width();
        for (ShapedText line : shapedTextBlock.lines()) {
            float lineX = switch (lineAlignment) {
                case LEFT -> blockLeft;
                case RIGHT -> blockLeft + (blockWidth - line.visualBounds().width());
                case CENTER -> blockLeft + (blockWidth - line.visualBounds().width()) / 2F;
            };
            this.text(line, lineX, currentY, TextOrigin.Horizontal.LOGICAL_LEFT, TextOrigin.Vertical.BASELINE);
            currentY += line.logicalBounds().height();
        }
    }


    void image(final Texture texture, final float x, final float y, final float width, final float height, final Color color);

    void custom(final RenderCommand.Custom renderCommand);

}
