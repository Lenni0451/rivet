package net.lenni0451.rivet.component;

public interface MouseListener {

    default void onMouseEnter() {
    }

    default void onMouseLeave() {
    }

    default void onMouseDown(final float mouseX, final float mouseY, final int button) {
    }

    default void onMouseUp(final float mouseX, final float mouseY, final int button) {
    }

    default void onMouseMove(final float mouseX, final float mouseY) {
    }

}
