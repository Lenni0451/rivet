package net.lenni0451.rivet.xml;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.parser.ParserRegistry;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.XmlParserRegistry;

import java.io.InputStream;
import java.util.Collections;
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
        return this.load(Component.class, xml, Collections.emptyMap());
    }

    public Component load(final String xml, final Map<String, Object> variables) {
        return this.load(Component.class, xml, variables);
    }

    public <T> T load(final Class<T> expectedType, final String xml) {
        return this.load(expectedType, xml, Collections.emptyMap());
    }

    public <T> T load(final Class<T> expectedType, final String xml, final Map<String, Object> variables) {
        return this.xmlParserRegistry.parse(expectedType, xml, this.createContext(variables));
    }

    public Component load(final InputStream stream) {
        return this.load(Component.class, stream, Collections.emptyMap());
    }

    public Component load(final InputStream stream, final Map<String, Object> variables) {
        return this.load(Component.class, stream, variables);
    }

    public <T> T load(final Class<T> expectedType, final InputStream stream) {
        return this.load(expectedType, stream, Collections.emptyMap());
    }

    public <T> T load(final Class<T> expectedType, final InputStream stream, final Map<String, Object> variables) {
        return this.xmlParserRegistry.parse(expectedType, stream, this.createContext(variables));
    }

}
