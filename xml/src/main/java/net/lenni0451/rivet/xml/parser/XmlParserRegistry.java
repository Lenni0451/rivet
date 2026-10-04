package net.lenni0451.rivet.xml.parser;

import org.w3c.dom.Node;

import java.util.LinkedHashMap;
import java.util.Map;

public class XmlParserRegistry {

    private final Map<Class<?>, XmlParser<?>> xmlParsers = new LinkedHashMap<>();
    private final Map<String, Class<?>> tagNames = new LinkedHashMap<>();

    public <T> XmlParserRegistry register(final Class<T> type, final XmlParser<? extends T> parser) {
        this.xmlParsers.put(type, parser);
        return this;
    }

    public <T> XmlParserRegistry register(final String tagName, final Class<T> type, final XmlParser<? extends T> parser) {
        this.tagNames.put(tagName, type);
        return this.register(type, parser);
    }

    public boolean supports(final Class<?> type) {
        return this.xmlParsers.containsKey(type);
    }

    public boolean supports(final String tagName) {
        return this.tagNames.containsKey(tagName);
    }

    public Class<?> getTypeByTag(final String tagName) {
        return this.tagNames.get(tagName);
    }

    public Object parseNode(final Node node, final XmlParserContext context) {
        String tagName = node.getNodeName();
        Class<?> type = this.tagNames.get(tagName);
        if (type == null) {
            throw new IllegalArgumentException("Unknown tag: " + tagName);
        }
        return this.parse(type, node, context);
    }

    public <T> T parse(final Class<T> type, final Node node, final XmlParserContext context) {
        XmlParser<?> parser = this.xmlParsers.get(type);
        if (parser == null) {
            throw new UnsupportedOperationException("Unsupported value type: " + type.getTypeName());
        }
        Object parsed;
        try {
            parsed = parser.parse(context, node);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Unable to parse node as " + type.getTypeName(), e);
        }
        if (parsed == null) {
            throw new IllegalArgumentException("Unable to parse node as " + type.getTypeName());
        }
        if (!type.isInstance(parsed)) {
            throw new IllegalStateException("Parser for " + type.getTypeName() + " returned " + parsed.getClass().getTypeName());
        }
        return type.cast(parsed);
    }

}
