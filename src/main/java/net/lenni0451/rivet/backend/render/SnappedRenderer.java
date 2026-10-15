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
    private float m00 = 1;
    private float m01 = 0;
    private float m10 = 0;
    private float m11 = 1;
    private boolean snap = true;

    public SnappedRenderer(final R delegate, final Component baseComponent) {
        this.delegate = delegate;

        Rectangle absoluteBounds = baseComponent.absoluteBounds();
        this.xOffset = absoluteBounds.x();
        this.yOffset = absoluteBounds.y();
    }

    private float snapX(final float x) {
        if (!this.snap) return x;
        float offset = this.m00 != 0 ? this.xOffset : this.yOffset;
        float scale = this.m00 != 0 ? this.m00 : this.m10;
        if (scale == 0) return x;
        return (Math.round(offset + x * scale) - offset) / scale;
    }

    private float snapY(final float y) {
        if (!this.snap) return y;
        float offset = this.m11 != 0 ? this.yOffset : this.xOffset;
        float scale = this.m11 != 0 ? this.m11 : this.m01;
        if (scale == 0) return y;
        return (Math.round(offset + y * scale) - offset) / scale;
    }

    private float snapWidth(final float x, final float width) {
        if (!this.snap) return width;
        float offset = this.m00 != 0 ? this.xOffset : this.yOffset;
        float scale = this.m00 != 0 ? this.m00 : this.m10;
        if (scale == 0) return width;
        return Math.abs(Math.round(offset + (x + width) * scale) - Math.round(offset + x * scale)) / Math.abs(scale);
    }

    private float snapHeight(final float y, final float height) {
        if (!this.snap) return height;
        float offset = this.m11 != 0 ? this.yOffset : this.xOffset;
        float scale = this.m11 != 0 ? this.m11 : this.m01;
        if (scale == 0) return height;
        return Math.abs(Math.round(offset + (y + height) * scale) - Math.round(offset + y * scale)) / Math.abs(scale);
    }

    private float snapRadius(final float radius) {
        return this.snap ? Math.round(radius) : radius;
    }


    @Override
    public void translate(final float x, final float y, final Runnable renderer) {
        float previousXOffset = this.xOffset;
        float previousYOffset = this.yOffset;
        this.xOffset += this.m00 * x + this.m01 * y;
        this.yOffset += this.m10 * x + this.m11 * y;
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
        float previousM00 = this.m00;
        float previousM01 = this.m01;
        float previousM10 = this.m10;
        float previousM11 = this.m11;
        this.m00 *= x;
        this.m10 *= x;
        this.m01 *= y;
        this.m11 *= y;
        this.delegate.scale(x, y, renderer);
        this.m00 = previousM00;
        this.m01 = previousM01;
        this.m10 = previousM10;
        this.m11 = previousM11;
    }

    @Override
    public void rotate(final float degrees, final Runnable renderer) {
        boolean previousSnap = this.snap;
        float previousM00 = this.m00;
        float previousM01 = this.m01;
        float previousM10 = this.m10;
        float previousM11 = this.m11;

        if (this.snap && degrees % 90F == 0) {
            int k = Math.floorMod(Math.round(degrees / 90F), 4);
            int cos = switch (k) {
                case 0 -> 1;
                case 1 -> 0;
                case 2 -> -1;
                case 3 -> 0;
                default -> 1;
            };
            int sin = switch (k) {
                case 0 -> 0;
                case 1 -> 1;
                case 2 -> 0;
                case 3 -> -1;
                default -> 0;
            };
            this.m00 = previousM00 * cos + previousM01 * sin;
            this.m01 = -previousM00 * sin + previousM01 * cos;
            this.m10 = previousM10 * cos + previousM11 * sin;
            this.m11 = -previousM10 * sin + previousM11 * cos;
        } else {
            this.snap = false;
        }

        this.delegate.rotate(degrees, renderer);

        this.snap = previousSnap;
        this.m00 = previousM00;
        this.m01 = previousM01;
        this.m10 = previousM10;
        this.m11 = previousM11;
    }

    @Override
    public void stencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        this.delegate.stencil(mr -> maskRenderer.accept(new SnappedRenderer<>(mr, this.xOffset, this.yOffset, this.m00, this.m01, this.m10, this.m11, this.snap)), renderer);
    }

    @Override
    public void inverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        this.delegate.inverseStencil(mr -> maskRenderer.accept(new SnappedRenderer<>(mr, this.xOffset, this.yOffset, this.m00, this.m01, this.m10, this.m11, this.snap)), renderer);
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
