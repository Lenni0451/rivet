package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.component.impl.Image;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class ImageParser implements XmlParser<Image> {

    private final XmlParser<Image> parser = ElementParserBuilder.createComponent(ctx -> new Image(
                    ctx.requireReferenceAttribute("texture", Texture.class)
            ))
            .attribute("color", Color.class, Image::color)
            .attribute("scaleMode", Image.ScaleMode.class, Image::scaleMode)
            .build();

    @Override
    public Image parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
