package net.lenni0451.rivet.input.mouse;

public interface MouseListener {

    default void onMouseEnter() {
    }

    default void onMouseLeave() {
    }

    default void onMouseDown(final MouseButtonEvent event) {
    }

    default void onMouseUp(final MouseButtonEvent event) {
    }

    default void onMouseMove(final MouseMoveEvent event) {
    }

    default void onMouseScroll(final MouseScrollEvent event) {
    }

}
