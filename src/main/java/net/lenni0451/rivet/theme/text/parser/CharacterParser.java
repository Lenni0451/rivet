package net.lenni0451.rivet.theme.text.parser;

public class CharacterParser implements Parser<Character> {

    @Override
    public Character parse(final String s) {
        if (s.length() != 1) {
            throw new IllegalArgumentException("Expected a single character but got: " + s);
        } else {
            return s.charAt(0);
        }
    }

    @Override
    public String toString(final Character value) {
        return value.toString();
    }

}
