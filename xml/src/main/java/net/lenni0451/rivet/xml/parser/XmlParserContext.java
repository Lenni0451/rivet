package net.lenni0451.rivet.xml.parser;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.parser.ParserRegistry;
import net.lenni0451.rivet.xml.XmlConstants;
import net.lenni0451.rivet.xml.utils.XmlUtils;

import java.util.*;

public record XmlParserContext(
        XmlParserRegistry registry,
        ParserRegistry stringParserRegistry,
        Map<String, Object> variables,
        Map<String, Component> componentsById,
        Map<String, String> templateParameters,
        Deque<String> templateStack,
        Map<String, String> constants
) {

    public XmlParserContext {
        variables = variables == null ? Map.of() : variables;
        componentsById = componentsById == null ? new HashMap<>() : componentsById;
        templateParameters = templateParameters == null ? Map.of() : templateParameters;
        templateStack = templateStack == null ? new ArrayDeque<>() : templateStack;
        constants = constants == null ? Map.of() : constants;
    }

    public XmlParserContext(final XmlParserRegistry registry, final ParserRegistry stringParserRegistry, final Map<String, Object> variables, final Map<String, String> constants) {
        this(registry, stringParserRegistry, variables, new HashMap<>(), new HashMap<>(), new ArrayDeque<>(), constants);
    }

    public XmlParserContext(final XmlParserRegistry registry, final ParserRegistry stringParserRegistry, final Map<String, Object> variables) {
        this(registry, stringParserRegistry, variables, Map.of());
    }

    public Optional<Object> getVariable(final String name) {
        return Optional.ofNullable(this.variables.get(name));
    }

    public <T> Optional<T> getVariable(final String name, final Class<T> type) {
        return this.getVariable(name).filter(type::isInstance).map(type::cast);
    }

    public Object requireVariable(final String name) {
        return this.getVariable(name)
                .orElseThrow(() -> new IllegalArgumentException("Variable '" + name + "' not found"));
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

    public XmlParserContext withTemplateParameters(final Map<String, String> parameters) {
        Map<String, String> merged = new HashMap<>(this.templateParameters);
        merged.putAll(parameters);
        return new XmlParserContext(this.registry, this.stringParserRegistry, this.variables, this.componentsById, Map.copyOf(merged), this.templateStack, this.constants);
    }

    public Optional<String> getConstant(final String name) {
        return Optional.ofNullable(this.constants.get(name));
    }

    public String resolveConstants(final String value) {
        return XmlUtils.resolvePlaceholders(value, XmlConstants.CONSTANT_PREFIX, XmlConstants.CONSTANT_SUFFIX, this.constants::get);
    }

    public Optional<String> getTemplateParameter(final String name) {
        return Optional.ofNullable(this.templateParameters.get(name));
    }

    public String resolveTemplateParameter(final String value) {
        if (XmlUtils.isTemplateParameter(value)) {
            String parameterName = XmlUtils.extractTemplateParameterName(value);
            return this.getTemplateParameter(parameterName)
                    .orElseThrow(() -> new IllegalArgumentException("Template parameter '" + parameterName + "' not found"));
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
