package net.lenni0451.rivet.backend;

import net.lenni0451.rivet.text.TextBuffer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public interface Backend {

    Renderer getRenderer();

    default Font loadFont(final Path path, final int size) throws IOException {
        return this.loadFont(Files.readAllBytes(path), size);
    }

    Font loadFont(final byte[] fontData, final int size);

    FontSet createFontSet(final List<Font> fonts);

    ShapedTextBuffer shapeTextBuffer(final TextBuffer textBuffer);

    // TODO: free() method

}
