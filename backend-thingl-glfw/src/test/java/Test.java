import lombok.SneakyThrows;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.thingl.GLFWMapper;
import net.lenni0451.rivet.backend.thingl.ThinGLBackend;
import net.lenni0451.rivet.backend.thingl.ThinGLRenderer;
import net.lenni0451.rivet.component.Container;
import net.lenni0451.rivet.component.base.Button;
import net.lenni0451.rivet.component.base.ScrollContainer;
import net.lenni0451.rivet.component.impl.Checkbox;
import net.lenni0451.rivet.component.impl.ColorPicker;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.TextField;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.flow.VerticalFlowLayout;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.layout.grid.GridAnchor;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridLayoutOptions;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeKey;
import net.lenni0451.rivet.theme.impl.DefaultDark;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.application.GLFWApplicationRunner;
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.resource.font.impl.FreeTypeFont;
import net.raphimc.thingl.text.font.FontSet;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

import java.util.Map;

public class Test extends GLFWApplicationRunner {

    public static void main(String[] ignoredArgs) {
        if (System.getProperty("os.name").contains("Linux")) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        }
        new Test().run();
    }


    private FontSet font;
    private Rivet rivet;

    public Test() {
        super(new Configuration());
    }

    @Override
    @SneakyThrows
    protected void init() {
        super.init();
        GLFW.glfwSetCursorPosCallback(this.window, (_, xpos, ypos) -> {
            float[] mouseScale = Test.this.getMouseScale();
            Test.this.rivet.onMouseMove(new MouseMoveEvent((float) xpos * mouseScale[0], (float) ypos * mouseScale[1]));
        });
        GLFW.glfwSetMouseButtonCallback(this.window, (window, button, action, mods) -> {
            float[] mouseScale = Test.this.getMouseScale();
            final double[] xpos = new double[1];
            final double[] ypos = new double[1];
            GLFW.glfwGetCursorPos(window, xpos, ypos);

            MouseButtonEvent event = GLFWMapper.mapMouseButton((float) xpos[0] * mouseScale[0], (float) ypos[0] * mouseScale[1], button, mods);
            if (event != null) {
                if (action == GLFW.GLFW_PRESS) {
                    Test.this.rivet.onMouseDown(event);
                } else if (action == GLFW.GLFW_RELEASE) {
                    Test.this.rivet.onMouseUp(event);
                }
            }
        });
        GLFW.glfwSetScrollCallback(this.window, (window, xoffset, yoffset) -> {
            float[] mouseScale = Test.this.getMouseScale();
            final double[] xpos = new double[1];
            final double[] ypos = new double[1];
            GLFW.glfwGetCursorPos(window, xpos, ypos);
            Test.this.rivet.onMouseScroll(new MouseScrollEvent((float) xpos[0] * mouseScale[0], (float) ypos[0] * mouseScale[1], (float) xoffset, (float) yoffset));
        });
        GLFW.glfwSetKeyCallback(this.window, (_, key, _, action, mods) -> {
            KeyEvent event = GLFWMapper.mapKey(key, mods);
            if (event != null) {
                if (action == GLFW.GLFW_PRESS) {
                    this.rivet.onKeyDown(event);
                } else if (action == GLFW.GLFW_RELEASE) {
                    this.rivet.onKeyUp(event);
                } else if (action == GLFW.GLFW_REPEAT) {
                    this.rivet.onKeyDown(event);
                }
            }
        });
        GLFW.glfwSetCharCallback(this.window, (_, codepoint) -> {
            if (Character.isBmpCodePoint(codepoint)) {
                Test.this.rivet.onCharTyped(new CharEvent((char) codepoint));
            } else if (Character.isValidCodePoint(codepoint)) {
                Test.this.rivet.onCharTyped(new CharEvent(Character.highSurrogate(codepoint)));
                Test.this.rivet.onCharTyped(new CharEvent(Character.lowSurrogate(codepoint)));
            }
        });
        GLFWFramebufferSizeCallback[] oldCallback = new GLFWFramebufferSizeCallback[1];
        oldCallback[0] = GLFW.glfwSetFramebufferSizeCallback(this.window, (window, width, height) -> {
            oldCallback[0].invoke(window, width, height);
            this.rivet.size(new Size(width, height));
        });
        Font font = new FreeTypeFont(Test.class.getResourceAsStream("/NotoSans-Regular.ttf").readAllBytes(), 40);
        FontSet fontSet = new FontSet(font);

        ThinGLBackend backend = new ThinGLBackend(this.window, fontSet);
        this.rivet = new Rivet(backend, FullSizeLayout.INSTANCE, new Size(ThinGL.windowInterface().getFramebufferWidth(), ThinGL.windowInterface().getFramebufferHeight()));
        this.rivet.theme(new DefaultDark() {
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
            }
        });

        Container container = new Container(this.rivet, new GridLayout(10, 10).homogeneousColumns(true));
        this.rivet.root().addChild(new ScrollContainer(this.rivet, container, true, true));
        container.addChild(new Button(this.rivet, new Label(this.rivet, "Singleplayer"), _ -> {}), button -> {
            button.layoutOptions(new GridLayoutOptions(0, 0).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
        });
        container.addChild(new Button(this.rivet, new Label(this.rivet, "Minecraft Realms"), _ -> {}), button -> {
            button.layoutOptions(new GridLayoutOptions(0, 1).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL));
        });
        container.addChild(new Button(this.rivet, new Label(this.rivet, "Multiplayer"), _ -> {}), button -> {
            button.layoutOptions(new GridLayoutOptions(1, 1).withAnchor(GridAnchor.EAST).withWeightX(1).withFill(GridFill.HORIZONTAL));
        });
        container.addChild(new Button(this.rivet, new Label(this.rivet, "DeepClient Menu"), _ -> {}), button -> {
            button.layoutOptions(new GridLayoutOptions(0, 2).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
        });
        container.addChild(new Button(this.rivet, new Label(this.rivet, "Options..."), _ -> {}), button -> {
            button.layoutOptions(new GridLayoutOptions(0, 3).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withPadding(Padding.EMPTY.withTop(20)));
        });
        container.addChild(new Button(this.rivet, new Label(this.rivet, "Quit Game"), _ -> {}), button -> {
            button.layoutOptions(new GridLayoutOptions(1, 3).withAnchor(GridAnchor.EAST).withWeightX(1).withFill(GridFill.HORIZONTAL).withPadding(Padding.EMPTY.withTop(20)));
        });
        container.addChild(new TextField(this.rivet), button -> {
            button.layoutOptions(new GridLayoutOptions(0, 4).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
        });
        Container comboBoxContainer = new Container(this.rivet, new VerticalFlowLayout(5, 5));
        container.addChild(new Checkbox(this.rivet, "Test Test Test", true), checkbox -> {
            checkbox.layoutOptions(new GridLayoutOptions(0, 5).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
            checkbox.cornerRadius().set(8F);
        });
        container.addChild(new Slider(this.rivet, 0, 100, 1), slider -> {
            slider.layoutOptions(new GridLayoutOptions(0, 6).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
        });
        container.addChild(new ColorPicker(this.rivet, Color.RED), colorPicker -> {
            colorPicker.layoutOptions(new GridLayoutOptions(0, 7).withAnchor(GridAnchor.WEST).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2));
        });
    }

    @Override
    protected void render(final Matrix4fStack matrix4fStack) {
        ThinGL.programs().getMsaa().bindInput();
        ThinGL.renderer2D().filledRectangle(matrix4fStack, 0, 0, ThinGL.windowInterface().getFramebufferWidth(), ThinGL.windowInterface().getFramebufferHeight(), Color.GRAY.darker().darker().darker().darker());
        ThinGLRenderer.renderList(matrix4fStack, this.rivet.render());
        //BatchedThinGLRenderer.renderList(matrix4fStack, this.rivet.render());
        ThinGL.programs().getMsaa().unbindInput();
        ThinGL.programs().getMsaa().renderFullscreen();
        ThinGL.programs().getMsaa().clearInput();
    }

    private float[] getMouseScale() {
        int[] windowSizeX = new int[1];
        int[] windowSizeY = new int[1];
        GLFW.glfwGetWindowSize(this.window, windowSizeX, windowSizeY);
        int[] framebufferSizeX = new int[1];
        int[] framebufferSizeY = new int[1];
        GLFW.glfwGetFramebufferSize(this.window, framebufferSizeX, framebufferSizeY);
        return new float[]{(float) framebufferSizeX[0] / windowSizeX[0], (float) framebufferSizeY[0] / windowSizeY[0]};
    }

}
