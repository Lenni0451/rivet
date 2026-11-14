package net.lenni0451.rivet.backend.teavm;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.backend.AssetLoader;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.teavm.text.TeaVMFont;
import net.lenni0451.rivet.backend.teavm.texture.TeaVMTexture;
import net.lenni0451.rivet.backend.teavm.utils.JsUtils;
import org.teavm.jso.typedarrays.Int8Array;
import org.teavm.jso.webgl.WebGL2RenderingContext;
import org.teavm.jso.webgl.WebGLRenderingContext;
import org.teavm.jso.webgl.WebGLTexture;

import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class TeaVMAssetLoader implements AssetLoader {

    private static final String FALLBACK_FAMILY = "sans-serif";
    private static int fontCounter;

    private final WebGL2RenderingContext gl;

    public static byte[] fetch(final String url) {
        JsUtils.Response response = JsUtils.fetch(url).await();
        if (!response.isOk()) {
            throw new IllegalStateException("HTTP " + response.getStatus() + " for " + url);
        }
        return new Int8Array(response.arrayBuffer().await()).copyToJavaArray();
    }

    public static TeaVMFont systemFont(final String family, final int size) {
        return new TeaVMFont(family, size);
    }

    @Override
    public TeaVMFont loadFont(final int size, @WillNotClose final InputStream... inputStreams) throws IOException {
        byte[][] data = new byte[inputStreams.length][];
        for (int i = 0; i < inputStreams.length; i++) {
            data[i] = inputStreams[i].readAllBytes();
        }
        return this.loadFont(size, data);
    }

    @Override
    public TeaVMFont loadFont(final int size, final byte[]... data) {
        StringBuilder family = new StringBuilder();
        for (byte[] fontData : data) {
            String name = "rivet-font-" + fontCounter++;
            JsUtils.loadFontFace(name, Int8Array.copyFromJavaArray(fontData)).await();
            family.append('\'').append(name).append("', ");
        }
        family.append(FALLBACK_FAMILY);
        return new TeaVMFont(family.toString(), size);
    }

    @Override
    public Texture loadTexture(@WillNotClose final InputStream inputStream) throws IOException {
        return this.loadTexture(inputStream.readAllBytes());
    }

    @Override
    public Texture loadTexture(final byte[] data) {
        JsUtils.ImageBitmap image = JsUtils.createImageBitmap(Int8Array.copyFromJavaArray(data)).await();
        try {
            return this.uploadTexture(image);
        } finally {
            image.close();
        }
    }

    public TeaVMTexture uploadTexture(final JsUtils.TexImageSource image) {
        WebGLTexture texture = this.gl.createTexture();
        this.gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, texture);
        this.gl.pixelStorei(WebGLRenderingContext.UNPACK_PREMULTIPLY_ALPHA_WEBGL, 1);
        JsUtils.texImage2D(this.gl, WebGLRenderingContext.TEXTURE_2D, image);
        this.gl.pixelStorei(WebGLRenderingContext.UNPACK_PREMULTIPLY_ALPHA_WEBGL, 0);
        this.gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
        this.gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
        this.gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_S, WebGLRenderingContext.CLAMP_TO_EDGE);
        this.gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_T, WebGLRenderingContext.CLAMP_TO_EDGE);
        return new TeaVMTexture(this.gl, texture, image.getWidth(), image.getHeight());
    }

}
