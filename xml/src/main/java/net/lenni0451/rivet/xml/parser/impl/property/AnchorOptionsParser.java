package net.lenni0451.rivet.xml.parser.impl.property;

import net.lenni0451.rivet.layout.anchor.AnchorOptions;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class AnchorOptionsParser implements XmlParser<AnchorOptions> {

    private final XmlParser<AnchorOptions> parser = ElementParserBuilder.create(() -> AnchorOptions.EMPTY)
            .mapAttribute("anchorMinX", float.class, AnchorOptions::withAnchorMinX)
            .mapAttribute("anchorMinY", float.class, AnchorOptions::withAnchorMinY)
            .mapAttribute("anchorMaxX", float.class, AnchorOptions::withAnchorMaxX)
            .mapAttribute("anchorMaxY", float.class, AnchorOptions::withAnchorMaxY)
            .mapAttribute("minX", float.class, AnchorOptions::withAnchorMinX)
            .mapAttribute("minY", float.class, AnchorOptions::withAnchorMinY)
            .mapAttribute("maxX", float.class, AnchorOptions::withAnchorMaxX)
            .mapAttribute("maxY", float.class, AnchorOptions::withAnchorMaxY)
            .mapAttribute("offsetLeft", float.class, AnchorOptions::withOffsetLeft)
            .mapAttribute("offsetTop", float.class, AnchorOptions::withOffsetTop)
            .mapAttribute("offsetRight", float.class, AnchorOptions::withOffsetRight)
            .mapAttribute("offsetBottom", float.class, AnchorOptions::withOffsetBottom)
            .mapAttribute("left", float.class, AnchorOptions::withOffsetLeft)
            .mapAttribute("top", float.class, AnchorOptions::withOffsetTop)
            .mapAttribute("right", float.class, AnchorOptions::withOffsetRight)
            .mapAttribute("bottom", float.class, AnchorOptions::withOffsetBottom)
            .mapAttribute("pivotX", float.class, AnchorOptions::withPivotX)
            .mapAttribute("pivotY", float.class, AnchorOptions::withPivotY)
            .build();

    @Override
    public AnchorOptions parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
