package net.lenni0451.rivet.text.highlight;

import net.lenni0451.rivet.text.model.TextBlock;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextLine;
import net.lenni0451.rivet.text.model.TextSection;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface SyntaxHighlighter {

    SyntaxHighlighter NONE = SyntaxHighlighter::defaultHighlight;


    TextBlock highlight(final String text, final TextFormat defaultFormat);

    static TextBlock defaultHighlight(final String text, final TextFormat defaultFormat) {
        String[] lines = text.split("\n", -1);
        List<TextLine> textLines = new ArrayList<>(lines.length);
        for (String line : lines) {
            textLines.add(new TextLine(new TextSection(line, defaultFormat)));
        }
        return new TextBlock(textLines);
    }

}
