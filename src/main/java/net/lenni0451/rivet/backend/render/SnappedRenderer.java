package net.lenni0451.rivet.backend.render;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.text.model.TextOrigin;

import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public class SnappedRenderer<R extends Renderer> implements Renderer {

    private final R delegate;

    private float snapX(final float x) {
        float offset = this.xOffset();
        return Math.round(offset + x) - offset;
    }

    private float snapY(final float y) {
        float offset = this.yOffset();
        return Math.round(offset + y) - offset;
    }

    private float snapWidth(final float x, final float width) {
        float offset = this.xOffset();
        return Math.round(offset + x + width) - Math.round(offset + x);
    }

    private float snapHeight(final float y, final float height) {
        float offset = this.yOffset();
        return Math.round(offset + y + height) - Math.round(offset + y);
    }


    @Override
    public float xOffset() {
        return this.delegate.xOffset();
    }

    @Override
    public float yOffset() {
        return this.delegate.yOffset();
    }

    @Override
    public void translate(final float x, final float y, final Runnable renderer) {
        this.delegate.translate(x, y, renderer);
    }

    @Override
    public void componentBounds(final float x, final float y, final float width, final float height, final Runnable renderer) {
        this.delegate.componentBounds(this.snapX(x), this.snapY(y), this.snapWidth(x, width), this.snapHeight(y, height), renderer);
    }

    @Override
    public void scissor(final float x, final float y, final float width, final float height, final Runnable renderer) {
        this.delegate.scissor(this.snapX(x), this.snapY(y), this.snapWidth(x, width), this.snapHeight(y, height), renderer);
    }

    @Override
    public void scale(final float x, final float y, final Runnable renderer) {
        this.delegate.scale(x, y, renderer);
    }

    @Override
    public void stencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        this.delegate.stencil(mr -> maskRenderer.accept(new SnappedRenderer<>(mr)), renderer);
    }

    @Override
    public void inverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        this.delegate.inverseStencil(mr -> maskRenderer.accept(new SnappedRenderer<>(mr)), renderer);
    }

    @Override
    public void custom(final ModifierCommand.Custom command, final Runnable renderer) {
        this.delegate.custom(command, renderer);
    }


    @Override
    public void fillCircle(final float x, final float y, final float radius, final Color color) {
        this.delegate.fillCircle(this.snapX(x), this.snapY(y), Math.round(radius), color);
    }

    @Override
    public void outlineCircle(final float x, final float y, final float radius, final float outlineWidth, final Color color) {
        this.delegate.outlineCircle(this.snapX(x), this.snapY(y), Math.round(radius), Math.round(outlineWidth), color);
    }

    @Override
    public void fillTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3, final Color color) {
        this.delegate.fillTriangle(this.snapX(x1), this.snapY(y1), this.snapX(x2), this.snapY(y2), this.snapX(x3), this.snapY(y3), color);
    }

    @Override
    public void fillRect(final float x, final float y, final float width, final float height, final Color color) {
        this.delegate.fillRect(this.snapX(x), this.snapY(y), this.snapWidth(x, width), this.snapHeight(y, height), color);
    }

    @Override
    public void outlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color) {
        this.delegate.outlineRect(this.snapX(x), this.snapY(y), this.snapWidth(x, width), this.snapHeight(y, height), Math.round(outlineWidth), color);
    }

    @Override
    public void fillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color) {
        this.delegate.fillRoundedRect(this.snapX(x), this.snapY(y), this.snapWidth(x, width), this.snapHeight(y, height), Math.round(rtl), Math.round(rbl), Math.round(rbr), Math.round(rtr), color);
    }

    @Override
    public void outlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color) {
        this.delegate.outlineRoundedRect(this.snapX(x), this.snapY(y), this.snapWidth(x, width), this.snapHeight(y, height), Math.round(rtl), Math.round(rbl), Math.round(rbr), Math.round(rtr), Math.round(outlineWidth), color);
    }

    @Override
    public void fillPolygon(final Point[] points, final Color color) {
        Point[] snappedPoints = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            Point point = points[i];
            snappedPoints[i] = new Point(this.snapX(point.x()), this.snapY(point.y()));
        }
        this.delegate.fillPolygon(snappedPoints, color);
    }

    @Override
    public void line(final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        this.delegate.line(this.snapX(x1), this.snapY(y1), this.snapX(x2), this.snapY(y2), Math.round(width), color);
    }

    @Override
    public void polyLine(final Point[] points, final float width, final Color color) {
        Point[] snappedPoints = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            Point point = points[i];
            snappedPoints[i] = new Point(this.snapX(point.x()), this.snapY(point.y()));
        }
        this.delegate.polyLine(snappedPoints, Math.round(width), color);
    }

    @Override
    public void fillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr) {
        this.delegate.fillGradientRect(this.snapX(x), this.snapY(y), this.snapWidth(x, width), this.snapHeight(y, height), ctl, cbl, cbr, ctr);
    }

    @Override
    public void text(final ShapedText shapedText, final float x, final float y, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin) {
        this.delegate.text(shapedText, this.snapX(x), this.snapY(y), horizontalOrigin, verticalOrigin);
    }

    @Override
    public void image(final Texture texture, final float x, final float y, final float width, final float height, final Color color) {
        this.delegate.image(texture, this.snapX(x), this.snapY(y), this.snapWidth(x, width), this.snapHeight(y, height), color);
    }

    @Override
    public void custom(final RenderCommand.Custom renderCommand) {
        this.delegate.custom(renderCommand);
    }

}
