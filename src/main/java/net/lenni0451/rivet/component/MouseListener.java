package net.lenni0451.rivet.component;

public interface MouseListener {

    default void onMouseEnter() {
    }

    default void onMouseLeave() {
    }

    default void onMouseDown(final float mouseX, final float mouseY, final int button, final int modifiers) {
    }

    default void onMouseUp(final float mouseX, final float mouseY, final int button, final int modifiers) {
    }

    default void onMouseMove(final float mouseX, final float mouseY) {
    }

    default void onMouseScroll(final float mouseX, final float mouseY, final float scrollX, final float scrollY) {
    }

}
