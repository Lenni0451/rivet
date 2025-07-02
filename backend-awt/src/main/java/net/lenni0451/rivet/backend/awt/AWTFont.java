package net.lenni0451.rivet.backend.awt;

import net.lenni0451.rivet.backend.text.Font;

import java.awt.image.BufferedImage;

public class AWTFont implements Font {

    private final java.awt.Font font;
    private final float height;

    public AWTFont(java.awt.Font font) {
        this.font = font;
        this.height = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics().getFontMetrics(this.font).getHeight();
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
