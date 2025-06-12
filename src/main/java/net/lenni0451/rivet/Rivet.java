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
    private final Vector2f unscaledSize = new Vector2f();
    private final ExtendedVector2f scaledSize = new ExtendedVector2f();
    private Container rootContainer;
    private float scaleFactor;
    private Component focusedComponent;

    public Rivet(final Renderer renderer, final Container rootContainer) {
        this.renderer = renderer;
        this.rootContainer = rootContainer;
    }

    public Rivet(final Renderer renderer, final Container rootContainer, final float width, final float height) {
        this.renderer = renderer;
        this.setRootContainer(rootContainer);
        this.unscaledSize.set(width, height);
        this.setScaleFactor(1F);
    }

    public Vector2f getUnscaledSize() {
        return this.unscaledSize;
    }

    public Vector2f getScaledSize() {
        return this.scaledSize;
    }

    public void setSize(final float width, final float height) {
        this.unscaledSize.set(width, height);
        this.scaledSize.set(this.unscaledSize).div(this.scaleFactor);
    }

    public Container getRootContainer() {
        return this.rootContainer;
    }

    public void setRootContainer(final Container rootContainer) {
        this.rootContainer = rootContainer;
        this.rootContainer.onAdded(this, null);
    }

    public float getScaleFactor() {
        return this.scaleFactor;
    }

    public void setScaleFactor(final float scaleFactor) {
        this.scaleFactor = scaleFactor;
        this.scaledSize.set(this.unscaledSize).div(scaleFactor);
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
        positionMatrix.pushMatrix();
        positionMatrix.scale(this.scaleFactor);
        this.rootContainer.render(this.renderer, positionMatrix, this.scaledSize);
        positionMatrix.popMatrix();
    }

    public void onMouseDown(final float mouseX, final float mouseY, final int button, final int modifiers) {
        if (mouseX < 0 || mouseX >= this.unscaledSize.x || mouseY < 0 || mouseY >= this.unscaledSize.y) return;
        this.rootContainer.onMouseDown(mouseX / this.scaleFactor, mouseY / this.scaleFactor, button, modifiers);
    }

    public void onMouseUp(final float mouseX, final float mouseY, final int button, final int modifiers) {
        if (mouseX < 0 || mouseX >= this.unscaledSize.x || mouseY < 0 || mouseY >= this.unscaledSize.y) return;
        this.rootContainer.onMouseUp(mouseX / this.scaleFactor, mouseY / this.scaleFactor, button, modifiers);
    }

    public void onMouseMove(final float mouseX, final float mouseY) {
        if (mouseX < 0 || mouseX >= this.unscaledSize.x || mouseY < 0 || mouseY >= this.unscaledSize.y) return;
        this.rootContainer.onMouseMove(mouseX / this.scaleFactor, mouseY / this.scaleFactor);
    }

    public void onMouseScroll(final float mouseX, final float mouseY, final float scrollX, final float scrollY) {
        if (mouseX < 0 || mouseX >= this.unscaledSize.x || mouseY < 0 || mouseY >= this.unscaledSize.y) return;
        this.rootContainer.onMouseScroll(mouseX / this.scaleFactor, mouseY / this.scaleFactor, scrollX, scrollY);
    }

    public void onKeyDown(final int key, final int scancode, final int action, final int modifiers) {
        if (this.focusedComponent instanceof KeyboardListener keyboardListener) {
            keyboardListener.onKeyDown(key, scancode, action, modifiers);
        }
    }

    public void onKeyUp(final int key, final int scancode, final int action, final int modifiers) {
        if (this.focusedComponent instanceof KeyboardListener keyboardListener) {
            keyboardListener.onKeyUp(key, scancode, action, modifiers);
        }
    }

    public void onCharTyped(final int codepoint) {
        if (this.focusedComponent instanceof KeyboardListener keyboardListener) {
            keyboardListener.onCharTyped(codepoint);
        }
    }

}
