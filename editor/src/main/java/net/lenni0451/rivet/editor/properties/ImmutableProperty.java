package net.lenni0451.rivet.editor.properties;

import java.util.function.BiFunction;
import java.util.function.Function;

public record ImmutableProperty<O, T>(String name, Class<T> type, Function<O, T> getter, BiFunction<O, T, O> setter) implements Property {
}
