package net.lenni0451.rivet.backend.awt;

import net.lenni0451.rivet.backend.Font;
import net.lenni0451.rivet.backend.FontSet;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AWTFontSet implements FontSet {

    private final List<AWTFont> fonts;

    public AWTFontSet(final List<AWTFont> fonts) {
        if (fonts.isEmpty()) throw new IllegalArgumentException("Font set must contain at least one font");
        this.fonts = fonts;
    }

    @Override
    public Font getMainFont() {
        return this.fonts.get(0);
    }

    @Override
    public @Nullable Font getFont(int codePoint) {
        for (AWTFont font : this.fonts) {
            if (font.font.canDisplay(codePoint)) return font;
        }
        return null;
    }

}
