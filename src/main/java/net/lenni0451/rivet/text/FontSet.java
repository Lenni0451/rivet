package net.lenni0451.rivet.text;

import net.lenni0451.rivet.backend.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// https://github.com/RaphiMC/ThinGL/blob/fd9e6bd95a53b66f23842fac23205187399d22cf/src/main/java/net/raphimc/thingl/text/font/FontSet.java
public class FontSet {

    private final Font mainFont;
    private final List<Map.Entry<Font, GlyphPredicate>> fonts = new ArrayList<>();

    public FontSet(final Font mainFont) {
        this(mainFont, GlyphPredicate.all());
    }

    public FontSet(final Font mainFont, final GlyphPredicate predicate) {
        this.mainFont = mainFont;
        this.addFont(mainFont, predicate);
    }

    public FontSet(final List<Font> fonts) {
        if (fonts.isEmpty()) {
            throw new IllegalArgumentException("Font list must contain at least one font");
        }
        this.mainFont = fonts.get(0);
        this.addFonts(fonts);
    }

    public FontSet addFont(final Font font) {
        return this.addFont(font, GlyphPredicate.all());
    }

    public FontSet addFont(final Font font, final GlyphPredicate predicate) {
        this.fonts.add(Map.entry(font, predicate));
        return this;
    }

    public FontSet addFonts(final Iterable<Font> fonts) {
        for (Font font : fonts) {
            this.addFont(font);
        }
        return this;
    }

    public Font getMainFont() {
        return this.mainFont;
    }

    public Font getFont(final int codePoint) {
        for (Map.Entry<Font, GlyphPredicate> pair : this.fonts) {
            final Font font = pair.getKey();
            final GlyphPredicate predicate = pair.getValue();
            if (predicate.test(codePoint) && font.hasGlyph(codePoint)) {
                return font;
            }
        }
        return null;
    }

}
