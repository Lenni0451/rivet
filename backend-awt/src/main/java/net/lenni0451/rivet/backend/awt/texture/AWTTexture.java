package net.lenni0451.rivet.backend.awt.texture;

import net.lenni0451.rivet.backend.Texture;

import java.awt.image.BufferedImage;

public record AWTTexture(BufferedImage image) implements Texture {

    @Override
    public int width() {
        return this.image.getWidth();
    }

    @Override
    public int height() {
        return this.image.getHeight();
    }

    @Override
    public Texture subTexture(final int x, final int y, final int width, final int height) {
        return new AWTTexture(this.image.getSubimage(x, y, width, height));
    }

}
