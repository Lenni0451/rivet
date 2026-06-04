package test.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.FormattedLabel;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridLayoutOptions;
import net.lenni0451.rivet.text.model.TextOrigin;
import test.TestBase;

public class FormattedLabelWrappingTest extends TestBase {

    static void main() {
        new FormattedLabelWrappingTest().run();
    }

    @Override
    protected void init(final Rivet rivet) {
        Container container = new Container(new GridLayout(10, 10));
        container.addChild(new FormattedLabel("<color=red italic bold underlined>Hello this is a really cool test string how are you doing lol\n<color=blue> <color=red>a\n\naaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"), label -> {
            label.horizontalOrigin(TextOrigin.Horizontal.VISUAL_LEFT);
            label.layoutOptions(new GridLayoutOptions(0, 0).withFill(GridFill.HORIZONTAL).withWeightX(1));
        });
        container.addChild(new Button("Testing Testing Testing Testing", () -> {
            System.out.println("click");
        }), button -> {
            button.clickOn().set(Button.ClickOn.BOTH);
            button.layoutOptions(new GridLayoutOptions(0, 1).withFill(GridFill.HORIZONTAL).withWeightX(1));
        });
        rivet.root().addChild(container);
    }

}
