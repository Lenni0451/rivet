package net.lenni0451.rivet;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.dragdrop.DragAndDropManager;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layer.Layer;
import net.lenni0451.rivet.layer.LayerBucket;
import net.lenni0451.rivet.layer.LayerList;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.math.WindowScale;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.utils.ContainerMouseHandler;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Accessors(fluent = true, chain = true, makeFinal = true)
public final class Rivet {

    @Getter
    private final Backend backend;
    private final LayerList layers;
    private final MouseHandler mouseHandler = new MouseHandler();
    @Getter
    private final DragAndDropManager dragAndDropManager = new DragAndDropManager(this);
    @Getter
    private Size size;
    @Getter
    private final WindowScale scale = new WindowScale();
    @Getter
    private Component focusedComponent;
    @Getter
    private Theme theme;
    private final Queue<Runnable> tasks = new LinkedBlockingQueue<>();
    private float lastMouseX = -Float.MAX_VALUE;
    private float lastMouseY = -Float.MAX_VALUE;

    @Getter
    private final ListenerList<BiPredicate<Component, Component>> focusChangeListener = new ListenerList<>();
    @Getter
    private final ListenerList<Predicate<KeyEvent>> keyDownListener = new ListenerList<>();
    @Getter
    private final ListenerList<Predicate<KeyEvent>> keyUpListener = new ListenerList<>();
    @Getter
    private final ListenerList<Predicate<CharEvent>> charTypedListener = new ListenerList<>();
    @Getter
    private final ListenerList<Predicate<MouseButtonEvent>> mouseDownListener = new ListenerList<>();
    @Getter
    private final ListenerList<Predicate<MouseButtonEvent>> mouseUpListener = new ListenerList<>();
    @Getter
    private final ListenerList<Predicate<MouseMoveEvent>> mouseMoveListener = new ListenerList<>();
    @Getter
    private final ListenerList<Predicate<MouseScrollEvent>> mouseScrollListener = new ListenerList<>();
    @Getter
    private final ListenerList<Runnable> renderListener = new ListenerList<>();

    public Rivet(final Backend backend, final Layout layout, final Size size) {
        this.backend = backend;
        this.layers = new LayerList(this, new Container(layout));
        this.size = size;
        this.theme = new DefaultTheme();
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
        layer.container().setRivet(this, layer);
        layer.recalculateNextFrame(true);
        return this;
    }

    public boolean removeLayer(final Layer layer) {
        if (this.layers.remove(layer)) {
            this.mouseHandler.checkAndRemove(layer);
            layer.container().setRivet(null, null);
            this.runSync(this::updateMouseState);
            return true;
        }
        return false;
    }

    public Rivet theme(final Theme theme) {
        this.theme = theme;
        this.theme.apply(this);
        for (Layer layer : this.layers.get()) {
            layer.container().onThemeChanged();
        }
        this.recalculateNextFrame();
        return this;
    }

    public Rivet size(final Size size) {
        if (!this.size.equals(size)) {
            this.size = size;
            this.recalculateNextFrame();
        }
        return this;
    }

    public Size scaledSize() {
        return this.scale.scale(this.size);
    }

    public Rivet focusedComponent(final Component component) {
        if (component != null && component.disabled()) return this;
        if (this.focusedComponent == component) return this;
        if (!this.focusChangeListener.call(l -> l.test(this.focusedComponent, component))) {
            if (this.focusedComponent != null) {
                this.focusedComponent.onFocusLost();
            }
            this.focusedComponent = component;
            if (component != null) {
                component.onFocusGained();
            }
        }
        return this;
    }

    public Rivet runSync(final Runnable task) {
        this.tasks.offer(task);
        return this;
    }

    public Rivet recalculateNextFrame() {
        for (Layer layer : this.layers.get()) {
            layer.recalculateNextFrame(true);
        }
        return this;
    }

    public Rivet updateMouseState() {
        if (this.layers.get().stream().anyMatch(Layer::recalculateNextFrame)) {
            return this;
        }
        if (this.lastMouseX != -Float.MAX_VALUE && this.lastMouseY != -Float.MAX_VALUE) {
            this.onMouseMove(new MouseMoveEvent(this.lastMouseX, this.lastMouseY, Set.of()));
        }
        return this;
    }

    public Rivet unfocus() {
        this.lastMouseX = -Float.MAX_VALUE;
        this.lastMouseY = -Float.MAX_VALUE;
        this.mouseHandler.clear();
        return this;
    }

    public void dispose() {
        this.unfocus();
        for (Layer layer : this.layers.get()) {
            if (layer.bucket().equals(LayerBucket.BASE)) {
                layer.container().clearChildren();
            } else {
                this.removeLayer(layer);
            }
        }
    }


    public boolean onKeyDown(final KeyEvent event) {
        return this.keyDownListener.call(l -> l.test(event), () -> {
            if (this.focusedComponent != null) {
                Component current = this.focusedComponent;
                while (true) {
                    if (current.onKeyDown(event)) return true;
                    if (current.parent() instanceof Component parent) {
                        current = parent;
                    } else {
                        break;
                    }
                }
            }
            return false;
        });
    }

    public boolean onKeyUp(final KeyEvent event) {
        return this.keyUpListener.call(l -> l.test(event), () -> {
            if (this.focusedComponent != null) {
                Component current = this.focusedComponent;
                while (true) {
                    if (current.onKeyUp(event)) return true;
                    if (current.parent() instanceof Component parent) {
                        current = parent;
                    } else {
                        break;
                    }
                }
            }
            return false;
        });
    }

    public boolean onCharTyped(final CharEvent event) {
        return this.charTypedListener.call(l -> l.test(event), () -> {
            if (this.focusedComponent != null) {
                Component current = this.focusedComponent;
                while (true) {
                    if (current.onCharTyped(event)) return true;
                    if (current.parent() instanceof Component parent) {
                        current = parent;
                    } else {
                        break;
                    }
                }
            }
            return false;
        });
    }

    public boolean onMouseDown(final MouseButtonEvent event) {
        this.lastMouseX = event.x();
        this.lastMouseY = event.y();
        if (event.x() < 0 || event.x() >= this.size.width()) return false;
        if (event.y() < 0 || event.y() >= this.size.height()) return false;
        return this.mouseDownListener.call(l -> l.test(event), () -> {
            float x = this.scale.scale(event.x());
            float y = this.scale.scale(event.y());
            return this.mouseHandler.onMouseDown(this, event.withX(x).withY(y), this.scaledSize()).handled();
        });
    }

    public boolean onMouseUp(final MouseButtonEvent event) {
        this.lastMouseX = event.x();
        this.lastMouseY = event.y();
        if (!this.dragAndDropManager.isDragging() && !this.mouseHandler.isMouseHeld()) {
            if (event.x() < 0 || event.x() >= this.size.width()) return false;
            if (event.y() < 0 || event.y() >= this.size.height()) return false;
        }
        return this.mouseUpListener.call(l -> l.test(event), () -> {
            float x = this.scale.scale(event.x());
            float y = this.scale.scale(event.y());
            MouseButtonEvent translatedEvent = event.withX(x).withY(y);
            boolean dragHandled = this.dragAndDropManager.onMouseUp(translatedEvent, this.layers::interactableLayers);
            boolean mouseHandled = this.mouseHandler.onMouseUp(this, translatedEvent, this.scaledSize()).handled();
            return dragHandled || mouseHandled;
        });
    }

    public boolean onMouseMove(final MouseMoveEvent event) {
        return this.mouseMoveListener.call(l -> l.test(event), () -> {
            this.lastMouseX = event.x();
            this.lastMouseY = event.y();
            float x = this.scale.scale(event.x());
            float y = this.scale.scale(event.y());
            MouseMoveEvent translatedEvent = event.withX(x).withY(y);
            boolean dragHandled = this.dragAndDropManager.onMouseMove(translatedEvent, this.layers::interactableLayers);
            boolean mouseHandled = this.mouseHandler.onMouseMove(translatedEvent, this.scaledSize()).handled();
            return dragHandled || mouseHandled;
        });
    }

    public boolean onMouseScroll(final MouseScrollEvent event) {
        this.lastMouseX = event.x();
        this.lastMouseY = event.y();
        if (event.x() < 0 || event.x() >= this.size.width()) return false;
        if (event.y() < 0 || event.y() >= this.size.height()) return false;
        return this.mouseScrollListener.call(l -> l.test(event), () -> {
            float x = this.scale.scale(event.x());
            float y = this.scale.scale(event.y());
            return this.mouseHandler.onMouseScroll(event.withX(x).withY(y), this.scaledSize()).handled();
        });
    }

    public <R extends Renderer> R render(final R renderer) {
        Runnable task;
        while ((task = this.tasks.poll()) != null) task.run();
        this.renderListener.callVoid(Runnable::run);

        Size scaledSize = this.scaledSize();
        renderer.scale(this.scale.scaleFactor(), () -> {
            for (Layer layer : this.layers.get()) {
                if (layer.container().rivet() == null) continue;
                if (layer.recalculateNextFrame()) {
                    layer.container().computeLayout(scaledSize);
                    layer.recalculateNextFrame(false);
                    this.updateMouseState();
                }
                if (layer.container().rivet() == null) continue;
                layer.container().render(renderer, scaledSize);
            }
        });
        return renderer;
    }


    private static class DefaultTheme extends Theme {
        @Override
        protected void addValues(final Rivet rivet, final Values values) {
        }
    }

    private class MouseHandler extends ContainerMouseHandler<Layer> {
        @Override
        protected Component map(final Layer element) {
            return element.container();
        }

        @Override
        protected Rectangle relativeBounds(final Size containerBounds, final Layer element) {
            return new Rectangle(Rivet.this.scaledSize());
        }

        @Override
        protected List<Layer> elementsAt(final float x, final float y, final Size containerBounds) {
            if (x < 0 || x >= containerBounds.width() || y < 0 || y >= containerBounds.height()) return List.of();
            return Rivet.this.layers.interactableLayers();
        }
    }

}
