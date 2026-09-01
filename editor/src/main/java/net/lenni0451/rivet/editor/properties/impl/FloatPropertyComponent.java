package net.lenni0451.rivet.editor.properties.impl;

import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.layout.grid.GridAnchor;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridOptions;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FloatPropertyComponent extends Container {

    public FloatPropertyComponent(final String name, final Supplier<Float> getter, final Consumer<Float> setter) {
        super(new GridLayout(2, 2));

        Label valueLabel = new Label(String.valueOf(getter.get()));
        Slider slider = new Slider(0, 100, 1, getter.get()); //TODO: Min, max and step should be configurable
        slider.valueChangeListener().add(value -> {
            valueLabel.text(String.valueOf(value.floatValue()));
            setter.accept(value.floatValue());
        });

        this.add(new Label(name).layoutOptions(new GridOptions(0, 0).withAnchor(GridAnchor.LEFT).withWeightX(1)));
        this.add(valueLabel.layoutOptions(new GridOptions(1, 0).withAnchor(GridAnchor.RIGHT)));
        this.add(slider.layoutOptions(new GridOptions(0, 1).withWeightX(1).withFill(GridFill.HORIZONTAL).withColumnSpan(2)));
    }

}
