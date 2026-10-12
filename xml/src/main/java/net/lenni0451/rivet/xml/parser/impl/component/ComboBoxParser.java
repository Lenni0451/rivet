package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.ComboBox;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

import java.util.List;

public class ComboBoxParser implements XmlParser<ComboBox> {

    private final XmlParser<ComboBox> parser = ElementParserBuilder.createComponent(ctx -> {
                Object text = ctx.componentOrStringAttribute("text").orElse(null);
                Component child = ctx.referenceAttribute("child", Component.class).orElse(null);
                List<Component> children = ctx.children(Component.class);
                if (children.isEmpty()) {
                    if (text == null || child == null) {
                        throw new IllegalArgumentException("ComboBox must have a text and child component");
                    }
                } else if (children.size() == 1) {
                    if (text == null) text = children.get(0);
                    else if (child == null) child = children.get(0);
                    else throw new IllegalArgumentException("ComboBox has too many children, only text and child are allowed");

                    if (text == null || child == null) {
                        throw new IllegalArgumentException("ComboBox must have a text and child component");
                    }
                } else if (children.size() == 2) {
                    if (text != null || child != null) {
                        throw new IllegalArgumentException("ComboBox has too many children, only text and child are allowed");
                    }
                    text = children.get(0);
                    child = children.get(1);
                } else {
                    throw new IllegalArgumentException("ComboBox has too many children, only text and child are allowed");
                }
                if (text instanceof Component textComponent) {
                    return new ComboBox(textComponent, child);
                } else {
                    return new ComboBox((String) text, child);
                }
            })
            .attribute("open", boolean.class, ComboBox::open)
            .themeOption("arrowColor", Color.class, ComboBox::arrowColor)
            .themeOption("arrowDisabledColor", Color.class, ComboBox::arrowDisabledColor)
            .themeOption("arrowLineWidth", Float.class, ComboBox::arrowLineWidth)
            .themeOption("arrowSize", Float.class, ComboBox::arrowSize)
            .themeOption("maxPopupHeight", Float.class, ComboBox::maxPopupHeight)
            .themeOption("interceptOutsideClicks", Boolean.class, ComboBox::interceptOutsideClicks)
            .build();

    @Override
    public ComboBox parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
