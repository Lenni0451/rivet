package test;

import net.lenni0451.commons.collections.Maps;
import net.lenni0451.rivet.backend.thingl.RivetThinGLApplication;
import net.raphimc.thingl.resource.font.face.impl.FreeTypeFontFace;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.resource.font.instance.FontInstanceSet;
import net.raphimc.thingl.text.util.GlyphPredicate;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

public abstract class TestBase extends RivetThinGLApplication {

    static {
        if (System.getProperty("os.name").contains("Linux")) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        }
    }

    @Override
    protected FontInstanceSet createFont() throws Exception {
        FontInstance font = new FreeTypeFontFace(Test.class.getResourceAsStream("/NotoSans-Regular.ttf").readAllBytes()).getInstance(40);
        return new FontInstanceSet(Maps.linkedHashMap(font, GlyphPredicate.all()));
    }

}
