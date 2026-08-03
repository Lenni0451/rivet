package net.lenni0451.rivet.editor.properties.registry;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.editor.properties.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PropertyComponentRegistry {

    private final static List<RegisteredPropertyComponent<?>> propertyComponents = new ArrayList<>();

    static {
        register(new RegisteredPropertyComponent<>(Integer.class, IntegerPropertyComponent::new));
        register(new RegisteredPropertyComponent<>(String.class, StringPropertyComponent::new));
        register(new RegisteredPropertyComponent<>(Float.class, FloatPropertyComponent::new));
        register(new RegisteredPropertyComponent<>(Boolean.class, BooleanPropertyComponent::new));
        register(new RegisteredPropertyComponent<>(Color.class, ColorPropertyComponent::new));
    }

    public static void register(final RegisteredPropertyComponent<?> propertyComponent) {
        propertyComponents.add(propertyComponent);
    }

    public static <T> Optional<RegisteredPropertyComponent<T>> forType(final Class<T> type) {
        return propertyComponents.stream()
                .filter(pc -> pc.type().equals(type))
                .findFirst()
                .map(pc -> (RegisteredPropertyComponent<T>) pc);
    }

}
