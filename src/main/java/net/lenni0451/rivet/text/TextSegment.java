package net.lenni0451.rivet.text;

import net.lenni0451.commons.color.Color;

public record TextSegment(String text, Color color, int styleFlags, Color outlineColor, float xVisualOffset, float yVisualOffset) {

    public static final int STYLE_SHADOW_BIT = 1 << 0;
    public static final int STYLE_BOLD_BIT = 1 << 1;
    public static final int STYLE_ITALIC_BIT = 1 << 2;
    public static final int STYLE_UNDERLINE_BIT = 1 << 3;
    public static final int STYLE_STRIKETHROUGH_BIT = 1 << 4;

    public static int buildStyleFlags(final boolean shadow, final boolean bold, final boolean italic, final boolean underline, final boolean strikethrough) {
        int flags = 0;
        if (shadow) {
            flags |= STYLE_SHADOW_BIT;
        }
        if (bold) {
            flags |= STYLE_BOLD_BIT;
        }
        if (italic) {
            flags |= STYLE_ITALIC_BIT;
        }
        if (underline) {
            flags |= STYLE_UNDERLINE_BIT;
        }
        if (strikethrough) {
            flags |= STYLE_STRIKETHROUGH_BIT;
        }
        return flags;
    }

    public TextSegment(final String text, final Color color, final int styleFlags, final Color outlineColor) {
        this(text, color, styleFlags, outlineColor, 0F, 0F);
    }

    public TextSegment(final String text, final Color color, final int styleFlags) {
        this(text, color, styleFlags, Color.TRANSPARENT);
    }

    public TextSegment(final String text, final Color color) {
        this(text, color, 0);
    }

    public TextSegment(final String text) {
        this(text, Color.WHITE);
    }

}
