package net.lenni0451.rivet.backend.awt;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.ShapedTextBuffer;
import net.lenni0451.rivet.text.TextBuffer;
import net.lenni0451.rivet.text.TextRun;
import net.lenni0451.rivet.text.TextSegment;
import org.joml.Matrix4f;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class Graphics2DRenderer implements Renderer {

    private final Graphics2D g2d;

    public Graphics2DRenderer(final Graphics2D g2d) {
        this.g2d = g2d;
    }

    @Override
    public void filledRectangle(Matrix4f positionMatrix, float x, float y, float width, float height, Color color) {
        this.applyColor(color);
        this.applyTransform(positionMatrix);
        this.g2d.fill(new Rectangle2D.Float(x, y, width, height));
    }

    @Override
    public void text(Matrix4f positionMatrix, ShapedTextBuffer shapedTextBuffer, float x, float y) {
        this.applyTransform(positionMatrix);
        TextBuffer textBuffer = ((AWTShapedTextBuffer) shapedTextBuffer).textBuffer();
        float currentX = x;
        float currentY = y;
        for (TextRun run : textBuffer.runs()) {
            Font font = ((AWTFont) run.font()).font();
            FontMetrics metrics = this.g2d.getFontMetrics(font);
            this.g2d.setFont(font);
            for (TextSegment segment : run.segments()) {
                //TODO: Text height is wrong
                Rectangle2D bounds = metrics.getStringBounds(segment.text(), this.g2d);
                this.g2d.setColor(java.awt.Color.YELLOW);
                this.g2d.drawRect((int) currentX, (int) currentY, (int) bounds.getWidth(), (int) bounds.getHeight());
                this.applyColor(segment.color());
                float baselineOffset = metrics.getDescent() + metrics.getAscent();
                this.g2d.drawString(segment.text(), currentX, (float) (currentY + baselineOffset));
                currentX += metrics.stringWidth(segment.text());
            }
        }
    }

    @Override
    public void beginBatch() {
    }

    @Override
    public void endBatch() {
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
