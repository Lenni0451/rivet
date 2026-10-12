package net.lenni0451.rivet.xml.parser.impl.layout;

import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class VerticalListLayoutParser implements XmlParser<VerticalListLayout> {

    private final XmlParser<VerticalListLayout> parser = ElementParserBuilder.create(() -> VerticalListLayout.DEFAULT)
            .mapAttribute("gap", int.class, VerticalListLayout::withGap)
            .mapAttribute("fullWidth", boolean.class, VerticalListLayout::withFullWidth)
            .mapAttribute("constrained", boolean.class, VerticalListLayout::withConstrained)
            .build();

    @Override
    public VerticalListLayout parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
