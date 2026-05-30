import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.thingl.RivetThinGLApplication;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.utils.SelectionModel;
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.resource.font.impl.FreeTypeFont;
import net.raphimc.thingl.text.font.FontSet;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class Test7 extends RivetThinGLApplication {

    public static void main(String[] ignoredArgs) {
        if (System.getProperty("os.name").contains("Linux")) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        }
        new Test7().run();
    }

    @Override
    protected FontSet createFontSet() throws Exception {
        Font font = new FreeTypeFont(Test.class.getResourceAsStream("/NotoSans-Regular.ttf").readAllBytes(), 40);
        return new FontSet(font);
    }

    @Override
    protected void init(final Rivet rivet) {
        List<SelectableLabel> labels = new ArrayList<>();
        SelectionModel<SelectableLabel> selectionModel = new SelectionModel<>(labels);
        Container container = new Container(new VerticalListLayout(5, true));
        for (int i = 0; i < 10; i++) {
            SelectableLabel label = new SelectableLabel("Test " + i, selectionModel);
            label.mouseDownListener().add((event, bounds) -> {
                if (event.button().equals(MouseButton.LEFT)) {
                    selectionModel.select(label, event.modifiers());
                }
                return false;
            });
            container.addChild(label);
            labels.add(label);
        }
        rivet.root().addChild(container);
    }


    private static class SelectableLabel extends Label {
        private static final Color SELECTED_COLOR = Color.BLUE;
        private static final Color HOVERED_COLOR = Color.GRAY.withAlpha(100);

        private final SelectionModel<SelectableLabel> selectionModel;
        private boolean hovered;

        public SelectableLabel(final String text, final SelectionModel<SelectableLabel> selectionModel) {
            super(text);
            this.selectionModel = selectionModel;
            this.horizontalOrigin(TextOrigin.Horizontal.VISUAL_LEFT);
        }

        @Override
        protected void onComponentMouseEnter() {
            this.hovered = true;
        }

        @Override
        protected void onComponentMouseLeave() {
            this.hovered = false;
        }

        @Override
        public void render(final Renderer renderer, final Rectangle bounds) {
            if (this.selectionModel.isSelected(this)) {
                renderer.fillRect(0, 0, bounds.width(), bounds.height(), SELECTED_COLOR);
            }
            if (this.hovered) {
                renderer.fillRect(0, 0, bounds.width(), bounds.height(), HOVERED_COLOR);
            }
            super.render(renderer, bounds);
        }
    }

}
