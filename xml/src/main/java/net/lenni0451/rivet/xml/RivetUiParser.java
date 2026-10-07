package net.lenni0451.rivet.xml;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.parser.ParserRegistry;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.XmlParserRegistry;
import net.lenni0451.rivet.xml.parser.impl.template.TemplateComponentParser;
import net.lenni0451.rivet.xml.utils.XmlUtils;
import org.w3c.dom.Element;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class RivetUiParser {

    private final XmlParserRegistry xmlParserRegistry;
    private final ParserRegistry stringParserRegistry;

    public XmlUi parse(final Element rivetUiElement, final Map<String, Object> variables) {
        XmlParserRegistry sessionRegistry = this.xmlParserRegistry.copy();
        Element definitionsElement = null;
        Element rootElement = null;
        for (Element child : XmlUtils.childElements(rivetUiElement)) {
            switch (child.getNodeName()) {
                case "Definitions" -> {
                    if (definitionsElement != null) {
                        throw new IllegalArgumentException("Duplicate <Definitions> element in <RivetUi>");
                    }
                    definitionsElement = child;
                }
                case "Root" -> {
                    if (rootElement != null) {
                        throw new IllegalArgumentException("Duplicate <Root> element in <RivetUi>");
                    }
                    rootElement = child;
                }
                default -> throw new IllegalArgumentException("Unknown <RivetUi> child: " + child.getNodeName());
            }
        }
        if (rootElement == null) {
            throw new IllegalArgumentException("<RivetUi> must contain a <Root> element");
        }
        if (definitionsElement != null) {
            this.parseDefinitions(definitionsElement, sessionRegistry);
        }
        List<Element> rootChildren = XmlUtils.childElements(rootElement);
        if (rootChildren.size() != 1) {
            throw new IllegalArgumentException("<Root> must contain exactly one component, but found " + rootChildren.size());
        }

        XmlParserContext context = new XmlParserContext(sessionRegistry, this.stringParserRegistry, variables);
        Component root = sessionRegistry.parse(Component.class, rootChildren.get(0), context);
        return new XmlUi(root, context.componentsById());
    }

    private void parseDefinitions(final Element definitionsElement, final XmlParserRegistry registry) {
        for (Element child : XmlUtils.childElements(definitionsElement)) {
            if ("Component".equals(child.getNodeName())) {
                this.parseComponentTemplate(child, registry);
            } else {
                throw new IllegalArgumentException("Unknown <Definitions> child: " + child.getNodeName());
            }
        }
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

}
