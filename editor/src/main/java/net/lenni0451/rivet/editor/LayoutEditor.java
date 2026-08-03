package net.lenni0451.rivet.editor;

import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.editor.editor.ComponentField;
import net.lenni0451.rivet.layout.border.BorderLayout;
import net.lenni0451.rivet.layout.border.BorderPosition;

public class LayoutEditor extends Container {

    public LayoutEditor() {
        super(BorderLayout.DEFAULT);

        this.addChild(new ComponentField().addChild(new Button("Test 1")).addChild(new Label("Test 2")).layoutOptions(BorderPosition.CENTER));
    }

}
