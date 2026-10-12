package net.lenni0451.rivet.xml.parser.impl.layout;

import net.lenni0451.rivet.layout.flow.VerticalFlowLayout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class VerticalFlowLayoutParser implements XmlParser<VerticalFlowLayout> {

    private final XmlParser<VerticalFlowLayout> parser = ElementParserBuilder.create(() -> VerticalFlowLayout.DEFAULT)
            .mapAttribute("gap", int.class, (layout, gap) -> layout.withHorizontalGap(gap).withVerticalGap(gap))
            .mapAttribute("horizontalGap", int.class, VerticalFlowLayout::withHorizontalGap)
            .mapAttribute("verticalGap", int.class, VerticalFlowLayout::withVerticalGap)
            .build();

    @Override
    public VerticalFlowLayout parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
