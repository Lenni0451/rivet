package net.lenni0451.rivet.backend.awt.render;

import lombok.RequiredArgsConstructor;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.awt.text.AWTShapedText;
import net.lenni0451.rivet.backend.awt.texture.AWTTexture;
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

@RequiredArgsConstructor
public class AWTRenderer extends CheckedRenderer {

    public static final float SHADOW_OFFSET_FACTOR = 0.075F;
    public static final float SHADOW_COLOR_MULTIPLIER = 0.25F;
    public static final float OUTLINE_WIDTH_FACTOR = 0.125F;

    private final Graphics2D graphics;

    @Override
    public void doTranslate(final float x, final float y, final Runnable renderer) {
        AffineTransform transform = new AffineTransform(this.graphics.getTransform());
        this.graphics.translate(x, y);
        renderer.run();
        this.graphics.setTransform(transform);
    }

    @Override
    public void doComponentBounds(final float x, final float y, final float width, final float height, final Runnable renderer) {
        this.scissor(x, y, width, height, renderer);
    }

    @Override
    public void doScissor(final float x, final float y, final float width, final float height, final Runnable renderer) {
        Rectangle2D bounds = new Rectangle2D.Float(x, y, width, height);
        this.clip(bounds, renderer);
    }

    @Override
    public void doScale(final float x, final float y, final Runnable renderer) {
        AffineTransform transform = new AffineTransform(this.graphics.getTransform());
        this.graphics.scale(x, y);
        renderer.run();
        this.graphics.setTransform(transform);
    }

    @Override
    public void doStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        // TODO
    }

    @Override
    public void doInverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        // TODO
    }

    @Override
    public void custom(final ModifierCommand.Custom command, final Runnable renderer) {
    }

    private void clip(final Shape shape, final Runnable renderer) {
        Shape clip = this.graphics.getClip();
        if (clip == null) {
            this.graphics.setClip(shape);
        } else {
            Area clipArea = new Area(clip);
            clipArea.intersect(new Area(shape));
            this.graphics.setClip(clipArea);
        }
        renderer.run();
        this.graphics.setClip(clip);
    }


    @Override
    public void doFillCircle(final float x, final float y, final float radius, final Color color) {
        Ellipse2D.Float circle = new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2);
        this.graphics.setColor(color.toAWT());
        this.graphics.fill(circle);
    }

    @Override
    public void doOutlineCircle(final float x, final float y, float radius, final float outlineWidth, final Color color) {
        radius -= outlineWidth / 2F;
        if (radius > 0) {
            Ellipse2D.Float circle = new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2);
            this.graphics.setStroke(new BasicStroke(outlineWidth));
            this.graphics.setColor(color.toAWT());
            this.graphics.draw(circle);
        }
    }

    @Override
    public void doFillTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3, final Color color) {
        Path2D triangle = new Path2D.Float();
        triangle.moveTo(x1, y1);
        triangle.lineTo(x2, y2);
        triangle.lineTo(x3, y3);
        triangle.closePath();
        this.graphics.setColor(color.toAWT());
        this.graphics.fill(triangle);
    }

    @Override
    public void doFillRect(final float x, final float y, final float width, final float height, final Color color) {
        Rectangle2D rectangle = new Rectangle2D.Float(x, y, width, height);
        this.graphics.setColor(color.toAWT());
        this.graphics.fill(rectangle);
    }

    @Override
    public void doOutlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color) {
        float halfOutline = outlineWidth / 2F;
        if (width >= outlineWidth && height >= outlineWidth) {
            Rectangle2D rectangle = new Rectangle2D.Float(x + halfOutline, y + halfOutline, width - outlineWidth, height - outlineWidth);
            this.graphics.setStroke(new BasicStroke(outlineWidth));
            this.graphics.setColor(color.toAWT());
            this.graphics.draw(rectangle);
        }
    }

    @Override
    public void doFillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color) {
        RoundRectangle2D rectangle = new RoundRectangle2D.Float(x, y, width, height, rtl + rtr, rbl + rbr); // TODO: Does this look good?
        this.graphics.setColor(color.toAWT());
        this.graphics.fill(rectangle);
    }

    @Override
    public void doOutlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color) {
        float halfOutline = outlineWidth / 2F;
        RoundRectangle2D rectangle = new RoundRectangle2D.Float(
                x + halfOutline,
                y + halfOutline,
                Math.max(0, width - outlineWidth),
                Math.max(0, height - outlineWidth),
                Math.max(0, rtl + rtr - outlineWidth),
                Math.max(0, rbl + rbr - outlineWidth)
        ); // TODO: Does this look good?
        this.graphics.setStroke(new BasicStroke(outlineWidth));
        this.graphics.setColor(color.toAWT());
        this.graphics.draw(rectangle);
    }

    @Override
    public void doFillPolygon(final Point[] points, final Color color) {
        Path2D polygon = new Path2D.Float();
        polygon.moveTo(points[0].x(), points[0].y());
        for (int i = 1; i < points.length; i++) {
            polygon.lineTo(points[i].x(), points[i].y());
        }
        polygon.closePath();
        this.graphics.setColor(color.toAWT());
        this.graphics.fill(polygon);
    }

    @Override
    public void doLine(final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        Line2D line = new Line2D.Float(x1, y1, x2, y2);
        this.graphics.setStroke(new BasicStroke(width));
        this.graphics.setColor(color.toAWT());
        this.graphics.draw(line);
    }

    @Override
    public void doPolyLine(final Point[] points, final float width, final Color color) {
        Path2D polyLine = new Path2D.Float();
        polyLine.moveTo(points[0].x(), points[0].y());
        for (int i = 1; i < points.length; i++) {
            polyLine.lineTo(points[i].x(), points[i].y());
        }
        this.graphics.setStroke(new BasicStroke(width));
        this.graphics.setColor(color.toAWT());
        this.graphics.draw(polyLine);
    }

    @Override
    public void doFillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr) {
        if (ctl.equals(ctr) && cbl.equals(cbr)) {
            this.graphics.setPaint(new GradientPaint(x, y, ctl.toAWT(), x, y + height, cbl.toAWT()));
            this.graphics.fill(new Rectangle2D.Float(x, y, width, height));
        } else if (ctl.equals(cbl) && ctr.equals(cbr)) {
            this.graphics.setPaint(new GradientPaint(x, y, ctl.toAWT(), x + width, y, ctr.toAWT()));
            this.graphics.fill(new Rectangle2D.Float(x, y, width, height));
        } else {
            this.graphics.setPaint(new GradientPaint(x, y, ctl.toAWT(), x + width, y, ctr.toAWT()));
            this.graphics.fill(new Rectangle2D.Float(x, y, width, height));
            this.graphics.setPaint(new GradientPaint(x, y, Color.TRANSPARENT.toAWT(), x, y + height, cbl.toAWT()));
            this.graphics.fill(new Rectangle2D.Float(x, y, width, height));
        }
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
        Stroke previousStroke = this.graphics.getStroke();
        java.awt.Color previousColor = this.graphics.getColor();

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
                    this.graphics.setColor(shadowOutline.toAWT());
                    this.graphics.setStroke(section.outlineStroke());
                    this.graphics.translate(shadowX, shadowY);
                    this.graphics.draw(outlineShape);
                    this.graphics.translate(-shadowX, -shadowY);
                }

                Color shadowColor = format.color().multiply(SHADOW_COLOR_MULTIPLIER);
                this.graphics.setColor(shadowColor.toAWT());
                this.graphics.drawGlyphVector(glyphVector, shadowX, shadowY);

                if (format.underlined()) {
                    float lineY = shadowY + lineMetrics.getUnderlineOffset();
                    float thickness = lineMetrics.getUnderlineThickness() * (format.bold() ? 1.5F : 1F);
                    this.graphics.fill(new Rectangle2D.Float(shadowX, lineY - thickness / 2, advance, thickness));
                }
                if (format.strikethrough()) {
                    float lineY = shadowY + lineMetrics.getStrikethroughOffset();
                    float thickness = lineMetrics.getStrikethroughThickness() * (format.bold() ? 1.5F : 1F);
                    this.graphics.fill(new Rectangle2D.Float(shadowX, lineY - thickness / 2, advance, thickness));
                }
            }

            if (format.outlineColor().getAlpha() > 0 && outlineShape != null && section.outlineStroke() != null) {
                this.graphics.setColor(format.outlineColor().toAWT());
                this.graphics.setStroke(section.outlineStroke());
                this.graphics.translate(sectionX, sectionY);
                this.graphics.draw(outlineShape);
                this.graphics.translate(-sectionX, -sectionY);
            }

            this.graphics.setColor(format.color().toAWT());
            this.graphics.drawGlyphVector(glyphVector, sectionX, sectionY);

            if (format.underlined()) {
                float lineY = sectionY + lineMetrics.getUnderlineOffset();
                float thickness = lineMetrics.getUnderlineThickness() * (format.bold() ? 1.5F : 1F);
                this.graphics.fill(new Rectangle2D.Float(sectionX, lineY - thickness / 2, advance, thickness));
            }
            if (format.strikethrough()) {
                float lineY = sectionY + lineMetrics.getStrikethroughOffset();
                float thickness = lineMetrics.getStrikethroughThickness() * (format.bold() ? 1.5F : 1F);
                this.graphics.fill(new Rectangle2D.Float(sectionX, lineY - thickness / 2, advance, thickness));
            }
        }

        this.graphics.setStroke(previousStroke);
        this.graphics.setColor(previousColor);
    }

    @Override
    public void doImage(final Texture texture, final float x, final float y, final float width, final float height, final Color color) {
        AWTTexture awtTexture = (AWTTexture) texture; // TODO: color
        float scaleX = width / awtTexture.width();
        float scaleY = height / awtTexture.height();

        AffineTransform transform = new AffineTransform();
        transform.translate(x, y);
        transform.scale(scaleX, scaleY);
        this.graphics.drawImage(awtTexture.image(), transform, null);
    }

    @Override
    public void custom(final RenderCommand.Custom renderCommand) {
    }

}
