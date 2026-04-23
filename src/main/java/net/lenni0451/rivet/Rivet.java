package net.lenni0451.rivet;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.render.RenderList;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Container;
import net.lenni0451.rivet.input.ClickedElement;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.keyboard.KeyboardListener;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layer.Layer;
import net.lenni0451.rivet.layer.LayerList;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.impl.DefaultDark;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public class Rivet {

    @Getter
    private final Backend backend;
    private final LayerList layers;
    private final ClickedElement<Layer> clickedLayer = new ClickedElement<>();
    @Getter
    private Size size;
    @Getter
    private float scale = 1;
    @Getter
    private Component focusedComponent;
    @Getter
    private Theme theme;
    private boolean recalculate = false;

    public Rivet(final Backend backend, final Layout layout, final Size size) {
        this.backend = backend;
        this.layers = new LayerList(new Container(this, layout));
        this.size = size;
        this.theme = new DefaultDark();
        this.theme.apply(this);
    }

    public Container root() {
        return this.baseLayer().container();
    }

    public Layer baseLayer() {
        return this.layers.baseLayer();
    }

    public List<Layer> layers() {
        return this.layers.get();
    }

    public void addLayer(final Layer layer) {
        this.layers.add(layer);
        this.recalculate = true;
    }

    public boolean removeLayer(final Layer layer) {
        if (this.layers.remove(layer)) {
            if (this.clickedLayer.is(layer)) this.clickedLayer.unset();
            this.recalculate = true;
            return true;
        }
        return false;
    }

    public Rivet theme(final Theme theme) {
        this.theme = theme;
        this.theme.apply(this);
        this.recalculateNextFrame();
        return this;
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
        float x = event.x() / this.scale;
        float y = event.y() / this.scale;
        Layer layer = this.findLayerAt(x, y);
        if (layer != null) {
            layer.container().onMouseDown(event.withX(x).withY(y), this.size.scale(this.scale, this.scale));
            this.clickedLayer.down(layer, event.button());
        }
    }

    public void onMouseUp(final MouseButtonEvent event) {
        if (!this.clickedLayer.isClicked()) {
            if (event.x() < 0 || event.x() >= this.size.width()) return;
            if (event.y() < 0 || event.y() >= this.size.height()) return;
        }
        float x = event.x() / this.scale;
        float y = event.y() / this.scale;
        List<Layer> layers = this.layers.get();
        for (Layer layer : layers) {
            if (this.clickedLayer.is(layer)) {
                layer.container().onMouseUp(event.withX(x).withY(y), this.size.scale(this.scale, this.scale));
                this.clickedLayer.up(event.button());
            }
        }
    }

    public void onMouseMove(final MouseMoveEvent event) {
        if (!this.clickedLayer.isClicked()) {
            if (event.x() < 0 || event.x() >= this.size.width()) return;
            if (event.y() < 0 || event.y() >= this.size.height()) return;
        }
        float x = event.x() / this.scale;
        float y = event.y() / this.scale;
        boolean intercepted = false;
        List<Layer> layers = this.layers.get();
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer layer = layers.get(i);
            if (this.clickedLayer.element() != null) {
                if (this.clickedLayer.is(layer)) {
                    layer.container().onMouseMove(event.withX(x).withY(y), this.size.scale(this.scale, this.scale));
                } else {
                    layer.container().onMouseMove(new MouseMoveEvent(-1, -1), this.size.scale(this.scale, this.scale));
                }
            } else {
                if (!intercepted && layer.container().intercepts(x, y)) {
                    layer.container().onMouseMove(event.withX(x).withY(y), this.size.scale(this.scale, this.scale));
                    intercepted = true;
                } else {
                    layer.container().onMouseMove(new MouseMoveEvent(-1, -1), this.size.scale(this.scale, this.scale));
                }
            }
        }
    }

    public void onMouseScroll(final MouseScrollEvent event) {
        if (!this.clickedLayer.isClicked()) {
            if (event.x() < 0 || event.x() >= this.size.width()) return;
            if (event.y() < 0 || event.y() >= this.size.height()) return;
        }
        float x = event.x() / this.scale;
        float y = event.y() / this.scale;
        Layer layer = this.findLayerAt(x, y);
        if (layer != null) {
            layer.container().onMouseScroll(event.withX(x).withY(y), this.size.scale(this.scale, this.scale));
        }
    }

    public void render(final Consumer<RenderList> renderListConsumer) {
        Size scaledSize = this.size.scale(this.scale, this.scale);
        List<Layer> layers = this.layers.get();
        if (this.recalculate) {
            for (Layer layer : layers) {
                layer.container().computeIdealSize();
                layer.container().computeLayout(scaledSize);
            }
            this.recalculate = false;
        }

        Renderer renderer = new Renderer();
        renderer.scale(this.scale, () -> {
            //TODO: Pass viewport to container to allow skipping out of viewport components
            for (Layer layer : layers) {
                layer.container().render(renderer, scaledSize);
            }
        });
        renderListConsumer.accept(renderer.renderList());
    }

    @Nullable
    private Layer findLayerAt(final float x, final float y) {
        for (int i = this.layers.get().size() - 1; i >= 0; i--) {
            Layer layer = this.layers.get().get(i);
            if (layer.bucket().interceptable() && layer.container().intercepts(x, y)) {
                return layer;
            }
        }
        return null;
    }

}
