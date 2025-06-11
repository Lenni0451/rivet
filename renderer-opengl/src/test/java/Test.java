import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.impl.Button;
import net.lenni0451.rivet.container.impl.AbsoluteContainer;
import net.lenni0451.rivet.renderer.opengl.OpenGLRenderer;
import net.raphimc.thingl.implementation.application.StandaloneApplicationRunner;
import org.joml.Matrix4fStack;

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
        Button button = new Button("Testing", mouseButton -> System.out.println("CLICKED! Button: " + mouseButton));
        AbsoluteContainer rootContainer = new AbsoluteContainer();
        this.rivet = new Rivet(new OpenGLRenderer(), rootContainer, 1280, 720);
        rootContainer.add(button, 50, 50);
    }

    @Override
    protected void render(final Matrix4fStack positionMatrix) {
        this.rivet.render(positionMatrix);
    }

}
