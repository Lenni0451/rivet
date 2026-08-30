package net.lenni0451.rivet.backend.awt.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.text.model.TextFormat;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Point2D;
import java.util.List;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public class AWTShapedText implements ShapedText {

    private final List<Section> sections;
    private final Rectangle visualBounds;
    private final Rectangle logicalBounds;

    @Override
    public Point cursorPosition(final int index) {
        if (index <= 0) return new Point(0, 0);
        int currentIndex = 0;
        for (Section section : this.sections) {
            int glyphCount = section.glyphVector().getNumGlyphs();
            if (index <= currentIndex + glyphCount) {
                int glyphIndex = index - currentIndex;
                Point2D position = section.glyphVector().getGlyphPosition(glyphIndex);
                return new Point(section.xOffset() + (float) position.getX(), 0);
            }
            currentIndex += glyphCount;
        }
        return new Point(this.logicalBounds.width(), 0);
    }

    @Override
    public int index(final float x, final float y) {
        if (x <= 0) return 0;
        int currentIndex = 0;
        for (Section section : this.sections) {
            GlyphVector glyphVector = section.glyphVector();
            int glyphCount = glyphVector.getNumGlyphs();
            for (int i = 0; i < glyphCount; i++) {
                float glyphX = section.xOffset() + (float) glyphVector.getGlyphPosition(i).getX();
                float nextGlyphX = section.xOffset() + (float) glyphVector.getGlyphPosition(i + 1).getX();
                if (x < (glyphX + nextGlyphX) / 2F) {
                    return currentIndex;
                }
                currentIndex++;
            }
        }
        return currentIndex;
    }


    public record Section(
            java.awt.Font font,
            TextFormat format,
            GlyphVector glyphVector,
            LineMetrics lineMetrics,
            Shape outlineShape,
            Stroke outlineStroke,
            float xOffset,
            float advanceWidth
    ) {
    }

}
