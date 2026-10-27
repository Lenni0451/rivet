package test;

import net.lenni0451.rivet.backend.AssetLoader;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.thingl.GLFWApplication;

public abstract class TestBase extends GLFWApplication {

    @Override
    public Font createFont(final AssetLoader assetLoader) throws Exception {
        return assetLoader.loadFont(20, TestBase.class.getResourceAsStream("/NotoSans-Regular.ttf"), TestBase.class.getResourceAsStream("/lucide.ttf"));
    }

}
