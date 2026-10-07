package net.lenni0451.rivet.xml.parser.impl;

import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.utils.XmlUtils;
import org.w3c.dom.Element;

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
        return XmlUtils.childElements(this.element());
    }

    default <T> Optional<T> resolveAttribute(final String name, final Class<T> type, final boolean referenceOnly) {
        if (!this.element().hasAttribute(name)) {
            return Optional.empty();
        }
        String value = this.context().resolveTemplateParameter(this.element().getAttribute(name));
        if (XmlUtils.isVariableReference(value)) {
            String varName = XmlUtils.extractVariableReferenceName(value);
            return Optional.of(this.context().requireVariable(varName, type));
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
