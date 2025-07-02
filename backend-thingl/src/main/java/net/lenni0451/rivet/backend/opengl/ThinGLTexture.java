package net.lenni0451.rivet.backend.opengl;

import net.lenni0451.rivet.backend.Texture;
import net.raphimc.thingl.resource.texture.Texture2D;

public record ThinGLTexture(Texture2D texture) implements Texture {

    public ThinGLTexture(final Texture2D texture) {
        this.texture = texture;
        ThinGLBackend.CLEANER.register(this, texture::free);
    }

    @Override
    public int getWidth() {
        return this.texture.getWidth();
    }

    @Override
    public int getHeight() {
        return this.texture.getHeight();
    }

}
