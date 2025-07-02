package net.lenni0451.rivet.component;

public interface KeyboardListener {

    default void onKeyDown(final int key, final int modifiers) {
    }

    default void onKeyUp(final int key, final int modifiers) {
    }

    default void onCharTyped(final char c) {
    }

}
