package net.lenni0451.rivet.xml.parser;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.layout.flex.FlexLayout;
import net.lenni0451.rivet.xml.parser.impl.component.ButtonParser;
import net.lenni0451.rivet.xml.parser.impl.component.ContainerParser;
import net.lenni0451.rivet.xml.parser.impl.layout.FlexLayoutParser;
import net.lenni0451.rivet.xml.parser.impl.property.CapabilitiesParser;
import net.lenni0451.rivet.xml.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class XmlParserRegistry {

    public static XmlParserRegistry standard() {
        XmlParserRegistry registry = new XmlParserRegistry();
        registry.register("Button", Button.class, new ButtonParser());
        registry.register("Container", Container.class, new ContainerParser());
        registry.register("FlexLayout", FlexLayout.class, new FlexLayoutParser());
        registry.register("Capabilities", Component.Capabilities.class, new CapabilitiesParser());
        return registry;
    }


    private final Map<Class<?>, XmlParser<?>> xmlParsers = new LinkedHashMap<>();
    private final Map<String, Class<?>> tagNames = new LinkedHashMap<>();
    private final Map<String, XmlParser<?>> parsersByTag = new LinkedHashMap<>();

    public XmlParserRegistry() {
    }

    public XmlParserRegistry(final XmlParserRegistry other) {
        this.xmlParsers.putAll(other.xmlParsers);
        this.tagNames.putAll(other.tagNames);
        this.parsersByTag.putAll(other.parsersByTag);
    }

    public XmlParserRegistry copy() {
        return new XmlParserRegistry(this);
    }

    public <T> XmlParserRegistry register(final Class<T> type, final XmlParser<? extends T> parser) {
        this.xmlParsers.put(type, parser);
        return this;
    }

    public <T> XmlParserRegistry register(final String tagName, final Class<T> type, final XmlParser<? extends T> parser) {
        this.tagNames.put(tagName, type);
        return this.register(type, parser);
    }

    public <T> XmlParserRegistry registerByTag(final String tagName, final Class<T> type, final XmlParser<? extends T> parser) {
        this.tagNames.put(tagName, type);
        this.parsersByTag.put(tagName, parser);
        return this;
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

    public XmlParser<?> getParserByTag(final String tagName) {
        return this.parsersByTag.get(tagName);
    }

    public Document parseDocument(final InputStream stream) {
        return XmlUtils.parseDocument(stream);
    }

    public Object parse(final Document document, final XmlParserContext context) {
        return this.parseNode(document.getDocumentElement(), context);
    }

    public <T> T parse(final Class<T> type, final Document document, final XmlParserContext context) {
        return this.parse(type, document.getDocumentElement(), context);
    }

    public Object parse(final InputStream stream, final XmlParserContext context) {
        return this.parse(this.parseDocument(stream), context);
    }

    public <T> T parse(final Class<T> type, final InputStream stream, final XmlParserContext context) {
        return this.parse(type, this.parseDocument(stream), context);
    }

    public Object parse(final String xml, final XmlParserContext context) {
        return this.parse(XmlUtils.parseDocument(xml), context);
    }

    public <T> T parse(final Class<T> type, final String xml, final XmlParserContext context) {
        return this.parse(type, XmlUtils.parseDocument(xml), context);
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
        ResolvedParser resolved = this.resolveParser(type, node);
        if (resolved == null) {
            throw new UnsupportedOperationException("Unsupported value type: " + type.getTypeName());
        }
        Object parsed;
        try {
            parsed = resolved.parser().parse(context, node);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Unable to parse node as " + resolved.targetType().getTypeName(), e);
        }
        if (parsed == null) {
            throw new IllegalArgumentException("Unable to parse node as " + resolved.targetType().getTypeName());
        }
        if (!type.isInstance(parsed)) {
            throw new IllegalStateException("Parser for " + resolved.targetType().getTypeName() + " returned " + parsed.getClass().getTypeName());
        }
        return type.cast(parsed);
    }

    private <T> ResolvedParser resolveParser(final Class<T> type, final Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String tagName = node.getNodeName();
            XmlParser<?> tagParser = this.parsersByTag.get(tagName);
            if (tagParser != null) {
                Class<?> tagType = this.tagNames.get(tagName);
                if (tagType != null && type.isAssignableFrom(tagType)) {
                    return new ResolvedParser(tagParser, tagType);
                }
            }
        }
        XmlParser<?> parser = this.xmlParsers.get(type);
        if (parser != null) {
            return new ResolvedParser(parser, type);
        }
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Class<?> tagType = this.tagNames.get(node.getNodeName());
            if (tagType != null && type.isAssignableFrom(tagType)) {
                XmlParser<?> tagTypeParser = this.xmlParsers.get(tagType);
                if (tagTypeParser != null) {
                    return new ResolvedParser(tagTypeParser, tagType);
                }
            }
        }
        return null;
    }


    private record ResolvedParser(XmlParser<?> parser, Class<?> targetType) {
    }

}
