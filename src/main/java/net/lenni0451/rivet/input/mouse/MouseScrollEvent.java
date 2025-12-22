package net.lenni0451.rivet.input.mouse;

import lombok.With;

@With
public record MouseScrollEvent(float x, float y, float scrollX, float scrollY) {
}
