package net.lenni0451.rivet.backend.awt;

import net.lenni0451.rivet.backend.Texture;

import java.awt.image.BufferedImage;

public record AWTTexture(BufferedImage image) implements Texture {

    @Override
    public int getWidth() {
        return this.image.getWidth();
    }

    @Override
    public int getHeight() {
        return this.image.getHeight();
    }

}
