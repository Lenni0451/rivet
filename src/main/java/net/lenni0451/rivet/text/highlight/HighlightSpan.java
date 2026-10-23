package net.lenni0451.rivet.text.highlight;

import net.lenni0451.rivet.text.model.TextFormat;

public record HighlightSpan(int startOffset, int endOffset, TextFormat format) {

    public HighlightSpan {
        if (startOffset < 0 || endOffset < startOffset) {
            throw new IllegalArgumentException("Invalid span range: [%d, %d)".formatted(startOffset, endOffset));
        }
    }

}
