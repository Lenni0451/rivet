package net.lenni0451.rivet.parser.impl;

import net.lenni0451.rivet.parser.StringParser;

public class StringValueParser implements StringParser<String> {

    @Override
    public String parse(final String s) {
        return s;
    }

    @Override
    public String toString(final String value) {
        return value;
    }

}
