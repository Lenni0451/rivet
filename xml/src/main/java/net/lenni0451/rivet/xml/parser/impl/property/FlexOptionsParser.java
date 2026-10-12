package net.lenni0451.rivet.xml.parser.impl.property;

import net.lenni0451.rivet.layout.flex.FlexAlignSelf;
import net.lenni0451.rivet.layout.flex.FlexOptions;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class FlexOptionsParser implements XmlParser<FlexOptions> {

    private final XmlParser<FlexOptions> parser = ElementParserBuilder.create(() -> FlexOptions.DEFAULT)
            .mapAttribute("order", int.class, FlexOptions::withOrder)
            .mapAttribute("grow", int.class, FlexOptions::withGrow)
            .mapAttribute("shrink", int.class, FlexOptions::withShrink)
            .mapAttribute("basis", int.class, FlexOptions::withBasis)
            .mapAttribute("align", FlexAlignSelf.class, FlexOptions::withAlign)
            .build();

    @Override
    public FlexOptions parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
