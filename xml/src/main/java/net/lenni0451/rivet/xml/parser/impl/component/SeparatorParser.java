package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.impl.Separator;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class SeparatorParser implements XmlParser<Separator> {

    private final XmlParser<Separator> parser = ElementParserBuilder.createComponent(ctx -> ctx.attribute("orientation", Separator.Orientation.class)
                    .map(Separator::new)
                    .orElseGet(Separator::new))
            .themeOption("color", Color.class, Separator::color)
            .themeOption("thickness", Float.class, Separator::thickness)
            .build();

    @Override
    public Separator parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
