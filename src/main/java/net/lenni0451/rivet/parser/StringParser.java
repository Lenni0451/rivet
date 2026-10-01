package net.lenni0451.rivet.parser;

import javax.annotation.Nullable;

public interface StringParser<T> {

    @Nullable
    T parse(final String s);

    @Nullable
    String toString(final T value);

}
