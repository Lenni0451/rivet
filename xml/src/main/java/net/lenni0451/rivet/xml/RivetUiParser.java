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
        return this.parse(rivetUiElement, variables, Map.of(), null);
    }

    public XmlUi parse(final Element rivetUiElement, final Map<String, Object> variables, final String source) {
        return this.parse(rivetUiElement, variables, Map.of(), source);
    }

    public XmlUi parse(final Element rivetUiElement, final Map<String, Object> variables, final Map<String, String> externalConstants, final String source) {
        XmlParserRegistry sessionRegistry = this.xmlParserRegistry.copy();
        DocumentParts parts = DocumentParts.of(rivetUiElement);
        if (parts.root() == null) {
            throw new IllegalArgumentException("<RivetUi> must contain a <Root> element");
        }

        Map<String, String> rawConstants = new LinkedHashMap<>();
        if (externalConstants != null) {
            rawConstants.putAll(externalConstants);
        }

        Set<String> importChain = new LinkedHashSet<>();
        if (source != null) {
            importChain.add(source);
        }

        for (Element importElem : parts.imports()) {
            this.parseImport(importElem, sessionRegistry, rawConstants, source, importChain);
        }
        for (Element constElem : parts.constants()) {
            this.parseConstantElement(constElem, rawConstants, "");
        }
        if (parts.definitions() != null) {
            this.parseDefinitions(parts.definitions(), sessionRegistry, rawConstants, source, importChain);
        }

        Map<String, String> resolvedConstants = this.resolveAndValidateConstants(rawConstants);
        XmlUtils.substituteConstants(rivetUiElement, resolvedConstants);

        List<Element> rootChildren = XmlUtils.childElements(parts.root());
        if (rootChildren.size() != 1) {
            throw new IllegalArgumentException("<Root> must contain exactly one component, but found " + rootChildren.size());
        }

        XmlParserContext context = new XmlParserContext(sessionRegistry, this.stringParserRegistry, variables, resolvedConstants);
        Component root = sessionRegistry.parse(Component.class, rootChildren.get(0), context);
        return new XmlUi(root, context.componentsById(), resolvedConstants);
    }

    private void parseDefinitions(final Element definitionsElement, final XmlParserRegistry registry, final Map<String, String> rawConstants, final String source, final Set<String> importChain) {
        for (Element child : XmlUtils.childElements(definitionsElement)) {
            switch (child.getNodeName()) {
                case "Component" -> this.parseComponentTemplate(child, registry);
                case "Import", "Include" -> this.parseImport(child, registry, rawConstants, source, importChain);
                case "Constant" -> this.parseConstant(child, rawConstants, "");
                case "Constants", "Map" -> this.parseConstantsGroup(child, rawConstants);
                default -> throw new IllegalArgumentException("Unknown <Definitions> child: " + child.getNodeName());
            }
        }
    }

    private void parseConstantElement(final Element element, final Map<String, String> rawConstants, final String prefix) {
        switch (element.getNodeName()) {
            case "Constant", "Entry" -> this.parseConstant(element, rawConstants, prefix);
            case "Constants", "Map" -> this.parseConstantsGroup(element, rawConstants);
            default -> throw new IllegalArgumentException("Unknown constant element: " + element.getNodeName());
        }
    }

    private void parseConstant(final Element element, final Map<String, String> rawConstants, final String prefix) {
        String tag = element.getNodeName();
        String name = element.hasAttribute("name") ? element.getAttribute("name") : (element.hasAttribute("key") ? element.getAttribute("key") : null);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("<" + tag + "> requires a 'name' or 'key' attribute");
        }

        boolean hasValue = element.hasAttribute("value");
        boolean hasCodepoint = element.hasAttribute("codepoint");
        boolean hasHex = element.hasAttribute("hex");

        int count = (hasValue ? 1 : 0) + (hasCodepoint ? 1 : 0) + (hasHex ? 1 : 0);
        if (count > 1) {
            throw new IllegalArgumentException("Only one of 'value', 'codepoint', or 'hex' may be specified on <" + tag + "> '" + name + "'");
        }

        String resolvedValue;
        if (hasValue) {
            resolvedValue = XmlUtils.unescapeJavaString(element.getAttribute("value"));
        } else if (hasCodepoint) {
            resolvedValue = XmlUtils.parseCodepoint(element.getAttribute("codepoint"));
        } else if (hasHex) {
            resolvedValue = XmlUtils.parseHexCodepoint(element.getAttribute("hex"));
        } else {
            String text = element.getTextContent();
            if (text != null && !text.isBlank()) {
                resolvedValue = XmlUtils.unescapeJavaString(text.trim());
            } else {
                throw new IllegalArgumentException("<" + tag + "> '" + name + "' must specify 'value', 'codepoint', 'hex', or text content");
            }
        }

        String fullName = prefix.isEmpty() ? name : prefix + name;
        if (rawConstants.putIfAbsent(fullName, resolvedValue) != null) {
            throw new IllegalArgumentException("Duplicate constant name: " + fullName);
        }
    }

    private void parseConstantsGroup(final Element groupElement, final Map<String, String> rawConstants) {
        String name = groupElement.hasAttribute("name")
                ? groupElement.getAttribute("name")
                : (groupElement.hasAttribute("id") ? groupElement.getAttribute("id") : null);
        String prefix = groupElement.hasAttribute("prefix") ? groupElement.getAttribute("prefix") : null;

        String effectivePrefix;
        if (prefix != null) {
            effectivePrefix = prefix;
        } else if (name != null && !name.isBlank()) {
            effectivePrefix = name + ".";
        } else {
            effectivePrefix = "";
        }

        for (Element child : XmlUtils.childElements(groupElement)) {
            String childTag = child.getNodeName();
            if (!"Constant".equals(childTag) && !"Entry".equals(childTag)) {
                throw new IllegalArgumentException("Unknown child element in <" + groupElement.getNodeName() + ">: " + childTag);
            }
            this.parseConstant(child, rawConstants, effectivePrefix);
        }
    }

    private Map<String, String> resolveAndValidateConstants(final Map<String, String> rawConstants) {
        Map<String, String> resolved = new LinkedHashMap<>();
        Set<String> resolving = new LinkedHashSet<>();
        for (String key : rawConstants.keySet()) {
            this.resolveConstant(key, rawConstants, resolved, resolving);
        }
        return resolved;
    }

    private String resolveConstant(final String key, final Map<String, String> rawConstants, final Map<String, String> resolved, final Set<String> resolving) {
        if (resolved.containsKey(key)) {
            return resolved.get(key);
        }
        if (!rawConstants.containsKey(key)) {
            throw new IllegalArgumentException("Unknown constant: " + key);
        }
        if (!resolving.add(key)) {
            throw new IllegalStateException("Circular constant reference detected: " + String.join(" -> ", resolving) + " -> " + key);
        }
        try {
            String rawValue = rawConstants.get(key);
            String resolvedValue = XmlUtils.resolvePlaceholders(rawValue, "#{", "}", refKey -> this.resolveConstant(refKey, rawConstants, resolved, resolving));
            resolved.put(key, resolvedValue);
            return resolvedValue;
        } finally {
            resolving.remove(key);
        }
    }

    private void parseImport(final Element importElement, final XmlParserRegistry registry, final Map<String, String> rawConstants, final String currentSource, final Set<String> importChain) {
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
                switch (documentElement.getNodeName()) {
                    case "Definitions" -> this.parseDefinitions(documentElement, registry, rawConstants, resolved.source(), importChain);
                    case "Constants", "Map" -> this.parseConstantsGroup(documentElement, rawConstants);
                    case "Constant" -> this.parseConstant(documentElement, rawConstants, "");
                    case "RivetUi" -> {
                        DocumentParts parts = DocumentParts.of(documentElement);
                        if (parts.root() != null) {
                            throw new IllegalArgumentException("Imported document '" + resolved.source() + "' may contain definitions only, but found <Root>");
                        }
                        for (Element imp : parts.imports()) {
                            this.parseImport(imp, registry, rawConstants, resolved.source(), importChain);
                        }
                        for (Element constElem : parts.constants()) {
                            this.parseConstantElement(constElem, rawConstants, "");
                        }
                        if (parts.definitions() != null) {
                            this.parseDefinitions(parts.definitions(), registry, rawConstants, resolved.source(), importChain);
                        }
                    }
                    default -> throw new IllegalArgumentException("Imported document '" + resolved.source() + "' must have <RivetUi>, <Definitions>, <Constants>, or <Map> as root element");
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


    private record DocumentParts(Element definitions, Element root, List<Element> constants, List<Element> imports) {
        private static DocumentParts of(final Element rivetUiElement) {
            Element definitions = null;
            Element root = null;
            List<Element> constants = new ArrayList<>();
            List<Element> imports = new ArrayList<>();
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
                    case "Constant", "Constants", "Map" -> constants.add(child);
                    case "Import", "Include" -> imports.add(child);
                    default -> throw new IllegalArgumentException("Unknown <RivetUi> child: " + child.getNodeName());
                }
            }
            return new DocumentParts(definitions, root, constants, imports);
        }
    }

}
