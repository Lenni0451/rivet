package net.lenni0451.rivet.backend.thingl.text;

import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.math.Rectangle;
import net.raphimc.thingl.text.shaping.ShapedTextLine;
import net.raphimc.thingl.text.shaping.ShapedTextRun;
import net.raphimc.thingl.text.shaping.ShapedTextSegment;
import net.raphimc.thingl.text.shaping.TextShaper;
import org.joml.primitives.Rectanglef;

public record ThinGLShapedText(ShapedTextLine shapedTextLine) implements ShapedText {

    @Override
    public Rectangle visualBounds() {
        Rectanglef bounds = this.shapedTextLine.visualBounds();
        return new Rectangle(bounds.minX, bounds.minY, bounds.lengthX(), bounds.lengthY());
    }

    @Override
    public Rectangle logicalBounds() {
        Rectanglef bounds = this.shapedTextLine.logicalBounds();
        return new Rectangle(bounds.minX, bounds.minY, bounds.lengthX(), bounds.lengthY());
    }

    @Override
    public float cursorPosition(final int index) {
        if (index <= 0) return 0;
        int currentIndex = 0;
        for (int i = 0; i < this.shapedTextLine.runs().size(); i++) {
            ShapedTextRun run = this.shapedTextLine.runs().get(i);
            for (ShapedTextSegment segment : run.segments()) {
                for (TextShaper.Glyph glyph : segment.glyphs()) {
                    if (currentIndex == index) return glyph.x();
                    currentIndex++;
                }
            }
        }
        return this.logicalBounds().width();
    }

    @Override
    public int index(final float x) {
        if (x <= 0) return 0;
        int currentIndex = 0;
        for (int i = 0; i < this.shapedTextLine.runs().size(); i++) {
            ShapedTextRun run = this.shapedTextLine.runs().get(i);
            for (ShapedTextSegment segment : run.segments()) {
                for (TextShaper.Glyph glyph : segment.glyphs()) {
                    float glyphWidth = glyph.fontGlyph().xAdvance();
                    if (x < glyph.x() + glyphWidth / 2F) return currentIndex;
                    currentIndex++;
                }
            }
        }
        return currentIndex;
    }

}
