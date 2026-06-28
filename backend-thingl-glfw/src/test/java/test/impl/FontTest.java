package test.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.thingl.text.ThinGLFont;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.TextField;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import test.TestBase;

import java.io.FileInputStream;

public class FontTest extends TestBase {

    static void main() {
        new FontTest().run();
    }

    @Override
    protected void init(final Rivet rivet) {
        Slider slider = new Slider(1, 200, 40);
        TextField textField = new TextField();
        Label label = new Label("Hello World").font(rivet.backend().font());

        slider.valueChangeListener().add(value -> label.font(label.font().derive(value.intValue())));
        textField.keyDownListener().add(event -> {
            if (event.key().isEquivalent(Key.ENTER)) {
                try (FileInputStream fis = new FileInputStream(textField.text())) {
                    ThinGLFont font = new ThinGLFont(createFont((int) slider.value(), fis));
                    slider.font(font.derive(40));
                    textField.font(font.derive(40));
                    label.font(font);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                return true;
            }
            return false;
        });

        rivet.root().addChild(new Container(new VerticalListLayout(5, true))
                .addChild(slider)
                .addChild(textField)
                .addChild(label));
    }

}
