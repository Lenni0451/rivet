package net.lenni0451.rivet.backend.thingl.text;

import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.math.Rectangle;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.text.shaping.ShapedTextLine;
import net.raphimc.thingl.text.shaping.ShapedTextRun;
import net.raphimc.thingl.text.shaping.ShapedTextSegment;
import net.raphimc.thingl.text.shaping.TextShaper;

public record ThinGLShapedText(ShapedTextLine shapedTextLine, Rectangle visualBounds, Rectangle logicalBounds) implements ShapedText {

    public ThinGLShapedText(final ShapedTextLine shapedTextLine) {
        this(
                shapedTextLine,
                new Rectangle(shapedTextLine.visualBounds().minX, shapedTextLine.visualBounds().minY, shapedTextLine.visualBounds().lengthX(), shapedTextLine.visualBounds().lengthY()),
                new Rectangle(shapedTextLine.logicalBounds().minX, shapedTextLine.logicalBounds().minY, shapedTextLine.logicalBounds().lengthX(), shapedTextLine.logicalBounds().lengthY())
        );
    }

    @Override
    public Point cursorPosition(final int index) {
        if (index <= 0) return new Point(0, 0);
        int currentIndex = 0;
        float runX = 0;
        for (ShapedTextRun run : this.shapedTextLine.runs()) {
            for (ShapedTextSegment segment : run.segments()) {
                for (TextShaper.Glyph glyph : segment.glyphs()) {
                    if (currentIndex == index) return new Point(runX + glyph.x(), 0);
                    currentIndex++;
                }
            }
            runX += run.logicalBounds().lengthX();
        }
        return new Point(this.logicalBounds().width(), 0);
    }

    @Override
    public int index(final float x, final float y) {
        if (x <= 0) return 0;
        int currentIndex = 0;
        float runX = 0;
        for (int i = 0; i < this.shapedTextLine.runs().size(); i++) {
            ShapedTextRun run = this.shapedTextLine.runs().get(i);
            FontInstance font = run.font();
            for (ShapedTextSegment segment : run.segments()) {
                for (TextShaper.Glyph glyph : segment.glyphs()) {
                    float glyphX = runX + glyph.x();
                    float glyphWidth = font.getGlyphMetrics(glyph.index()).xAdvance();
                    if (x < glyphX + glyphWidth / 2F) return currentIndex;
                    currentIndex++;
                }
            }
            runX += run.logicalBounds().lengthX();
        }
        return currentIndex;
    }

}
