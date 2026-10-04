package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class ContainerParser implements XmlParser<Container> {

    private final XmlParser<Container> parser = ElementParserBuilder.createComponent(ctx -> new Container(ctx.requireChild(Layout.class)))
            .child(Component.class, Container::add)
            .build();

    @Override
    public Container parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
