package test;

import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.thingl.GLFWApplication;
import net.lenni0451.rivet.backend.thingl.ThinGLAssetLoader;

public abstract class TestBase extends GLFWApplication {

    @Override
    protected Font createFont(final ThinGLAssetLoader assetLoader) throws Exception {
        return assetLoader.loadFont(20, TestBase.class.getResourceAsStream("/NotoSans-Regular.ttf"), TestBase.class.getResourceAsStream("/lucide.ttf"));
    }

}
