package net.lenni0451.rivet.xml;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.parser.ParserRegistry;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.XmlParserRegistry;
import net.lenni0451.rivet.xml.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.util.Map;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public class XmlLoader {

    public static XmlLoader standard() {
        return new XmlLoader(XmlParserRegistry.standard(), ParserRegistry.standard());
    }


    private final XmlParserRegistry xmlParserRegistry;
    private final ParserRegistry stringParserRegistry;

    public XmlParserContext createContext(final Map<String, Object> variables) {
        return new XmlParserContext(this.xmlParserRegistry, this.stringParserRegistry, variables);
    }

    public Component load(final String xml) {
        return this.loadUi(xml, Map.of()).root();
    }

    public Component load(final String xml, final Map<String, Object> variables) {
        return this.loadUi(xml, variables).root();
    }

    public <T> T load(final Class<T> expectedType, final String xml) {
        return this.load(expectedType, xml, Map.of());
    }

    public <T> T load(final Class<T> expectedType, final String xml, final Map<String, Object> variables) {
        if (Component.class.isAssignableFrom(expectedType)) {
            return expectedType.cast(this.loadUi(xml, variables).root());
        }
        return this.xmlParserRegistry.parse(expectedType, xml, this.createContext(variables));
    }

    public Component load(final InputStream stream) {
        return this.loadUi(stream, Map.of()).root();
    }

    public Component load(final InputStream stream, final Map<String, Object> variables) {
        return this.loadUi(stream, variables).root();
    }

    public <T> T load(final Class<T> expectedType, final InputStream stream) {
        return this.load(expectedType, stream, Map.of());
    }

    public <T> T load(final Class<T> expectedType, final InputStream stream, final Map<String, Object> variables) {
        if (Component.class.isAssignableFrom(expectedType)) {
            return expectedType.cast(this.loadUi(stream, variables).root());
        }
        return this.xmlParserRegistry.parse(expectedType, stream, this.createContext(variables));
    }

    public XmlUi loadUi(final String xml) {
        return this.loadUi(xml, Map.of());
    }

    public XmlUi loadUi(final String xml, final Map<String, Object> variables) {
        return this.loadUi(XmlUtils.parseDocument(xml), variables);
    }

    public XmlUi loadUi(final InputStream stream) {
        return this.loadUi(stream, Map.of());
    }

    public XmlUi loadUi(final InputStream stream, final Map<String, Object> variables) {
        return this.loadUi(XmlUtils.parseDocument(stream), variables);
    }

    public XmlUi loadUi(final Document document, final Map<String, Object> variables) {
        Element docElement = document.getDocumentElement();
        if ("RivetUi".equals(docElement.getNodeName())) {
            return new RivetUiParser(this.xmlParserRegistry, this.stringParserRegistry).parse(docElement, variables);
        }
        XmlParserContext context = this.createContext(variables);
        Component root = this.xmlParserRegistry.parse(Component.class, docElement, context);
        return new XmlUi(root, context.componentsById());
    }

}
