package net.lenni0451.rivet.input.mouse;

import lombok.With;
import lombok.experimental.WithBy;

import java.util.Set;

@With
@WithBy
public record MouseMoveEvent(float x, float y, Set<MouseButton> buttons) {

    public MouseMoveEvent {
        buttons = Set.copyOf(buttons);
    }

    public boolean isHeld(final MouseButton button) {
        return this.buttons.contains(button);
    }

}
