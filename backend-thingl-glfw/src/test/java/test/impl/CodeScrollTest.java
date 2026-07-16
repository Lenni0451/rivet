package test.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.component.impl.Checkbox;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.layout.border.BorderLayout;
import net.lenni0451.rivet.layout.border.BorderPosition;
import net.lenni0451.rivet.layout.flow.HorizontalFlowLayout;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import test.TestBase;

public class CodeScrollTest extends TestBase {

    void main() {
        this.run();
    }

    @Override
    protected void init(final Rivet rivet) {
        Container container = new Container(BorderLayout.DEFAULT);
        Container labelContainer = new Container(new VerticalListLayout());
        ScrollContainer sc = new ScrollContainer(labelContainer);
        Container buttonContainer = new Container(new HorizontalFlowLayout());
        buttonContainer.addChild(new Button("print", () -> {
            System.out.println("scrollX: " + sc.scrollX() + ", scrollY: " + sc.scrollY() + ", maxScrollX: " + sc.maxScrollX() + ", maxScrollY: " + sc.maxScrollY());
        }));
        boolean[] instant = {false};
        buttonContainer.addChild(new Button("0%", () -> sc.scrollY(0, instant[0])));
        buttonContainer.addChild(new Button("25%", () -> sc.scrollY(sc.maxScrollY() * 0.25F, instant[0])));
        buttonContainer.addChild(new Button("50%", () -> sc.scrollY(sc.maxScrollY() * 0.5F, instant[0])));
        buttonContainer.addChild(new Button("75%", () -> sc.scrollY(sc.maxScrollY() * 0.75F, instant[0])));
        buttonContainer.addChild(new Button("100%", () -> sc.scrollY(sc.maxScrollY(), instant[0])));
        buttonContainer.addChild(new Checkbox("instant", instant[0]), cb -> cb.toggleListener().add(s -> instant[0] = s));
        container.addChild(buttonContainer.layoutOptions(BorderPosition.TOP));
        for (int i = 0; i < 100; i++) {
            labelContainer.addChild(new Label("Label " + i));
        }
        container.addChild(sc.layoutOptions(BorderPosition.CENTER));
        rivet.root().addChild(container);
    }

}
