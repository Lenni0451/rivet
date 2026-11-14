package net.lenni0451.rivet.backend.teavm.texture;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.Texture;
import org.teavm.jso.webgl.WebGL2RenderingContext;
import org.teavm.jso.webgl.WebGLTexture;

@Getter
@Accessors(fluent = true, chain = true, makeFinal = true)
public class TeaVMTexture implements Texture {

    private final WebGL2RenderingContext gl;
    private final WebGLTexture glTexture;
    private final int textureWidth;
    private final int textureHeight;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final float u0;
    private final float v0;
    private final float u1;
    private final float v1;
    private final TeaVMTexture root;

    public TeaVMTexture(final WebGL2RenderingContext gl, final WebGLTexture glTexture, final int width, final int height) {
        this(gl, glTexture, width, height, 0, 0, width, height, null);
    }

    private TeaVMTexture(final WebGL2RenderingContext gl, final WebGLTexture glTexture, final int textureWidth, final int textureHeight, final int x, final int y, final int width, final int height, final TeaVMTexture root) {
        this.gl = gl;
        this.glTexture = glTexture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.u0 = (float) x / textureWidth;
        this.v0 = (float) y / textureHeight;
        this.u1 = (float) (x + width) / textureWidth;
        this.v1 = (float) (y + height) / textureHeight;
        this.root = root == null ? this : root;
    }

    @Override
    public Texture subTexture(final int x, final int y, final int width, final int height) {
        if (x < 0 || y < 0 || width < 0 || height < 0 || x + width > this.width || y + height > this.height) {
            throw new IllegalArgumentException("Sub texture is out of bounds");
        }
        return new TeaVMTexture(this.gl, this.glTexture, this.textureWidth, this.textureHeight, this.x + x, this.y + y, width, height, this.root);
    }

    @Override
    public void close() {
        if (this.root == this) {
            this.gl.deleteTexture(this.glTexture);
        }
    }

}
