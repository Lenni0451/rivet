package net.lenni0451.rivet.parser;

import javax.annotation.Nullable;

public interface Parser<T> {

    @Nullable
    T parse(final String s);

    @Nullable
    String toString(final T value);

}
