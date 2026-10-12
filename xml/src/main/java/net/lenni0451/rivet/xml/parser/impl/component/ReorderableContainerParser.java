package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.container.ReorderableContainer;
import net.lenni0451.rivet.dragdrop.DropMarkerStrategy;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

import java.util.function.Predicate;

public class ReorderableContainerParser implements XmlParser<ReorderableContainer> {

    private final XmlParser<ReorderableContainer> parser = ElementParserBuilder.createComponent(ctx -> new ReorderableContainer(
                    ctx.requireChildOrReference("layout", Layout.class),
                    ctx.requireReferenceAttribute("strategy", DropMarkerStrategy.class),
                    ctx.requireReferenceAttribute("dropFilter", Predicate.class)
            ))
            .attribute("markerColor", Color.class, ReorderableContainer::markerColor)
            .child(Component.class, Container::add)
            .build();

    @Override
    public ReorderableContainer parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
