package net.lenni0451.rivet.backend.render;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.utils.MathUtils;

import java.util.function.Consumer;

@RequiredArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = true, makeFinal = true)
public class SnappedRenderer<R extends Renderer> implements Renderer {

    @Getter
    private final R delegate;
    private float xOffset = 0;
    private float yOffset = 0;
    private float xScale = 1;
    private float yScale = 1;

    private float snapX(final float x) {
        if (this.xScale == 0) return x;
        return (Math.round(this.xOffset + x * this.xScale) - this.xOffset) / this.xScale;
    }

    private float snapY(final float y) {
        if (this.yScale == 0) return y;
        return (Math.round(this.yOffset + y * this.yScale) - this.yOffset) / this.yScale;
    }

    private float snapWidth(final float x, final float width) {
        if (this.xScale == 0) return width;
        return (Math.round(this.xOffset + (x + width) * this.xScale) - Math.round(this.xOffset + x * this.xScale)) / this.xScale;
    }

    private float snapHeight(final float y, final float height) {
        if (this.yScale == 0) return height;
        return (Math.round(this.yOffset + (y + height) * this.yScale) - Math.round(this.yOffset + y * this.yScale)) / this.yScale;
    }


    @Override
    public void translate(final float x, final float y, final Runnable renderer) {
        float previousXOffset = this.xOffset;
        float previousYOffset = this.yOffset;
        this.xOffset += x * this.xScale;
        this.yOffset += y * this.yScale;
        this.delegate.translate(x, y, renderer);
        this.xOffset = previousXOffset;
        this.yOffset = previousYOffset;
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
        float previousXScale = this.xScale;
        float previousYScale = this.yScale;
        this.xScale *= x;
        this.yScale *= y;
        this.delegate.scale(x, y, renderer);
        this.xScale = previousXScale;
        this.yScale = previousYScale;
    }

    @Override
    public void stencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        this.delegate.stencil(mr -> maskRenderer.accept(new SnappedRenderer<>(mr, this.xOffset, this.yOffset, this.xScale, this.yScale)), renderer);
    }

    @Override
    public void inverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        this.delegate.inverseStencil(mr -> maskRenderer.accept(new SnappedRenderer<>(mr, this.xOffset, this.yOffset, this.xScale, this.yScale)), renderer);
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
        float snappedWidth = this.snapWidth(x, width);
        float snappedHeight = this.snapHeight(y, height);
        Corners corners = MathUtils.clampCorners(snappedWidth, snappedHeight, Math.round(rtl), Math.round(rbl), Math.round(rbr), Math.round(rtr));
        this.delegate.fillRoundedRect(this.snapX(x), this.snapY(y), snappedWidth, snappedHeight, corners.topLeft(), corners.bottomLeft(), corners.bottomRight(), corners.topRight(), color);
    }

    @Override
    public void outlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color) {
        float snappedWidth = this.snapWidth(x, width);
        float snappedHeight = this.snapHeight(y, height);
        Corners corners = MathUtils.clampCorners(snappedWidth, snappedHeight, Math.round(rtl), Math.round(rbl), Math.round(rbr), Math.round(rtr));
        this.delegate.outlineRoundedRect(this.snapX(x), this.snapY(y), snappedWidth, snappedHeight, corners.topLeft(), corners.bottomLeft(), corners.bottomRight(), corners.topRight(), Math.round(outlineWidth), color);
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
    public void text(final ShapedText shapedText, final float anchorX, final float anchorY, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin) {
        this.delegate.text(shapedText, this.snapX(anchorX), this.snapY(anchorY), horizontalOrigin, verticalOrigin);
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
