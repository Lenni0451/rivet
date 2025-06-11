package net.lenni0451.rivet;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.KeyboardListener;
import net.lenni0451.rivet.container.Container;
import org.joml.Vector2f;

import javax.annotation.Nullable;

public class RootContainer {

    private final Vector2f size = new Vector2f();
    private Container rootContainer;
    private Component focusedComponent;

    public RootContainer(final Container rootContainer) {
        this.rootContainer = rootContainer;
    }

    public Vector2f getSize() {
        return this.size;
    }

    public void setSize(final float width, final float height) {
        this.size.set(width, height);
    }

    public Container getRootContainer() {
        return this.rootContainer;
    }

    public void setRootContainer(final Container rootContainer) {
        this.rootContainer = rootContainer;
    }

    public Component getFocusedComponent() {
        return this.focusedComponent;
    }

    public void setFocusedComponent(@Nullable final Component component) {
        if (this.focusedComponent == component) return;
        if (this.focusedComponent != null) {
            this.focusedComponent.onFocusLost();
        }
        this.focusedComponent = component;
        if (component != null) {
            component.onFocusGained();
        }
    }

    public void onMouseDown(final float mouseX, final float mouseY, final int button) {
        if (mouseX < 0 || mouseX >= this.size.x || mouseY < 0 || mouseY >= this.size.y) return;
        this.rootContainer.onMouseDown(mouseX, mouseY, button);
    }

    public void onMouseUp(final float mouseX, final float mouseY, final int button) {
        if (mouseX < 0 || mouseX >= this.size.x || mouseY < 0 || mouseY >= this.size.y) return;
        this.rootContainer.onMouseUp(mouseX, mouseY, button);
    }

    public void onMouseMove(final float mouseX, final float mouseY) {
        if (mouseX < 0 || mouseX >= this.size.x || mouseY < 0 || mouseY >= this.size.y) return;
        this.rootContainer.onMouseMove(mouseX, mouseY);
    }

    public void onKeyDown(final int key, final int scancode, final int action, final int modifier) {
        if (this.focusedComponent instanceof KeyboardListener keyboardListener) {
            keyboardListener.onKeyDown(key, scancode, action, modifier);
        }
    }

    public void onKeyUp(final int key, final int scancode, final int action, final int modifier) {
        if (this.focusedComponent instanceof KeyboardListener keyboardListener) {
            keyboardListener.onKeyUp(key, scancode, action, modifier);
        }
    }

    public void onCharTyped(final int codepoint) {
        if (this.focusedComponent instanceof KeyboardListener keyboardListener) {
            keyboardListener.onCharTyped(codepoint);
        }
    }

}
