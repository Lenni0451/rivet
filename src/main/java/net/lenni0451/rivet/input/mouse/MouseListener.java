package net.lenni0451.rivet.input.mouse;

import net.lenni0451.rivet.math.Size;

public interface MouseListener {

    default void onMouseEnter() {
    }

    default void onMouseLeave() {
    }

    default void onMouseDown(final MouseButtonEvent event, final Size size) {
    }

    default void onMouseUp(final MouseButtonEvent event, final Size size) {
    }

    default void onMouseMove(final MouseMoveEvent event, final Size size) {
    }

    default void onMouseScroll(final MouseScrollEvent event, final Size size) {
    }

}
