package net.lenni0451.rivet.xml.parser.impl.layout;

import net.lenni0451.rivet.layout.border.*;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class BorderLayoutParser implements XmlParser<BorderLayout> {

    private final XmlParser<BorderLayout> parser = ElementParserBuilder.create(() -> BorderLayout.DEFAULT)
            .mapAttribute("topLeftPriority", TopLeftPriority.class, BorderLayout::withTopLeftPriority)
            .mapAttribute("bottomLeftPriority", BottomLeftPriority.class, BorderLayout::withBottomLeftPriority)
            .mapAttribute("bottomRightPriority", BottomRightPriority.class, BorderLayout::withBottomRightPriority)
            .mapAttribute("topRightPriority", TopRightPriority.class, BorderLayout::withTopRightPriority)
            .build();

    @Override
    public BorderLayout parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
