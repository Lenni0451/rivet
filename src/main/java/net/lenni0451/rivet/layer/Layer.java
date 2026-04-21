package net.lenni0451.rivet.layer;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.component.Container;

@With
@WithBy
public record Layer(Container container, int priority) {

    public static final int BASE_LAYER = 0;
    public static final int OVERLAY = 100;
    public static final int TOOLTIP = 200;

    public Layer {
        if (priority < BASE_LAYER) {
            throw new IllegalArgumentException("Layer priority " + priority + " cannot be behind base layer (" + BASE_LAYER + ")");
        }
    }

}
