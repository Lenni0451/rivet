package net.lenni0451.rivet.theme.text.parser;

import org.jetbrains.annotations.Nullable;

public class StringParser implements Parser<String> {

    @Override
    public @Nullable String parse(final String s) {
        return s;
    }

    @Override
    public @Nullable String toString(final String value) {
        return value;
    }

}
