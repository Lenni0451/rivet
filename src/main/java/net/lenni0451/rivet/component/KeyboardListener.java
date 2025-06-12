package net.lenni0451.rivet.component;

public interface KeyboardListener {

    default void onKeyDown(final int key, final int scancode, final int action, final int modifier) {
    }

    default void onKeyUp(final int key, final int scancode, final int action, final int modifier) {
    }

    default void onCharTyped(final int codepoint) {
    }

}
