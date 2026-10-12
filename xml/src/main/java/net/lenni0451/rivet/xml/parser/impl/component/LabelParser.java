package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedTextBlock;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class LabelParser implements XmlParser<Label> {

    public static <T extends Label> ElementParserBuilder<T> applyLabelAttributes(final ElementParserBuilder<T> builder) {
        return builder
                .referenceAttribute("font", Font.class, Label::font)
                .referenceAttribute("textFormat", TextFormat.class, Label::textFormat)
                .attribute("scale", float.class, Label::scale)
                .attribute("horizontalOrigin", TextOrigin.Horizontal.class, Label::horizontalOrigin)
                .attribute("verticalOrigin", TextOrigin.Vertical.class, Label::verticalOrigin)
                .attribute("lineAlignment", ShapedTextBlock.LineAlignment.class, Label::lineAlignment)
                .themeOption("textColor", Color.class, Label::textColor)
                .themeOption("disabledTextColor", Color.class, Label::disabledTextColor)
                .themeOption("overflowBehavior", Label.OverflowBehavior.class, Label::overflowBehavior);
    }


    private final XmlParser<Label> parser = applyLabelAttributes(
            ElementParserBuilder.createComponent(ctx -> new Label(ctx.requireAttribute("text", String.class)))
    ).build();

    @Override
    public Label parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
