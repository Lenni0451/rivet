package test.impl;

import lombok.AllArgsConstructor;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.layout.border.BorderLayout;
import net.lenni0451.rivet.layout.border.BorderPosition;
import net.lenni0451.rivet.layout.tile.TileLayout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import test.TestBase;

import java.util.function.Consumer;

public class StencilTest extends TestBase {

    void main() {
        this.run();
    }

    @Override
    protected void init(final Rivet rivet) {
        Container header = new Container(new TileLayout(2, 1));
        header.add(new Label("Stencil"));
        header.add(new Label("Inverse Stencil"));

        Container body = new Container(new TileLayout(2, 1));
        body.add(new StencilTestComponent(StencilType.STENCIL));
        body.add(new StencilTestComponent(StencilType.INVERSE));

        rivet.root().add(new Container(new BorderLayout())
                .add(header.layoutOptions(BorderPosition.TOP))
                .add(body.layoutOptions(BorderPosition.CENTER))
        );
    }


    @AllArgsConstructor
    private static class StencilTestComponent extends Component {
        private final StencilType type;

        @Override
        protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
            Consumer<Renderer> mask = s -> {
                s.fillCircle(size.width() / 2, size.height() / 2, System.currentTimeMillis() / 20 % 200, Color.WHITE);
            };
            Runnable render = () -> {
                renderer.fillRect(0, 0, size.width(), size.height(), Color.fromRGBA(0, 150, 0, 100));
                renderer.text(this.rivet().backend().font().shapeText("Testing Testing Testing Testing", Color.RED), size.width() / 2, size.height() / 2, TextOrigin.Horizontal.VISUAL_CENTER, TextOrigin.Vertical.VISUAL_CENTER);
            };
            switch (this.type) {
                case STENCIL -> renderer.stencil(mask, render);
                case INVERSE -> renderer.inverseStencil(mask, render);
            }
        }

        @Override
        public Size computeIdealSize(final Size constraints) {
            return constraints;
        }
    }

    private enum StencilType {
        STENCIL, INVERSE
    }

}
