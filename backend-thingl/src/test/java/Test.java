import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.opengl.ThinGLBackend;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.component.impl.TextField;
import net.lenni0451.rivet.constants.KeyboardConstants;
import net.lenni0451.rivet.container.impl.AbsoluteContainer;
import net.lenni0451.rivet.text.FontSet;
import net.raphimc.thingl.implementation.application.StandaloneApplicationRunner;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class Test extends StandaloneApplicationRunner {

    public static void main(String[] args) {
        new Test().launch();
    }

    public Test() {
        super(new Configuration().setWindowTitle("Rivet ThinGL Test").setDebugMode(true));
    }

    private Rivet rivet;

    @Override
    protected void init() {
        super.init();
        GLFW.glfwSetCursorPosCallback(this.window, (window, xpos, ypos) -> {
            Test.this.rivet.onMouseMove((float) xpos, (float) ypos);
        });
        GLFW.glfwSetMouseButtonCallback(this.window, (window, button, action, mods) -> {
            final double[] xpos = new double[1];
            final double[] ypos = new double[1];
            GLFW.glfwGetCursorPos(window, xpos, ypos);
            if (action == GLFW.GLFW_PRESS) {
                Test.this.rivet.onMouseDown((float) xpos[0], (float) ypos[0], button, mods);
            } else if (action == GLFW.GLFW_RELEASE) {
                Test.this.rivet.onMouseUp((float) xpos[0], (float) ypos[0], button, mods);
            }
        });
        GLFW.glfwSetScrollCallback(this.window, (window, xoffset, yoffset) -> {
            final double[] xpos = new double[1];
            final double[] ypos = new double[1];
            GLFW.glfwGetCursorPos(window, xpos, ypos);
            Test.this.rivet.onMouseScroll((float) xpos[0], (float) ypos[0], (float) xoffset, (float) yoffset);
        });
        GLFW.glfwSetKeyCallback(this.window, (window, key, scancode, action, mods) -> {
            if (!KeyboardConstants.isValidKey(key)) return;
            if (action == GLFW.GLFW_PRESS) {
                this.rivet.onKeyDown(key, mods);
            } else if (action == GLFW.GLFW_RELEASE) {
                this.rivet.onKeyUp(key, mods);
            } else if (action == GLFW.GLFW_REPEAT) {
                this.rivet.onKeyDown(key,  mods);
            }
        });
        GLFW.glfwSetCharCallback(this.window, (window, codepoint) -> {
            if (Character.isBmpCodePoint(codepoint)) {
                Test.this.rivet.onCharTyped((char) codepoint);
            } else if (Character.isValidCodePoint(codepoint)) {
                Test.this.rivet.onCharTyped(Character.highSurrogate(codepoint));
                Test.this.rivet.onCharTyped(Character.lowSurrogate(codepoint));
            }
        });

        final ThinGLBackend backend = new ThinGLBackend(() -> GLFW.glfwGetClipboardString(this.window), text -> GLFW.glfwSetClipboardString(this.window, text));

        Font font;
        try {
            font = backend.loadFont(Test.class.getResourceAsStream("/Roboto-Regular.ttf").readAllBytes(), 30);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AbsoluteContainer rootContainer = new AbsoluteContainer();
        //Button button = new TextButton("Testing", mouseButton -> System.out.println("CLICKED! Button: " + mouseButton));
        //rootContainer.add(button, 50, 50);
        TextField textField = new TextField();
        rootContainer.add(textField, 50, 50);
        this.rivet = new Rivet(backend, new FontSet(font), rootContainer, 1280, 720);
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        this.rivet.render(positionMatrix);
    }

}
