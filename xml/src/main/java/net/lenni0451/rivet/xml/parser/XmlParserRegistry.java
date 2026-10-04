package net.lenni0451.rivet.xml.parser;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.layout.flex.FlexLayout;
import net.lenni0451.rivet.xml.parser.impl.component.ButtonParser;
import net.lenni0451.rivet.xml.parser.impl.component.ContainerParser;
import net.lenni0451.rivet.xml.parser.impl.layout.FlexLayoutParser;
import net.lenni0451.rivet.xml.parser.impl.property.CapabilitiesParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    public Document parseDocument(final InputStream stream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(stream);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse XML document", e);
        }
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
        return this.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), context);
    }

    public <T> T parse(final Class<T> type, final String xml, final XmlParserContext context) {
        return this.parse(type, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), context);
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
        Class<?> targetType = type;
        XmlParser<?> parser = this.xmlParsers.get(targetType);
        if (parser == null && node.getNodeType() == Node.ELEMENT_NODE) {
            Class<?> tagType = this.tagNames.get(node.getNodeName());
            if (tagType != null && type.isAssignableFrom(tagType)) {
                targetType = tagType;
                parser = this.xmlParsers.get(targetType);
            }
        }
        if (parser == null) {
            throw new UnsupportedOperationException("Unsupported value type: " + type.getTypeName());
        }
        Object parsed;
        try {
            parsed = parser.parse(context, node);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Unable to parse node as " + targetType.getTypeName(), e);
        }
        if (parsed == null) {
            throw new IllegalArgumentException("Unable to parse node as " + targetType.getTypeName());
        }
        if (!type.isInstance(parsed)) {
            throw new IllegalStateException("Parser for " + targetType.getTypeName() + " returned " + parsed.getClass().getTypeName());
        }
        return type.cast(parsed);
    }

}
