package net.lenni0451.rivet.xml.parser.impl.layout;

import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class FullSizeLayoutParser implements XmlParser<FullSizeLayout> {

    private final XmlParser<FullSizeLayout> parser = ElementParserBuilder.create(() -> FullSizeLayout.INSTANCE).build();

    @Override
    public FullSizeLayout parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
