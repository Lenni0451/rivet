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
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Transform2D;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.utils.MathUtils;

import java.util.function.Consumer;

@RequiredArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = true, makeFinal = true)
public class SnappedRenderer<R extends Renderer> implements Renderer {

    @Getter
    private final R delegate;
    private Transform2D transform = Transform2D.IDENTITY;
    private boolean snap = true;

    public SnappedRenderer(final R delegate, final Component baseComponent) {
        this.delegate = delegate;

        Rectangle absoluteBounds = baseComponent.absoluteBounds();
        this.transform = Transform2D.IDENTITY.translate(absoluteBounds.x(), absoluteBounds.y());
    }

    private float snapX(final float x) {
        if (!this.snap) return x;
        Transform2D transform = this.transform;
        float offset = transform.m00() != 0 ? transform.m02() : transform.m12();
        float scale = transform.m00() != 0 ? transform.m00() : transform.m10();
        if (scale == 0) return x;
        return (Math.round(offset + x * scale) - offset) / scale;
    }

    private float snapY(final float y) {
        if (!this.snap) return y;
        Transform2D transform = this.transform;
        float offset = transform.m11() != 0 ? transform.m12() : transform.m02();
        float scale = transform.m11() != 0 ? transform.m11() : transform.m01();
        if (scale == 0) return y;
        return (Math.round(offset + y * scale) - offset) / scale;
    }

    private float snapWidth(final float x, final float width) {
        if (!this.snap) return width;
        Transform2D transform = this.transform;
        float offset = transform.m00() != 0 ? transform.m02() : transform.m12();
        float scale = transform.m00() != 0 ? transform.m00() : transform.m10();
        if (scale == 0) return width;
        return Math.abs(Math.round(offset + (x + width) * scale) - Math.round(offset + x * scale)) / Math.abs(scale);
    }

    private float snapHeight(final float y, final float height) {
        if (!this.snap) return height;
        Transform2D transform = this.transform;
        float offset = transform.m11() != 0 ? transform.m12() : transform.m02();
        float scale = transform.m11() != 0 ? transform.m11() : transform.m01();
        if (scale == 0) return height;
        return Math.abs(Math.round(offset + (y + height) * scale) - Math.round(offset + y * scale)) / Math.abs(scale);
    }

    private float snapRadius(final float radius) {
        return this.snap ? Math.round(radius) : radius;
    }


    @Override
    public void translate(final float x, final float y, final Runnable renderer) {
        Transform2D previousTransform = this.transform;
        this.transform = previousTransform.translate(x, y);
        this.delegate.translate(x, y, renderer);
        this.transform = previousTransform;
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
        Transform2D previousTransform = this.transform;
        this.transform = previousTransform.scale(x, y);
        this.delegate.scale(x, y, renderer);
        this.transform = previousTransform;
    }

    @Override
    public void rotate(final float degrees, final Runnable renderer) {
        Transform2D previousTransform = this.transform;
        boolean previousSnap = this.snap;
        if (this.snap && degrees % 90F == 0) {
            this.transform = previousTransform.rotateQuarterTurns(Math.round(degrees / 90F));
        } else {
            this.snap = false;
        }
        this.delegate.rotate(degrees, renderer);
        this.transform = previousTransform;
        this.snap = previousSnap;
    }

    @Override
    public void stencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        this.delegate.stencil(mr -> maskRenderer.accept(new SnappedRenderer<>(mr, this.transform, this.snap)), renderer);
    }

    @Override
    public void inverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        this.delegate.inverseStencil(mr -> maskRenderer.accept(new SnappedRenderer<>(mr, this.transform, this.snap)), renderer);
    }

    @Override
    public void custom(final ModifierCommand.Custom command, final Runnable renderer) {
        this.delegate.custom(command, renderer);
    }


    @Override
    public void fillCircle(final float x, final float y, final float radius, final Color color) {
        this.delegate.fillCircle(this.snapX(x), this.snapY(y), this.snapRadius(radius), color);
    }

    @Override
    public void outlineCircle(final float x, final float y, final float radius, final float outlineWidth, final Color color) {
        this.delegate.outlineCircle(this.snapX(x), this.snapY(y), this.snapRadius(radius), this.snapRadius(outlineWidth), color);
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
        this.delegate.outlineRect(this.snapX(x), this.snapY(y), this.snapWidth(x, width), this.snapHeight(y, height), this.snapRadius(outlineWidth), color);
    }

    @Override
    public void fillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color) {
        float snappedWidth = this.snapWidth(x, width);
        float snappedHeight = this.snapHeight(y, height);
        Corners corners = MathUtils.clampCorners(snappedWidth, snappedHeight, this.snapRadius(rtl), this.snapRadius(rbl), this.snapRadius(rbr), this.snapRadius(rtr));
        this.delegate.fillRoundedRect(this.snapX(x), this.snapY(y), snappedWidth, snappedHeight, corners.topLeft(), corners.bottomLeft(), corners.bottomRight(), corners.topRight(), color);
    }

    @Override
    public void outlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color) {
        float snappedWidth = this.snapWidth(x, width);
        float snappedHeight = this.snapHeight(y, height);
        Corners corners = MathUtils.clampCorners(snappedWidth, snappedHeight, this.snapRadius(rtl), this.snapRadius(rbl), this.snapRadius(rbr), this.snapRadius(rtr));
        this.delegate.outlineRoundedRect(this.snapX(x), this.snapY(y), snappedWidth, snappedHeight, corners.topLeft(), corners.bottomLeft(), corners.bottomRight(), corners.topRight(), this.snapRadius(outlineWidth), color);
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
        this.delegate.line(this.snapX(x1), this.snapY(y1), this.snapX(x2), this.snapY(y2), this.snapRadius(width), color);
    }

    @Override
    public void polyLine(final Point[] points, final float width, final Color color) {
        Point[] snappedPoints = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            Point point = points[i];
            snappedPoints[i] = new Point(this.snapX(point.x()), this.snapY(point.y()));
        }
        this.delegate.polyLine(snappedPoints, this.snapRadius(width), color);
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
