package net.lenni0451.rivet.parser.impl;

import net.lenni0451.rivet.parser.Parser;

public class BooleanParser implements Parser<Boolean> {

    @Override
    public Boolean parse(final String s) {
        if ("true".equalsIgnoreCase(s)) return true;
        if ("false".equalsIgnoreCase(s)) return false;
        throw new IllegalArgumentException("Expected 'true' or 'false' but got: " + s);
    }

    @Override
    public String toString(final Boolean value) {
        return value.toString();
    }

}
