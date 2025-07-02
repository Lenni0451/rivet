package net.lenni0451.rivet;

import net.lenni0451.commons.logging.Logger;
import net.lenni0451.commons.logging.LoggerFactory;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.KeyboardListener;
import net.lenni0451.rivet.container.Container;
import net.lenni0451.rivet.math.impl.ExtendedVector2f;
import net.lenni0451.rivet.text.FontSet;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4fStack;
import org.joml.Vector2f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Rivet {

    public static Logger LOGGER = LoggerFactory.getLogger("Rivet");

    private final Backend backend;
    private final FontSet defaultFonts;
    private final Vector2f unscaledSize = new Vector2f();
    private final ExtendedVector2f scaledSize = new ExtendedVector2f();
    private final List<Runnable> eventQueue = new ArrayList<>();
    private Container rootContainer;
    private float scaleFactor;
    private Component focusedComponent;

    public Rivet(final Backend backend, final FontSet defaultFonts, final Container rootContainer) {
        this.backend = backend;
        this.defaultFonts = defaultFonts;
        this.rootContainer = rootContainer;
    }

    public Rivet(final Backend backend, final FontSet defaultFonts, final Container rootContainer, final float width, final float height) {
        this.backend = backend;
        this.defaultFonts = defaultFonts;
        this.setRootContainer(rootContainer);
        this.unscaledSize.set(width, height);
        this.setScaleFactor(1F);
    }

    public Backend getBackend() {
        return this.backend;
    }

    public FontSet getDefaultFonts() {
        return this.defaultFonts;
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
        this.computeLayout();
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

    @ApiStatus.Internal
    public void computeLayout() {
        this.rootContainer.computeLayout(this.getScaledSize());
    }

    public void render() {
        this.render(new Matrix4fStack(8));
    }

    public void render(final Matrix4fStack positionMatrix) {
        List<Runnable> actions;
        synchronized (this.eventQueue) {
            actions = List.copyOf(this.eventQueue);
            this.eventQueue.clear();
        }
        for (Runnable runnable : actions) {
            runnable.run();
        }

        positionMatrix.pushMatrix();
        positionMatrix.scale(this.scaleFactor);
        this.rootContainer.render(this.backend.getRenderer(), positionMatrix, this.scaledSize);
        positionMatrix.popMatrix();
    }

    public void onMouseDown(final float mouseX, final float mouseY, final int button, final int modifiers) {
        if (mouseX < 0 || mouseX >= this.unscaledSize.x || mouseY < 0 || mouseY >= this.unscaledSize.y) return;
        if (this.backend.isOnRenderThread()) {
            this.rootContainer.onMouseDown(mouseX / this.scaleFactor, mouseY / this.scaleFactor, button, modifiers);
        } else {
            synchronized (this.eventQueue) {
                this.eventQueue.add(() -> {
                    this.rootContainer.onMouseDown(mouseX / this.scaleFactor, mouseY / this.scaleFactor, button, modifiers);
                });
            }
        }
    }

    public void onMouseUp(final float mouseX, final float mouseY, final int button, final int modifiers) {
        if (mouseX < 0 || mouseX >= this.unscaledSize.x || mouseY < 0 || mouseY >= this.unscaledSize.y) return;
        if (this.backend.isOnRenderThread()) {
            this.rootContainer.onMouseUp(mouseX / this.scaleFactor, mouseY / this.scaleFactor, button, modifiers);
        } else {
            synchronized (this.eventQueue) {
                this.eventQueue.add(() -> {
                    this.rootContainer.onMouseUp(mouseX / this.scaleFactor, mouseY / this.scaleFactor, button, modifiers);
                });
            }
        }
    }

    public void onMouseMove(final float mouseX, final float mouseY) {
        if (mouseX < 0 || mouseX >= this.unscaledSize.x || mouseY < 0 || mouseY >= this.unscaledSize.y) return;
        if (this.backend.isOnRenderThread()) {
            this.rootContainer.onMouseMove(mouseX / this.scaleFactor, mouseY / this.scaleFactor);
        } else {
            synchronized (this.eventQueue) {
                this.eventQueue.add(() -> {
                    this.rootContainer.onMouseMove(mouseX / this.scaleFactor, mouseY / this.scaleFactor);
                });
            }
        }
    }

    public void onMouseScroll(final float mouseX, final float mouseY, final float scrollX, final float scrollY) {
        if (mouseX < 0 || mouseX >= this.unscaledSize.x || mouseY < 0 || mouseY >= this.unscaledSize.y) return;
        if (this.backend.isOnRenderThread()) {
            this.rootContainer.onMouseScroll(mouseX / this.scaleFactor, mouseY / this.scaleFactor, scrollX, scrollY);
        } else {
            synchronized (this.eventQueue) {
                this.eventQueue.add(() -> {
                    this.rootContainer.onMouseScroll(mouseX / this.scaleFactor, mouseY / this.scaleFactor, scrollX, scrollY);
                });
            }
        }
    }

    public void onKeyDown(final int key, final int modifiers) {
        if (this.focusedComponent instanceof KeyboardListener keyboardListener) {
            if (this.backend.isOnRenderThread()) {
                keyboardListener.onKeyDown(key, modifiers);
            } else {
                synchronized (this.eventQueue) {
                    this.eventQueue.add(() -> {
                        keyboardListener.onKeyDown(key, modifiers);
                    });
                }
            }
        }
    }

    public void onKeyUp(final int key, final int modifiers) {
        if (this.focusedComponent instanceof KeyboardListener keyboardListener) {
            if (this.backend.isOnRenderThread()) {
                keyboardListener.onKeyUp(key, modifiers);
            } else {
                synchronized (this.eventQueue) {
                    this.eventQueue.add(() -> {
                        keyboardListener.onKeyUp(key, modifiers);
                    });
                }
            }
        }
    }

    public void onCharTyped(final char c) {
        if (this.focusedComponent instanceof KeyboardListener keyboardListener) {
            if (this.backend.isOnRenderThread()) {
                keyboardListener.onCharTyped(c);
            } else {
                synchronized (this.eventQueue) {
                    this.eventQueue.add(() -> {
                        keyboardListener.onCharTyped(c);
                    });
                }
            }
        }
    }

}
