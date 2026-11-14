package net.lenni0451.rivet.backend.teavm.render;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * GLSL ES 3.0 sources of the single uber shader used for all primitives.<br>
 * Shapes are rendered as signed distance fields, which gives anti aliased edges without MSAA.<br>
 * The sources are embedded into the compiled JavaScript by {@link ShaderResourcesPolicy}.
 */
@UtilityClass
class Shaders {

    private static final String PATH = "net/lenni0451/rivet/backend/teavm/render/shaders/";
    public static final String VERTEX_FILE = PATH + "vertex.glsl";
    public static final String FRAGMENT_FILE = PATH + "fragment.glsl";

    public static final String VERTEX = load(VERTEX_FILE);
    public static final String FRAGMENT = load(FRAGMENT_FILE);

    private static String load(final String path) {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Missing shader resource: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read shader resource: " + path, e);
        }
    }

}
