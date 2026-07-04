package net.lenni0451.rivet.backend.thingl;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.thingl.render.ThinGLRenderer;
import net.lenni0451.rivet.backend.thingl.text.ThinGLFont;
import net.lenni0451.rivet.backend.thingl.util.GLFWMapper;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.math.Size;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.application.GLFWApplicationRunner;
import net.raphimc.thingl.resource.font.instance.FontInstanceSet;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

import java.util.EnumSet;
import java.util.Set;

@Getter
@Accessors(fluent = true, chain = true)
public abstract class RivetThinGLApplication extends GLFWApplicationRunner {

    private final ThinGLRenderer renderer = new ThinGLRenderer();
    private FontInstanceSet fontInstanceSet;
    private ThinGLBackend backend;
    private Rivet rivet;
    private final Set<MouseButton> heldMouseButtons = EnumSet.noneOf(MouseButton.class);

    public RivetThinGLApplication() {
        this(new Configuration());
    }

    public RivetThinGLApplication(final Configuration configuration) {
        super(configuration);
    }

    @Override
    @SneakyThrows
    protected void init() {
        super.init();
        this.setupCallbacks();

        this.fontInstanceSet = this.createFont();
        this.backend = new ThinGLBackend(this.window, new ThinGLFont(this.fontInstanceSet));
        this.rivet = new Rivet(this.backend, FullSizeLayout.INSTANCE, new Size(ThinGL.windowInterface().getFramebufferWidth(), ThinGL.windowInterface().getFramebufferHeight()));

        this.init(this.rivet);
    }

    protected abstract FontInstanceSet createFont() throws Exception;

    protected abstract void init(final Rivet rivet);

    protected void setupCallbacks() {
        GLFW.glfwSetCursorPosCallback(this.window, (_, xpos, ypos) -> {
            float[] mouseScale = this.getMouseScale();
            this.rivet.onMouseMove(new MouseMoveEvent((float) xpos * mouseScale[0], (float) ypos * mouseScale[1], this.heldMouseButtons));
        });
        GLFW.glfwSetMouseButtonCallback(this.window, (window, button, action, mods) -> {
            float[] mouseScale = this.getMouseScale();
            final double[] xpos = new double[1];
            final double[] ypos = new double[1];
            GLFW.glfwGetCursorPos(window, xpos, ypos);

            MouseButtonEvent event = GLFWMapper.mapMouseButton((float) xpos[0] * mouseScale[0], (float) ypos[0] * mouseScale[1], button, mods);
            if (event != null) {
                if (action == GLFW.GLFW_PRESS) {
                    this.heldMouseButtons.add(event.button());
                    this.rivet.onMouseDown(event.withHeldButtons(this.heldMouseButtons));
                } else if (action == GLFW.GLFW_RELEASE) {
                    this.rivet.onMouseUp(event.withHeldButtons(this.heldMouseButtons));
                    this.heldMouseButtons.remove(event.button());
                }
            }
        });
        GLFW.glfwSetScrollCallback(this.window, (window, xoffset, yoffset) -> {
            float[] mouseScale = this.getMouseScale();
            final double[] xpos = new double[1];
            final double[] ypos = new double[1];
            GLFW.glfwGetCursorPos(window, xpos, ypos);
            this.rivet.onMouseScroll(new MouseScrollEvent((float) xpos[0] * mouseScale[0], (float) ypos[0] * mouseScale[1], (float) xoffset, (float) yoffset));
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
            this.rivet.onCharTyped(new CharEvent(codepoint));
        });
        GLFWFramebufferSizeCallback[] oldCallback = new GLFWFramebufferSizeCallback[1];
        oldCallback[0] = GLFW.glfwSetFramebufferSizeCallback(this.window, (window, width, height) -> {
            if (oldCallback[0] != null) oldCallback[0].invoke(window, width, height);
            this.rivet.size(new Size(width, height));
        });
        GLFW.glfwSetWindowFocusCallback(this.window, (window, focused) -> {
            if (!focused) {
                this.heldMouseButtons.clear();
                this.rivet.unfocus();
            }
        });
    }

    @Override
    protected void render(final Matrix4fStack matrix4fStack) {
        ThinGL.programs().getMsaa().bindInput();
        this.renderBackground(matrix4fStack);
        this.renderer.renderList(matrix4fStack, this.rivet.render());
        ThinGL.programs().getMsaa().unbindInput();
        ThinGL.programs().getMsaa().renderFullscreen();
        ThinGL.programs().getMsaa().clearInput();
    }

    protected void renderBackground(final Matrix4fStack matrix4fStack) {
        ThinGL.renderer2D().filledRectangle(matrix4fStack, 0, 0, ThinGL.windowInterface().getFramebufferWidth(), ThinGL.windowInterface().getFramebufferHeight(), Color.GRAY.darker().darker().darker().darker());
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
