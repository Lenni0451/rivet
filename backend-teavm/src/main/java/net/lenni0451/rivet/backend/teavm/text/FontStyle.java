package net.lenni0451.rivet.backend.teavm.text;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.canvas.TextMetrics;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;

import java.util.HashMap;
import java.util.Map;

/**
 * Metrics of a font family in a specific size and style, measured using the browser's canvas text API.<br>
 * All measurements are cached since {@code measureText} is comparatively expensive.
 */
@Accessors(fluent = true, chain = true, makeFinal = true)
public final class FontStyle {

    private static CanvasRenderingContext2D measureContext;
    private static String currentFont;

    @Getter
    private final String family;
    @Getter
    private final int size;
    @Getter
    private final boolean bold;
    @Getter
    private final boolean italic;
    @Getter
    private final String css;
    @Getter
    private final float ascent;
    @Getter
    private final float descent;
    private final Map<Integer, float[]> glyphMetrics = new HashMap<>();
    private final Map<String, Float> pairWidths = new HashMap<>();

    FontStyle(final String family, final int size, final boolean bold, final boolean italic) {
        this.family = family;
        this.size = size;
        this.bold = bold;
        this.italic = italic;
        this.css = this.css(size);
        TextMetrics metrics = this.measure("Hg");
        float ascent = (float) metrics.getFontBoundingBoxAscent();
        float descent = (float) metrics.getFontBoundingBoxDescent();
        // fontBoundingBox* is not available in some older browsers
        this.ascent = ascent > 0 ? ascent : size * 0.8F;
        this.descent = descent > 0 ? descent : size * 0.2F;
    }

    private static CanvasRenderingContext2D context() {
        if (measureContext == null) {
            HTMLCanvasElement canvas = (HTMLCanvasElement) HTMLDocument.current().createElement("canvas");
            canvas.setWidth(1);
            canvas.setHeight(1);
            measureContext = (CanvasRenderingContext2D) canvas.getContext("2d");
        }
        return measureContext;
    }

    public String css(final int pixelSize) {
        StringBuilder out = new StringBuilder();
        if (this.italic) out.append("italic ");
        if (this.bold) out.append("bold ");
        return out.append(pixelSize).append("px ").append(this.family).toString();
    }

    public float[] glyph(final int codePoint) {
        float[] metrics = this.glyphMetrics.get(codePoint);
        if (metrics == null) {
            TextMetrics textMetrics = this.measure(new String(Character.toChars(codePoint)));
            metrics = new float[]{
                    (float) textMetrics.getWidth(),
                    (float) textMetrics.getActualBoundingBoxLeft(),
                    (float) textMetrics.getActualBoundingBoxRight(),
                    (float) textMetrics.getActualBoundingBoxAscent(),
                    (float) textMetrics.getActualBoundingBoxDescent()
            };
            this.glyphMetrics.put(codePoint, metrics);
        }
        return metrics;
    }

    public float advance(final int codePoint, final int nextCodePoint) {
        if (nextCodePoint < 0) return this.glyph(codePoint)[0];
        String pair = new StringBuilder(4).appendCodePoint(codePoint).appendCodePoint(nextCodePoint).toString();
        Float pairWidth = this.pairWidths.get(pair);
        if (pairWidth == null) {
            pairWidth = (float) this.measure(pair).getWidth();
            this.pairWidths.put(pair, pairWidth);
        }
        return pairWidth - this.glyph(nextCodePoint)[0];
    }

    private TextMetrics measure(final String text) {
        CanvasRenderingContext2D context = context();
        if (!this.css.equals(currentFont)) {
            context.setFont(this.css);
            currentFont = this.css;
        }
        return context.measureText(text);
    }

}
