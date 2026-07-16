package net.lenni0451.rivet.backend.thingl;

import net.lenni0451.rivet.backend.Texture;
import net.raphimc.thingl.gl.resource.image.texture.impl.Texture2D;
import org.joml.primitives.Rectanglei;

public record ThinGLTexture(Texture2D texture, Rectanglei view) implements Texture {

    public ThinGLTexture(final Texture2D texture) {
        this(texture, new Rectanglei(0, 0, texture.getWidth(), texture.getHeight()));
    }

    @Override
    public int width() {
        // Allow negative image bounds (e.g. flipping the image)
        return Math.abs(this.view.lengthX());
    }

    @Override
    public int height() {
        // Allow negative image bounds (e.g. flipping the image)
        return Math.abs(this.view.lengthY());
    }

    @Override
    public Texture subTexture(final int x, final int y, final int width, final int height) {
        return new ThinGLTexture(
                this.texture,
                new Rectanglei(
                        this.view.minX + x,
                        this.view.minY + y,
                        this.view.maxX - (this.view.lengthX() - (x + width)),
                        this.view.maxY - (this.view.lengthY() - (y + height))
                )
        );
    }

}
