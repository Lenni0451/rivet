package net.lenni0451.rivet.xml.parser.impl.property;

import net.lenni0451.rivet.layout.tile.TileOptions;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class TileOptionsParser implements XmlParser<TileOptions> {

    private final XmlParser<TileOptions> parser = ElementParserBuilder.create(() -> new TileOptions(0, 0))
            .mapAttribute("column", int.class, TileOptions::withColumn)
            .mapAttribute("row", int.class, TileOptions::withRow)
            .build();

    @Override
    public TileOptions parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
