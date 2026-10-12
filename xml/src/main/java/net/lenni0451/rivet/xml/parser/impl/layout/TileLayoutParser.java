package net.lenni0451.rivet.xml.parser.impl.layout;

import net.lenni0451.rivet.layout.tile.TileLayout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class TileLayoutParser implements XmlParser<TileLayout> {

    private final XmlParser<TileLayout> parser = ElementParserBuilder.create(ctx -> {
                int columns = ctx.requireAttribute("columns", int.class);
                int rows = ctx.requireAttribute("rows", int.class);
                int gap = ctx.attributeOrDefault("gap", int.class, 0);
                int horizontalGap = ctx.attributeOrDefault("horizontalGap", int.class, gap);
                int verticalGap = ctx.attributeOrDefault("verticalGap", int.class, gap);
                return new TileLayout(columns, rows, horizontalGap, verticalGap);
            })
            .mapAttribute("columns", int.class, TileLayout::withColumns)
            .mapAttribute("rows", int.class, TileLayout::withRows)
            .mapAttribute("gap", int.class, (layout, gap) -> layout.withHorizontalGap(gap).withVerticalGap(gap))
            .mapAttribute("horizontalGap", int.class, TileLayout::withHorizontalGap)
            .mapAttribute("verticalGap", int.class, TileLayout::withVerticalGap)
            .build();

    @Override
    public TileLayout parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
