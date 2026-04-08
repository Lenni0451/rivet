import lombok.SneakyThrows;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.thingl.GLFWMapper;
import net.lenni0451.rivet.backend.thingl.ThinGLBackend;
import net.lenni0451.rivet.backend.thingl.ThinGLRenderer;
import net.lenni0451.rivet.component.base.Button;
import net.lenni0451.rivet.component.impl.FormattedLabel;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.Slider;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.flow.HorizontalFlowLayout;
import net.lenni0451.rivet.math.Size;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.application.GLFWApplicationRunner;
import net.raphimc.thingl.resource.font.Font;
import net.raphimc.thingl.resource.font.impl.FreeTypeFont;
import net.raphimc.thingl.text.font.FontSet;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

public class Test extends GLFWApplicationRunner {

    public static void main(String[] args) {
        if (System.getProperty("os.name").contains("Linux")) {
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        }
        new Test().launch();
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
        GLFW.glfwSetCursorPosCallback(this.window, (window, xpos, ypos) -> {
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
        GLFW.glfwSetKeyCallback(this.window, (window, key, scancode, action, mods) -> {
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
        GLFW.glfwSetCharCallback(this.window, (window, codepoint) -> {
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
            this.rivet.setSize(new Size(width, height));
        });
        Font font = new FreeTypeFont(Test.class.getResourceAsStream("/NotoSans-Regular.ttf").readAllBytes(), 40);
        FontSet fontSet = new FontSet(font);

        ThinGLBackend backend = new ThinGLBackend(fontSet);
        this.rivet = new Rivet(backend, new HorizontalFlowLayout(5, 5), new Size(ThinGL.windowInterface().getFramebufferWidth(), ThinGL.windowInterface().getFramebufferHeight()));
//        for (int i = 0; i < 10; i++) {
//            TestComponent comp = new TestComponent(this.rivet);
//            comp.setMinSize(new Size(100 + 100 * i, 100));
//            comp.setMaxSize(comp.minSize());
//            this.rivet.getRootContainer().addChild(comp);
//        }
        Button button = new Button(this.rivet, new Label(this.rivet, "Hello, World!"), System.out::println);
//        button.setMinSize(new Size(1000, 500));
        this.rivet.getRootContainer().addChild(button);
        Button button2 = new Button(this.rivet, new Label(this.rivet, "Bye, World!"), System.out::println);
//        button.setMinSize(new Size(1000, 500));
        this.rivet.getRootContainer().addChild(button2);

        FormattedLabel formattedLabel = new FormattedLabel(this.rivet, "This is <bold>bold</bold>, <italic>italic</italic>, <color=red>red</color> and <shadow>shadowed</shadow> text!");
        this.rivet.getRootContainer().addChild(formattedLabel);

        Slider slider = new Slider(this.rivet, 0, 100, 0);
//        slider.setMinSize(new Size(500, 100));
        this.rivet.getRootContainer().addChild(slider.ticks(new Slider.Ticks(10, 2.5, d -> String.format("%,d", (int) d))));
    }

    @Override
    protected void render(final Matrix4fStack matrix4fStack) {
        this.rivet.render(new ThinGLRenderer(matrix4fStack));
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
