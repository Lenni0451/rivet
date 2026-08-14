package net.lenni0451.rivet.backend;

import net.lenni0451.rivet.backend.text.Font;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface AssetLoader {

    Font loadFont(final InputStream inputStream, final int size) throws IOException;

    default Font loadFont(final byte[] data, final int size) throws IOException {
        return this.loadFont(new ByteArrayInputStream(data), size);
    }

    Texture loadTexture(final InputStream inputStream) throws IOException;

    default Texture loadTexture(final byte[] data) throws IOException {
        return this.loadTexture(new ByteArrayInputStream(data));
    }

}
