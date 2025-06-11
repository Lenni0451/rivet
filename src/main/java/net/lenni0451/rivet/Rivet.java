package net.lenni0451.rivet;

import net.lenni0451.commons.logging.Logger;
import net.lenni0451.commons.logging.LoggerFactory;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.KeyboardListener;
import net.lenni0451.rivet.container.Container;
import net.lenni0451.rivet.math.impl.ExtendedVector2f;
import net.lenni0451.rivet.renderer.Renderer;
import org.joml.Matrix4fStack;
import org.joml.Vector2f;

import javax.annotation.Nullable;

public class Rivet {

    public static Logger LOGGER = LoggerFactory.getLogger("Rivet");

    private final Renderer renderer;
    private final ExtendedVector2f size = new ExtendedVector2f();
    private Container rootContainer;
    private Component focusedComponent;

    public Rivet(final Renderer renderer, final Container rootContainer) {
        this.renderer = renderer;
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

    public void render() {
        this.render(new Matrix4fStack(8));
    }

    public void render(final Matrix4fStack positionMatrix) {
        this.rootContainer.render(this.renderer, positionMatrix, this.size);
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
