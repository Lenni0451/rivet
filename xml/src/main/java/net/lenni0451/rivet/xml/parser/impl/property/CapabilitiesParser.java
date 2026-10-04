package net.lenni0451.rivet.xml.parser.impl.property;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class CapabilitiesParser implements XmlParser<Component.Capabilities> {

    private final XmlParser<Component.Capabilities> parser = ElementParserBuilder.create(Component.Capabilities::new)
            .attribute("keyboardInput", Boolean.class, Component.Capabilities::keyboardInput)
            .attribute("mouseInput", Boolean.class, Component.Capabilities::mouseInput)
            .attribute("mouseHover", Boolean.class, Component.Capabilities::mouseHover)
            .attribute("dragAndDrop", Boolean.class, Component.Capabilities::dragAndDrop)
            .build();

    @Override
    public Component.Capabilities parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
