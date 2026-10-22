package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.ComboBox;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import net.lenni0451.rivet.xml.parser.impl.NodeContext;
import org.w3c.dom.Node;

public class ComboBoxParser implements XmlParser<ComboBox> {

    private final XmlParser<ComboBox> parser = ElementParserBuilder.createComponent(ctx -> {
                NodeContext.TwoSlotComponents<Object, Component> slots = ctx.resolveTwoSlots(
                        ctx.componentOrStringAttribute("text").orElse(null),
                        ctx.referenceAttribute("child", Component.class).orElse(null),
                        "ComboBox",
                        "text",
                        "child"
                );
                if (slots.first() instanceof Component textComponent) {
                    return new ComboBox(textComponent, slots.second());
                } else {
                    return new ComboBox((String) slots.first(), slots.second());
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
