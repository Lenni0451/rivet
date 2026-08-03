package net.lenni0451.rivet.editor.properties.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.container.ComboBox;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.ColorPicker;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.SolidColor;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorPropertyComponent extends ComboBox {

    public ColorPropertyComponent(final String name, final Supplier<Color> getter, final Consumer<Color> setter) {
        super(
                new Container(FullSizeLayout.INSTANCE)
                        .addChild(new SolidColor(getter.get()))
                        .addChild(new Label(name)),
                new ColorPicker(getter.get())
        );

        SolidColor preview = (SolidColor) ((Container) this.button().child()).children().get(0);
        ColorPicker colorPicker = (ColorPicker) this.child();
        colorPicker.colorChangeListener().add(color -> {
            preview.color(color);
            setter.accept(color);
        });
    }

}
