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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
    private final List<XmlImportResolver> importResolvers;

    public XmlLoader(final XmlParserRegistry xmlParserRegistry, final ParserRegistry stringParserRegistry) {
        this(xmlParserRegistry, stringParserRegistry, List.of(XmlImportResolver.classpath(), XmlImportResolver.filesystem()));
    }

    public XmlParserContext createContext(final Map<String, Object> variables) {
        return this.createContext(variables, Map.of());
    }

    public XmlParserContext createContext(final Map<String, Object> variables, final Map<String, String> constants) {
        return new XmlParserContext(this.xmlParserRegistry, this.stringParserRegistry, variables, constants);
    }

    public Component load(final String xml) {
        return this.loadUi(xml, Map.of()).root();
    }

    public Component load(final String xml, final Map<String, Object> variables) {
        return this.loadUi(xml, variables).root();
    }

    public Component load(final String xml, final Map<String, Object> variables, final Map<String, String> constants) {
        return this.loadUi(xml, variables, constants).root();
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
        return this.loadUi(xml, variables, Map.of());
    }

    public XmlUi loadUi(final String xml, final Map<String, Object> variables, final Map<String, String> constants) {
        return this.loadUi(XmlUtils.parseDocument(xml), variables, constants, null);
    }

    public XmlUi loadUi(final InputStream stream) {
        return this.loadUi(stream, Map.of());
    }

    public XmlUi loadUi(final InputStream stream, final Map<String, Object> variables) {
        return this.loadUi(stream, variables, Map.of(), null);
    }

    public XmlUi loadUi(final InputStream stream, final Map<String, Object> variables, final String source) {
        return this.loadUi(stream, variables, Map.of(), source);
    }

    public XmlUi loadUi(final InputStream stream, final Map<String, Object> variables, final Map<String, String> constants) {
        return this.loadUi(stream, variables, constants, null);
    }

    public XmlUi loadUi(final InputStream stream, final Map<String, Object> variables, final Map<String, String> constants, final String source) {
        return this.loadUi(XmlUtils.parseDocument(stream), variables, constants, source);
    }

    public XmlUi loadUi(final Path path) {
        return this.loadUi(path, Map.of());
    }

    public XmlUi loadUi(final Path path, final Map<String, Object> variables) {
        return this.loadUi(path, variables, Map.of());
    }

    public XmlUi loadUi(final Path path, final Map<String, Object> variables, final Map<String, String> constants) {
        try (InputStream stream = Files.newInputStream(path)) {
            return this.loadUi(stream, variables, constants, XmlImportResolver.FILE_PREFIX + path.toAbsolutePath().normalize());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read path: " + path, e);
        }
    }

    public XmlUi loadUi(final Document document, final Map<String, Object> variables) {
        return this.loadUi(document, variables, Map.of(), null);
    }

    public XmlUi loadUi(final Document document, final Map<String, Object> variables, final String source) {
        return this.loadUi(document, variables, Map.of(), source);
    }

    public XmlUi loadUi(final Document document, final Map<String, Object> variables, final Map<String, String> constants, final String source) {
        Element docElement = document.getDocumentElement();
        if ("RivetUi".equals(docElement.getNodeName())) {
            return new RivetUiParser(this.xmlParserRegistry, this.stringParserRegistry, this.importResolvers).parse(docElement, variables, constants, source);
        }
        XmlUtils.substituteConstants(docElement, constants);
        XmlParserContext context = this.createContext(variables, constants);
        Component root = this.xmlParserRegistry.parse(Component.class, docElement, context);
        return new XmlUi(root, context.componentsById(), constants);
    }

}
