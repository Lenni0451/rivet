package net.lenni0451.rivet.text.diagnostic;

import lombok.With;
import net.lenni0451.commons.color.Color;

import javax.annotation.Nullable;

/**
 * A marked character range in a text.
 *
 * @param start   The start offset (inclusive)
 * @param end     The end offset (exclusive). If equal to the start, a short marker is rendered at this position
 * @param message The message shown when hovering the marked text, or null to not show a tooltip
 * @param color   The color of the line, or null to use the theme color
 * @param style   The design of the line, or null to use the theme style
 */
@With
public record TextDiagnostic(int start, int end, @Nullable String message, @Nullable Color color, @Nullable UnderlineStyle style) {

    public TextDiagnostic(final int start, final int end) {
        this(start, end, null);
    }

    public TextDiagnostic(final int start, final int end, @Nullable final String message) {
        this(start, end, message, null, null);
    }

    public TextDiagnostic {
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Invalid diagnostic range: [%d, %d)".formatted(start, end));
        }
    }

}
