package net.lenni0451.rivet.input;

import net.lenni0451.rivet.input.mouse.MouseButton;

import java.util.HashSet;
import java.util.Set;

public class ClickedElement<E> {

    private E element;
    private final Set<MouseButton> buttons = new HashSet<>();

    public boolean is(final E element) {
        return this.element == element;
    }

    public void unset() {
        this.element = null;
        this.buttons.clear();
    }

    public void down(final E element, final MouseButton button) {
        if (this.element != element) {
            this.element = element;
            this.buttons.clear();
        }
        this.buttons.add(button);
    }

    public void up(final MouseButton button) {
        this.buttons.remove(button);
        if (this.buttons.isEmpty()) {
            this.element = null;
        }
    }

}
