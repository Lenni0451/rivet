package net.lenni0451.rivet.editor.properties.impl;

import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.layout.grid.GridAnchor;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridOptions;

public class IntegerPropertyComponent extends Container {

    public IntegerPropertyComponent(final String name) {
        super(new GridLayout(2, 2));
        this.addChild(new Label(name).layoutOptions(new GridOptions(0, 0).withAnchor(GridAnchor.LEFT)));
        this.addChild(new Slider(0, 100, 1, 10).layoutOptions(new GridOptions(0, 1).withWeightX(1).withFill(GridFill.HORIZONTAL)));
    }

}
