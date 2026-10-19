package net.lenni0451.rivet.xml;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.parser.ParserRegistry;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.XmlParserRegistry;
import net.lenni0451.rivet.xml.parser.impl.template.TemplateComponentParser;
import net.lenni0451.rivet.xml.utils.XmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
public class RivetUiParser {

    private final XmlParserRegistry xmlParserRegistry;
    private final ParserRegistry stringParserRegistry;
    private final List<XmlImportResolver> importResolvers;

    public XmlUi parse(final Element rivetUiElement, final Map<String, Object> variables) {
        return this.parse(rivetUiElement, variables, null);
    }

    public XmlUi parse(final Element rivetUiElement, final Map<String, Object> variables, final String source) {
        XmlParserRegistry sessionRegistry = this.xmlParserRegistry.copy();
        DocumentParts parts = DocumentParts.of(rivetUiElement);
        if (parts.root() == null) {
            throw new IllegalArgumentException("<RivetUi> must contain a <Root> element");
        }
        if (parts.definitions() != null) {
            Set<String> importChain = new LinkedHashSet<>();
            if (source != null) {
                importChain.add(source);
            }
            this.parseDefinitions(parts.definitions(), sessionRegistry, source, importChain);
        }
        List<Element> rootChildren = XmlUtils.childElements(parts.root());
        if (rootChildren.size() != 1) {
            throw new IllegalArgumentException("<Root> must contain exactly one component, but found " + rootChildren.size());
        }

        XmlParserContext context = new XmlParserContext(sessionRegistry, this.stringParserRegistry, variables);
        Component root = sessionRegistry.parse(Component.class, rootChildren.get(0), context);
        return new XmlUi(root, context.componentsById());
    }

    private void parseDefinitions(final Element definitionsElement, final XmlParserRegistry registry, final String source, final Set<String> importChain) {
        for (Element child : XmlUtils.childElements(definitionsElement)) {
            switch (child.getNodeName()) {
                case "Component" -> this.parseComponentTemplate(child, registry);
                case "Import", "Include" -> this.parseImport(child, registry, source, importChain);
                default -> throw new IllegalArgumentException("Unknown <Definitions> child: " + child.getNodeName());
            }
        }
    }

    private void parseImport(final Element importElement, final XmlParserRegistry registry, final String currentSource, final Set<String> importChain) {
        String tag = importElement.getNodeName();
        NamedNodeMap attributes = importElement.getAttributes();
        if (attributes.getLength() != 1 || !(importElement.hasAttribute("source") || importElement.hasAttribute("file"))) {
            throw new IllegalArgumentException("<" + tag + "> requires exactly one 'source' or 'file' attribute");
        }
        if (!XmlUtils.childElements(importElement).isEmpty()) {
            throw new IllegalArgumentException("<" + tag + "> cannot have child elements");
        }
        String source = attributes.item(0).getNodeValue();

        try (XmlImportResolver.ResolvedImport resolved = this.resolveImport(source, currentSource)) {
            if (!importChain.add(resolved.source())) {
                throw new IllegalStateException("Circular import detected: " + String.join(" -> ", importChain) + " -> " + resolved.source());
            }
            try {
                Element documentElement = XmlUtils.parseDocument(resolved.stream()).getDocumentElement();
                Element definitions = switch (documentElement.getNodeName()) {
                    case "Definitions" -> documentElement;
                    case "RivetUi" -> {
                        DocumentParts parts = DocumentParts.of(documentElement);
                        if (parts.root() != null) {
                            throw new IllegalArgumentException("Imported document '" + resolved.source() + "' may contain definitions only, but found <Root>");
                        }
                        yield parts.definitions();
                    }
                    default -> throw new IllegalArgumentException("Imported document '" + resolved.source() + "' must have <RivetUi> or <Definitions> as root element");
                };
                if (definitions != null) {
                    this.parseDefinitions(definitions, registry, resolved.source(), importChain);
                }
            } finally {
                importChain.remove(resolved.source());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read import '" + source + "'", e);
        }
    }

    private XmlImportResolver.ResolvedImport resolveImport(final String source, final String currentSource) {
        for (XmlImportResolver resolver : this.importResolvers) {
            try {
                XmlImportResolver.ResolvedImport resolved = resolver.resolve(source, currentSource);
                if (resolved != null) {
                    return resolved;
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to resolve import '" + source + "'", e);
            }
        }
        throw new IllegalArgumentException("Unable to resolve import '" + source + "'");
    }

    private void parseComponentTemplate(final Element componentElement, final XmlParserRegistry registry) {
        if (!componentElement.hasAttribute("name")) {
            throw new IllegalArgumentException("<Component> template definition requires a 'name' attribute");
        }
        String name = componentElement.getAttribute("name");
        if (registry.supports(name)) {
            throw new IllegalArgumentException("Duplicate component or template name: " + name);
        }

        Map<String, TemplateComponentParser.TemplateParameter> parameters = new LinkedHashMap<>();
        Element templateElement = null;
        for (Element child : XmlUtils.childElements(componentElement)) {
            switch (child.getNodeName()) {
                case "Parameter" -> {
                    if (!child.hasAttribute("name")) {
                        throw new IllegalArgumentException("<Parameter> requires a 'name' attribute in template <" + name + ">");
                    }
                    String paramName = child.getAttribute("name");
                    boolean required = child.hasAttribute("required") && Boolean.parseBoolean(child.getAttribute("required"));
                    String defaultValue = child.hasAttribute("default") ? child.getAttribute("default") : null;
                    if (required && defaultValue != null) {
                        throw new IllegalArgumentException("Required parameter '" + paramName + "' cannot have a default value");
                    }
                    if (parameters.putIfAbsent(paramName, new TemplateComponentParser.TemplateParameter(required, defaultValue)) != null) {
                        throw new IllegalArgumentException("Duplicate parameter '" + paramName + "' in template <" + name + ">");
                    }
                }
                case "Template" -> {
                    if (templateElement != null) {
                        throw new IllegalArgumentException("Duplicate <Template> in <Component name=\"" + name + "\">");
                    }
                    templateElement = child;
                }
                default -> throw new IllegalArgumentException("Unknown <Component> definition child: " + child.getNodeName());
            }
        }
        if (templateElement == null) {
            throw new IllegalArgumentException("<Component name=\"" + name + "\"> is missing required <Template> element");
        }

        List<Element> templateChildren = XmlUtils.childElements(templateElement);
        if (templateChildren.size() != 1) {
            throw new IllegalArgumentException("<Template> in <Component name=\"" + name + "\"> must contain exactly one component");
        }

        Element templateRoot = templateChildren.get(0);
        if ("Slot".equals(templateRoot.getNodeName())) {
            throw new IllegalArgumentException("<Template> in <Component name=\"" + name + "\"> root cannot be a <Slot>");
        }
        TemplateComponentParser templateParser = new TemplateComponentParser(name, parameters, templateRoot);
        registry.registerByTag(name, Component.class, templateParser);
    }


    private record DocumentParts(Element definitions, Element root) {
        private static DocumentParts of(final Element rivetUiElement) {
            Element definitions = null;
            Element root = null;
            for (Element child : XmlUtils.childElements(rivetUiElement)) {
                switch (child.getNodeName()) {
                    case "Definitions" -> {
                        if (definitions != null) {
                            throw new IllegalArgumentException("Duplicate <Definitions> element in <RivetUi>");
                        }
                        definitions = child;
                    }
                    case "Root" -> {
                        if (root != null) {
                            throw new IllegalArgumentException("Duplicate <Root> element in <RivetUi>");
                        }
                        root = child;
                    }
                    default -> throw new IllegalArgumentException("Unknown <RivetUi> child: " + child.getNodeName());
                }
            }
            return new DocumentParts(definitions, root);
        }
    }

}
