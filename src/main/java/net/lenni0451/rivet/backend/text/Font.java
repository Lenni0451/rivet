package net.lenni0451.rivet.backend.text;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.text.model.TextBlock;
import net.lenni0451.rivet.text.model.TextLine;
import net.lenni0451.rivet.text.model.TextSection;

import java.util.ArrayList;
import java.util.List;

public interface Font {

    int size();

    float height();

    Font derive(final int size);

    ShapedText shapeText(final String text, final Color color);

    default ShapedText shapeText(final TextSection section) {
        return this.shapeText(new TextLine(section));
    }

    ShapedText shapeText(final TextLine line);

    default ShapedTextBlock shapeText(final TextBlock block) {
        List<ShapedText> lines = new ArrayList<>();
        for (TextLine line : block.lines()) {
            lines.add(this.shapeText(line));
        }
        return new ShapedTextBlock(lines);
    }

}
