package net.lenni0451.rivet.xml.parser.impl.layout;

import net.lenni0451.rivet.layout.flex.*;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class FlexLayoutParser implements XmlParser<FlexLayout> {

    private final XmlParser<FlexLayout> parser = ElementParserBuilder.create(() -> FlexLayout.DEFAULT)
            .mapAttribute("direction", FlexDirection.class, FlexLayout::withDirection)
            .mapAttribute("wrap", FlexWrap.class, FlexLayout::withWrap)
            .mapAttribute("justifyContent", FlexJustify.class, FlexLayout::withJustifyContent)
            .mapAttribute("alignItems", FlexAlignItems.class, FlexLayout::withAlignItems)
            .mapAttribute("alignContent", FlexAlignContent.class, FlexLayout::withAlignContent)
            .mapAttribute("rowGap", int.class, FlexLayout::withRowGap)
            .mapAttribute("columnGap", int.class, FlexLayout::withColumnGap)
            .build();

    @Override
    public FlexLayout parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
