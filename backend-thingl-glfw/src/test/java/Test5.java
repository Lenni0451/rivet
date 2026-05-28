import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.thingl.RivetThinGLApplication;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.component.impl.Separator;
import net.lenni0451.rivet.component.impl.SolidColor;
import net.lenni0451.rivet.layout.anchor.AnchorLayout;
import net.lenni0451.rivet.layout.anchor.AnchorLayoutOptions;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.resource.font.impl.FreeTypeFont;
import net.raphimc.thingl.text.font.FontSet;
import org.lwjgl.glfw.GLFW;

public class Test5 extends RivetThinGLApplication {

    public static void main(String[] ignoredArgs) {
        if (System.getProperty("os.name").contains("Linux")) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        }
        new Test5().run();
    }


    @Override
    protected FontSet createFontSet() throws Exception {
        Font font = new FreeTypeFont(Test.class.getResourceAsStream("/NotoSans-Regular.ttf").readAllBytes(), 40);
        return new FontSet(font);
    }

    @Override
    protected void init(final Rivet rivet) {
        Container container = new Container(AnchorLayout.INSTANCE);
        container.addChild(new SolidColor(), c -> {
            c.color(Color.RED);
            c.layoutOptions(AnchorLayoutOptions.EMPTY.from(0, 0.7F).to(1, 1));
        });
        container.addChild(new SolidColor(), c -> {
            c.color(Color.GREEN);
            c.layoutOptions(AnchorLayoutOptions.EMPTY.from(0, 0).to(0.2F, 0.7F));
        });
        container.addChild(new ScrollContainer(new Container(new VerticalListLayout(5, true)), c -> {
            for (int i = 0; i < 10; i++) {
                if (i == 5) {
                    c.addChild(new Separator());
                }
                c.addChild(new Button("Button " + i, e -> {}));
            }
        }), c -> {
            c.layoutOptions(AnchorLayoutOptions.EMPTY.from(0.7F, 0).to(1, 0.7F));
        });
        container.addChild(new SolidColor(), c -> {
            c.color(Color.BLUE);
            c.layoutOptions(AnchorLayoutOptions.EMPTY.from(0.2F, 0).to(0.7F, 0.7F));
        });
        rivet.root().addChild(container);
    }

}
