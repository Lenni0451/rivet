package net.lenni0451.rivet.editor.editor;

import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.layout.anchor.AnchorLayout;
import net.lenni0451.rivet.math.Size;

public class ComponentField extends Container {

    public ComponentField() {
        super(AnchorLayout.INSTANCE);
    }

    @Override
    protected void onMouseLeaveInternal() {
        super.onMouseLeaveInternal();
    }

    @Override
    protected boolean onMouseMoveInternal(final MouseMoveEvent event, final Size size) {
        return super.onMouseMoveInternal(event, size);
    }

}
