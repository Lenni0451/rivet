package net.lenni0451.rivet.component;

public interface KeyboardListener {

    void keyDown(final int key, final int scancode, final int action, final int modifier);

    void keyUp(final int key, final int scancode, final int action, final int modifier);

    void charTyped(final int codepoint, final int modifier);

}
