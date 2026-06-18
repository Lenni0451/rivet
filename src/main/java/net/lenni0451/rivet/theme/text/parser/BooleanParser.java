package net.lenni0451.rivet.theme.text.parser;

public class BooleanParser implements Parser<Boolean> {

    @Override
    public Boolean parse(final String s) {
        return Boolean.valueOf(s);
    }

    @Override
    public String toString(final Boolean value) {
        return value.toString();
    }

}
