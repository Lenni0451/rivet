package net.lenni0451.rivet;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.render.RenderList;
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
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.impl.DefaultDark;

import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public class Rivet {

    @Getter
    private final Backend backend;
    @Getter
    private Container rootContainer;
    @Getter
    private Size size;
    @Getter
    private float scale = 1;
    @Getter
    private Component focusedComponent;
    @Getter
    private Theme theme;
    private boolean recalculate = false;

    public Rivet theme(final Theme theme) {
        this.theme = theme;
        this.theme.apply(this);
        this.recalculateNextFrame();
        return this;
    }

    public Rivet(final Backend backend, final Layout layout, final Size size) {
        this.backend = backend;
        this.rootContainer = new Container(this, layout);
        this.size = size;
        this.theme = new DefaultDark();
        this.theme.apply(this);
    }

    public void rootContainer(final Container rootContainer) {
        this.rootContainer = rootContainer;
        this.recalculate = true;
    }

    public void size(final Size size) {
        this.size = size;
        this.recalculate = true;
    }

    public void scale(final float scale) {
        this.scale = scale;
        this.recalculate = true;
    }

    public void focusedComponent(final Component component) {
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
        this.rootContainer.onMouseDown(event.withX(event.x() / this.scale).withY(event.y() / this.scale), this.size.scale(this.scale, this.scale));
    }

    public void onMouseUp(final MouseButtonEvent event) {
        if (event.x() < 0 || event.x() >= this.size.width()) return;
        if (event.y() < 0 || event.y() >= this.size.height()) return;
        this.rootContainer.onMouseUp(event.withX(event.x() / this.scale).withY(event.y() / this.scale), this.size.scale(this.scale, this.scale));
    }

    public void onMouseMove(final MouseMoveEvent event) {
        if (event.x() < 0 || event.x() >= this.size.width()) return;
        if (event.y() < 0 || event.y() >= this.size.height()) return;
        this.rootContainer.onMouseMove(event.withX(event.x() / this.scale).withY(event.y() / this.scale), this.size.scale(this.scale, this.scale));
    }

    public void onMouseScroll(final MouseScrollEvent event) {
        if (event.x() < 0 || event.x() >= this.size.width()) return;
        if (event.y() < 0 || event.y() >= this.size.height()) return;
        this.rootContainer.onMouseScroll(event.withX(event.x() / this.scale).withY(event.y() / this.scale), this.size.scale(this.scale, this.scale));
    }

    public void render(final Consumer<RenderList> renderListConsumer) {
        Size scaledSize = this.size.scale(this.scale, this.scale);
        if (this.recalculate) {
            this.rootContainer.computeIdealSize();
            this.rootContainer.computeLayout(scaledSize);
            this.recalculate = false;
        }

        Renderer renderer = new Renderer();
        renderer.scale(this.scale, () -> {
            //TODO: Pass viewport to container to allow skipping out of viewport components
            this.rootContainer.render(renderer, scaledSize);
        });
        renderListConsumer.accept(renderer.renderList());
    }

}
