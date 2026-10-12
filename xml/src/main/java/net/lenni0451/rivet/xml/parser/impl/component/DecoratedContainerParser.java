package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.DecoratedContainer;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

import java.util.List;

public class DecoratedContainerParser implements XmlParser<DecoratedContainer> {

    private final XmlParser<DecoratedContainer> parser = ElementParserBuilder.createComponent(ctx -> {
                Component background = ctx.referenceAttribute("background", Component.class).orElse(null);
                Component child = ctx.referenceAttribute("child", Component.class).orElse(null);
                List<Component> children = ctx.children(Component.class);
                if (children.isEmpty()) {
                    if (background == null || child == null) {
                        throw new IllegalArgumentException("DecoratedContainer must have a background and child component");
                    }
                } else if (children.size() == 1) {
                    if (background == null) background = children.get(0);
                    else if (child == null) child = children.get(0);
                    else throw new IllegalArgumentException("DecoratedContainer has too many children, only background and child are allowed");

                    if (background == null || child == null) {
                        throw new IllegalArgumentException("DecoratedContainer must have a background and child component");
                    }
                } else if (children.size() == 2) {
                    if (background != null || child != null) {
                        throw new IllegalArgumentException("DecoratedContainer has too many children, only background and child are allowed");
                    }
                    background = children.get(0);
                    child = children.get(1);
                } else {
                    throw new IllegalArgumentException("DecoratedContainer has too many children, only background and child are allowed");
                }
                return new DecoratedContainer(background, child);
            })
            .attribute("innerPadding", Padding.class, DecoratedContainer::innerPadding)
            .build();

    @Override
    public DecoratedContainer parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
