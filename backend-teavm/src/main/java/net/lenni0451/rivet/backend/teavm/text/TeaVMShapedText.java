package net.lenni0451.rivet.backend.teavm.text;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.backend.text.TextDecorations;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.text.model.TextFormat;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public class TeaVMShapedText implements ShapedText {

    private final List<Section> sections;
    private final Rectangle visualBounds;
    private final Rectangle logicalBounds;

    @Override
    public Point cursorPosition(final int index) {
        if (index <= 0) return new Point(0, 0);
        int currentIndex = 0;
        for (Section section : this.sections) {
            int length = section.text().length();
            if (index <= currentIndex + length) {
                return new Point(section.x() + section.positions()[index - currentIndex], 0);
            }
            currentIndex += length;
        }
        return new Point(this.logicalBounds.width(), 0);
    }

    @Override
    public int index(final float x, final float y) {
        if (x <= 0) return 0;
        int currentIndex = 0;
        for (Section section : this.sections) {
            String text = section.text();
            float[] positions = section.positions();
            int i = 0;
            while (i < text.length()) {
                int charCount = Character.charCount(text.codePointAt(i));
                float glyphX = section.x() + positions[i];
                float nextGlyphX = section.x() + positions[i + charCount];
                if (x < (glyphX + nextGlyphX) / 2F) {
                    return currentIndex + i;
                }
                i += charCount;
            }
            currentIndex += text.length();
        }
        return currentIndex;
    }


    @Getter
    @RequiredArgsConstructor
    @Accessors(fluent = true, chain = true, makeFinal = true)
    public static final class Section {
        private final FontStyle style;
        private final TextFormat format;
        private final String text;
        private final float x;
        private final float[] positions;
        private final float advance;

        // Render cache (only accessed by the render thread)
        @Getter(AccessLevel.NONE)
        private GlyphAtlas.Glyph[] cachedGlyphs;
        @Getter(AccessLevel.NONE)
        private GlyphAtlas.Glyph[] cachedOutlineGlyphs;
        @Getter(AccessLevel.NONE)
        private int cachedRasterSize = -1;
        @Getter(AccessLevel.NONE)
        private int cachedGeneration = -1;

        public GlyphAtlas.Glyph[] glyphs(final GlyphAtlas atlas, final int rasterSize, final boolean outline) {
            if (this.cachedRasterSize != rasterSize || this.cachedGeneration != atlas.generation()) {
                this.cachedGlyphs = null;
                this.cachedOutlineGlyphs = null;
                this.cachedRasterSize = rasterSize;
                this.cachedGeneration = atlas.generation();
            }
            GlyphAtlas.Glyph[] glyphs = outline ? this.cachedOutlineGlyphs : this.cachedGlyphs;
            if (glyphs == null) {
                float outlineWidth = outline ? TextDecorations.OUTLINE_WIDTH_FACTOR * rasterSize : 0;
                GlyphAtlas.GlyphSet glyphSet = atlas.glyphSet(this.style.css(rasterSize), outlineWidth);
                glyphs = new GlyphAtlas.Glyph[this.text.length()];
                int i = 0;
                while (i < this.text.length()) {
                    int codePoint = this.text.codePointAt(i);
                    glyphs[i] = glyphSet.get(codePoint);
                    i += Character.charCount(codePoint);
                }
                if (outline) {
                    this.cachedOutlineGlyphs = glyphs;
                } else {
                    this.cachedGlyphs = glyphs;
                }
            }
            return glyphs;
        }
    }

}
