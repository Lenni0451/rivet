package net.lenni0451.rivet.xml.parser.impl.layout;

import net.lenni0451.rivet.layout.dock.DockLayout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class DockLayoutParser implements XmlParser<DockLayout> {

    private final XmlParser<DockLayout> parser = ElementParserBuilder.create(() -> DockLayout.DEFAULT)
            .mapAttribute("gap", float.class, DockLayout::withGap)
            .build();

    @Override
    public DockLayout parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
