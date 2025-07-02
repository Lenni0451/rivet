package net.lenni0451.rivet.backend.awt;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.text.ShapedTextBuffer;
import net.lenni0451.rivet.text.TextBuffer;
import net.lenni0451.rivet.text.TextRun;
import net.lenni0451.rivet.text.TextSegment;
import org.joml.Matrix4f;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class Graphics2DRenderer implements Renderer {

    public static final float SHADOW_OFFSET_FACTOR = 0.075F;
    public static final float SHADOW_COLOR_MULTIPLIER = 0.25F;
    public static final float OUTLINE_WIDTH_FACTOR = 0.125F;

    private final Graphics2D g2d;

    public Graphics2DRenderer(final Graphics2D g2d) {
        this.g2d = g2d;
        this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        this.g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        this.g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        this.g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        this.g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }

    @Override
    public void filledRectangle(Matrix4f positionMatrix, float x, float y, float width, float height, Color color) {
        this.applyColor(color);
        this.applyTransform(positionMatrix);
        this.g2d.fill(new Rectangle2D.Float(x, y, width, height));
    }

    @Override
    public void outlinedRectangle(Matrix4f positionMatrix, float x, float y, float width, float height, Color color, float lineWidth) {
        this.applyTransform(positionMatrix);
        this.applyColor(color);
        Stroke previousStroke = this.g2d.getStroke();
        this.g2d.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        float halfLineWidth = lineWidth / 2;
        this.g2d.draw(new Rectangle2D.Float(x - halfLineWidth, y - halfLineWidth, width + lineWidth, height + lineWidth));
        this.g2d.setStroke(previousStroke);
    }

    @Override
    public void filledRoundedRectangle(Matrix4f positionMatrix, float x, float y, float width, float height, float radius, Color color) {
        this.applyTransform(positionMatrix);
        this.applyColor(color);
        this.g2d.fill(new RoundRectangle2D.Float(x, y, width, height, radius, radius));
    }

    @Override
    public void outlinedRoundedRectangle(Matrix4f positionMatrix, float x, float y, float width, float height, float radius, Color color, float lineWidth) {
        this.applyTransform(positionMatrix);
        this.applyColor(color);
        Stroke previousStroke = this.g2d.getStroke();
        this.g2d.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        float halfLineWidth = lineWidth / 2;
        //TODO: Rounded rect corners don't work correctly
        this.g2d.draw(new RoundRectangle2D.Float(x - halfLineWidth, y - halfLineWidth, width + lineWidth, height + lineWidth, radius, radius));
        this.g2d.setStroke(previousStroke);
    }

    @Override
    public void text(Matrix4f positionMatrix, ShapedTextBuffer shapedTextBuffer, float x, float y) {
        this.applyTransform(positionMatrix);
        TextBuffer textBuffer = ((AWTShapedTextBuffer) shapedTextBuffer).textBuffer();
        FontRenderContext fontRenderContext = this.g2d.getFontRenderContext();
        float currentX = x - shapedTextBuffer.bounds().minX;
        float currentY = y - shapedTextBuffer.bounds().minY;
        for (TextRun run : textBuffer.runs()) {
            currentX += run.xOffset();
            currentY += run.yOffset();
            for (TextSegment segment : run.segments()) {
                int fontStyle = Font.PLAIN;
                if ((segment.styleFlags() & TextSegment.STYLE_ITALIC_BIT) != 0) fontStyle |= Font.ITALIC;
                if ((segment.styleFlags() & TextSegment.STYLE_BOLD_BIT) != 0) fontStyle |= Font.BOLD;
                Font font = ((AWTFont) run.font()).font().deriveFont(fontStyle);
                this.g2d.setFont(font);
                FontMetrics metrics = this.g2d.getFontMetrics(font);

                Rectangle2D logicalBounds = font.createGlyphVector(fontRenderContext, segment.text()).getLogicalBounds();
                float segmentX = currentX + segment.xVisualOffset();
                float segmentY = currentY + segment.yVisualOffset();
                TextLayout textLayout = new TextLayout(segment.text(), font, fontRenderContext);
                Shape outline = textLayout.getOutline(null);
                if ((segment.styleFlags() & TextSegment.STYLE_SHADOW_BIT) != 0) {
                    float shadowOffset = SHADOW_OFFSET_FACTOR * run.font().getSize();
                    this.applyColor(segment.color().multiply(SHADOW_COLOR_MULTIPLIER));
                    if (segment.outlineColor().getAlpha() > 0) {
                        Stroke previousStroke = this.g2d.getStroke();
                        this.g2d.setStroke(new BasicStroke(font.getSize() * OUTLINE_WIDTH_FACTOR, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        this.g2d.translate(segmentX + shadowOffset, segmentY + shadowOffset);
                        this.g2d.draw(outline);
                        this.g2d.translate(-(segmentX + shadowOffset), -(segmentY + shadowOffset));
                        this.g2d.setStroke(previousStroke);
                    }
                    this.g2d.drawString(segment.text(), segmentX + shadowOffset, segmentY + shadowOffset);
                    this.renderTextDecorations(segment, segmentX + shadowOffset, segmentY + shadowOffset, metrics, logicalBounds);
                }
                if (segment.outlineColor().getAlpha() > 0) {
                    this.applyColor(segment.outlineColor());
                    Stroke previousStroke = this.g2d.getStroke();
                    this.g2d.setStroke(new BasicStroke(font.getSize() * OUTLINE_WIDTH_FACTOR, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    this.g2d.translate(segmentX, segmentY);
                    this.g2d.draw(outline);
                    this.g2d.translate(-segmentX, -segmentY);
                    this.g2d.setStroke(previousStroke);
                }
                this.applyColor(segment.color());
                this.g2d.drawString(segment.text(), segmentX, segmentY);
                this.renderTextDecorations(segment, segmentX, segmentY, metrics, logicalBounds);
                currentX += (float) logicalBounds.getWidth();
            }
        }
    }

    @Override
    public void texture(Matrix4f positionMatrix, Texture texture, float x, float y, float width, float height) {
        this.applyTransform(positionMatrix);
        BufferedImage image = ((AWTTexture) texture).image();
        this.g2d.drawImage(image, (int) x, (int) y, (int) width, (int) height, null);
    }

    @Override
    public void beginBatch() {
    }

    @Override
    public void endBatch() {
    }

    protected void renderTextDecorations(final TextSegment segment, final float x, final float y, final FontMetrics metrics, final Rectangle2D logicalBounds) {
        LineMetrics lineMetrics = metrics.getLineMetrics(segment.text(), this.g2d);
        final int styleFlags = segment.styleFlags();
        if ((styleFlags & TextSegment.STYLE_UNDERLINE_BIT) != 0 || (styleFlags & TextSegment.STYLE_STRIKETHROUGH_BIT) != 0) {
            if ((styleFlags & TextSegment.STYLE_UNDERLINE_BIT) != 0) {
                float halfLineThickness = lineMetrics.getUnderlineThickness() / 2F;
                if ((styleFlags & TextSegment.STYLE_BOLD_BIT) != 0) {
                    halfLineThickness *= 1.5F;
                }
                final float lineY = y + lineMetrics.getUnderlineOffset();
                this.g2d.fill(new Rectangle2D.Float((float) (x + logicalBounds.getX()), lineY - halfLineThickness, (float) logicalBounds.getWidth(), halfLineThickness * 2));
            }
            if ((styleFlags & TextSegment.STYLE_STRIKETHROUGH_BIT) != 0) {
                float lineThickness = lineMetrics.getStrikethroughThickness();
                if ((styleFlags & TextSegment.STYLE_BOLD_BIT) != 0) {
                    lineThickness *= 1.5F;
                }
                final float lineY = y + lineMetrics.getStrikethroughOffset();
                this.g2d.fill(new Rectangle2D.Float((float) (x + logicalBounds.getX()), lineY, (float) logicalBounds.getWidth(), lineThickness));
            }
        }
    }

    private void applyColor(final Color color) {
        this.g2d.setColor(new java.awt.Color(color.toARGB(), true));
    }

    private void applyTransform(final Matrix4f positionMatrix) {
        this.g2d.setTransform(new AffineTransform(
                positionMatrix.m00(), positionMatrix.m10(),
                positionMatrix.m10(), positionMatrix.m11(),
                positionMatrix.m30(), positionMatrix.m31()
        ));
    }

}
