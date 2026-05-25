package net.lenni0451.rivet;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.render.RenderList;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Container;
import net.lenni0451.rivet.input.ContainerMouseHandler;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layer.Layer;
import net.lenni0451.rivet.layer.LayerList;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.impl.DefaultDark;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

@Accessors(fluent = true, chain = true)
public final class Rivet {

    @Getter
    private final Backend backend;
    private final LayerList layers;
    private final ContainerMouseHandler<Layer> mouseHandler = new ContainerMouseHandler<>();
    @Getter
    private Size size;
    @Getter
    private float scale = 1;
    @Getter
    private Component focusedComponent;
    @Getter
    private Theme theme;
    private final Queue<Runnable> tasks = new LinkedBlockingQueue<>();
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

    public Rivet addLayer(final Layer layer) {
        this.layers.add(layer);
        this.recalculate = true;
        return this;
    }

    public boolean removeLayer(final Layer layer) {
        if (this.layers.remove(layer)) {
            this.mouseHandler.checkAndRemove(layer, l -> l.container().onMouseLeave(), (l, mouseButton) -> {
                l.container().onMouseUp(new MouseButtonEvent(0, 0, mouseButton, Set.of()), new Rectangle(this.scaledSize()));
            });
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

    public Rivet size(final Size size) {
        if (!this.size.equals(size)) {
            this.size = size;
            this.recalculate = true;
        }
        return this;
    }

    public Size scaledSize() {
        return this.size.scale(this.scale, this.scale);
    }

    public Rivet scale(final float scale) {
        this.scale = scale;
        this.recalculate = true;
        return this;
    }

    public Rivet focusedComponent(final Component component) {
        if (this.focusedComponent == component) return this;
        if (this.focusedComponent != null) {
            this.focusedComponent.onFocusLost();
        }
        this.focusedComponent = component;
        if (component != null) {
            component.onFocusGained();
        }
        return this;
    }

    public Rivet runSync(final Runnable task) {
        this.tasks.offer(task);
        return this;
    }

    public Rivet recalculateNextFrame() {
        this.recalculate = true;
        return this;
    }

    public Rivet unfocus() {
        this.mouseHandler.clear(l -> l.container().onMouseLeave(), (l, mouseButton) -> {
            l.container().onMouseUp(new MouseButtonEvent(0, 0, mouseButton, Set.of()), new Rectangle(this.scaledSize()));
        });
        return this;
    }


    public boolean onKeyDown(final KeyEvent event) {
        if (this.focusedComponent != null) {
            return this.focusedComponent.onKeyDown(event);
        }
        return false;
    }

    public boolean onKeyUp(final KeyEvent event) {
        if (this.focusedComponent != null) {
            return this.focusedComponent.onKeyUp(event);
        }
        return false;
    }

    public boolean onCharTyped(final CharEvent event) {
        if (this.focusedComponent != null) {
            return this.focusedComponent.onCharTyped(event);
        }
        return false;
    }

    public boolean onMouseDown(final MouseButtonEvent event) {
        if (event.x() < 0 || event.x() >= this.size.width()) return false;
        if (event.y() < 0 || event.y() >= this.size.height()) return false;
        float x = event.x() / this.scale;
        float y = event.y() / this.scale;
        return this.mouseHandler.onMouseDown(
                event,
                this.findLayerAt(x, y),
                layer -> layer.container().onMouseDown(event.withX(x).withY(y), new Rectangle(this.scaledSize())),
                () -> false
        );
    }

    public boolean onMouseUp(final MouseButtonEvent event) {
        if (!this.mouseHandler.isMouseHeld()) {
            if (event.x() < 0 || event.x() >= this.size.width()) return false;
            if (event.y() < 0 || event.y() >= this.size.height()) return false;
        }
        float x = event.x() / this.scale;
        float y = event.y() / this.scale;
        return this.mouseHandler.onMouseUp(
                event,
                layer -> layer.container().onMouseUp(event.withX(x).withY(y), new Rectangle(this.scaledSize())),
                () -> false
        );
    }

    public boolean onMouseMove(final MouseMoveEvent event) {
        if (!this.mouseHandler.isMouseHeld()) {
            if (event.x() < 0 || event.x() >= this.size.width()) return false;
            if (event.y() < 0 || event.y() >= this.size.height()) return false;
        }
        float x = event.x() / this.scale;
        float y = event.y() / this.scale;
        return this.mouseHandler.onMouseMove(
                this.findLayerAt(x, y),
                layer -> {},
                layer -> layer.container().onMouseMove(new MouseMoveEvent(-Float.MAX_VALUE, -Float.MAX_VALUE), new Rectangle(this.scaledSize())),
                layer -> layer.container().onMouseMove(event.withX(x).withY(y), new Rectangle(this.scaledSize())),
                () -> false
        );
    }

    public boolean onMouseScroll(final MouseScrollEvent event) {
        if (event.x() < 0 || event.x() >= this.size.width()) return false;
        if (event.y() < 0 || event.y() >= this.size.height()) return false;
        float x = event.x() / this.scale;
        float y = event.y() / this.scale;
        return this.mouseHandler.onMouseScroll(
                this.findLayerAt(x, y),
                layer -> layer.container().onMouseScroll(event.withX(x).withY(y), new Rectangle(this.scaledSize())),
                () -> false
        );
    }

    public RenderList render() {
        Runnable task;
        while ((task = this.tasks.poll()) != null) task.run();

        Size scaledSize = this.scaledSize();
        Renderer renderer = new Renderer();
        renderer.scale(this.scale, () -> {
            for (Layer layer : this.layers.get()) {
                if (this.recalculate) layer.container().computeLayout(scaledSize);
                layer.container().render(renderer, new Rectangle(scaledSize));
            }
        });
        this.recalculate = false;
        return renderer.complete();
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
