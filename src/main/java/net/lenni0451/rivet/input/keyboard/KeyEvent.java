package net.lenni0451.rivet.input.keyboard;

import lombok.With;

import java.util.Set;

@With
public record KeyEvent(Key key, Set<ModifierKey> modifiers) {
}
