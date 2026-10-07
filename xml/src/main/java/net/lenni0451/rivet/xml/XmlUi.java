package net.lenni0451.rivet.xml;

import net.lenni0451.rivet.component.Component;

import java.util.Map;
import java.util.Optional;

public record XmlUi(Component root, Map<String, Component> components) {

    public XmlUi {
        components = Map.copyOf(components);
    }

    public Optional<Component> find(final String id) {
        return Optional.ofNullable(this.components.get(id));
    }

    public <T extends Component> Optional<T> find(final String id, final Class<T> type) {
        Component component = this.components.get(id);
        if (!type.isInstance(component)) {
            return Optional.empty();
        }
        return Optional.of(type.cast(component));
    }

    public Component require(final String id) {
        Component component = this.components.get(id);
        if (component == null) {
            throw new IllegalArgumentException("Unknown XML component ID: " + id);
        }
        return component;
    }

    public <T extends Component> T require(final String id, final Class<T> type) {
        Component component = this.require(id);
        if (!type.isInstance(component)) {
            throw new IllegalArgumentException("XML component '" + id + "' must be " + type.getTypeName() + " but is " + component.getClass().getTypeName());
        }
        return type.cast(component);
    }

}
