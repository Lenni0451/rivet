package test.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import test.TestBase;

public class StencilTest extends TestBase {

    void main() {
        this.run();
    }

    @Override
    protected void init(final Rivet rivet) {
        rivet.root().addChild(new StencilTestComponent());
    }


    private static class StencilTestComponent extends Component {
        @Override
        public void render(final Renderer renderer, final Size size) {
            renderer.stencil(s -> {
                s.fillCircle(size.width() / 2, size.height() / 2, System.currentTimeMillis() / 20 % 200, Color.WHITE);
            }, () -> {
                renderer.fillRect(0, 0, size.width(), size.height(), Color.fromRGBA(0, 150, 0, 100));
                renderer.text(this.rivet().backend().font().shapeText("Testing Testing Testing Testing", Color.RED), size.width() / 2, size.height() / 2, TextOrigin.Horizontal.VISUAL_CENTER, TextOrigin.Vertical.VISUAL_CENTER);
            });
        }

        @Override
        public Size computeIdealSize(final Size constraints) {
            return constraints;
        }
    }

}
