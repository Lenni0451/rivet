import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.thingl.RivetThinGLApplication;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.resource.font.impl.FreeTypeFont;
import net.raphimc.thingl.text.font.FontSet;
import org.lwjgl.glfw.GLFW;

public class Test2 extends RivetThinGLApplication {

    public static void main(String[] ignoredArgs) {
        if (System.getProperty("os.name").contains("Linux")) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        }
        new Test2().run();
    }


    @Override
    protected FontSet createFontSet() throws Exception {
        Font font = new FreeTypeFont(Test.class.getResourceAsStream("/NotoSans-Regular.ttf").readAllBytes(), 40);
        return new FontSet(font);
    }

    @Override
    protected void init(final Rivet rivet) {
        rivet.theme(new TestTheme());
        Container container = new Container(new VerticalListLayout(10, true));
        rivet.root().addChild(new ScrollContainer(container, true, true).autoScroll(true));
        Thread.ofVirtual().start(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    final int finalI = i;
                    rivet.runSync(() -> {
                        container.addChild(new Label("Label " + finalI).horizontalOrigin(TextOrigin.Horizontal.VISUAL_RIGHT));
                    });
                    Thread.sleep(500);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

}
