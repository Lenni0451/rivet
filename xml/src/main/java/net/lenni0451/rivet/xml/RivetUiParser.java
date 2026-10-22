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

    public XmlUi parse(final Element rivetUiElement, final Map<String, Object> variables, final Map<String, String> externalConstants, final String source) {
        XmlParserRegistry sessionRegistry = this.xmlParserRegistry.copy();
        DocumentParts parts = DocumentParts.of(rivetUiElement);
        if (parts.root() == null) {
            throw new IllegalArgumentException("<" + XmlConstants.TAG_RIVET_UI + "> must contain a <" + XmlConstants.TAG_ROOT + "> element");
        }

        Map<String, String> rawConstants = new LinkedHashMap<>();
        if (externalConstants != null) {
            rawConstants.putAll(externalConstants);
        }

        Set<String> importChain = new LinkedHashSet<>();
        if (source != null) {
            importChain.add(source);
        }

        this.parseDocumentParts(parts, sessionRegistry, rawConstants, source, importChain);

        Map<String, String> resolvedConstants = this.resolveAndValidateConstants(rawConstants);
        XmlUtils.substituteConstants(rivetUiElement, resolvedConstants);

        List<Element> rootChildren = XmlUtils.childElements(parts.root());
        if (rootChildren.size() != 1) {
            throw new IllegalArgumentException("<" + XmlConstants.TAG_ROOT + "> must contain exactly one component, but found " + rootChildren.size());
        }

        XmlParserContext context = new XmlParserContext(sessionRegistry, this.stringParserRegistry, variables, resolvedConstants);
        Component root = sessionRegistry.parse(Component.class, rootChildren.get(0), context);
        return new XmlUi(root, context.componentsById(), resolvedConstants);
    }

    private void parseDocumentParts(final DocumentParts parts, final XmlParserRegistry registry, final Map<String, String> rawConstants, final String source, final Set<String> importChain) {
        for (Element importElement : parts.imports()) {
            this.parseImport(importElement, registry, rawConstants, source, importChain);
        }
        for (Element constantElement : parts.constants()) {
            this.parseConstantElement(constantElement, rawConstants, "");
        }
        if (parts.definitions() != null) {
            this.parseDefinitions(parts.definitions(), registry, rawConstants, source, importChain);
        }
    }

    private void parseDefinitions(final Element definitionsElement, final XmlParserRegistry registry, final Map<String, String> rawConstants, final String source, final Set<String> importChain) {
        for (Element child : XmlUtils.childElements(definitionsElement)) {
            switch (child.getNodeName()) {
                case XmlConstants.TAG_COMPONENT -> this.parseComponentTemplate(child, registry);
                case XmlConstants.TAG_IMPORT, XmlConstants.TAG_INCLUDE -> this.parseImport(child, registry, rawConstants, source, importChain);
                case XmlConstants.TAG_CONSTANT -> this.parseConstant(child, rawConstants, "");
                case XmlConstants.TAG_CONSTANTS, XmlConstants.TAG_MAP -> this.parseConstantsGroup(child, rawConstants);
                default -> throw new IllegalArgumentException("Unknown <" + XmlConstants.TAG_DEFINITIONS + "> child: " + child.getNodeName());
            }
        }
    }

    private void parseConstantElement(final Element element, final Map<String, String> rawConstants, final String prefix) {
        switch (element.getNodeName()) {
            case XmlConstants.TAG_CONSTANT, XmlConstants.TAG_ENTRY -> this.parseConstant(element, rawConstants, prefix);
            case XmlConstants.TAG_CONSTANTS, XmlConstants.TAG_MAP -> this.parseConstantsGroup(element, rawConstants);
            default -> throw new IllegalArgumentException("Unknown constant element: " + element.getNodeName());
        }
    }

    private void parseConstant(final Element element, final Map<String, String> rawConstants, final String prefix) {
        String tag = element.getNodeName();
        String name = element.hasAttribute(XmlConstants.ATTRIBUTE_NAME)
                ? element.getAttribute(XmlConstants.ATTRIBUTE_NAME)
                : (element.hasAttribute(XmlConstants.ATTRIBUTE_KEY) ? element.getAttribute(XmlConstants.ATTRIBUTE_KEY) : null);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("<" + tag + "> requires a '" + XmlConstants.ATTRIBUTE_NAME + "' or '" + XmlConstants.ATTRIBUTE_KEY + "' attribute");
        }

        boolean hasValue = element.hasAttribute(XmlConstants.ATTRIBUTE_VALUE);
        boolean hasCodepoint = element.hasAttribute(XmlConstants.ATTRIBUTE_CODEPOINT);
        boolean hasHex = element.hasAttribute(XmlConstants.ATTRIBUTE_HEX);

        int count = (hasValue ? 1 : 0) + (hasCodepoint ? 1 : 0) + (hasHex ? 1 : 0);
        if (count > 1) {
            throw new IllegalArgumentException("Only one of '" + XmlConstants.ATTRIBUTE_VALUE + "', '" + XmlConstants.ATTRIBUTE_CODEPOINT + "', or '" + XmlConstants.ATTRIBUTE_HEX + "' may be specified on <" + tag + "> '" + name + "'");
        }

        String resolvedValue;
        if (hasValue) {
            resolvedValue = XmlUtils.unescapeJavaString(element.getAttribute(XmlConstants.ATTRIBUTE_VALUE));
        } else if (hasCodepoint) {
            resolvedValue = XmlUtils.parseCodepoint(element.getAttribute(XmlConstants.ATTRIBUTE_CODEPOINT));
        } else if (hasHex) {
            resolvedValue = XmlUtils.parseHexCodepoint(element.getAttribute(XmlConstants.ATTRIBUTE_HEX));
        } else {
            String text = element.getTextContent();
            if (text != null && !text.isBlank()) {
                resolvedValue = XmlUtils.unescapeJavaString(text.trim());
            } else {
                throw new IllegalArgumentException("<" + tag + "> '" + name + "' must specify '" + XmlConstants.ATTRIBUTE_VALUE + "', '" + XmlConstants.ATTRIBUTE_CODEPOINT + "', '" + XmlConstants.ATTRIBUTE_HEX + "', or text content");
            }
        }

        String fullName = prefix.isEmpty() ? name : prefix + name;
        if (rawConstants.putIfAbsent(fullName, resolvedValue) != null) {
            throw new IllegalArgumentException("Duplicate constant name: " + fullName);
        }
    }

    private void parseConstantsGroup(final Element groupElement, final Map<String, String> rawConstants) {
        String name = groupElement.hasAttribute(XmlConstants.ATTRIBUTE_NAME)
                ? groupElement.getAttribute(XmlConstants.ATTRIBUTE_NAME)
                : (groupElement.hasAttribute(XmlConstants.ATTRIBUTE_ID) ? groupElement.getAttribute(XmlConstants.ATTRIBUTE_ID) : null);
        String prefix = groupElement.hasAttribute(XmlConstants.ATTRIBUTE_PREFIX) ? groupElement.getAttribute(XmlConstants.ATTRIBUTE_PREFIX) : null;

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
            if (!XmlConstants.TAG_CONSTANT.equals(childTag) && !XmlConstants.TAG_ENTRY.equals(childTag)) {
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
            String resolvedValue = XmlUtils.resolvePlaceholders(rawValue, XmlConstants.CONSTANT_PREFIX, XmlConstants.CONSTANT_SUFFIX, referenceKey -> this.resolveConstant(referenceKey, rawConstants, resolved, resolving));
            resolved.put(key, resolvedValue);
            return resolvedValue;
        } finally {
            resolving.remove(key);
        }
    }

    private void parseImport(final Element importElement, final XmlParserRegistry registry, final Map<String, String> rawConstants, final String currentSource, final Set<String> importChain) {
        String tag = importElement.getNodeName();
        NamedNodeMap attributes = importElement.getAttributes();
        if (attributes.getLength() != 1 || !(importElement.hasAttribute(XmlConstants.ATTRIBUTE_SOURCE) || importElement.hasAttribute(XmlConstants.ATTRIBUTE_FILE))) {
            throw new IllegalArgumentException("<" + tag + "> requires exactly one '" + XmlConstants.ATTRIBUTE_SOURCE + "' or '" + XmlConstants.ATTRIBUTE_FILE + "' attribute");
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
                    case XmlConstants.TAG_DEFINITIONS -> this.parseDefinitions(documentElement, registry, rawConstants, resolved.source(), importChain);
                    case XmlConstants.TAG_CONSTANTS, XmlConstants.TAG_MAP -> this.parseConstantsGroup(documentElement, rawConstants);
                    case XmlConstants.TAG_CONSTANT -> this.parseConstant(documentElement, rawConstants, "");
                    case XmlConstants.TAG_RIVET_UI -> {
                        DocumentParts parts = DocumentParts.of(documentElement);
                        if (parts.root() != null) {
                            throw new IllegalArgumentException("Imported document '" + resolved.source() + "' may contain definitions only, but found <" + XmlConstants.TAG_ROOT + ">");
                        }
                        this.parseDocumentParts(parts, registry, rawConstants, resolved.source(), importChain);
                    }
                    default -> throw new IllegalArgumentException("Imported document '" + resolved.source() + "' must have <" + XmlConstants.TAG_RIVET_UI + ">, <" + XmlConstants.TAG_DEFINITIONS + ">, <" + XmlConstants.TAG_CONSTANTS + ">, or <" + XmlConstants.TAG_MAP + "> as root element");
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
        if (!componentElement.hasAttribute(XmlConstants.ATTRIBUTE_NAME)) {
            throw new IllegalArgumentException("<" + XmlConstants.TAG_COMPONENT + "> template definition requires a '" + XmlConstants.ATTRIBUTE_NAME + "' attribute");
        }
        String name = componentElement.getAttribute(XmlConstants.ATTRIBUTE_NAME);
        if (registry.supports(name)) {
            throw new IllegalArgumentException("Duplicate component or template name: " + name);
        }

        Map<String, TemplateComponentParser.TemplateParameter> parameters = new LinkedHashMap<>();
        Element templateElement = null;
        for (Element child : XmlUtils.childElements(componentElement)) {
            switch (child.getNodeName()) {
                case XmlConstants.TAG_PARAMETER -> {
                    if (!child.hasAttribute(XmlConstants.ATTRIBUTE_NAME)) {
                        throw new IllegalArgumentException("<" + XmlConstants.TAG_PARAMETER + "> requires a '" + XmlConstants.ATTRIBUTE_NAME + "' attribute in template <" + name + ">");
                    }
                    String parameterName = child.getAttribute(XmlConstants.ATTRIBUTE_NAME);
                    boolean required = child.hasAttribute(XmlConstants.ATTRIBUTE_REQUIRED) && Boolean.parseBoolean(child.getAttribute(XmlConstants.ATTRIBUTE_REQUIRED));
                    String defaultValue = child.hasAttribute(XmlConstants.ATTRIBUTE_DEFAULT) ? child.getAttribute(XmlConstants.ATTRIBUTE_DEFAULT) : null;
                    if (required && defaultValue != null) {
                        throw new IllegalArgumentException("Required parameter '" + parameterName + "' cannot have a default value");
                    }
                    if (parameters.putIfAbsent(parameterName, new TemplateComponentParser.TemplateParameter(required, defaultValue)) != null) {
                        throw new IllegalArgumentException("Duplicate parameter '" + parameterName + "' in template <" + name + ">");
                    }
                }
                case XmlConstants.TAG_TEMPLATE -> {
                    if (templateElement != null) {
                        throw new IllegalArgumentException("Duplicate <" + XmlConstants.TAG_TEMPLATE + "> in <" + XmlConstants.TAG_COMPONENT + " name=\"" + name + "\">");
                    }
                    templateElement = child;
                }
                default -> throw new IllegalArgumentException("Unknown <" + XmlConstants.TAG_COMPONENT + "> definition child: " + child.getNodeName());
            }
        }
        if (templateElement == null) {
            throw new IllegalArgumentException("<" + XmlConstants.TAG_COMPONENT + " name=\"" + name + "\"> is missing required <" + XmlConstants.TAG_TEMPLATE + "> element");
        }

        List<Element> templateChildren = XmlUtils.childElements(templateElement);
        if (templateChildren.size() != 1) {
            throw new IllegalArgumentException("<" + XmlConstants.TAG_TEMPLATE + "> in <" + XmlConstants.TAG_COMPONENT + " name=\"" + name + "\"> must contain exactly one component");
        }

        Element templateRoot = templateChildren.get(0);
        if (XmlConstants.TAG_SLOT.equals(templateRoot.getNodeName())) {
            throw new IllegalArgumentException("<" + XmlConstants.TAG_TEMPLATE + "> in <" + XmlConstants.TAG_COMPONENT + " name=\"" + name + "\"> root cannot be a <" + XmlConstants.TAG_SLOT + ">");
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
                    case XmlConstants.TAG_DEFINITIONS -> {
                        if (definitions != null) {
                            throw new IllegalArgumentException("Duplicate <" + XmlConstants.TAG_DEFINITIONS + "> element in <" + XmlConstants.TAG_RIVET_UI + ">");
                        }
                        definitions = child;
                    }
                    case XmlConstants.TAG_ROOT -> {
                        if (root != null) {
                            throw new IllegalArgumentException("Duplicate <" + XmlConstants.TAG_ROOT + "> element in <" + XmlConstants.TAG_RIVET_UI + ">");
                        }
                        root = child;
                    }
                    case XmlConstants.TAG_CONSTANT, XmlConstants.TAG_CONSTANTS, XmlConstants.TAG_MAP -> constants.add(child);
                    case XmlConstants.TAG_IMPORT, XmlConstants.TAG_INCLUDE -> imports.add(child);
                    default -> throw new IllegalArgumentException("Unknown <" + XmlConstants.TAG_RIVET_UI + "> child: " + child.getNodeName());
                }
            }
            return new DocumentParts(definitions, root, constants, imports);
        }
    }

}
