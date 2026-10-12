package net.lenni0451.rivet.xml.parser.impl.layout;

import net.lenni0451.rivet.layout.flow.HorizontalFlowLayout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class HorizontalFlowLayoutParser implements XmlParser<HorizontalFlowLayout> {

    private final XmlParser<HorizontalFlowLayout> parser = ElementParserBuilder.create(() -> HorizontalFlowLayout.DEFAULT)
            .mapAttribute("gap", int.class, (layout, gap) -> layout.withHorizontalGap(gap).withVerticalGap(gap))
            .mapAttribute("horizontalGap", int.class, HorizontalFlowLayout::withHorizontalGap)
            .mapAttribute("verticalGap", int.class, HorizontalFlowLayout::withVerticalGap)
            .build();

    @Override
    public HorizontalFlowLayout parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
