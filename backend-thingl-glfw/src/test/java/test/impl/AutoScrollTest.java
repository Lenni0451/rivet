package test.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.text.model.TextOrigin;
import test.TestBase;
import test.TestTheme;

public class AutoScrollTest extends TestBase {

    void main() {
        this.run();
    }

    @Override
    protected void init(final Rivet rivet) {
        rivet.theme(new TestTheme());
        Container container = new Container(new VerticalListLayout(10, true));
        rivet.root().addChild(new ScrollContainer(container, true, true).autoScroll(true));
        Thread.ofVirtual().start(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    final int finalI = i;
                    rivet.runSync(() -> {
                        container.addChild(new Label("Label " + finalI).horizontalOrigin(TextOrigin.Horizontal.VISUAL_RIGHT));
                    });
                    Thread.sleep(500);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

}
