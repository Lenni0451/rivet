package test;

import net.lenni0451.commons.collections.Maps;
import net.lenni0451.rivet.backend.thingl.RivetThinGLApplication;
import net.raphimc.thingl.resource.font.face.impl.FreeTypeFontFace;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.resource.font.instance.FontInstanceSet;
import net.raphimc.thingl.text.util.GlyphPredicate;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;

public abstract class TestBase extends RivetThinGLApplication {

    static {
        if (System.getProperty("os.name").contains("Linux")) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        }
    }

    public static FontInstanceSet createFont(final InputStream is, final int size) throws IOException {
        FontInstance font = new FreeTypeFontFace(is.readAllBytes()).getInstance(size);
        return new FontInstanceSet(Maps.linkedHashMap(font, GlyphPredicate.all()));
    }


    @Override
    protected FontInstanceSet createFont() throws Exception {
        return createFont(Test.class.getResourceAsStream("/NotoSans-Regular.ttf"), 40);
    }

}
