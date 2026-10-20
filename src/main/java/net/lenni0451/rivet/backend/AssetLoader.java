package net.lenni0451.rivet.backend;

import net.lenni0451.rivet.backend.text.Font;

import javax.annotation.WillNotClose;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public interface AssetLoader {

    Font loadFont(@WillNotClose final InputStream inputStream, final int size) throws IOException;

    default Font loadFont(final byte[] data, final int size) throws IOException {
        return this.loadFont(new ByteArrayInputStream(data), size);
    }

    default Font loadFont(final File file, final int size) throws IOException {
        return this.loadFont(file.toPath(), size);
    }

    default Font loadFont(final Path path, final int size) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return this.loadFont(is, size);
        }
    }

    Texture loadTexture(@WillNotClose final InputStream inputStream) throws IOException;

    default Texture loadTexture(final byte[] data) throws IOException {
        return this.loadTexture(new ByteArrayInputStream(data));
    }

    default Texture loadTexture(final File file) throws IOException {
        return this.loadTexture(file.toPath());
    }

    default Texture loadTexture(final Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return this.loadTexture(is);
        }
    }

}
