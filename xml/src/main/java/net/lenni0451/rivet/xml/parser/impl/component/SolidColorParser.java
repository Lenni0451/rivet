package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.impl.SolidColor;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class SolidColorParser implements XmlParser<SolidColor> {

    private final XmlParser<SolidColor> parser = ElementParserBuilder.createComponent(ctx -> ctx.attribute("color", Color.class)
                    .map(SolidColor::new)
                    .orElseGet(SolidColor::new))
            .attribute("color", Color.class, SolidColor::color)
            .attribute("outlineColor", Color.class, SolidColor::outlineColor)
            .attribute("outlineWidth", float.class, SolidColor::outlineWidth)
            .attribute("cornerRadius", Corners.class, SolidColor::cornerRadius)
            .build();

    @Override
    public SolidColor parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
