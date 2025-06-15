package net.lenni0451.rivet.backend.awt;

import net.lenni0451.rivet.backend.ShapedTextBuffer;
import net.lenni0451.rivet.text.TextBuffer;
import net.lenni0451.rivet.text.TextRun;
import net.lenni0451.rivet.text.TextSegment;
import org.joml.primitives.Rectanglef;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class AWTShapedTextBuffer implements ShapedTextBuffer {

    private static final FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(), true, true);

    private final TextBuffer textBuffer;
    private final Rectanglef bounds;

    public AWTShapedTextBuffer(final TextBuffer textBuffer) {
        this.textBuffer = textBuffer;
        this.bounds = this.calculateBounds();
    }

    public TextBuffer textBuffer() {
        return this.textBuffer;
    }

    @Override
    public Rectanglef bounds() {
        return new Rectanglef(this.bounds);
    }

    private Rectanglef calculateBounds() {
        Rectanglef out = new Rectanglef(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        float currentX = 0;
        float currentY = 0;
        for (TextRun run : this.textBuffer.runs()) {
            currentX += run.xOffset();
            currentY += run.yOffset();

            Rectanglef runBounds = new Rectanglef();
            for (TextSegment segment : run.segments()) {
                int fontStyle = Font.PLAIN;
                if ((segment.styleFlags() & TextSegment.STYLE_ITALIC_BIT) != 0) fontStyle |= Font.ITALIC;
                if ((segment.styleFlags() & TextSegment.STYLE_BOLD_BIT) != 0) fontStyle |= Font.BOLD;
                Font font = ((AWTFont) run.font()).font().deriveFont(fontStyle);

                GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, segment.text());
                Rectangle2D bounds = font.createGlyphVector(fontRenderContext, segment.text()).getVisualBounds();
                float shadowOffset = 0;
                if ((segment.styleFlags() & TextSegment.STYLE_SHADOW_BIT) != 0) {
                    shadowOffset = Graphics2DRenderer.SHADOW_OFFSET_FACTOR * font.getSize();
                }
                float outlineOffset = 0;
                if (segment.outlineColor().getAlpha() > 0) {
                    outlineOffset = Graphics2DRenderer.OUTLINE_WIDTH_FACTOR * font.getSize() / 2;
                }
                runBounds.minX = Math.min(runBounds.minX, currentX + (float) bounds.getX() - outlineOffset);
                runBounds.minY = Math.min(runBounds.minY, currentY + (float) bounds.getY() - outlineOffset);
                runBounds.maxX = Math.max(runBounds.maxX, currentX + (float) (bounds.getX() + bounds.getWidth() + shadowOffset + outlineOffset));
                runBounds.maxY = Math.max(runBounds.maxY, currentY + (float) (bounds.getY() + bounds.getHeight() + shadowOffset + outlineOffset));
                currentX += (float) glyphVector.getLogicalBounds().getWidth();
            }
            out.minX = Math.min(out.minX, runBounds.minX);
            out.minY = Math.min(out.minY, runBounds.minY);
            out.maxX = Math.max(out.maxX, runBounds.maxX);
            out.maxY = Math.max(out.maxY, runBounds.maxY);
        }
        return out;
    }

}
