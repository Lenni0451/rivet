package net.lenni0451.rivet.backend.thingl.text;

import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.math.Rectangle;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.text.shaping.ShapedTextLine;
import net.raphimc.thingl.text.shaping.ShapedTextRun;
import net.raphimc.thingl.text.shaping.ShapedTextSegment;
import net.raphimc.thingl.text.shaping.TextShaper;
import org.joml.primitives.Rectanglef;

public record ThinGLShapedText(ShapedTextLine shapedTextLine) implements ShapedText {

    @Override
    public Rectangle visualBounds() {
        Rectanglef bounds = ThinGL.rendererText().getScaledVisualBounds(this.shapedTextLine);
        return new Rectangle(bounds.minX, bounds.minY, bounds.lengthX(), bounds.lengthY());
    }

    @Override
    public Rectangle logicalBounds() {
        Rectanglef bounds = ThinGL.rendererText().getScaledLogicalBounds(this.shapedTextLine);
        return new Rectangle(bounds.minX, bounds.minY, bounds.lengthX(), bounds.lengthY());
    }

    @Override
    public Point cursorPosition(final int index) {
        if (index <= 0) return new Point(0, 0);
        float globalScale = ThinGL.rendererText().getGlobalScale();
        int currentIndex = 0;
        for (int i = 0; i < this.shapedTextLine.runs().size(); i++) {
            ShapedTextRun run = this.shapedTextLine.runs().get(i);
            for (ShapedTextSegment segment : run.segments()) {
                for (TextShaper.Glyph glyph : segment.glyphs()) {
                    if (currentIndex == index) return new Point(glyph.x() * globalScale, 0);
                    currentIndex++;
                }
            }
        }
        return new Point(this.logicalBounds().width(), 0);
    }

    @Override
    public int index(final float x, final float y) {
        if (x <= 0) return 0;
        float globalScale = ThinGL.rendererText().getGlobalScale();
        int currentIndex = 0;
        for (int i = 0; i < this.shapedTextLine.runs().size(); i++) {
            ShapedTextRun run = this.shapedTextLine.runs().get(i);
            for (ShapedTextSegment segment : run.segments()) {
                for (TextShaper.Glyph glyph : segment.glyphs()) {
                    float glyphX = glyph.x() * globalScale;
                    float glyphWidth = glyph.fontGlyph().xAdvance() * globalScale;
                    if (x < glyphX + glyphWidth / 2F) return currentIndex;
                    currentIndex++;
                }
            }
        }
        return currentIndex;
    }

}
