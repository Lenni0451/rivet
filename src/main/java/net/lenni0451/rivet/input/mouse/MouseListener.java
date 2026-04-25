package net.lenni0451.rivet.input.mouse;

import net.lenni0451.rivet.math.Rectangle;

public interface MouseListener {

    default void onMouseEnter() {
    }

    default void onMouseLeave() {
    }

    default void onMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
    }

    default void onMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
    }

    default void onMouseMove(final MouseMoveEvent event, final Rectangle bounds) {
    }

    default void onMouseScroll(final MouseScrollEvent event, final Rectangle bounds) {
    }

}
