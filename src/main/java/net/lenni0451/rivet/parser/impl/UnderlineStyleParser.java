package net.lenni0451.rivet.parser.impl;

import net.lenni0451.rivet.parser.StringParser;
import net.lenni0451.rivet.text.diagnostic.UnderlineStyle;

import javax.annotation.Nullable;
import java.util.Locale;

public final class UnderlineStyleParser implements StringParser<UnderlineStyle> {

    @Nullable
    @Override
    public UnderlineStyle parse(final String s) {
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "solid" -> UnderlineStyle.SOLID;
            case "double" -> UnderlineStyle.DOUBLE;
            case "dashed" -> UnderlineStyle.DASHED;
            case "dotted" -> UnderlineStyle.DOTTED;
            case "wavy" -> UnderlineStyle.WAVY;
            case "zigzag" -> UnderlineStyle.ZIGZAG;
            default -> null;
        };
    }

    @Nullable
    @Override
    public String toString(final UnderlineStyle value) {
        if (value == UnderlineStyle.SOLID) return "solid";
        if (value == UnderlineStyle.DOUBLE) return "double";
        if (value == UnderlineStyle.DASHED) return "dashed";
        if (value == UnderlineStyle.DOTTED) return "dotted";
        if (value == UnderlineStyle.WAVY) return "wavy";
        if (value == UnderlineStyle.ZIGZAG) return "zigzag";
        return null;
    }

}
