import net.lenni0451.commons.logging.impl.SysoutLogger;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.impl.Button;
import net.lenni0451.rivet.container.impl.AbsoluteContainer;
import net.lenni0451.rivet.renderer.opengl.OpenGLRenderer;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.framebuffer.impl.TextureFramebuffer;
import net.raphimc.thingl.framebuffer.impl.WindowFramebuffer;
import net.raphimc.thingl.implementation.DebugMessageCallback;
import net.raphimc.thingl.implementation.GLFWWindowInterface;
import net.raphimc.thingl.implementation.StandaloneApplicationInterface;
import net.raphimc.thingl.wrapper.Blending;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;

public class Test implements Runnable {

    public static void main(String[] args) {
        new Test().run();
    }

    @Override
    public void run() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        final long window = GLFW.glfwCreateWindow(1280, 720, "ThinGL Example (" + this.getClass().getSimpleName() + ")", 0L, 0L);
        if (window == 0L) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();

        ThinGL.LOGGER = SysoutLogger.builder().name("ThinGL").build();
        ThinGL.setInstance(new ThinGL(thingl -> new StandaloneApplicationInterface(thingl) {
            {
                thinGL.getWindowInterface().addFramebufferResizeCallback(this::createProjectionMatrix);
                this.createProjectionMatrix(thinGL.getWindowInterface().getFramebufferWidth(), thinGL.getWindowInterface().getFramebufferHeight());
            }

            private void createProjectionMatrix(final int width, final int height) {
                this.projectionMatrixStack.setOrtho(0F, width, height, 0F, -5000F, 5000F);
                GL11C.glViewport(0, 0, width, height);
            }
        }, GLFWWindowInterface::new)); // Init ThinGL
        DebugMessageCallback.install(true); // Enable synchronous debug messages to ensure stack traces are correct (Only use this for debugging!)

        ThinGL.glStateManager().enable(GL11C.GL_BLEND);
        Blending.standardBlending();
        ThinGL.glStateManager().disable(GL11C.GL_DEPTH_TEST);
        ThinGL.glStateManager().setDepthFunc(GL11C.GL_LEQUAL);
        this.init(); // Initialize the example
        final TextureFramebuffer mainFramebuffer = new TextureFramebuffer(); // Create the main framebuffer
        final Matrix4fStack positionMatrix = new Matrix4fStack(8);

        while (!GLFW.glfwWindowShouldClose(window)) {
            ThinGL.get().onStartFrame(); // Let ThinGL know that the current frame is starting
            mainFramebuffer.bind(true); // Bind the main framebuffer
            mainFramebuffer.clear(); // Clear the main framebuffer

            positionMatrix.pushMatrix();
            this.render(positionMatrix); // Render the example
            positionMatrix.popMatrix();

            mainFramebuffer.unbind();
            mainFramebuffer.blitTo(WindowFramebuffer.INSTANCE, true, false, false); // Blit the main framebuffer to the window framebuffer
            ThinGL.get().onFinishFrame(); // Let ThinGL know that the current frame is done rendering and ready to be presented
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
            ThinGL.get().onEndFrame(); // Let ThinGL know that the current frame is done and the next frame can start
            // FPSLimiter.limitFPS(30); // Example to limit the FPS to 30 FPS
        }

        ThinGL.get().free(); // Destroy the ThinGL instance and free all resources
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    private Rivet rivet;

    private void init() {
        Button button = new Button("Testing", mouseButton -> System.out.println("CLICKED! Button: " + mouseButton));
        AbsoluteContainer rootContainer = new AbsoluteContainer();
        this.rivet = new Rivet(new OpenGLRenderer(), rootContainer, 1280, 720);
        rootContainer.add(button, 50, 50);
    }

    private void render(final Matrix4fStack positionMatrix) {
        this.rivet.render(positionMatrix);
    }

}
