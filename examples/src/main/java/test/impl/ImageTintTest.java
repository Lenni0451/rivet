package test.impl;

import lombok.SneakyThrows;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.ColorPicker;
import net.lenni0451.rivet.component.impl.Image;
import net.lenni0451.rivet.layout.border.BorderLayout;
import net.lenni0451.rivet.layout.border.BorderPosition;
import test.TestBase;

public class ImageTintTest extends TestBase {

    void main() {
        this.run();
    }

    @Override
    @SneakyThrows
    protected void init(final Rivet rivet) {
        ColorPicker tintPicker = new ColorPicker(Color.WHITE);
        Image image = new Image(rivet.backend().assetLoader().loadTexture(ImageTintTest.class.getClassLoader().getResourceAsStream("image.jpg")));
        tintPicker.colorChangeListener().add(image::color);

        Container container = new Container(BorderLayout.DEFAULT);
        container.add(tintPicker.layoutOptions(BorderPosition.TOP));
        container.add(image.scaleMode(Image.ScaleMode.FIT).layoutOptions(BorderPosition.CENTER));
        rivet.root().add(container);
    }

}
