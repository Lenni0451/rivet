package net.lenni0451.rivet.xml.parser.impl.layout;

import net.lenni0451.rivet.layout.list.HorizontalListLayout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class HorizontalListLayoutParser implements XmlParser<HorizontalListLayout> {

    private final XmlParser<HorizontalListLayout> parser = ElementParserBuilder.create(() -> HorizontalListLayout.DEFAULT)
            .mapAttribute("gap", int.class, HorizontalListLayout::withGap)
            .mapAttribute("fullHeight", boolean.class, HorizontalListLayout::withFullHeight)
            .mapAttribute("constrained", boolean.class, HorizontalListLayout::withConstrained)
            .build();

    @Override
    public HorizontalListLayout parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
