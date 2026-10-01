package net.lenni0451.rivet.parser.impl;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.parser.StringParser;

import javax.annotation.Nullable;
import java.util.function.Function;

@RequiredArgsConstructor
public class NumberParser<N extends Number> implements StringParser<N> {

    private final Function<String, N> parser;
    private final Function<N, String> toString;

    public NumberParser(final Function<String, N> parser) {
        this(parser, Object::toString);
    }

    @Nullable
    @Override
    public N parse(final String s) {
        return this.parser.apply(s);
    }

    @Override
    public String toString(final N value) {
        return this.toString.apply(value);
    }

}
