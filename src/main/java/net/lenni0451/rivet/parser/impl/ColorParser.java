package net.lenni0451.rivet.parser.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.parser.Parser;
import net.lenni0451.rivet.parser.impl.color.ColorFormat;
import net.lenni0451.rivet.text.ParserException;

import javax.annotation.Nullable;

public final class ColorParser implements Parser<Color> {

    @Nullable
    @Override
    public Color parse(final String s) {
        for (ColorFormat format : ColorFormat.FORMATS) {
            try {
                if (format.canParse(s)) {
                    return format.parse(s);
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
