import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.thingl.RivetThinGLApplication;
import net.lenni0451.rivet.component.Container;
import net.lenni0451.rivet.component.base.Button;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.layout.tile.TileLayout;
import net.lenni0451.rivet.layout.tile.TileLayoutOptions;
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.resource.font.impl.FreeTypeFont;
import net.raphimc.thingl.text.font.FontSet;
import org.lwjgl.glfw.GLFW;

public class Test4 extends RivetThinGLApplication {

    public static void main(String[] ignoredArgs) {
        if (System.getProperty("os.name").contains("Linux")) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        }
        new Test4().run();
    }


    @Override
    protected FontSet createFontSet() throws Exception {
        Font font = new FreeTypeFont(Test.class.getResourceAsStream("/NotoSans-Regular.ttf").readAllBytes(), 40);
        return new FontSet(font);
    }

    @Override
    protected void init(final Rivet rivet) {
        Container container = new Container(new TileLayout(3, 3, 10, 10));
        for (int i = 0; i < 9; i++) {
            final int finalI = i;
            final int column = i % 3;
            final int row = i / 3;
            if (column == 1 && row == 0) continue;
            if (column == 0 && row == 1) continue;
            if (column == 2 && row == 1) continue;
            if (column == 1 && row == 2) continue;
            container.addChild(new Button(new Label("Testing " + i), event -> {
                System.out.println("Clicked " + finalI);
            }), button -> button.layoutOptions(new TileLayoutOptions(column, row)));
        }
        rivet.root().addChild(container);
    }

}
