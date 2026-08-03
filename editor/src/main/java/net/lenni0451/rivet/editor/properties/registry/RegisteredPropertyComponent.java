package net.lenni0451.rivet.editor.properties.registry;

import net.lenni0451.rivet.component.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record RegisteredPropertyComponent<T>(Class<T> type, Constructor<T> constructor) {

    @FunctionalInterface
    public interface Constructor<T> {
        Component create(final String name, final Supplier<T> getter, final Consumer<T> setter);
    }

}
