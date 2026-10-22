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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
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

    public LoadBuilder load() {
        return new LoadBuilder();
    }

    public LoadBuilder load(final String xml) {
        return this.load().xml(xml);
    }

    public LoadBuilder load(final InputStream stream) {
        return this.load().stream(stream);
    }

    public LoadBuilder load(final Path path) {
        return this.load().path(path);
    }

    public LoadBuilder load(final File file) {
        return this.load().file(file);
    }

    public LoadBuilder load(final Document document) {
        return this.load().document(document);
    }

    public LoadBuilder load(final Element element) {
        return this.load().element(element);
    }

    public XmlUi loadUi(final String xml) {
        return this.load(xml).loadUi();
    }

    public XmlUi loadUi(final InputStream stream) {
        return this.load(stream).loadUi();
    }

    public XmlUi loadUi(final Path path) {
        return this.load(path).loadUi();
    }


    public class LoadBuilder {

        private String xml;
        private InputStream stream;
        private Path path;
        private Document document;
        private Element element;
        private final Map<String, Object> variables = new LinkedHashMap<>();
        private final Map<String, String> constants = new LinkedHashMap<>();
        private String source;

        public LoadBuilder xml(final String xml) {
            this.xml = xml;
            return this;
        }

        public LoadBuilder stream(final InputStream stream) {
            this.stream = stream;
            return this;
        }

        public LoadBuilder path(final Path path) {
            this.path = path;
            return this;
        }

        public LoadBuilder file(final File file) {
            return this.path(file.toPath());
        }

        public LoadBuilder document(final Document document) {
            this.document = document;
            return this;
        }

        public LoadBuilder element(final Element element) {
            this.element = element;
            return this;
        }

        public LoadBuilder variable(final String name, final Object value) {
            this.variables.put(name, value);
            return this;
        }

        public LoadBuilder variables(final Map<String, ?> variables) {
            if (variables != null) {
                this.variables.putAll(variables);
            }
            return this;
        }

        public LoadBuilder constant(final String name, final String value) {
            this.constants.put(name, value);
            return this;
        }

        public LoadBuilder constants(final Map<String, String> constants) {
            if (constants != null) {
                this.constants.putAll(constants);
            }
            return this;
        }

        public LoadBuilder source(final String source) {
            this.source = source;
            return this;
        }

        public XmlUi loadUi() {
            String effectiveSource = this.source;
            if (effectiveSource == null && this.path != null) {
                effectiveSource = XmlConstants.FILE_PREFIX + this.path.toAbsolutePath().normalize();
            }
            Element rootElement = this.resolveElement();
            if (XmlConstants.TAG_RIVET_UI.equals(rootElement.getNodeName())) {
                return new RivetUiParser(XmlLoader.this.xmlParserRegistry, XmlLoader.this.stringParserRegistry, XmlLoader.this.importResolvers)
                        .parse(rootElement, this.variables, this.constants, effectiveSource);
            }
            XmlUtils.substituteConstants(rootElement, this.constants);
            XmlParserContext context = XmlLoader.this.createContext(this.variables, this.constants);
            Component root = XmlLoader.this.xmlParserRegistry.parse(Component.class, rootElement, context);
            return new XmlUi(root, context.componentsById(), this.constants);
        }

        public Component load() {
            return this.loadUi().root();
        }

        public <T> T load(final Class<T> expectedType) {
            if (Component.class.isAssignableFrom(expectedType)) {
                return expectedType.cast(this.load());
            }
            Element rootElement = this.resolveElement();
            XmlUtils.substituteConstants(rootElement, this.constants);
            XmlParserContext context = XmlLoader.this.createContext(this.variables, this.constants);
            return XmlLoader.this.xmlParserRegistry.parse(expectedType, rootElement, context);
        }

        private Element resolveElement() {
            if (this.element != null) {
                return this.element;
            }
            if (this.document != null) {
                return this.document.getDocumentElement();
            }
            if (this.path != null) {
                try (InputStream in = Files.newInputStream(this.path)) {
                    return XmlUtils.parseDocument(in).getDocumentElement();
                } catch (IOException e) {
                    throw new IllegalArgumentException("Unable to read path: " + this.path, e);
                }
            }
            if (this.stream != null) {
                return XmlUtils.parseDocument(this.stream).getDocumentElement();
            }
            if (this.xml != null) {
                return XmlUtils.parseDocument(this.xml).getDocumentElement();
            }
            throw new IllegalStateException("No XML source specified for loading");
        }

    }

}
