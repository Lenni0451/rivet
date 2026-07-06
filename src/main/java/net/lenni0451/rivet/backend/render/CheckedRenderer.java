package net.lenni0451.rivet.backend.render;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.text.model.TextOrigin;

import java.util.function.Consumer;

public abstract class CheckedRenderer implements Renderer {

    @Override
    public final void translate(final float x, final float y, final Runnable renderer) {
        if (x == 0 && y == 0) {
            renderer.run();
        } else {
            this.doTranslate(x, y, renderer);
        }
    }

    protected abstract void doTranslate(final float x, final float y, final Runnable renderer);

    @Override
    public final void componentBounds(final float x, final float y, final float width, final float height, final Runnable renderer) {
        if (width > 0 && height > 0) {
            this.doComponentBounds(x, y, width, height, renderer);
        }
    }

    protected abstract void doComponentBounds(final float x, final float y, final float width, final float height, final Runnable renderer);

    @Override
    public final void scissor(final float x, final float y, final float width, final float height, final Runnable renderer) {
        if (width > 0 && height > 0) {
            this.doScissor(x, y, width, height, renderer);
        }
    }

    protected abstract void doScissor(final float x, final float y, final float width, final float height, final Runnable renderer);

    @Override
    public final void scale(final float x, final float y, final Runnable renderer) {
        if (x == 1 && y == 1) {
            renderer.run();
        } else if (x != 0 && y != 0) {
            this.doScale(x, y, renderer);
        }
    }

    protected abstract void doScale(final float x, final float y, final Runnable renderer);

    @Override
    public final void stencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        this.doStencil(maskRenderer, renderer);
    }

    protected abstract void doStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer);

    @Override
    public final void inverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        this.doInverseStencil(maskRenderer, renderer);
    }

    protected abstract void doInverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer);


    @Override
    public final void fillCircle(final float x, final float y, final float radius, final Color color) {
        if (radius > 0 && color.getAlpha() > 0) {
            this.doFillCircle(x, y, radius, color);
        }
    }

    protected abstract void doFillCircle(final float x, final float y, final float radius, final Color color);

    @Override
    public final void outlineCircle(final float x, final float y, final float radius, final float outlineWidth, final Color color) {
        if (radius > 0 && outlineWidth > 0 && color.getAlpha() > 0) {
            this.doOutlineCircle(x, y, radius, outlineWidth, color);
        }
    }

    protected abstract void doOutlineCircle(final float x, final float y, final float radius, final float outlineWidth, final Color color);

    @Override
    public final void fillTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3, final Color color) {
        if (color.getAlpha() > 0) {
            this.doFillTriangle(x1, y1, x2, y2, x3, y3, color);
        }
    }

    protected abstract void doFillTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3, final Color color);

    @Override
    public final void fillRect(final float x, final float y, final float width, final float height, final Color color) {
        if (width > 0 && height > 0 && color.getAlpha() > 0) {
            this.doFillRect(x, y, width, height, color);
        }
    }

    protected abstract void doFillRect(final float x, final float y, final float width, final float height, final Color color);

    @Override
    public final void outlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color) {
        if (width > 0 && height > 0 && outlineWidth > 0 && color.getAlpha() > 0) {
            this.doOutlineRect(x, y, width, height, outlineWidth, color);
        }
    }

    protected abstract void doOutlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color);

    @Override
    public final void fillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color) {
        if (width > 0 && height > 0 && color.getAlpha() > 0) {
            this.doFillRoundedRect(x, y, width, height, Math.max(0, rtl), Math.max(0, rbl), Math.max(0, rbr), Math.max(0, rtr), color);
        }
    }

    protected abstract void doFillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color);

    @Override
    public final void outlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color) {
        if (width > 0 && height > 0 && outlineWidth > 0 && color.getAlpha() > 0) {
            this.doOutlineRoundedRect(x, y, width, height, Math.max(0, rtl), Math.max(0, rbl), Math.max(0, rbr), Math.max(0, rtr), outlineWidth, color);
        }
    }

    protected abstract void doOutlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color);

    @Override
    public final void fillPolygon(final Point[] points, final Color color) {
        if (points.length > 0 && points.length < 3) {
            throw new IllegalArgumentException("Polygon must have at least 3 points");
        } else if (points.length >= 3 && color.getAlpha() > 0) {
            this.doFillPolygon(points, color);
        }
    }

    protected abstract void doFillPolygon(final Point[] points, final Color color);

    @Override
    public final void line(final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        if (width > 0 && color.getAlpha() > 0 && (x1 != x2 || y1 != y2)) {
            this.doLine(x1, y1, x2, y2, width, color);
        }
    }

    protected abstract void doLine(final float x1, final float y1, final float x2, final float y2, final float width, final Color color);

    @Override
    public final void polyLine(final Point[] points, final float width, final Color color) {
        if (points.length == 1) {
            throw new IllegalArgumentException("Polyline must have at least 2 points");
        } else if (points.length >= 2 && color.getAlpha() > 0) {
            this.doPolyLine(points, width, color);
        }
    }

    protected abstract void doPolyLine(final Point[] points, final float width, final Color color);

    @Override
    public final void fillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr) {
        if (width > 0 && height > 0 && (ctl.getAlpha() > 0 || cbl.getAlpha() > 0 || cbr.getAlpha() > 0 || ctr.getAlpha() > 0)) {
            this.doFillGradientRect(x, y, width, height, ctl, cbl, cbr, ctr);
        }
    }

    protected abstract void doFillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr);

    @Override
    public final void text(final ShapedText shapedText, final float anchorX, final float anchorY, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin) {
        if (shapedText.visualBounds().width() > 0 && shapedText.visualBounds().height() > 0) {
            this.doText(shapedText, anchorX, anchorY, horizontalOrigin, verticalOrigin);
        }
    }

    protected abstract void doText(final ShapedText shapedText, final float anchorX, final float anchorY, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin);

    @Override
    public final void image(final Texture texture, final float x, final float y, final float width, final float height, final Color color) {
        if (width > 0 && height > 0 && texture.width() > 0 && texture.height() > 0 && color.getAlpha() > 0) {
            this.doImage(texture, x, y, width, height, color);
        }
    }

    protected abstract void doImage(final Texture texture, final float x, final float y, final float width, final float height, final Color color);

}
