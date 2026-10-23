package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.impl.Arrow;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class ArrowParser implements XmlParser<Arrow> {

    private final XmlParser<Arrow> parser = ElementParserBuilder.createComponent(ctx -> ctx.attribute("progress", float.class)
                    .map(Arrow::new)
                    .orElseGet(Arrow::new))
            .themeOption("color", Color.class, Arrow::color)
            .themeOption("disabledColor", Color.class, Arrow::disabledColor)
            .themeOption("lineWidth", Float.class, Arrow::lineWidth)
            .themeOption("size", Float.class, Arrow::size)
            .build();

    @Override
    public Arrow parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
