package net.lenni0451.rivet.backend.thingl.texture;

import net.lenni0451.rivet.backend.Texture;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.resource.image.texture.impl.Texture2D;
import org.joml.primitives.Rectanglei;

public record ThinGLGPUTexture(Texture2D texture, Rectanglei view) implements Texture {

    public ThinGLGPUTexture(final Texture2D texture) {
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
        int minX = this.view.minX + x;
        int minY = this.view.minY + y;
        return new ThinGLGPUTexture(this.texture, new Rectanglei(minX, minY, minX + width, minY + height));
    }

    @Override
    public void close() {
        ThinGL.get().runOnRenderThread(this.texture::free);
    }

}
