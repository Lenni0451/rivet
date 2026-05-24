import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.thingl.RivetThinGLApplication;
import net.lenni0451.rivet.component.Container;
import net.lenni0451.rivet.component.base.Button;
import net.lenni0451.rivet.component.base.ComboBox;
import net.lenni0451.rivet.component.base.DecoratedContainer;
import net.lenni0451.rivet.component.base.ScrollContainer;
import net.lenni0451.rivet.component.impl.*;
import net.lenni0451.rivet.component.impl.graphics.SolidColor;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.layout.grid.GridAnchor;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridLayoutOptions;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeKey;
import net.lenni0451.rivet.theme.impl.DefaultDark;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.resource.font.impl.FreeTypeFont;
import net.raphimc.thingl.text.font.FontSet;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class Test extends RivetThinGLApplication {

    public static void main(String[] ignoredArgs) {
        if (System.getProperty("os.name").contains("Linux")) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        }
        new Test().run();
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
            protected void addValues(final Rivet rivet, final Map<ThemeKey<?>, Object> values) {
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

        Container container = new Container(rivet, new GridLayout(10, 10).homogeneousColumns(true).shrinkColumns(true));
        rivet.root().addChild(new DecoratedContainer(rivet, new SolidColor(rivet), new ScrollContainer(rivet, container, false, true)), decoratedContainer -> {
            decoratedContainer.innerPadding(new Padding(20, 20, 20, 20));
            SolidColor background = (SolidColor) decoratedContainer.background();
            background.color(Color.fromARGB(Integer.MIN_VALUE));
            background.cornerRadius(20F);
            background.outlineColor(Color.GREEN);
        });
        container.addChild(new FormattedLabel(rivet, "Hello this is a really cool test string how are you doing lol"), label -> {
            label.horizontalOrigin(TextOrigin.Horizontal.VISUAL_LEFT);
            label.layoutOptions(new GridLayoutOptions(0, 0).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
        });
        container.addChild(new Button(rivet, new Label(rivet, "Singleplayer"), _ -> {}), button -> {
            button.layoutOptions(new GridLayoutOptions(0, 1).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
        });
        container.addChild(new Button(rivet, new Label(rivet, "Minecraft Realms"), _ -> {}), button -> {
            button.layoutOptions(new GridLayoutOptions(0, 2).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL));
        });
        container.addChild(new Button(rivet, new Label(rivet, "Multiplayer"), _ -> {}), button -> {
            button.layoutOptions(new GridLayoutOptions(1, 2).withAnchor(GridAnchor.EAST).withWeightX(1).withFill(GridFill.HORIZONTAL));
        });
        container.addChild(new Button(rivet, new Label(rivet, "DeepClient Menu"), _ -> {}), button -> {
            button.layoutOptions(new GridLayoutOptions(0, 3).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
        });
        container.addChild(new Button(rivet, new Label(rivet, "Options..."), _ -> {}), button -> {
            button.layoutOptions(new GridLayoutOptions(0, 4).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withPadding(Padding.EMPTY.withTop(20)));
        });
        container.addChild(new Button(rivet, new Label(rivet, "Quit Game"), _ -> {}), button -> {
            button.layoutOptions(new GridLayoutOptions(1, 4).withAnchor(GridAnchor.EAST).withWeightX(1).withFill(GridFill.HORIZONTAL).withPadding(Padding.EMPTY.withTop(20)));
        });
        container.addChild(new ComboBox(rivet, "Testing since 2k26", new DecoratedContainer(rivet, new SolidColor(rivet), new ScrollContainer(rivet, new Container(rivet, new VerticalListLayout(5, true)), comboBoxContainer -> {
            for (int i = 0; i < 10; i++) {
                comboBoxContainer.addChild(new Button(rivet, new Label(rivet, "Test " + i), _ -> {}));
            }
        })), decoratedContainer -> {
            ((SolidColor) decoratedContainer.background()).color(Color.GREEN);
        }), comboBox -> {
            comboBox.layoutOptions(new GridLayoutOptions(0, 5).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
        });
        container.addChild(new TextField(rivet), textField -> {
            textField.layoutOptions(new GridLayoutOptions(0, 6).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
            textField.keyDownListener().add(event -> {
                if (event.key().isEquivalent(Key.ENTER)) {
                    System.out.println(textField.text());
                    return true;
                }
                return false;
            });
        });
        container.addChild(new Checkbox(rivet, "Test Test Test", true), checkbox -> {
            checkbox.layoutOptions(new GridLayoutOptions(0, 7).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
            checkbox.cornerRadius().set(8F);
        });
        container.addChild(new Slider(rivet, 0, 100, 1), slider -> {
            slider.layoutOptions(new GridLayoutOptions(0, 8).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
        });
        container.addChild(new ColorPicker(rivet, Color.RED), colorPicker -> {
            colorPicker.layoutOptions(new GridLayoutOptions(0, 9).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
        });
    }

    @Override
    protected void renderBackground(final Matrix4fStack matrix4fStack) {
        ThinGL.renderer2D().filledRectangle(matrix4fStack, 0, 0, ThinGL.windowInterface().getFramebufferWidth(), ThinGL.windowInterface().getFramebufferHeight(), Color.getRainbowColor(0, 10));
    }

}
