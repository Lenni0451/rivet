package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.PaddedContainer;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class PaddedContainerParser implements XmlParser<PaddedContainer> {

    private final XmlParser<PaddedContainer> parser = ElementParserBuilder.createComponent(ctx -> new PaddedContainer(
                    ctx.requireAttribute("padding", Padding.class),
                    ctx.requireChildOrReference("child", Component.class)
            ))
            .attribute("cropChild", boolean.class, PaddedContainer::cropChild)
            .build();

    @Override
    public PaddedContainer parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
