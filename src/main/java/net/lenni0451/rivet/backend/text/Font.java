package net.lenni0451.rivet.backend.text;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.text.model.TextBlock;
import net.lenni0451.rivet.text.model.TextLine;
import net.lenni0451.rivet.text.model.TextSection;

public interface Font {

    float height();

    Font derive(final int size);

    ShapedText shapeText(final String text, final Color color);

    default ShapedText shapeText(final TextSection section) {
        return this.shapeText(new TextLine(section));
    }

    ShapedText shapeText(final TextLine line);

    ShapedTextBlock shapeText(final TextBlock block);

}
