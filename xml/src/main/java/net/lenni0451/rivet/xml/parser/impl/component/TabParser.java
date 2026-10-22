package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import net.lenni0451.rivet.xml.parser.impl.NodeContext;
import org.w3c.dom.Node;

public class TabParser implements XmlParser<TabParser.TabDefinition> {

    private final XmlParser<TabDefinition> parser = ElementParserBuilder.create(ctx -> {
        NodeContext.TwoSlotComponents<Component, Component> slots = ctx.resolveTwoSlots(
                ctx.componentOrTextAttribute("title").or(() -> ctx.componentOrTextAttribute("header")).orElse(null),
                ctx.referenceAttribute("content", Component.class).orElse(null),
                "Tab",
                "title/header",
                "content"
        );
        return new TabDefinition(slots.first(), slots.second());
    }).build();

    @Override
    public TabDefinition parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }


    public record TabDefinition(Component header, Component content) {
    }

}
