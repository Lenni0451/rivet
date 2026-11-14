package net.lenni0451.rivet.backend.teavm.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.backend.text.TextDecorations;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextLine;
import net.lenni0451.rivet.text.model.TextSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A font backed by the browser's font system.<br>
 * The family is a CSS font family list (e.g. {@code "'My Font', sans-serif"}), so the browser takes care of glyph fallback.
 */
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public class TeaVMFont implements Font {

    // The canvas text API does not expose decoration metrics, so they are approximated relative to the font size
    public static final float LINE_THICKNESS_FACTOR = 1 / 16F;
    public static final float UNDERLINE_OFFSET_FACTOR = 0.1F;
    public static final float STRIKETHROUGH_OFFSET_FACTOR = -0.3F;

    private static final int SHAPE_CACHE_SIZE = 1024;

    @Getter
    private final String family;
    @Getter
    private final int size;
    private final FontStyle[] styles = new FontStyle[4];
    private final Map<TextLine, TeaVMShapedText> shapeCache = new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<TextLine, TeaVMShapedText> eldest) {
            return this.size() > SHAPE_CACHE_SIZE;
        }
    };

    @Override
    public float height() {
        FontStyle style = this.style(false, false);
        return style.ascent() + style.descent();
    }

    @Override
    public Font derive(final int size) {
        return new TeaVMFont(this.family, size);
    }

    @Override
    public ShapedText shapeText(final String text, final Color color) {
        return this.shapeText(new TextLine(new TextSection(text, TextFormat.DEFAULT.withColor(color))));
    }

    @Override
    public ShapedText shapeText(final TextLine line) {
        TeaVMShapedText shapedText = this.shapeCache.get(line);
        if (shapedText == null) {
            shapedText = this.shape(line);
            this.shapeCache.put(line, shapedText);
        }
        return shapedText;
    }

    public FontStyle style(final boolean bold, final boolean italic) {
        int index = (bold ? 1 : 0) | (italic ? 2 : 0);
        FontStyle style = this.styles[index];
        if (style == null) {
            style = new FontStyle(this.family, this.size, bold, italic);
            this.styles[index] = style;
        }
        return style;
    }

    private TeaVMShapedText shape(final TextLine line) {
        FontStyle defaultStyle = this.style(false, false);
        TextDecorations.VisualBounds visualBounds = new TextDecorations.VisualBounds();
        float minLogicalY = -defaultStyle.ascent();
        float maxLogicalY = defaultStyle.descent();

        float currentX = 0;
        List<TeaVMShapedText.Section> sections = new ArrayList<>();
        for (TextSection section : line.sections()) {
            TextFormat format = section.format();
            FontStyle style = this.style(format.bold(), format.italic());
            String text = section.text();

            float[] positions = new float[text.length() + 1];
            float x = 0;
            int i = 0;
            while (i < text.length()) {
                int codePoint = text.codePointAt(i);
                int charCount = Character.charCount(codePoint);
                int nextCodePoint = i + charCount < text.length() ? text.codePointAt(i + charCount) : -1;
                positions[i] = x;
                if (charCount > 1) positions[i + 1] = x;

                float[] glyph = style.glyph(codePoint);
                if (glyph[1] + glyph[2] > 0 && glyph[3] + glyph[4] > 0) {
                    visualBounds.addGlyphs(currentX + x - glyph[1], -glyph[3], currentX + x + glyph[2], glyph[4], format, this.size);
                }
                x += style.advance(codePoint, nextCodePoint);
                i += charCount;
            }
            positions[text.length()] = x;

            if (x > 0) {
                float thickness = lineThickness(this.size, format);
                if (format.underlined()) {
                    visualBounds.addLine(currentX, x, UNDERLINE_OFFSET_FACTOR * this.size, thickness, format, this.size);
                }
                if (format.strikethrough()) {
                    visualBounds.addLine(currentX, x, STRIKETHROUGH_OFFSET_FACTOR * this.size, thickness, format, this.size);
                }
            }

            minLogicalY = Math.min(minLogicalY, -style.ascent());
            maxLogicalY = Math.max(maxLogicalY, style.descent());
            sections.add(new TeaVMShapedText.Section(style, format, text, currentX, positions, x));
            currentX += x;
        }

        return new TeaVMShapedText(sections, visualBounds.build(), new Rectangle(0, minLogicalY, currentX, maxLogicalY - minLogicalY));
    }

    public static float lineThickness(final int size, final TextFormat format) {
        return TextDecorations.lineThickness(Math.max(1, LINE_THICKNESS_FACTOR * size), format);
    }

}
