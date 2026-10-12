package net.lenni0451.rivet.xml.parser.impl.property;

import net.lenni0451.rivet.layout.grid.GridAnchor;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridOptions;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class GridOptionsParser implements XmlParser<GridOptions> {

    private final XmlParser<GridOptions> parser = ElementParserBuilder.create(() -> GridOptions.EMPTY)
            .mapAttribute("column", int.class, GridOptions::withColumn)
            .mapAttribute("row", int.class, GridOptions::withRow)
            .mapAttribute("columnSpan", int.class, GridOptions::withColumnSpan)
            .mapAttribute("rowSpan", int.class, GridOptions::withRowSpan)
            .mapAttribute("weightX", float.class, GridOptions::withWeightX)
            .mapAttribute("weightY", float.class, GridOptions::withWeightY)
            .mapAttribute("anchor", GridAnchor.class, GridOptions::withAnchor)
            .mapAttribute("fill", GridFill.class, GridOptions::withFill)
            .mapAttribute("padding", Padding.class, GridOptions::withPadding)
            .build();

    @Override
    public GridOptions parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
