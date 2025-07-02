package net.lenni0451.rivet.backend.awt;

import lombok.SneakyThrows;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedTextBuffer;
import net.lenni0451.rivet.text.TextBuffer;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

public class AWTBackend implements Backend {

    private final Thread renderThread = Thread.currentThread();
    private Graphics2DRenderer renderer;

    public AWTBackend(final Graphics2DRenderer renderer) {
        this.renderer = renderer;
    }

    public void setRenderer(final Graphics2DRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public boolean isOnRenderThread() {
        return Thread.currentThread() == this.renderThread;
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
    @SneakyThrows
    public Texture loadTexture(byte[] pngData) {
        return new AWTTexture(ImageIO.read(new ByteArrayInputStream(pngData)));
    }

    @Override
    public ShapedTextBuffer shapeTextBuffer(TextBuffer textBuffer) {
        return new AWTShapedTextBuffer(textBuffer);
    }

}
