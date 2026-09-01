package net.lenni0451.rivet.editor.properties.impl;

import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.ToggleSwitch;
import net.lenni0451.rivet.layout.grid.GridAnchor;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridOptions;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BooleanPropertyComponent extends Container {

    public BooleanPropertyComponent(final String name, final Supplier<Boolean> getter, final Consumer<Boolean> setter) {
        super(new GridLayout(2, 2));

        ToggleSwitch toggleSwitch = new ToggleSwitch(getter.get());
        toggleSwitch.toggleListener().add(setter);

        this.add(new Label(name).layoutOptions(new GridOptions(0, 0).withAnchor(GridAnchor.LEFT).withWeightX(1)));
        this.add(toggleSwitch.layoutOptions(new GridOptions(1, 0).withAnchor(GridAnchor.RIGHT)));
    }

}
