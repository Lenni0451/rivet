package net.lenni0451.rivet.backend.awt.text;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.awt.render.AWTRenderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextLine;
import net.lenni0451.rivet.text.model.TextSection;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true, chain = true, makeFinal = true)
public class AWTFont implements Font {

    public static final FontRenderContext CONTEXT = new FontRenderContext(null, true, true);

    private final java.awt.Font font;
    private final List<java.awt.Font> fonts;
    private final LineMetrics lineMetrics;

    public AWTFont(final java.awt.Font font) {
        this(List.of(font));
    }

    public AWTFont(final List<java.awt.Font> fonts) {
        if (fonts.isEmpty()) throw new IllegalArgumentException("Fonts list must not be empty");
        this.fonts = List.copyOf(fonts);
        this.font = this.fonts.get(0);
        this.lineMetrics = this.font.getLineMetrics("", CONTEXT);
    }

    @Override
    public int size() {
        return this.font.getSize();
    }

    @Override
    public float height() {
        return this.lineMetrics.getHeight();
    }

    @Override
    public Font derive(final int size) {
        List<java.awt.Font> derivedFonts = new ArrayList<>();
        for (java.awt.Font font : this.fonts) {
            derivedFonts.add(font.deriveFont((float) size));
        }
        return new AWTFont(derivedFonts);
    }

    @Override
    public ShapedText shapeText(final String text, final Color color) {
        return this.shapeText(new TextLine(new TextSection(text, TextFormat.DEFAULT.withColor(color))));
    }

    @Override
    public ShapedText shapeText(final TextLine line) {
        float minVisualX = Float.POSITIVE_INFINITY;
        float minVisualY = Float.POSITIVE_INFINITY;
        float maxVisualX = Float.NEGATIVE_INFINITY;
        float maxVisualY = Float.NEGATIVE_INFINITY;
        float minLogicalY = -this.lineMetrics.getAscent();
        float maxLogicalY = this.lineMetrics.getDescent() + this.lineMetrics.getLeading();

        float currentX = 0;
        List<AWTShapedText.Section> shapedSections = new ArrayList<>();

        for (TextSection section : line.sections()) {
            TextFormat format = section.format();
            int style = java.awt.Font.PLAIN;
            if (format.bold()) style |= java.awt.Font.BOLD;
            if (format.italic()) style |= java.awt.Font.ITALIC;

            List<FontRun> runs = this.splitIntoRuns(section.text(), style);
            for (FontRun run : runs) {
                java.awt.Font sectionFont = run.font();
                String runText = run.text();

                GlyphVector glyphVector = sectionFont.createGlyphVector(CONTEXT, runText);
                LineMetrics sectionLineMetrics = sectionFont.getLineMetrics(runText.isEmpty() ? " " : runText, CONTEXT);
                float advance = (float) glyphVector.getLogicalBounds().getWidth();

                float outlineWidth = format.outlineColor().getAlpha() > 0 ? AWTRenderer.OUTLINE_WIDTH_FACTOR * sectionFont.getSize() : 0;
                float outlineOffset = outlineWidth / 2;
                float shadowOffset = format.shadow() ? AWTRenderer.SHADOW_OFFSET_FACTOR * sectionFont.getSize() : 0;

                Shape outlineShape = null;
                Stroke outlineStroke = null;
                if (glyphVector.getNumGlyphs() > 0) {
                    if (format.outlineColor().getAlpha() > 0) {
                        outlineShape = glyphVector.getOutline();
                        outlineStroke = new BasicStroke(outlineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                    }

                    Rectangle2D visualBounds = glyphVector.getVisualBounds();
                    if (visualBounds.getWidth() > 0 && visualBounds.getHeight() > 0) {
                        float minX = currentX + (float) visualBounds.getX() - outlineOffset;
                        float minY = (float) visualBounds.getY() - outlineOffset;
                        float maxX = currentX + (float) (visualBounds.getX() + visualBounds.getWidth()) + shadowOffset + outlineOffset;
                        float maxY = (float) (visualBounds.getY() + visualBounds.getHeight()) + shadowOffset + outlineOffset;

                        minVisualX = Math.min(minVisualX, minX);
                        minVisualY = Math.min(minVisualY, minY);
                        maxVisualX = Math.max(maxVisualX, maxX);
                        maxVisualY = Math.max(maxVisualY, maxY);
                    }
                }
                if (advance > 0) {
                    if (format.underlined()) {
                        float y = sectionLineMetrics.getUnderlineOffset();
                        float thickness = sectionLineMetrics.getUnderlineThickness() * (format.bold() ? 1.5F : 1F);
                        minVisualX = Math.min(minVisualX, currentX - outlineOffset);
                        minVisualY = Math.min(minVisualY, y - thickness / 2 - outlineOffset);
                        maxVisualX = Math.max(maxVisualX, currentX + advance + shadowOffset + outlineOffset);
                        maxVisualY = Math.max(maxVisualY, y + thickness / 2 + shadowOffset + outlineOffset);
                    }
                    if (format.strikethrough()) {
                        float y = sectionLineMetrics.getStrikethroughOffset();
                        float thickness = sectionLineMetrics.getStrikethroughThickness() * (format.bold() ? 1.5F : 1F);
                        minVisualX = Math.min(minVisualX, currentX - outlineOffset);
                        minVisualY = Math.min(minVisualY, y - thickness / 2 - outlineOffset);
                        maxVisualX = Math.max(maxVisualX, currentX + advance + shadowOffset + outlineOffset);
                        maxVisualY = Math.max(maxVisualY, y + thickness / 2 + shadowOffset + outlineOffset);
                    }
                }

                minLogicalY = Math.min(minLogicalY, -sectionLineMetrics.getAscent());
                maxLogicalY = Math.max(maxLogicalY, sectionLineMetrics.getDescent() + sectionLineMetrics.getLeading());
                shapedSections.add(new AWTShapedText.Section(
                        sectionFont,
                        format,
                        glyphVector,
                        sectionLineMetrics,
                        outlineShape,
                        outlineStroke,
                        currentX,
                        advance
                ));
                currentX += advance;
            }
        }

        Rectangle visualBounds;
        if (minVisualX <= maxVisualX && minVisualY <= maxVisualY) {
            visualBounds = new Rectangle(minVisualX, minVisualY, maxVisualX - minVisualX, maxVisualY - minVisualY);
        } else {
            visualBounds = new Rectangle(0, 0, 0, 0);
        }
        Rectangle logicalBounds = new Rectangle(0, minLogicalY, currentX, maxLogicalY - minLogicalY);

        return new AWTShapedText(shapedSections, visualBounds, logicalBounds);
    }

    private List<FontRun> splitIntoRuns(final String text, final int style) {
        if (this.fonts.size() <= 1 || text.isEmpty()) {
            java.awt.Font sectionFont = (style == this.font.getStyle()) ? this.font : this.font.deriveFont(style);
            return List.of(new FontRun(sectionFont, text));
        }

        List<FontRun> runs = new ArrayList<>();
        int length = text.length();
        int i = 0;
        while (i < length) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            java.awt.Font matchingFont = this.findFont(codePoint, style);
            int runStart = i;
            i += charCount;
            while (i < length) {
                int nextCodePoint = text.codePointAt(i);
                if (this.findFont(nextCodePoint, style) != matchingFont) break;
                i += Character.charCount(nextCodePoint);
            }
            runs.add(new FontRun(matchingFont, text.substring(runStart, i)));
        }
        return runs;
    }

    private java.awt.Font findFont(final int codePoint, final int style) {
        for (java.awt.Font f : this.fonts) {
            if (f.canDisplay(codePoint)) {
                return (style == f.getStyle()) ? f : f.deriveFont(style);
            }
        }
        return (style == this.font.getStyle()) ? this.font : this.font.deriveFont(style);
    }


    private record FontRun(java.awt.Font font, String text) {
    }

}
