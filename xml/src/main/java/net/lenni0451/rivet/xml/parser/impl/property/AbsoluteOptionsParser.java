package net.lenni0451.rivet.xml.parser.impl.property;

import net.lenni0451.rivet.layout.absolute.AbsoluteOptions;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class AbsoluteOptionsParser implements XmlParser<AbsoluteOptions> {

    private final XmlParser<AbsoluteOptions> parser = ElementParserBuilder.create(() -> AbsoluteOptions.EMPTY)
            .mapAttribute("x", float.class, AbsoluteOptions::withX)
            .mapAttribute("y", float.class, AbsoluteOptions::withY)
            .mapAttribute("width", Float.class, AbsoluteOptions::withWidth)
            .mapAttribute("height", Float.class, AbsoluteOptions::withHeight)
            .build();

    @Override
    public AbsoluteOptions parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
