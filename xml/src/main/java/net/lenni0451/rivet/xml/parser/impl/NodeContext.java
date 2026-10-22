package net.lenni0451.rivet.xml.parser.impl;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.utils.XmlUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface NodeContext {

    static NodeContext of(final XmlParserContext context, final Element element) {
        return new NodeContext() {
            @Override
            public XmlParserContext context() {
                return context;
            }

            @Override
            public Element element() {
                return element;
            }
        };
    }


    XmlParserContext context();

    Element element();

    default List<Element> childElements() {
        return XmlUtils.childElements(this.element());
    }

    default <T> Optional<T> resolveAttribute(final String name, final Class<T> type, final boolean referenceOnly) {
        if (!this.element().hasAttribute(name)) {
            return Optional.empty();
        }
        String value = this.context().resolveTemplateParameter(this.element().getAttribute(name));
        if (XmlUtils.isVariableReference(value)) {
            String varName = XmlUtils.extractVariableReferenceName(value);
            Optional<T> var = this.context().getVariable(varName, type);
            if (var.isPresent()) {
                return var;
            }
            Optional<String> constant = this.context().getConstant(varName);
            if (constant.isPresent()) {
                if (referenceOnly) {
                    throw new IllegalArgumentException("Attribute '" + name + "' must be a variable reference (${...})");
                }
                return Optional.ofNullable(this.context().stringParserRegistry().parse(type, constant.get()));
            }
            throw new IllegalArgumentException("Variable '" + varName + "' not found or not of type " + type.getTypeName());
        }
        if (referenceOnly) {
            throw new IllegalArgumentException("Attribute '" + name + "' must be a variable reference (${...})");
        }
        return Optional.ofNullable(this.context().stringParserRegistry().parse(type, value));
    }

    default <T> Optional<T> attribute(final String name, final Class<T> type) {
        return this.resolveAttribute(name, type, false);
    }

    default <T> T requireAttribute(final String name, final Class<T> type) {
        return this.attribute(name, type).orElseThrow(() -> new IllegalArgumentException("Missing required attribute '" + name + "'"));
    }

    default <T> T attributeOrDefault(final String name, final Class<T> type, final T defaultValue) {
        return this.attribute(name, type).orElse(defaultValue);
    }

    default <T> Optional<T> referenceAttribute(final String name, final Class<T> type) {
        return this.resolveAttribute(name, type, true);
    }

    default <T> T requireReferenceAttribute(final String name, final Class<T> type) {
        return this.referenceAttribute(name, type).orElseThrow(() -> new IllegalArgumentException("Missing required reference attribute '" + name + "'"));
    }

    default Optional<Object> componentOrStringAttribute(final String name) {
        if (!this.element().hasAttribute(name)) {
            return Optional.empty();
        }
        String value = this.context().resolveTemplateParameter(this.element().getAttribute(name));
        if (XmlUtils.isVariableReference(value)) {
            String varName = XmlUtils.extractVariableReferenceName(value);
            Optional<Component> comp = this.context().getVariable(varName, Component.class);
            if (comp.isPresent()) {
                return Optional.of(comp.get());
            }
            Optional<String> constant = this.context().getConstant(varName);
            if (constant.isPresent()) {
                return Optional.of(constant.get());
            }
            throw new IllegalArgumentException("Variable '" + varName + "' not found or not of type " + Component.class.getTypeName());
        }
        return Optional.of(value);
    }

    default Optional<Component> componentOrTextAttribute(final String name, final Function<String, Component> textMapper) {
        return this.componentOrStringAttribute(name).map(obj -> {
            if (obj instanceof Component component) {
                return component;
            } else {
                return textMapper.apply((String) obj);
            }
        });
    }

    default Optional<Component> componentOrTextAttribute(final String name) {
        return this.componentOrTextAttribute(name, Label::new);
    }

    default <T> Optional<T> child(final Class<T> expectedType) {
        for (Element child : this.childElements()) {
            String tagName = child.getNodeName();
            Class<?> type = this.context().registry().getTypeByTag(tagName);
            if (type != null && expectedType.isAssignableFrom(type)) {
                return Optional.ofNullable(expectedType.cast(this.context().registry().parseNode(child, this.context())));
            }
        }
        return Optional.empty();
    }

    default <T> Optional<T> childOrReference(final String referenceAttributeName, final Class<T> expectedType) {
        return this.child(expectedType)
                .or(() -> this.referenceAttribute(referenceAttributeName, expectedType));
    }

    default <T> T requireChild(final Class<T> expectedType) {
        return this.child(expectedType)
                .orElseThrow(() -> new IllegalArgumentException("Missing required child of type " + expectedType.getTypeName()));
    }

    default <T> T requireChildOrReference(final String referenceAttributeName, final Class<T> expectedType) {
        return this.childOrReference(referenceAttributeName, expectedType)
                .orElseThrow(() -> new IllegalArgumentException("Missing required child or reference of type " + expectedType.getTypeName()));
    }

    default <T> List<T> children(final Class<T> expectedType) {
        List<T> list = new ArrayList<>();
        for (Element child : this.childElements()) {
            String tagName = child.getNodeName();
            Class<?> type = this.context().registry().getTypeByTag(tagName);
            if (type != null && expectedType.isAssignableFrom(type)) {
                list.add(expectedType.cast(this.context().registry().parseNode(child, this.context())));
            }
        }
        return list;
    }

}
