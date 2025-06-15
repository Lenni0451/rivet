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

            Font font = ((AWTFont) run.font()).font();
            Rectanglef runBounds = new Rectanglef();
            for (TextSegment segment : run.segments()) {
                GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, segment.text());
                Rectangle2D bounds = font.createGlyphVector(fontRenderContext, segment.text()).getVisualBounds();
                runBounds.minX = Math.min(runBounds.minX, currentX + (float) bounds.getX());
                runBounds.minY = Math.min(runBounds.minY, currentY + (float) bounds.getY());
                runBounds.maxX = Math.max(runBounds.maxX, currentX + (float) (bounds.getX() + bounds.getWidth()));
                runBounds.maxY = Math.max(runBounds.maxY, currentY + (float) (bounds.getY() + bounds.getHeight()));
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
