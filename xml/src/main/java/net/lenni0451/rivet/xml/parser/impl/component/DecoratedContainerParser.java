package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.DecoratedContainer;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import net.lenni0451.rivet.xml.parser.impl.NodeContext;
import org.w3c.dom.Node;

public class DecoratedContainerParser implements XmlParser<DecoratedContainer> {

    private final XmlParser<DecoratedContainer> parser = ElementParserBuilder.createComponent(ctx -> {
                NodeContext.TwoSlotComponents<Component, Component> slots = ctx.resolveTwoSlots(
                        ctx.referenceAttribute("background", Component.class).orElse(null),
                        ctx.referenceAttribute("child", Component.class).orElse(null),
                        "DecoratedContainer",
                        "background",
                        "child"
                );
                return new DecoratedContainer(slots.first(), slots.second());
            })
            .attribute("innerPadding", Padding.class, DecoratedContainer::innerPadding)
            .build();

    @Override
    public DecoratedContainer parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
