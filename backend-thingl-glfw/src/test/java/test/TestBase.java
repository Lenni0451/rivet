package test;

import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.thingl.GLFWApplication;
import net.lenni0451.rivet.backend.thingl.ThinGLAssetLoader;
import org.junit.jupiter.api.Test;

public abstract class TestBase extends GLFWApplication {

    // static {
    //     if (System.getProperty("os.name").contains("Linux")) {
    //         GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
    //     }
    // }

    @Override
    protected Font createFont(final ThinGLAssetLoader assetLoader) throws Exception {
        return assetLoader.loadFont(20, Test.class.getResourceAsStream("/NotoSans-Regular.ttf"), Test.class.getResourceAsStream("/lucide.ttf"));
    }

}
