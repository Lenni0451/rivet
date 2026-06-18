package net.lenni0451.rivet.theme.text.parser;

public class StringParser implements Parser<String> {

    @Override
    public String parse(final String s) {
        return s;
    }

    @Override
    public String toString(final String value) {
        return value;
    }

}
