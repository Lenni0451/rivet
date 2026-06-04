package test.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.component.impl.Separator;
import net.lenni0451.rivet.component.impl.SolidColor;
import net.lenni0451.rivet.layout.anchor.AnchorLayout;
import net.lenni0451.rivet.layout.anchor.AnchorLayoutOptions;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import test.TestBase;

public class AnchorLayoutTest extends TestBase {

    static void main() {
        new AnchorLayoutTest().run();
    }

    @Override
    protected void init(final Rivet rivet) {
        Container container = new Container(AnchorLayout.INSTANCE);
        container.addChild(new SolidColor(), c -> {
            c.color(Color.RED);
            c.layoutOptions(AnchorLayoutOptions.EMPTY.from(0, 0.7F).to(1, 1));
        });
        container.addChild(new SolidColor(), c -> {
            c.color(Color.GREEN);
            c.layoutOptions(AnchorLayoutOptions.EMPTY.from(0, 0).to(0.2F, 0.7F));
        });
        container.addChild(new ScrollContainer(new Container(new VerticalListLayout(5, true)), c -> {
            for (int i = 0; i < 10; i++) {
                if (i == 5) {
                    c.addChild(new Separator());
                }
                c.addChild(new Button("Button " + i, () -> {}));
            }
        }), c -> {
            c.layoutOptions(AnchorLayoutOptions.EMPTY.from(0.7F, 0).to(1, 0.7F));
        });
        container.addChild(new SolidColor(), c -> {
            c.color(Color.BLUE);
            c.layoutOptions(AnchorLayoutOptions.EMPTY.from(0.2F, 0).to(0.7F, 0.7F));
        });
        rivet.root().addChild(container);
    }

}
