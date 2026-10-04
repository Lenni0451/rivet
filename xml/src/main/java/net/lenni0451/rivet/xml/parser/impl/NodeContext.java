package net.lenni0451.rivet.xml.parser.impl;

import net.lenni0451.rivet.xml.parser.XmlParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        NodeList children = this.element().getChildNodes();
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) child);
            }
        }
        return elements;
    }

    default <T> Optional<T> attribute(final String name, final Class<T> type) {
        if (!this.element().hasAttribute(name)) {
            return Optional.empty();
        }
        String value = this.element().getAttribute(name);
        if (value.startsWith("${") && value.endsWith("}")) {
            String varName = value.substring(2, value.length() - 1);
            T varValue = this.context().getVariable(varName, type).orElse(null);
            if (varValue == null) {
                throw new IllegalArgumentException("Variable '" + varName + "' not found or not of type " + type.getTypeName());
            }
            return Optional.of(varValue);
        }
        return Optional.ofNullable(this.context().stringParserRegistry().parse(type, value));
    }

    default <T> T requireAttribute(final String name, final Class<T> type) {
        return this.attribute(name, type).orElseThrow(() -> new IllegalArgumentException("Missing required attribute '" + name + "'"));
    }

    default <T> T attributeOrDefault(final String name, final Class<T> type, final T defaultValue) {
        return this.attribute(name, type).orElse(defaultValue);
    }

    default <T> Optional<T> referenceAttribute(final String name, final Class<T> type) {
        if (!this.element().hasAttribute(name)) {
            return Optional.empty();
        }
        String value = this.element().getAttribute(name);
        if (value.startsWith("${") && value.endsWith("}")) {
            String varName = value.substring(2, value.length() - 1);
            T varValue = this.context().getVariable(varName, type).orElse(null);
            if (varValue == null) {
                throw new IllegalArgumentException("Variable '" + varName + "' not found or not of type " + type.getTypeName());
            }
            return Optional.of(varValue);
        }
        throw new IllegalArgumentException("Attribute '" + name + "' must be a variable reference (${...})");
    }

    default <T> T requireReferenceAttribute(final String name, final Class<T> type) {
        return this.referenceAttribute(name, type).orElseThrow(() -> new IllegalArgumentException("Missing required reference attribute '" + name + "'"));
    }

    default <T> Optional<T> child(final Class<T> expectedType) {
        for (Element child : this.childElements()) {
            String tagName = child.getNodeName();
            Class<?> type = this.context().registry().getTypeByTag(tagName);
            if (type != null && expectedType.isAssignableFrom(type)) {
                return Optional.of(expectedType.cast(this.context().registry().parseNode(child, this.context())));
            }
        }
        return Optional.empty();
    }

    default <T> T requireChild(final Class<T> expectedType) {
        return this.child(expectedType).orElseThrow(() -> new IllegalArgumentException("Missing required child of type " + expectedType.getTypeName()));
    }

}
