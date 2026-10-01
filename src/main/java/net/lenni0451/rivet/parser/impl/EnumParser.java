package net.lenni0451.rivet.parser.impl;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.parser.StringParser;

import javax.annotation.Nullable;

@RequiredArgsConstructor
public final class EnumParser<E extends Enum<E>> implements StringParser<E> {

    private final Class<E> enumClass;

    @Nullable
    @Override
    public E parse(final String s) {
        for (E value : this.enumClass.getEnumConstants()) {
            if (value.name().equalsIgnoreCase(s)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String toString(final E value) {
        return value.name();
    }

}
