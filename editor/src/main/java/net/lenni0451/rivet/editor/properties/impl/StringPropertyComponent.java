package net.lenni0451.rivet.editor.properties.impl;

import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.TextField;
import net.lenni0451.rivet.layout.grid.GridAnchor;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridOptions;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringPropertyComponent extends Container {

    public StringPropertyComponent(final String name, final Supplier<String> getter, final Consumer<String> setter) {
        super(new GridLayout(2, 2));

        TextField textField = new TextField(getter.get());
        textField.valueChangeListener().add(setter);

        this.addChild(new Label(name).layoutOptions(new GridOptions(0, 0).withAnchor(GridAnchor.LEFT)));
        this.addChild(textField.layoutOptions(new GridOptions(0, 1).withWeightX(1).withFill(GridFill.HORIZONTAL)));
    }

}
