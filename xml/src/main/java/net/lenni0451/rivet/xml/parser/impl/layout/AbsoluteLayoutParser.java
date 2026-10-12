package net.lenni0451.rivet.xml.parser.impl.layout;

import net.lenni0451.rivet.layout.absolute.AbsoluteLayout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class AbsoluteLayoutParser implements XmlParser<AbsoluteLayout> {

    private final XmlParser<AbsoluteLayout> parser = ElementParserBuilder.create(() -> AbsoluteLayout.INSTANCE).build();

    @Override
    public AbsoluteLayout parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
