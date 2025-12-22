package net.lenni0451.rivet;

import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Container;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.keyboard.KeyboardListener;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Size;

public class Rivet {

    private final Backend backend;
    private Container rootContainer;
    private Size size;
    private float scale = 1;
    private Component focusedComponent;
    private boolean recalculate = false;

    public Rivet(final Backend backend, final Layout layout, final Size size) {
        this.backend = backend;
        this.rootContainer = new Container(this, layout);
        this.size = size;
    }

    public Backend getBackend() {
        return this.backend;
    }

    public Container getRootContainer() {
        return this.rootContainer;
    }

    public void setRootContainer(final Container rootContainer) {
        this.rootContainer = rootContainer;
        this.recalculate = true;
    }

    public Size getSize() {
        return this.size;
    }

    public void setSize(final Size size) {
        this.size = size;
        this.recalculate = true;
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(final float scale) {
        this.scale = scale;
        this.recalculate = true;
    }

    public Component getFocused() {
        return this.focusedComponent;
    }

    public void setFocused(final Component component) {
        if (this.focusedComponent == component) return;
        if (this.focusedComponent != null) {
            this.focusedComponent.onFocusLost();
        }
        this.focusedComponent = component;
        if (component != null) {
            component.onFocusGained();
        }
    }

    public void recalculateNextFrame() {
        this.recalculate = true;
    }


    public void onKeyDown(final KeyEvent event) {
        if (this.focusedComponent instanceof KeyboardListener keyboardListener) {
            keyboardListener.onKeyDown(event);
        }
    }

    public void onKeyUp(final KeyEvent event) {
        if (this.focusedComponent instanceof KeyboardListener keyboardListener) {
            keyboardListener.onKeyUp(event);
        }
    }

    public void onCharTyped(final CharEvent event) {
        if (this.focusedComponent instanceof KeyboardListener keyboardListener) {
            keyboardListener.onCharTyped(event);
        }
    }

    public void onMouseDown(final MouseButtonEvent event) {
        if (event.x() < 0 || event.x() >= this.size.width()) return;
        if (event.y() < 0 || event.y() >= this.size.height()) return;
        this.rootContainer.onMouseDown(event.withX(event.x() / this.scale).withY(event.y() / this.scale));
    }

    public void onMouseUp(final MouseButtonEvent event) {
        if (event.x() < 0 || event.x() >= this.size.width()) return;
        if (event.y() < 0 || event.y() >= this.size.height()) return;
        this.rootContainer.onMouseUp(event.withX(event.x() / this.scale).withY(event.y() / this.scale));
    }

    public void onMouseMove(final MouseMoveEvent event) {
        if (event.x() < 0 || event.x() >= this.size.width()) return;
        if (event.y() < 0 || event.y() >= this.size.height()) return;
        this.rootContainer.onMouseMove(event.withX(event.x() / this.scale).withY(event.y() / this.scale));
    }

    public void onMouseScroll(final MouseScrollEvent event) {
        if (event.x() < 0 || event.x() >= this.size.width()) return;
        if (event.y() < 0 || event.y() >= this.size.height()) return;
        this.rootContainer.onMouseScroll(event.withX(event.x() / this.scale).withY(event.y() / this.scale));
    }

    public void render(final Renderer renderer) {
        Size scaledSize = this.size.scale(this.scale, this.scale);
        if (this.recalculate) {
            this.rootContainer.computeIdealSize();
            this.rootContainer.computeLayout(scaledSize);
            this.recalculate = false;
        }

        renderer.push();
        renderer.scale(this.scale);
        this.rootContainer.render(renderer, scaledSize);
        renderer.pop();
    }

}
