package net.lenni0451.rivet.parser.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.parser.StringParser;
import net.lenni0451.rivet.parser.impl.color.ColorFormat;
import net.lenni0451.rivet.text.ParserException;

import javax.annotation.Nullable;
import java.util.Locale;

public final class ColorParser implements StringParser<Color> {

    @Nullable
    @Override
    public Color parse(final String s) {
        String value = s.trim().toLowerCase(Locale.ROOT);
        for (ColorFormat format : ColorFormat.FORMATS) {
            try {
                if (format.canParse(value)) {
                    return format.parse(value);
                }
            } catch (ParserException e) {
                throw new IllegalArgumentException("Unable to parse '" + s + "': " + e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public String toString(final Color value) {
        return "rgba(" + value.getRed() + ", " + value.getGreen() + ", " + value.getBlue() + ", " + value.getAlpha() + ")";
    }

}
