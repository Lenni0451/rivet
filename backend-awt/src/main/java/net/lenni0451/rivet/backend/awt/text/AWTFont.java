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
    private final LineMetrics lineMetrics;

    public AWTFont(final java.awt.Font font) {
        this.font = font;
        this.lineMetrics = font.getLineMetrics("", CONTEXT);
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
        return new AWTFont(this.font.deriveFont((float) size));
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
            java.awt.Font sectionFont = (style == this.font.getStyle()) ? this.font : this.font.deriveFont(style);

            GlyphVector glyphVector = sectionFont.createGlyphVector(CONTEXT, section.text());
            LineMetrics sectionLineMetrics = sectionFont.getLineMetrics(section.text().isEmpty() ? " " : section.text(), CONTEXT);
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

        Rectangle visualBounds;
        if (minVisualX <= maxVisualX && minVisualY <= maxVisualY) {
            visualBounds = new Rectangle(minVisualX, minVisualY, maxVisualX - minVisualX, maxVisualY - minVisualY);
        } else {
            visualBounds = new Rectangle(0, 0, 0, 0);
        }
        Rectangle logicalBounds = new Rectangle(0, minLogicalY, currentX, maxLogicalY - minLogicalY);

        return new AWTShapedText(shapedSections, visualBounds, logicalBounds);
    }

}
