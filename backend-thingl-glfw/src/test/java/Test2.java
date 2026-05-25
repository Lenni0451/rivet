import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.thingl.RivetThinGLApplication;
import net.lenni0451.rivet.component.Container;
import net.lenni0451.rivet.component.base.ScrollContainer;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.impl.DefaultDark;
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
        rivet.theme(new DefaultDark() {
            @Override
            protected void addValues(final Rivet rivet, final Values values) {
                super.addValues(rivet, values);
                values.put(Theme.BUTTON_INACTIVE_COLOR, Color.GRAY.withAlpha(50));
                values.put(Theme.BUTTON_INACTIVE_OUTLINE_COLOR, Color.BLACK);
                values.put(Theme.BUTTON_ACTIVE_COLOR, Color.GRAY.withAlpha(150));
                values.put(Theme.BUTTON_ACTIVE_OUTLINE_COLOR, Color.fromRGB(116, 165, 229));
                values.put(Theme.BUTTON_CLICK_COLOR, Color.GRAY.withAlpha(150).darker());
                values.put(Theme.BUTTON_CLICK_OUTLINE_COLOR, Color.fromRGB(116, 165, 229).darker());
                values.put(Theme.BUTTON_CORNER_RADIUS, 0);
                values.put(Theme.BUTTON_OUTLINE_WIDTH, 3);
                values.put(Theme.COLOR_PICKER_SELECTOR_SIZE, 6F);
                values.put(Theme.SCROLL_BAR_TYPE, ScrollContainer.ScrollBarType.NORMAL);
            }
        });

        Container container = new Container(rivet, new VerticalListLayout(10, true));
        rivet.root().addChild(new ScrollContainer(rivet, container, true, true).autoScroll(true));
        Thread.ofVirtual().start(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    final int finalI = i;
                    rivet.runSync(() -> {
                        container.addChild(new Label(rivet, "Label " + finalI).horizontalOrigin(TextOrigin.Horizontal.VISUAL_RIGHT));
                    });
                    Thread.sleep(500);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

}
