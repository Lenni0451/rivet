package net.lenni0451.rivet.editor.component;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.editor.properties.Property;

import java.util.List;
import java.util.function.Supplier;

public record RegisteredComponent(String name, Supplier<Component> constructor, List<Property> properties) {

    public RegisteredComponent(final String name, final Supplier<Component> constructor) {
        this(name, constructor, List.of());
    }

}
