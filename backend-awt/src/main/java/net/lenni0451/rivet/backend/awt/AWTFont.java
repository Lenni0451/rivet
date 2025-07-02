package net.lenni0451.rivet.backend.awt;

import net.lenni0451.rivet.backend.text.Font;

import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;

public class AWTFont implements Font {

    private static final FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(), true, true);

    private final java.awt.Font font;
    private final float ascent;
    private final float descent;
    private final float height;

    public AWTFont(java.awt.Font font) {
        this.font = font;
        LineMetrics lineMetrics = this.font.getLineMetrics("", fontRenderContext);
        this.ascent = lineMetrics.getAscent();
        this.descent = lineMetrics.getDescent();
        this.height = lineMetrics.getHeight();
    }

    public java.awt.Font font() {
        return this.font;
    }

    @Override
    public boolean hasGlyph(int codePoint) {
        return this.font.canDisplay(codePoint);
    }

    @Override
    public int getSize() {
        return this.font.getSize();
    }

    @Override
    public float getAscent() {
        return this.ascent;
    }

    @Override
    public float getDescent() {
        return this.descent;
    }

    @Override
    public float getHeight() {
        return this.height;
    }

    @Override
    public String getName() {
        return this.font.getPSName();
    }

    @Override
    public String getFamily() {
        return this.font.getFamily();
    }

}
