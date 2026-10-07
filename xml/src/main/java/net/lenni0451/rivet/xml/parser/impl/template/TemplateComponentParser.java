package net.lenni0451.rivet.xml.parser.impl.template;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import net.lenni0451.rivet.xml.parser.impl.NodeContext;
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
    private final Element templateRootElement;
    private final XmlParser<Component> parser;

    public TemplateComponentParser(final String name, final Map<String, TemplateParameter> parameters, final Element templateRootElement) {
        this.name = name;
        this.parameters = Map.copyOf(parameters);
        this.templateRootElement = templateRootElement;

        ElementParserBuilder<Component> builder = ElementParserBuilder.create(this::instantiateTemplate);
        this.parser = ElementParserBuilder.applyComponentAttributes(builder).build();
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
            String paramName = entry.getKey();
            TemplateParameter param = entry.getValue();

            String rawValue = callerElement.hasAttribute(paramName) ? callerElement.getAttribute(paramName) : param.defaultValue();
            if (rawValue == null && param.required()) {
                throw new IllegalArgumentException("Missing required parameter '" + paramName + "' for component <" + this.name + ">");
            }
            if (rawValue != null) {
                rawValue = callerContext.context().resolveTemplateParameter(rawValue);
                parameterValues.put(paramName, rawValue);
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

}
