package net.lenni0451.rivet.input.keyboard;

public interface KeyboardListener {

    default void onKeyDown(final KeyEvent event) {
    }

    default void onKeyUp(final KeyEvent event) {
    }

    default void onCharTyped(final CharEvent event) {
    }

}
