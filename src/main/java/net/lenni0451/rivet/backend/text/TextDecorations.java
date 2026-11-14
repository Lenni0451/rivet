package net.lenni0451.rivet.backend.text;

import lombok.experimental.UtilityClass;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.text.model.TextFormat;

@UtilityClass
public class TextDecorations {

    public static final float SHADOW_OFFSET_FACTOR = 0.075F;
    public static final float SHADOW_COLOR_MULTIPLIER = 0.25F;
    public static final float OUTLINE_WIDTH_FACTOR = 0.125F;
    public static final float BOLD_LINE_THICKNESS_MULTIPLIER = 1.5F;

    public static boolean hasOutline(final TextFormat format) {
        return format.outlineColor().getAlpha() > 0;
    }

    public static boolean hasLines(final TextFormat format) {
        return format.underlined() || format.strikethrough();
    }

    public static float outlineWidth(final TextFormat format, final float fontSize) {
        return hasOutline(format) ? OUTLINE_WIDTH_FACTOR * fontSize : 0;
    }

    public static float shadowOffset(final TextFormat format, final float fontSize) {
        return format.shadow() ? SHADOW_OFFSET_FACTOR * fontSize : 0;
    }

    public static Color shadowColor(final Color color) {
        return color.multiply(SHADOW_COLOR_MULTIPLIER);
    }

    public static float lineThickness(final float baseThickness, final TextFormat format) {
        return format.bold() ? baseThickness * BOLD_LINE_THICKNESS_MULTIPLIER : baseThickness;
    }

    public static void render(final TextFormat format, final float fontSize, final float x, final float y, final Painter painter) {
        boolean outline = hasOutline(format);
        boolean lines = hasLines(format);
        if (format.shadow()) {
            float offset = shadowOffset(format, fontSize);
            Color shadowColor = shadowColor(format.color());
            if (outline) painter.outline(x + offset, y + offset, shadowColor(format.outlineColor()));
            painter.glyphs(x + offset, y + offset, shadowColor);
            if (lines) painter.lines(x + offset, y + offset, shadowColor);
        }
        if (outline) painter.outline(x, y, format.outlineColor());
        painter.glyphs(x, y, format.color());
        if (lines) painter.lines(x, y, format.color());
    }


    public interface Painter {
        void outline(final float x, final float y, final Color color);

        void glyphs(final float x, final float y, final Color color);

        void lines(final float x, final float y, final Color color);
    }

    public static final class VisualBounds {
        private float minX = Float.POSITIVE_INFINITY;
        private float minY = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;

        public VisualBounds addGlyphs(final float minX, final float minY, final float maxX, final float maxY, final TextFormat format, final float fontSize) {
            float outlineOffset = outlineWidth(format, fontSize) / 2F;
            float shadowOffset = shadowOffset(format, fontSize);
            this.minX = Math.min(this.minX, minX - outlineOffset);
            this.minY = Math.min(this.minY, minY - outlineOffset);
            this.maxX = Math.max(this.maxX, maxX + shadowOffset + outlineOffset);
            this.maxY = Math.max(this.maxY, maxY + shadowOffset + outlineOffset);
            return this;
        }

        public VisualBounds addLine(final float x, final float width, final float centerY, final float thickness, final TextFormat format, final float fontSize) {
            return this.addGlyphs(x, centerY - thickness / 2, x + width, centerY + thickness / 2, format, fontSize);
        }

        public Rectangle build() {
            if (this.minX <= this.maxX && this.minY <= this.maxY) {
                return new Rectangle(this.minX, this.minY, this.maxX - this.minX, this.maxY - this.minY);
            }
            return new Rectangle(0, 0, 0, 0);
        }

    }

}
