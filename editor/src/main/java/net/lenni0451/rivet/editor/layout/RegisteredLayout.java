package net.lenni0451.rivet.editor.layout;

import net.lenni0451.rivet.editor.properties.Property;
import net.lenni0451.rivet.layout.Layout;

import java.util.List;
import java.util.function.Supplier;

public record RegisteredLayout(String name, Supplier<Layout> constructor, List<Property> properties) {

    public RegisteredLayout(final String name, final Supplier<Layout> constructor) {
        this(name, constructor, List.of());
    }

}
