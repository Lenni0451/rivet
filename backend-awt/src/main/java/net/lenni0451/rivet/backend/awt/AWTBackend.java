package net.lenni0451.rivet.backend.awt;

import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedTextBuffer;
import net.lenni0451.rivet.text.TextBuffer;

public class AWTBackend implements Backend {

    private Graphics2DRenderer renderer;

    public AWTBackend(final Graphics2DRenderer renderer) {
        this.renderer = renderer;
    }

    public void setRenderer(final Graphics2DRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public Renderer getRenderer() {
        return this.renderer;
    }

    @Override
    public Font loadFont(byte[] fontData, int size) {
        try {
            java.awt.Font awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new java.io.ByteArrayInputStream(fontData));
            return new AWTFont(awtFont.deriveFont((float) size));
        } catch (Throwable t) {
            throw new IllegalArgumentException("Failed to load font", t);
        }
    }

    @Override
    public ShapedTextBuffer shapeTextBuffer(TextBuffer textBuffer) {
        return new AWTShapedTextBuffer(textBuffer);
    }

}
