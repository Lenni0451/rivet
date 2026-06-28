package net.lenni0451.rivet.backend.thingl.text;

import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.math.Rectangle;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.text.shaping.*;
import org.joml.primitives.Rectanglef;

public record ThinGLShapedTextBlock(ShapedTextBlock shapedTextBlock) implements net.lenni0451.rivet.backend.text.ShapedTextBlock {

    @Override
    public Rectangle visualBounds() {
        Rectanglef bounds = this.shapedTextBlock.visualBounds();
        return new Rectangle(bounds.minX, bounds.minY, bounds.lengthX(), bounds.lengthY());
    }

    @Override
    public Rectangle logicalBounds() {
        Rectanglef bounds = this.shapedTextBlock.logicalBounds();
        return new Rectangle(bounds.minX, bounds.minY, bounds.lengthX(), bounds.lengthY());
    }

    @Override
    public Point cursorPosition(final int index) {
        if (index <= 0) return new Point(0, 0);
        int currentIndex = 0;
        float currentY = 0F;
        for (ShapedTextLine line : this.shapedTextBlock.lines()) {
            float runX = 0F;
            for (int i = 0; i < line.runs().size(); i++) {
                ShapedTextRun run = line.runs().get(i);
                for (ShapedTextSegment segment : run.segments()) {
                    for (TextShaper.Glyph glyph : segment.glyphs()) {
                        if (currentIndex == index) return new Point(runX + glyph.x(), currentY);
                        currentIndex++;
                    }
                }
                runX += run.logicalBounds().lengthX();
            }
            currentY += line.logicalBounds().lengthY();
        }
        if (this.shapedTextBlock.lines().isEmpty()) {
            return new Point(0, 0);
        }
        ShapedTextLine lastLine = this.shapedTextBlock.lines().getLast();
        return new Point(lastLine.logicalBounds().lengthX(), currentY - lastLine.logicalBounds().lengthY());
    }

    @Override
    public int index(final float x, final float y) {
        if (this.shapedTextBlock.lines().isEmpty()) return 0;
        int currentIndex = 0;
        float currentY = 0F;
        for (ShapedTextLine line : this.shapedTextBlock.lines()) {
            float lineBottomY = currentY + line.logicalBounds().lengthY();
            boolean isLastLine = line == this.shapedTextBlock.lines().getLast();
            if (y <= lineBottomY || isLastLine) {
                if (x <= 0) return currentIndex;
                float runX = 0F;
                for (int i = 0; i < line.runs().size(); i++) {
                    ShapedTextRun run = line.runs().get(i);
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
            } else {
                for (int i = 0; i < line.runs().size(); i++) {
                    ShapedTextRun run = line.runs().get(i);
                    for (ShapedTextSegment segment : run.segments()) {
                        currentIndex += segment.glyphs().size();
                    }
                }
            }
            currentY += line.logicalBounds().lengthY();
        }
        return currentIndex;
    }

}
