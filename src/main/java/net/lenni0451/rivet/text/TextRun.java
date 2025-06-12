package net.lenni0451.rivet.text;

import net.lenni0451.commons.collections.Lists;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Font;

import java.util.ArrayList;
import java.util.List;

public record TextRun(Font font, List<TextSegment> segments, float xOffset, float yOffset) {

    public static TextRun fromString(final Font font, final String text) {
        return fromString(font, text, Color.WHITE);
    }

    public static TextRun fromString(final Font font, final String text, final Color color) {
        return fromString(font, text, color, 0);
    }

    public static TextRun fromString(final Font font, final String text, final Color color, final int styleFlags) {
        return new TextRun(font, new TextSegment(text, color, styleFlags));
    }

    public TextRun(final Font font, final List<TextSegment> segments) {
        this(font, segments, 0F, 0F);
    }

    public TextRun(final Font font, float xOffset, float yOffset) {
        this(font, new ArrayList<>(), xOffset, yOffset);
    }

    public TextRun(final Font font, final TextSegment... segment) {
        this(font, Lists.arrayList(segment));
    }

    public TextRun addSegment(final TextSegment segment) {
        this.segments.add(segment);
        return this;
    }

    public TextRun add(final TextSegment segment) {
        return this.addSegment(segment);
    }

}
