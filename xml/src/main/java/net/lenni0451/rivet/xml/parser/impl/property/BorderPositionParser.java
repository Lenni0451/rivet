package net.lenni0451.rivet.xml.parser.impl.property;

import net.lenni0451.rivet.layout.border.BorderPosition;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.utils.XmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Optional;

public class BorderPositionParser implements XmlParser<BorderPosition> {

    @Override
    public BorderPosition parse(final XmlParserContext context, final Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            throw new IllegalArgumentException("Expected an element node");
        }
        Element element = (Element) node;
        String value = null;
        if (element.hasAttribute("position")) {
            value = element.getAttribute("position");
        } else if (element.hasAttribute("value")) {
            value = element.getAttribute("value");
        } else {
            String text = element.getTextContent();
            if (text != null && !text.isBlank()) {
                value = text.trim();
            }
        }
        if (value == null) {
            throw new IllegalArgumentException("BorderPosition must specify position/value attribute or text content");
        }
        value = context.resolveTemplateParameter(value);
        if (XmlUtils.isVariableReference(value)) {
            String variableName = XmlUtils.extractVariableReferenceName(value);
            Optional<BorderPosition> variable = context.getVariable(variableName, BorderPosition.class);
            if (variable.isPresent()) {
                return variable.get();
            }
            Optional<String> constant = context.getConstant(variableName);
            if (constant.isPresent()) {
                return context.stringParserRegistry().parse(BorderPosition.class, constant.get());
            }
            throw new IllegalArgumentException("Variable '" + variableName + "' not found or not of type " + BorderPosition.class.getTypeName());
        }
        return context.stringParserRegistry().parse(BorderPosition.class, value);
    }

}
