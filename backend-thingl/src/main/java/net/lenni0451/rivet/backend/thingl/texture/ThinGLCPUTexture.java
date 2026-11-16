package net.lenni0451.rivet.backend.thingl.texture;

import net.lenni0451.commons.lazy.Lazy;
import net.lenni0451.rivet.backend.Texture;
import net.raphimc.thingl.gl.resource.image.texture.impl.Texture2D;
import net.raphimc.thingl.resource.image.impl.ByteImage2D;
import org.joml.primitives.Rectanglei;
import org.lwjgl.opengl.GL11C;

public class ThinGLCPUTexture implements Texture {

    private final ByteImage2D texture;
    private final Rectanglei view;
    private final Lazy<Texture2D> gpuTexture;

    public ThinGLCPUTexture(final ByteImage2D texture, final int filter) {
        this(texture, filter, new Rectanglei(0, 0, texture.getWidth(), texture.getHeight()));
    }

    public ThinGLCPUTexture(final ByteImage2D texture, final int filter, final Rectanglei view) {
        this(texture, view, Lazy.of(() -> {
            Texture2D gpuTexture = Texture2D.fromImage(GL11C.GL_RGBA8, texture, false);
            gpuTexture.setFilter(filter);
            return gpuTexture;
        }));
    }

    private ThinGLCPUTexture(final ByteImage2D texture, final Rectanglei view, final Lazy<Texture2D> gpuTexture) {
        this.texture = texture;
        this.view = view;
        this.gpuTexture = gpuTexture;
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
        return new ThinGLCPUTexture(this.texture, new Rectanglei(minX, minY, minX + width, minY + height), this.gpuTexture);
    }

    @Override
    public void close() {
        this.texture.free();
        if (this.gpuTexture.isInitialized()) {
            this.uploadIfNeeded().close();
        }
    }

    public ThinGLGPUTexture uploadIfNeeded() {
        return new ThinGLGPUTexture(this.gpuTexture.get(), this.view);
    }

}
