package net.lenni0451.rivet.input.mouse;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.input.keyboard.ModifierKey;

import java.util.Set;

/**
 * @param x           The x position of the mouse cursor relative to the component that received the event
 * @param y           The y position of the mouse cursor relative to the component that received the event
 * @param button      The button that was pressed or released
 * @param modifiers   The modifier keys that were held down when the event occurred
 * @param heldButtons The set of mouse buttons that are currently held, including the button that triggered the event
 */
@With
@WithBy
public record MouseButtonEvent(float x, float y, MouseButton button, Set<ModifierKey> modifiers, Set<MouseButton> heldButtons) {

    public MouseButtonEvent {
        modifiers = Set.copyOf(modifiers);
        heldButtons = Set.copyOf(heldButtons);
    }

}
