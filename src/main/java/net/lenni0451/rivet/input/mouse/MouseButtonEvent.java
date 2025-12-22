package net.lenni0451.rivet.input.mouse;

import lombok.With;
import net.lenni0451.rivet.input.keyboard.ModifierKey;

import java.util.Set;

@With
public record MouseButtonEvent(float x, float y, MouseButton button, Set<ModifierKey> modifiers) {
}
