package net.lenni0451.rivet.backend.awt.render;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.awt.shape.RoundedRect;
import net.lenni0451.rivet.backend.awt.text.AWTShapedText;
import net.lenni0451.rivet.backend.render.CheckedRenderer;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextOrigin;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.*;
import java.util.function.Consumer;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = true, makeFinal = true)
public class ShapeRenderer extends CheckedRenderer {

    @Getter
    private Area shape;
    @Getter
    private AffineTransform transform;

    public ShapeRenderer() {
        this(new Area(), new AffineTransform());
    }

    public ShapeRenderer(final AffineTransform transform) {
        this(new Area(), transform);
    }

    public ShapeRenderer add(final Shape shape) {
        Shape transformed = this.transform.isIdentity() ? shape : this.transform.createTransformedShape(shape);
        this.shape.add(transformed instanceof Area area ? area : new Area(transformed));
        return this;
    }

    @Override
    public void doTranslate(final float x, final float y, final Runnable renderer) {
        AffineTransform previous = new AffineTransform(this.transform);
        this.transform.translate(x, y);
        renderer.run();
        this.transform = previous;
    }

    @Override
    public void doComponentBounds(final float x, final float y, final float width, final float height, final Runnable renderer) {
        this.scissor(x, y, width, height, renderer);
    }

    @Override
    public void doScissor(final float x, final float y, final float width, final float height, final Runnable renderer) {
        Shape bounds = new Rectangle2D.Float(x, y, width, height);
        Shape transformedBounds = this.transform.isIdentity() ? bounds : this.transform.createTransformedShape(bounds);
        Area boundsArea = transformedBounds instanceof Area area ? area : new Area(transformedBounds);

        Area previousShape = this.shape;
        this.shape = new Area();
        renderer.run();
        this.shape.intersect(boundsArea);
        previousShape.add(this.shape);
        this.shape = previousShape;
    }

    @Override
    public void doScale(final float x, final float y, final Runnable renderer) {
        AffineTransform previous = new AffineTransform(this.transform);
        this.transform.scale(x, y);
        renderer.run();
        this.transform = previous;
    }

    @Override
    public void doStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        ShapeRenderer maskShapeRenderer = new ShapeRenderer(new AffineTransform(this.transform));
        maskRenderer.accept(maskShapeRenderer);
        Area maskArea = maskShapeRenderer.shape();

        Area previousShape = this.shape;
        this.shape = new Area();
        renderer.run();
        this.shape.intersect(maskArea);
        previousShape.add(this.shape);
        this.shape = previousShape;
    }

    @Override
    public void doInverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        ShapeRenderer maskShapeRenderer = new ShapeRenderer(new AffineTransform(this.transform));
        maskRenderer.accept(maskShapeRenderer);
        Area maskArea = maskShapeRenderer.shape();

        Area previousShape = this.shape;
        this.shape = new Area();
        renderer.run();
        this.shape.subtract(maskArea);
        previousShape.add(this.shape);
        this.shape = previousShape;
    }

    @Override
    public void custom(final ModifierCommand.Custom command, final Runnable renderer) {
        throw new UnsupportedOperationException("Custom modifier commands are not supported in ShapeRenderer");
    }


    @Override
    public void doFillCircle(final float x, final float y, final float radius, final Color color) {
        Ellipse2D.Float circle = new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2);
        this.add(circle);
    }

    @Override
    public void doOutlineCircle(final float x, final float y, float radius, final float outlineWidth, final Color color) {
        radius -= outlineWidth / 2F;
        if (radius > 0) {
            Ellipse2D.Float circle = new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2);
            this.add(new BasicStroke(outlineWidth).createStrokedShape(circle));
        }
    }

    @Override
    public void doFillTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3, final Color color) {
        Path2D triangle = new Path2D.Float();
        triangle.moveTo(x1, y1);
        triangle.lineTo(x2, y2);
        triangle.lineTo(x3, y3);
        triangle.closePath();
        this.add(triangle);
    }

    @Override
    public void doFillRect(final float x, final float y, final float width, final float height, final Color color) {
        Rectangle2D rectangle = new Rectangle2D.Float(x, y, width, height);
        this.add(rectangle);
    }

    @Override
    public void doOutlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color) {
        float halfOutline = outlineWidth / 2F;
        if (width >= outlineWidth && height >= outlineWidth) {
            Rectangle2D rectangle = new Rectangle2D.Float(x + halfOutline, y + halfOutline, width - outlineWidth, height - outlineWidth);
            this.add(new BasicStroke(outlineWidth).createStrokedShape(rectangle));
        }
    }

    @Override
    public void doFillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color) {
        RoundedRect rectangle = new RoundedRect(x, y, width, height, rtl, rbl, rbr, rtr);
        this.add(rectangle);
    }

    @Override
    public void doOutlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color) {
        RoundedRect rectangle = new RoundedRect(Path2D.WIND_EVEN_ODD, x, y, width, height, rtl, rbl, rbr, rtr);
        if (width > outlineWidth * 2 && height > outlineWidth * 2) {
            rectangle.add(
                    x + outlineWidth,
                    y + outlineWidth,
                    width - outlineWidth * 2,
                    height - outlineWidth * 2,
                    Math.max(0, rtl - outlineWidth),
                    Math.max(0, rbl - outlineWidth),
                    Math.max(0, rbr - outlineWidth),
                    Math.max(0, rtr - outlineWidth)
            );
        }
        this.add(rectangle);
    }

    @Override
    public void doFillPolygon(final Point[] points, final Color color) {
        Path2D polygon = new Path2D.Float();
        polygon.moveTo(points[0].x(), points[0].y());
        for (int i = 1; i < points.length; i++) {
            polygon.lineTo(points[i].x(), points[i].y());
        }
        polygon.closePath();
        this.add(polygon);
    }

    @Override
    public void doLine(final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        Line2D line = new Line2D.Float(x1, y1, x2, y2);
        this.add(new BasicStroke(width).createStrokedShape(line));
    }

    @Override
    public void doPolyLine(final Point[] points, final float width, final Color color) {
        Path2D polyLine = new Path2D.Float();
        polyLine.moveTo(points[0].x(), points[0].y());
        for (int i = 1; i < points.length; i++) {
            polyLine.lineTo(points[i].x(), points[i].y());
        }
        this.add(new BasicStroke(width).createStrokedShape(polyLine));
    }

    @Override
    public void doFillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr) {
        Rectangle2D rectangle = new Rectangle2D.Float(x, y, width, height);
        this.add(rectangle);
    }

    @Override
    public void doText(final ShapedText shapedText, final float anchorX, final float anchorY, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin) {
        float x = shapedText.alignAnchorTo(anchorX, horizontalOrigin, TextOrigin.Horizontal.LOGICAL_LEFT);
        float y = shapedText.alignAnchorTo(anchorY, verticalOrigin, TextOrigin.Vertical.BASELINE);
        if (shapedText instanceof AWTShapedText awtShapedText) {
            this.renderShapedText(awtShapedText, x, y);
        } else {
            throw new UnsupportedOperationException(shapedText.getClass().getName());
        }
    }

    private void renderShapedText(final AWTShapedText shapedText, final float originX, final float originY) {
        for (AWTShapedText.Section section : shapedText.sections()) {
            if (section.glyphVector().getNumGlyphs() == 0) continue;

            float sectionX = originX + section.xOffset();
            float sectionY = originY;
            TextFormat format = section.format();
            GlyphVector glyphVector = section.glyphVector();
            Shape outlineShape = section.outlineShape();
            LineMetrics lineMetrics = section.lineMetrics();
            float advance = section.advanceWidth();

            if (format.shadow()) {
                float shadowOffset = AWTRenderer.SHADOW_OFFSET_FACTOR * section.font().getSize();
                float shadowX = sectionX + shadowOffset;
                float shadowY = sectionY + shadowOffset;

                if (format.outlineColor().getAlpha() > 0 && outlineShape != null && section.outlineStroke() != null) {
                    AffineTransform outlineTransform = AffineTransform.getTranslateInstance(shadowX, shadowY);
                    this.add(outlineTransform.createTransformedShape(section.outlineStroke().createStrokedShape(outlineShape)));
                }

                this.add(glyphVector.getOutline(shadowX, shadowY));

                if (format.underlined()) {
                    float lineY = shadowY + lineMetrics.getUnderlineOffset();
                    float thickness = lineMetrics.getUnderlineThickness() * (format.bold() ? 1.5F : 1F);
                    this.add(new Rectangle2D.Float(shadowX, lineY - thickness / 2, advance, thickness));
                }
                if (format.strikethrough()) {
                    float lineY = shadowY + lineMetrics.getStrikethroughOffset();
                    float thickness = lineMetrics.getStrikethroughThickness() * (format.bold() ? 1.5F : 1F);
                    this.add(new Rectangle2D.Float(shadowX, lineY - thickness / 2, advance, thickness));
                }
            }

            if (format.outlineColor().getAlpha() > 0 && outlineShape != null && section.outlineStroke() != null) {
                AffineTransform outlineTransform = AffineTransform.getTranslateInstance(sectionX, sectionY);
                this.add(outlineTransform.createTransformedShape(section.outlineStroke().createStrokedShape(outlineShape)));
            }

            this.add(glyphVector.getOutline(sectionX, sectionY));

            if (format.underlined()) {
                float lineY = sectionY + lineMetrics.getUnderlineOffset();
                float thickness = lineMetrics.getUnderlineThickness() * (format.bold() ? 1.5F : 1F);
                this.add(new Rectangle2D.Float(sectionX, lineY - thickness / 2, advance, thickness));
            }
            if (format.strikethrough()) {
                float lineY = sectionY + lineMetrics.getStrikethroughOffset();
                float thickness = lineMetrics.getStrikethroughThickness() * (format.bold() ? 1.5F : 1F);
                this.add(new Rectangle2D.Float(sectionX, lineY - thickness / 2, advance, thickness));
            }
        }
    }

    @Override
    public void doImage(final Texture texture, final float x, final float y, final float width, final float height, final Color color) {
        this.add(new Rectangle2D.Float(x, y, width, height));
    }

    @Override
    public void custom(final RenderCommand.Custom renderCommand) {
        throw new UnsupportedOperationException("Custom render commands are not supported in ShapeRenderer");
    }

}
