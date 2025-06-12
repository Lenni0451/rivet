package net.lenni0451.rivet.text;

import net.lenni0451.commons.collections.Lists;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Font;
import net.lenni0451.rivet.backend.FontSet;

import java.util.ArrayList;
import java.util.List;

public record TextBuffer(List<TextRun> runs) {

    public static TextBuffer fromString(final Font font, final String text) {
        return new TextBuffer(TextRun.fromString(font, text));
    }

    public static TextBuffer fromString(final Font font, final String text, final Color color) {
        return new TextBuffer(TextRun.fromString(font, text, color));
    }

    public static TextBuffer fromString(final Font font, final String text, final Color color, final int styleFlags) {
        return new TextBuffer(TextRun.fromString(font, text, color, styleFlags));
    }

    public static TextBuffer fromString(final FontSet fontSet, final String text) {
        return fromString(fontSet, text, Color.WHITE);
    }

    public static TextBuffer fromString(final FontSet fontSet, final String text, final Color color) {
        return fromString(fontSet, text, color, 0);
    }

    public static TextBuffer fromString(final FontSet fontSet, final String text, final Color color, final int styleFlags) {
        Font currentFont = fontSet.getMainFont();
        final StringBuilder currentText = new StringBuilder(text.length());
        final List<TextRun> runs = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            final int codePoint = text.codePointAt(i);
            if (codePoint >= Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                i++;
            }
            Font font = fontSet.getFont(codePoint);
            if (font == null) { // If the character is not supported by the font set, use the main font to display the missing glyph character
                font = fontSet.getMainFont();
            }
            if (font != currentFont) {
                if (!currentText.isEmpty()) {
                    runs.add(new TextRun(currentFont, new TextSegment(currentText.toString(), color, styleFlags)));
                    currentText.setLength(0);
                }
                currentFont = font;
            }
            currentText.appendCodePoint(codePoint);
        }
        if (!currentText.isEmpty()) {
            runs.add(new TextRun(currentFont, new TextSegment(currentText.toString(), color, styleFlags)));
        }
        return new TextBuffer(runs);
    }

    public TextBuffer(final TextRun... run) {
        this(Lists.arrayList(run));
    }

    public TextBuffer addRun(final TextRun run) {
        this.runs.add(run);
        return this;
    }

    public TextBuffer add(final TextRun run) {
        return this.addRun(run);
    }

}
