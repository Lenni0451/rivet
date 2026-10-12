package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

import java.util.List;

public class TabParser implements XmlParser<TabParser.TabDefinition> {

    private final XmlParser<TabDefinition> parser = ElementParserBuilder.create(ctx -> {
        Component header = ctx.componentOrTextAttribute("title")
                .or(() -> ctx.componentOrTextAttribute("header"))
                .orElse(null);
        Component content = ctx.referenceAttribute("content", Component.class).orElse(null);
        List<Component> children = ctx.children(Component.class);
        if (children.isEmpty()) {
            if (header == null || content == null) {
                throw new IllegalArgumentException("Tab must have a title/header and content component");
            }
        } else if (children.size() == 1) {
            if (header == null) header = children.get(0);
            else if (content == null) content = children.get(0);
            else throw new IllegalArgumentException("Tab has too many children, only header and content are allowed");

            if (header == null || content == null) {
                throw new IllegalArgumentException("Tab must have a title/header and content component");
            }
        } else if (children.size() == 2) {
            if (header != null || content != null) {
                throw new IllegalArgumentException("Tab has too many children, only header and content are allowed");
            }
            header = children.get(0);
            content = children.get(1);
        } else {
            throw new IllegalArgumentException("Tab has too many children, only header and content are allowed");
        }
        return new TabDefinition(header, content);
    }).build();

    @Override
    public TabDefinition parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }


    public record TabDefinition(Component header, Component content) {
    }

}
