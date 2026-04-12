package net.lenni0451.rivet.backend.thingl;

import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.math.Size;
import net.raphimc.thingl.text.shaping.ShapedTextLine;
import net.raphimc.thingl.text.shaping.ShapedTextRun;
import net.raphimc.thingl.text.shaping.ShapedTextSegment;
import net.raphimc.thingl.text.shaping.TextShaper;
import org.joml.primitives.Rectanglef;

public record ThinGLShapedText(ShapedTextLine shapedTextLine) implements ShapedText {

    @Override
    public Size visualSize() {
        Rectanglef bounds = this.shapedTextLine.visualBounds();
        return new Size(bounds.lengthX(), bounds.lengthY());
    }

    @Override
    public Size logicalSize() {
        Rectanglef bounds = this.shapedTextLine.logicalBounds();
        return new Size(bounds.lengthX(), bounds.lengthY());
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
        return this.logicalSize().width();
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
