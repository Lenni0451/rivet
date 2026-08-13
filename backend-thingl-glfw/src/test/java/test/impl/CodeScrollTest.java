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
        buttonContainer.add(new Button("print", () -> {
            System.out.println("scrollX: " + sc.scrollX() + ", scrollY: " + sc.scrollY() + ", maxScrollX: " + sc.maxScrollX() + ", maxScrollY: " + sc.maxScrollY());
        }));
        boolean[] instant = {false};
        buttonContainer.add(new Button("0%", () -> sc.scrollY(0, instant[0])));
        buttonContainer.add(new Button("25%", () -> sc.scrollY(sc.maxScrollY() * 0.25F, instant[0])));
        buttonContainer.add(new Button("50%", () -> sc.scrollY(sc.maxScrollY() * 0.5F, instant[0])));
        buttonContainer.add(new Button("75%", () -> sc.scrollY(sc.maxScrollY() * 0.75F, instant[0])));
        buttonContainer.add(new Button("100%", () -> sc.scrollY(sc.maxScrollY(), instant[0])));
        buttonContainer.add(new Checkbox("instant", instant[0]), cb -> cb.toggleListener().add(s -> instant[0] = s));
        container.add(buttonContainer.layoutOptions(BorderPosition.TOP));
        for (int i = 0; i < 100; i++) {
            labelContainer.add(new Label("Label " + i));
        }
        container.add(sc.layoutOptions(BorderPosition.CENTER));
        rivet.root().add(container);
    }

}
