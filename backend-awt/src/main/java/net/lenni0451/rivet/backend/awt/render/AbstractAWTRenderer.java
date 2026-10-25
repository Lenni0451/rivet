package net.lenni0451.rivet.backend.awt.render;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.awt.shape.RoundedRect;
import net.lenni0451.rivet.backend.awt.text.AWTShapedText;
import net.lenni0451.rivet.backend.render.CheckedRenderer;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextOrigin;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public abstract class AbstractAWTRenderer extends CheckedRenderer {

    public static final float SHADOW_OFFSET_FACTOR = 0.075F;
    public static final float SHADOW_COLOR_MULTIPLIER = 0.25F;
    public static final float OUTLINE_WIDTH_FACTOR = 0.125F;

    @Override
    public void doComponentBounds(final float x, final float y, final float width, final float height, final Runnable renderer) {
        this.scissor(x, y, width, height, renderer);
    }

    @Override
    public void doFillCircle(final float x, final float y, final float radius, final Color color) {
        Ellipse2D.Float circle = new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2);
        this.fillShape(circle, color);
    }

    @Override
    public void doOutlineCircle(final float x, final float y, float radius, final float outlineWidth, final Color color) {
        radius -= outlineWidth / 2F;
        if (radius > 0) {
            Ellipse2D.Float circle = new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2);
            this.strokeShape(circle, new BasicStroke(outlineWidth), color);
        }
    }

    @Override
    public void doFillTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3, final Color color) {
        Path2D triangle = new Path2D.Float();
        triangle.moveTo(x1, y1);
        triangle.lineTo(x2, y2);
        triangle.lineTo(x3, y3);
        triangle.closePath();
        this.fillShape(triangle, color);
    }

    @Override
    public void doFillRect(final float x, final float y, final float width, final float height, final Color color) {
        Rectangle2D rectangle = new Rectangle2D.Float(x, y, width, height);
        this.fillShape(rectangle, color);
    }

    @Override
    public void doOutlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color) {
        float halfOutline = outlineWidth / 2F;
        if (width >= outlineWidth && height >= outlineWidth) {
            Rectangle2D rectangle = new Rectangle2D.Float(x + halfOutline, y + halfOutline, width - outlineWidth, height - outlineWidth);
            this.strokeShape(rectangle, new BasicStroke(outlineWidth), color);
        }
    }

    @Override
    public void doFillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color) {
        RoundedRect rectangle = new RoundedRect(x, y, width, height, rtl, rbl, rbr, rtr);
        this.fillShape(rectangle, color);
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
        this.fillShape(rectangle, color);
    }

    @Override
    public void doFillPolygon(final Point[] points, final Color color) {
        Path2D polygon = new Path2D.Float();
        polygon.moveTo(points[0].x(), points[0].y());
        for (int i = 1; i < points.length; i++) {
            polygon.lineTo(points[i].x(), points[i].y());
        }
        polygon.closePath();
        this.fillShape(polygon, color);
    }

    @Override
    public void doLine(final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        Line2D line = new Line2D.Float(x1, y1, x2, y2);
        this.strokeShape(line, new BasicStroke(width), color);
    }

    @Override
    public void doPolyLine(final Point[] points, final float width, final Color color) {
        Path2D polyLine = new Path2D.Float();
        polyLine.moveTo(points[0].x(), points[0].y());
        for (int i = 1; i < points.length; i++) {
            polyLine.lineTo(points[i].x(), points[i].y());
        }
        this.strokeShape(polyLine, new BasicStroke(width), color);
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

    protected void renderShapedText(final AWTShapedText shapedText, final float originX, final float originY) {
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
                float shadowOffset = SHADOW_OFFSET_FACTOR * section.font().getSize();
                float shadowX = sectionX + shadowOffset;
                float shadowY = sectionY + shadowOffset;

                if (format.outlineColor().getAlpha() > 0 && outlineShape != null && section.outlineStroke() != null) {
                    Color shadowOutline = format.outlineColor().multiply(SHADOW_COLOR_MULTIPLIER);
                    this.strokeTextOutline(outlineShape, section.outlineStroke(), shadowX, shadowY, shadowOutline);
                }

                Color shadowColor = format.color().multiply(SHADOW_COLOR_MULTIPLIER);
                this.drawGlyphs(glyphVector, shadowX, shadowY, shadowColor);

                if (format.underlined()) {
                    float lineY = shadowY + lineMetrics.getUnderlineOffset();
                    float thickness = lineMetrics.getUnderlineThickness() * (format.bold() ? 1.5F : 1F);
                    this.fillShape(new Rectangle2D.Float(shadowX, lineY - thickness / 2, advance, thickness), shadowColor);
                }
                if (format.strikethrough()) {
                    float lineY = shadowY + lineMetrics.getStrikethroughOffset();
                    float thickness = lineMetrics.getStrikethroughThickness() * (format.bold() ? 1.5F : 1F);
                    this.fillShape(new Rectangle2D.Float(shadowX, lineY - thickness / 2, advance, thickness), shadowColor);
                }
            }

            if (format.outlineColor().getAlpha() > 0 && outlineShape != null && section.outlineStroke() != null) {
                this.strokeTextOutline(outlineShape, section.outlineStroke(), sectionX, sectionY, format.outlineColor());
            }

            this.drawGlyphs(glyphVector, sectionX, sectionY, format.color());

            if (format.underlined()) {
                float lineY = sectionY + lineMetrics.getUnderlineOffset();
                float thickness = lineMetrics.getUnderlineThickness() * (format.bold() ? 1.5F : 1F);
                this.fillShape(new Rectangle2D.Float(sectionX, lineY - thickness / 2, advance, thickness), format.color());
            }
            if (format.strikethrough()) {
                float lineY = sectionY + lineMetrics.getStrikethroughOffset();
                float thickness = lineMetrics.getStrikethroughThickness() * (format.bold() ? 1.5F : 1F);
                this.fillShape(new Rectangle2D.Float(sectionX, lineY - thickness / 2, advance, thickness), format.color());
            }
        }
    }

    protected abstract void fillShape(final Shape shape, final Color color);

    protected abstract void strokeShape(final Shape shape, final Stroke stroke, final Color color);

    protected abstract void drawGlyphs(final GlyphVector glyphVector, final float x, final float y, final Color color);

    protected abstract void strokeTextOutline(final Shape outlineShape, final Stroke stroke, final float x, final float y, final Color color);

}
