import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.thingl.RivetThinGLApplication;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.SolidColor;
import net.lenni0451.rivet.layout.anchor.AnchorLayout;
import net.lenni0451.rivet.layout.anchor.AnchorLayoutOptions;
import net.lenni0451.rivet.math.Size;
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.resource.font.impl.FreeTypeFont;
import net.raphimc.thingl.text.font.FontSet;
import org.lwjgl.glfw.GLFW;

public class Test6 extends RivetThinGLApplication {

    public static void main(String[] ignoredArgs) {
        if (System.getProperty("os.name").contains("Linux")) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        }
        new Test6().run();
    }

    @Override
    protected FontSet createFontSet() throws Exception {
        Font font = new FreeTypeFont(Test.class.getResourceAsStream("/NotoSans-Regular.ttf").readAllBytes(), 40);
        return new FontSet(font);
    }

    @Override
    protected void init(final Rivet rivet) {
        Container container = new Container(AnchorLayout.INSTANCE);

        // --- Draggable Source ---
        Container dragSourceContainer = new Container(AnchorLayout.INSTANCE);
        dragSourceContainer.layoutOptions(AnchorLayoutOptions.EMPTY.from(0.1F, 0.4F).to(0.4F, 0.6F));

        SolidColor draggableBg = new SolidColor();
        draggableBg.color(Color.RED);
        draggableBg.layoutOptions(AnchorLayoutOptions.EMPTY.from(0, 0).to(1, 1));

        Label dragLabel = new Label("Drag Me");
        dragLabel.interactive(false);
        dragLabel.layoutOptions(AnchorLayoutOptions.EMPTY.from(0, 0).to(1, 1));

        dragSourceContainer.addChild(draggableBg);
        dragSourceContainer.addChild(dragLabel);

        // Trigger drag on mouse down
        draggableBg.mouseDownListener().add((event, bounds) -> {
            SolidColor ghost = new SolidColor();
            ghost.color(Color.RED.withAlpha(127)); // Semi-transparent ghost

            // Start the drag with some payload
            rivet.dragAndDropManager().startDrag(
                    "Sample Payload",
                    ghost,
                    new Size(bounds.width(), bounds.height()),
                    -bounds.width() / 2f,
                    -bounds.height() / 2f
            );
            return true;
        });

        // --- Drop Target ---
        Container dropTargetContainer = new Container(AnchorLayout.INSTANCE);
        dropTargetContainer.layoutOptions(AnchorLayoutOptions.EMPTY.from(0.6F, 0.2F).to(0.9F, 0.8F));

        SolidColor dropTargetBg = new SolidColor();
        dropTargetBg.color(Color.BLUE);
        dropTargetBg.layoutOptions(AnchorLayoutOptions.EMPTY.from(0, 0).to(1, 1));

        Label targetLabel = new Label("Drop Here");
        targetLabel.interactive(false); // Make label non-interactive so background gets events
        targetLabel.layoutOptions(AnchorLayoutOptions.EMPTY.from(0, 0).to(1, 1));

        dropTargetContainer.addChild(dropTargetBg);
        dropTargetContainer.addChild(targetLabel);

        // Listen for drag over events
        dropTargetBg.dragOverListener().add((event, bounds) -> {
            if ("Sample Payload".equals(event.dragData())) {
                dropTargetBg.color(Color.YELLOW);
                targetLabel.text("Release to drop");
            }
            return true;
        });

        // Also reset color if dragged away
        dropTargetBg.dragLeaveListener().add(() -> {
            if ("Release to drop".equals(targetLabel.text())) {
                dropTargetBg.color(Color.BLUE);
                targetLabel.text("Drop Here");
            }
            return false;
        });

        // Listen for drop events
        dropTargetBg.dropListener().add((event, bounds) -> {
            if ("Sample Payload".equals(event.dragData())) {
                dropTargetBg.color(Color.GREEN);
                targetLabel.text("Dropped!");
            }
            return true;
        });

        // Add to main container
        container.addChild(dragSourceContainer);
        container.addChild(dropTargetContainer);

        rivet.root().addChild(container);
    }

}
