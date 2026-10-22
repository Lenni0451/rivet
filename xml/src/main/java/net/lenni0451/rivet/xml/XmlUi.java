package net.lenni0451.rivet.xml;

import net.lenni0451.rivet.component.Component;

import java.util.Map;
import java.util.Optional;

public record XmlUi(Component root, Map<String, Component> components, Map<String, String> constants) {

    public XmlUi {
        components = components == null ? Map.of() : Map.copyOf(components);
        constants = constants == null ? Map.of() : Map.copyOf(constants);
    }

    public XmlUi(final Component root, final Map<String, Component> components) {
        this(root, components, Map.of());
    }

    public <T extends Component> Optional<T> find(final String id, final Class<T> type) {
        return Optional.ofNullable(this.components.get(id)).filter(type::isInstance).map(type::cast);
    }

    public Optional<Component> find(final String id) {
        return this.find(id, Component.class);
    }

    public <T extends Component> T require(final String id, final Class<T> type) {
        Component component = this.components.get(id);
        if (component == null) {
            throw new IllegalArgumentException("Unknown XML component ID: " + id);
        }
        if (!type.isInstance(component)) {
            throw new IllegalArgumentException("XML component '" + id + "' must be " + type.getTypeName() + " but is " + component.getClass().getTypeName());
        }
        return type.cast(component);
    }

    public Component require(final String id) {
        return this.require(id, Component.class);
    }

    public Optional<String> constant(final String name) {
        return Optional.ofNullable(this.constants.get(name));
    }

    public String requireConstant(final String name) {
        return this.constant(name)
                .orElseThrow(() -> new IllegalArgumentException("Unknown XML constant: " + name));
    }

}
