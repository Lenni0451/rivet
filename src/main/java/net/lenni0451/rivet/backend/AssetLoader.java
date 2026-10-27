package net.lenni0451.rivet.backend;

import net.lenni0451.rivet.backend.text.Font;

import javax.annotation.WillNotClose;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface AssetLoader {

    Font loadFont(final int size, @WillNotClose final InputStream... inputStreams) throws IOException;

    default Font loadFont(@WillNotClose final InputStream inputStream, final int size) throws IOException {
        return this.loadFont(size, inputStream);
    }

    default Font loadFont(final int size, final byte[]... data) throws IOException {
        return this.loadFont(size, Stream.of(data).map(ByteArrayInputStream::new).toArray(InputStream[]::new));
    }

    default Font loadFont(final byte[] data, final int size) throws IOException {
        return this.loadFont(new ByteArrayInputStream(data), size);
    }

    default Font loadFont(final int size, final File... files) throws IOException {
        return this.loadFont(size, Stream.of(files).map(File::toPath).toArray(Path[]::new));
    }

    default Font loadFont(final File file, final int size) throws IOException {
        return this.loadFont(file.toPath(), size);
    }

    default Font loadFont(final int size, final Path... paths) throws IOException {
        InputStream[] inputStreams = new InputStream[paths.length];
        try {
            for (int i = 0; i < paths.length; i++) {
                inputStreams[i] = Files.newInputStream(paths[i]);
            }
            return this.loadFont(size, inputStreams);
        } finally {
            for (InputStream inputStream : inputStreams) {
                if (inputStream == null) continue;
                try {
                    inputStream.close();
                } catch (Throwable ignored) {
                }
            }
        }
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
