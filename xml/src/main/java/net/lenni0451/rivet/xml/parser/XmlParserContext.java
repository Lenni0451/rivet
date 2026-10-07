package net.lenni0451.rivet.xml.parser;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.parser.ParserRegistry;
import net.lenni0451.rivet.xml.utils.XmlUtils;

import java.util.*;

public record XmlParserContext(
        XmlParserRegistry registry,
        ParserRegistry stringParserRegistry,
        Map<String, Object> variables,
        Map<String, Component> componentsById,
        Map<String, String> templateParameters,
        Deque<String> templateStack
) {

    public XmlParserContext(final XmlParserRegistry registry, final ParserRegistry stringParserRegistry, final Map<String, Object> variables) {
        this(registry, stringParserRegistry, variables, new HashMap<>(), new HashMap<>(), new ArrayDeque<>());
    }

    public Optional<Object> getVariable(final String name) {
        return Optional.ofNullable(this.variables.get(name));
    }

    public <T> Optional<T> getVariable(final String name, final Class<T> type) {
        return Optional.ofNullable(this.variables.get(name)).filter(type::isInstance).map(type::cast);
    }

    public <T> T requireVariable(final String name, final Class<T> type) {
        return this.getVariable(name, type)
                .orElseThrow(() -> new IllegalArgumentException("Variable '" + name + "' not found or not of type " + type.getTypeName()));
    }

    public void registerComponent(final String id, final Component component) {
        if (this.componentsById.putIfAbsent(id, component) != null) {
            throw new IllegalArgumentException("Duplicate component ID: " + id);
        }
    }

    public Optional<Component> getComponent(final String id) {
        return Optional.ofNullable(this.componentsById.get(id));
    }

    public <T extends Component> Optional<T> getComponent(final String id, final Class<T> type) {
        Component component = this.componentsById.get(id);
        if (!type.isInstance(component)) {
            return Optional.empty();
        }
        return Optional.of(type.cast(component));
    }

    public XmlParserContext withRegistry(final XmlParserRegistry registry) {
        return new XmlParserContext(registry, this.stringParserRegistry, this.variables, this.componentsById, this.templateParameters, this.templateStack);
    }

    public XmlParserContext withTemplateParameters(final Map<String, String> parameters) {
        Map<String, String> merged = new HashMap<>(this.templateParameters);
        merged.putAll(parameters);
        return new XmlParserContext(this.registry, this.stringParserRegistry, this.variables, this.componentsById, Map.copyOf(merged), this.templateStack);
    }

    public Optional<String> getTemplateParameter(final String name) {
        return Optional.ofNullable(this.templateParameters.get(name));
    }

    public String resolveTemplateParameter(final String value) {
        if (XmlUtils.isTemplateParameter(value)) {
            String paramName = XmlUtils.extractTemplateParameterName(value);
            return this.getTemplateParameter(paramName)
                    .orElseThrow(() -> new IllegalArgumentException("Template parameter '" + paramName + "' not found"));
        }
        return value;
    }

    public void pushTemplate(final String templateName) {
        if (this.templateStack.contains(templateName)) {
            throw new IllegalStateException("Circular template reference detected: " + String.join(" -> ", this.templateStack) + " -> " + templateName);
        }
        this.templateStack.addLast(templateName);
    }

    public void popTemplate() {
        this.templateStack.removeLast();
    }

}
