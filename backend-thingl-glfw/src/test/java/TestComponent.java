import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.input.mouse.MouseListener;
import net.lenni0451.rivet.math.Size;

public class TestComponent extends Component implements Renderable, MouseListener {

    private boolean mouseOver = false;

    public TestComponent(final Rivet rivet) {
        super(rivet);
    }

    @Override
    public void computeIdealSize() {
        this.idealSize = new Size(0, 0);
    }

    @Override
    public void computeLayout(Size size) {
    }

    @Override
    public void onMouseEnter() {
        this.mouseOver = true;
    }

    @Override
    public void onMouseLeave() {
        this.mouseOver = false;
    }

    @Override
    public void render(Renderer renderer, Size size) {
        renderer.fillRect(0, 0, size.width(), size.height(), this.mouseOver ? Color.GREEN : Color.RED);
    }

}
