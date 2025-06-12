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

    ShapedTextBuffer shapeTextBuffer(final TextBuffer textBuffer);

}
