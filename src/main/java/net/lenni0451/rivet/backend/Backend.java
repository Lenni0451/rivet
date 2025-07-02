package net.lenni0451.rivet.backend;

import net.lenni0451.rivet.text.TextBuffer;

import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Backend {

    Renderer getRenderer();

    default Font loadFont(final Path path, final int size) throws IOException {
        return this.loadFont(Files.readAllBytes(path), size);
    }

    default Font loadFont(@WillNotClose final InputStream is, final int size) throws IOException {
        return this.loadFont(is.readAllBytes(), size);
    }

    Font loadFont(final byte[] fontData, final int size);

    default Texture loadTexture(final Path path) throws IOException {
        return this.loadTexture(Files.readAllBytes(path));
    }

    default Texture loadTexture(@WillNotClose final InputStream is) throws IOException {
        return this.loadTexture(is.readAllBytes());
    }

    Texture loadTexture(final byte[] textureData);

    ShapedTextBuffer shapeTextBuffer(final TextBuffer textBuffer);

}
