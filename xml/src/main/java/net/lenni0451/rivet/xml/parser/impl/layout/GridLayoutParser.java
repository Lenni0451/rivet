package net.lenni0451.rivet.xml.parser.impl.layout;

import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class GridLayoutParser implements XmlParser<GridLayout> {

    private final XmlParser<GridLayout> parser = ElementParserBuilder.create(() -> GridLayout.DEFAULT)
            .mapAttribute("gap", int.class, (layout, gap) -> layout.withHorizontalGap(gap).withVerticalGap(gap))
            .mapAttribute("horizontalGap", int.class, GridLayout::withHorizontalGap)
            .mapAttribute("verticalGap", int.class, GridLayout::withVerticalGap)
            .mapAttribute("homogeneousColumns", boolean.class, GridLayout::withHomogeneousColumns)
            .mapAttribute("homogeneousRows", boolean.class, GridLayout::withHomogeneousRows)
            .mapAttribute("shrinkColumns", boolean.class, GridLayout::withShrinkColumns)
            .mapAttribute("shrinkRows", boolean.class, GridLayout::withShrinkRows)
            .build();

    @Override
    public GridLayout parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
