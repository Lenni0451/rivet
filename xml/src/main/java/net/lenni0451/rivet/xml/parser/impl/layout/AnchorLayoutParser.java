package net.lenni0451.rivet.xml.parser.impl.layout;

import net.lenni0451.rivet.layout.anchor.AnchorLayout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class AnchorLayoutParser implements XmlParser<AnchorLayout> {

    private final XmlParser<AnchorLayout> parser = ElementParserBuilder.create(() -> AnchorLayout.INSTANCE).build();

    @Override
    public AnchorLayout parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
