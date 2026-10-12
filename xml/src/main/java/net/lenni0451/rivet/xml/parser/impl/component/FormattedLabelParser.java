package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.rivet.component.impl.FormattedLabel;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class FormattedLabelParser implements XmlParser<FormattedLabel> {

    private final XmlParser<FormattedLabel> parser = LabelParser.applyLabelAttributes(
            ElementParserBuilder.createComponent(ctx -> new FormattedLabel(ctx.requireAttribute("text", String.class)))
    ).build();

    @Override
    public FormattedLabel parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
