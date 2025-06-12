import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.impl.Button;
import net.lenni0451.rivet.container.impl.AbsoluteContainer;
import net.lenni0451.rivet.renderer.opengl.OpenGLRenderer;
import net.raphimc.thingl.implementation.application.StandaloneApplicationRunner;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;

public class Test extends StandaloneApplicationRunner {

    public static void main(String[] args) {
        new Test().launch();
    }

    public Test() {
        super(new Configuration().setWindowTitle("Rivet OpenGL Test").setDebugMode(true));
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
        });
        GLFW.glfwSetCharCallback(this.window, (window, codepoint) -> Test.this.rivet.onCharTyped(codepoint));

        AbsoluteContainer rootContainer = new AbsoluteContainer();
        Button button = new Button("Testing", mouseButton -> System.out.println("CLICKED! Button: " + mouseButton));
        rootContainer.add(button, 50, 50);
        this.rivet = new Rivet(new OpenGLRenderer(), rootContainer, 1280, 720);
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        this.rivet.render(positionMatrix);
    }

}
