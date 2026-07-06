package net.lenni0451.rivet.backend.render;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.text.model.TextOrigin;

import java.util.function.Consumer;

public interface Renderer {

    float xOffset();

    float yOffset();

    void translate(final float x, final float y, final Runnable renderer);

    void componentBounds(final float x, final float y, final float width, final float height, final Runnable renderer);

    void scissor(final float x, final float y, final float width, final float height, final Runnable renderer);

    default void scale(final float xy, final Runnable renderer) {
        this.scale(xy, xy, renderer);
    }

    void scale(final float x, final float y, final Runnable renderer);

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

    void fillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color);

    default void outlineRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final float outlineWidth, final Color color) {
        this.outlineRoundedRect(x, y, width, height, cornerRadius, cornerRadius, cornerRadius, cornerRadius, outlineWidth, color);
    }

    void outlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color);

    default void optimizedFillRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final Color color) {
        if (width > 0 && height > 0 && color.getAlpha() > 0) {
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
    }

    default void optimizedFillRoundedRect(final float x, final float y, final float width, final float height, float rtl, float rbl, float rbr, float rtr, final Color color) {
        if (width > 0 && height > 0 && color.getAlpha() > 0) {
            if (rtl == rbl && rtl == rbr && rtl == rtr) {
                this.optimizedFillRoundedRect(x, y, width, height, rtl, color);
            } else {
                rtl = Math.min(rtl, Math.min(width, height));
                rtr = Math.min(rtr, Math.min(width - rtl, height));
                rbl = Math.min(rbl, Math.min(width, height - rtl));
                rbr = Math.min(rbr, Math.min(width - rbl, height - rtr));
                if (rtl <= 0 && rbl <= 0 && rbr <= 0 && rtr <= 0) {
                    this.fillRect(x, y, width, height, color);
                } else if (width == height && rtl == width / 2 && rbl == width / 2 && rbr == width / 2 && rtr == width / 2) {
                    this.fillCircle(x + rtl, y + rtl, rtl, color);
                } else {
                    this.fillRoundedRect(x, y, width, height, rtl, rbl, rbr, rtr, color);
                }
            }
        }
    }

    default void optimizedOutlineRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final float outlineWidth, final Color color) {
        if (width > 0 && height > 0 && outlineWidth > 0 && color.getAlpha() > 0) {
            if (outlineWidth >= width || outlineWidth >= height) {
                this.optimizedFillRoundedRect(x, y, width, height, cornerRadius, color);
            } else {
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
        }
    }

    default void optimizedOutlineRoundedRect(final float x, final float y, final float width, final float height, float rtl, float rbl, float rbr, float rtr, final float outlineWidth, final Color color) {
        if (width > 0 && height > 0 && outlineWidth > 0 && color.getAlpha() > 0) {
            if (rtl == rbl && rtl == rbr && rtl == rtr) {
                this.optimizedOutlineRoundedRect(x, y, width, height, rtl, outlineWidth, color);
            } else {
                rtl = Math.min(rtl, Math.min(width, height));
                rtr = Math.min(rtr, Math.min(width - rtl, height));
                rbl = Math.min(rbl, Math.min(width, height - rtl));
                rbr = Math.min(rbr, Math.min(width - rbl, height - rtr));
                if (outlineWidth >= width || outlineWidth >= height) {
                    this.optimizedFillRoundedRect(x, y, width, height, rtl, rbl, rbr, rtr, color);
                } else if (rtl <= 0 && rbl <= 0 && rbr <= 0 && rtr <= 0) {
                    this.outlineRect(x, y, width, height, outlineWidth, color);
                } else if (width == height && rtl == width / 2 && rbl == width / 2 && rbr == width / 2 && rtr == width / 2) {
                    this.outlineCircle(x + width / 2, y + width / 2, width / 2, outlineWidth, color);
                } else {
                    this.outlineRoundedRect(x, y, width, height, rtl, rbl, rbr, rtr, outlineWidth, color);
                }
            }
        }
    }

    void fillPolygon(final Point[] points, final Color color);

    void line(final float x1, final float y1, final float x2, final float y2, final float width, final Color color);

    void polyLine(final Point[] points, final float width, final Color color);

    void fillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr);

    void text(final ShapedText shapedText, final float anchorX, final float anchorY, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin);

    void image(final Texture texture, final float x, final float y, final float width, final float height, final Color color);

    void custom(final RenderCommand.Custom renderCommand);

}
