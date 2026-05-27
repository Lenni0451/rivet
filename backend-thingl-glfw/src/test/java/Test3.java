import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.thingl.RivetThinGLApplication;
import net.lenni0451.rivet.component.Container;
import net.lenni0451.rivet.component.base.Button;
import net.lenni0451.rivet.component.impl.FormattedLabel;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridLayoutOptions;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.resource.font.impl.FreeTypeFont;
import net.raphimc.thingl.text.font.FontSet;
import org.lwjgl.glfw.GLFW;

public class Test3 extends RivetThinGLApplication {

    public static void main(String[] ignoredArgs) {
        if (System.getProperty("os.name").contains("Linux")) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        }
        new Test3().run();
    }


    @Override
    protected FontSet createFontSet() throws Exception {
        Font font = new FreeTypeFont(Test.class.getResourceAsStream("/NotoSans-Regular.ttf").readAllBytes(), 40);
        return new FontSet(font);
    }

    @Override
    protected void init(final Rivet rivet) {
        Container container = new Container(new GridLayout(10, 10));
        container.addChild(new FormattedLabel("<color=red italic bold underlined>Hello this is a really cool test string how are you doing lol\n<color=blue> <color=red>a\n\naaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"), label -> {
            label.horizontalOrigin(TextOrigin.Horizontal.VISUAL_LEFT);
            label.layoutOptions(new GridLayoutOptions(0, 0).withFill(GridFill.HORIZONTAL).withWeightX(1));
        });
        container.addChild(new Button(new Label("Testing Testing Testing Testing Testing Testing Testing"), e -> {
            System.out.println("click");
        }), button -> {
            button.clickOn().set(Button.ClickOn.BOTH);
            button.layoutOptions(new GridLayoutOptions(0, 1).withFill(GridFill.HORIZONTAL).withWeightX(1));
        });
        rivet.root().addChild(container);
    }

}
