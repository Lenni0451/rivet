package net.lenni0451.rivet.xml.parser.impl.template;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.theme.ThemeKey;
import net.lenni0451.rivet.xml.XmlConstants;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import net.lenni0451.rivet.xml.parser.impl.NodeContext;
import net.lenni0451.rivet.xml.utils.XmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class TemplateComponentParser implements XmlParser<Component> {

    @Getter
    private final String name;
    @Getter
    private final Map<String, TemplateParameter> parameters;
    @Getter
    private final Map<String, TemplateThemeOption> themeOptions;
    @Getter
    private final Element templateRootElement;
    private final XmlParser<Component> parser;

    public TemplateComponentParser(final String name, final Map<String, TemplateParameter> parameters, final Element templateRootElement) {
        this(name, parameters, Map.of(), templateRootElement);
    }

    public TemplateComponentParser(final String name, final Map<String, TemplateParameter> parameters, final Map<String, TemplateThemeOption> themeOptions, final Element templateRootElement) {
        this.name = name;
        this.parameters = Map.copyOf(parameters);
        this.themeOptions = Map.copyOf(themeOptions);
        this.templateRootElement = templateRootElement;

        ElementParserBuilder<Component> builder = ElementParserBuilder.create(this::instantiateTemplate);
        this.parser = ElementParserBuilder.applyComponentAttributes(
                builder,
                attributeName -> !this.parameters.containsKey(attributeName) && !this.themeOptions.containsKey(attributeName)
        ).build();
    }

    public boolean hasDeclaredSlot(final String slotName) {
        return this.hasDeclaredSlot(this.templateRootElement, slotName);
    }

    private boolean hasDeclaredSlot(final Element parent, final String slotName) {
        for (Element child : XmlUtils.childElements(parent)) {
            if (XmlConstants.TAG_SLOT.equals(child.getNodeName())) {
                String declaredName = child.hasAttribute(XmlConstants.ATTRIBUTE_NAME)
                        ? child.getAttribute(XmlConstants.ATTRIBUTE_NAME)
                        : XmlConstants.DEFAULT_SLOT_NAME;
                if (slotName.equals(declaredName)) {
                    return true;
                }
            }
            if (this.hasDeclaredSlot(child, slotName)) {
                return true;
            }
        }
        return false;
    }

    private Component instantiateTemplate(final NodeContext callerContext) {
        Map<String, String> parameterValues = this.resolveParameters(callerContext);

        TemplateSlotExpander expander = new TemplateSlotExpander(this.name, this.templateRootElement, callerContext.context());
        Element templateClone = expander.expand(callerContext.element());

        XmlParserContext scopedContext = callerContext.context().withTemplateParameters(parameterValues);
        scopedContext.pushTemplate(this.name);
        try {
            return scopedContext.registry().parse(Component.class, templateClone, scopedContext);
        } finally {
            scopedContext.popTemplate();
        }
    }

    private Map<String, String> resolveParameters(final NodeContext callerContext) {
        Element callerElement = callerContext.element();
        Map<String, String> parameterValues = new HashMap<>();

        for (Map.Entry<String, TemplateParameter> entry : this.parameters.entrySet()) {
            String parameterName = entry.getKey();
            TemplateParameter parameter = entry.getValue();

            boolean suppliedByCaller = callerElement.hasAttribute(parameterName);
            String rawValue = suppliedByCaller ? callerElement.getAttribute(parameterName) : parameter.defaultValue();
            if (rawValue == null && parameter.required()) {
                throw new IllegalArgumentException("Missing required parameter '" + parameterName + "' for component <" + this.name + ">");
            }
            if (rawValue != null) {
                if (!suppliedByCaller) {
                    rawValue = callerContext.context().resolveConstants(rawValue);
                }
                rawValue = callerContext.context().resolveTemplateParameter(rawValue);
                parameterValues.put(parameterName, rawValue);
            }
        }
        for (Map.Entry<String, TemplateThemeOption> entry : this.themeOptions.entrySet()) {
            String optionName = entry.getKey();
            TemplateThemeOption themeOption = entry.getValue();

            if (callerElement.hasAttribute(optionName)) {
                String rawValue = callerElement.getAttribute(optionName);
                rawValue = callerContext.context().resolveTemplateParameter(rawValue);
                parameterValues.put(optionName, rawValue);
            } else {
                parameterValues.put(optionName, XmlConstants.THEME_REF_PREFIX + themeOption.themeKey().name() + XmlConstants.THEME_REF_SUFFIX);
            }
        }
        return parameterValues;
    }

    @Override
    public Component parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }


    public record TemplateParameter(boolean required, String defaultValue) {
    }

    public record TemplateThemeOption(String name, ThemeKey<?> themeKey) {
    }

}
