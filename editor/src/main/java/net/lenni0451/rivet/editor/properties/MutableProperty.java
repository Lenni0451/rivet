package net.lenni0451.rivet.editor.properties;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record MutableProperty<O, T>(String name, Class<T> type, Function<O, T> getter, BiConsumer<O, T> setter) implements Property {
}
