package net.lenni0451.rivet.component;

public interface KeyboardListener {

    void onKeyDown(final int key, final int scancode, final int action, final int modifier);

    void onKeyUp(final int key, final int scancode, final int action, final int modifier);

    void onCharTyped(final int codepoint);

}
