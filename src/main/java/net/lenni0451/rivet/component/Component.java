package net.lenni0451.rivet.component;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.dragdrop.DragOverEvent;
import net.lenni0451.rivet.dragdrop.DropEvent;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.event.PrePostListenerList;
import net.lenni0451.rivet.event.listener.BiReturnableListener;
import net.lenni0451.rivet.event.listener.NullaryVoidListener;
import net.lenni0451.rivet.event.listener.ReturnableListener;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.LayoutOptions;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public abstract class Component {

    @Getter
    private Rivet rivet;
    @Getter
    @Nullable
    private Parent parent;
    @Getter
    private Size minSize = Size.EMPTY;
    @Getter
    private Size maxSize = new Size(Float.MAX_VALUE, Float.MAX_VALUE);
    @Getter
    private LayoutOptions layoutOptions;
    @Getter
    @Setter
    private Capabilities capabilities = new Capabilities();
    @Getter
    private boolean disabled = false;

    @Getter
    private final ListenerList<Runnable> addedListener = new ListenerList<>();
    @Getter
    private final ListenerList<Runnable> removedListener = new ListenerList<>();
    @Getter
    private final ListenerList<Runnable> disabledListener = new ListenerList<>();
    @Getter
    private final ListenerList<Runnable> enabledListener = new ListenerList<>();
    @Getter
    private final PrePostListenerList<Runnable, Runnable> themeChangedListener = new PrePostListenerList<>();
    @Getter
    private final PrePostListenerList<NullaryVoidListener, Runnable> focusGainedListener = new PrePostListenerList<>();
    @Getter
    private final PrePostListenerList<NullaryVoidListener, Runnable> focusLostListener = new PrePostListenerList<>();
    @Getter
    private final PrePostListenerList<ReturnableListener<Boolean, KeyEvent>, Consumer<KeyEvent>> keyDownListener = new PrePostListenerList<>();
    @Getter
    private final PrePostListenerList<ReturnableListener<Boolean, KeyEvent>, Consumer<KeyEvent>> keyUpListener = new PrePostListenerList<>();
    @Getter
    private final PrePostListenerList<ReturnableListener<Boolean, CharEvent>, Consumer<CharEvent>> charTypedListener = new PrePostListenerList<>();
    @Getter
    private final ListenerList<NullaryVoidListener> mouseEnterListener = new ListenerList<>();
    @Getter
    private final ListenerList<NullaryVoidListener> mouseLeaveListener = new ListenerList<>();
    @Getter
    private final PrePostListenerList<BiReturnableListener<Boolean, MouseButtonEvent, Size>, BiConsumer<MouseButtonEvent, Size>> mouseDownListener = new PrePostListenerList<>();
    @Getter
    private final PrePostListenerList<BiReturnableListener<Boolean, MouseButtonEvent, Size>, BiConsumer<MouseButtonEvent, Size>> mouseUpListener = new PrePostListenerList<>();
    @Getter
    private final PrePostListenerList<BiReturnableListener<Boolean, MouseMoveEvent, Size>, BiConsumer<MouseMoveEvent, Size>> mouseMoveListener = new PrePostListenerList<>();
    @Getter
    private final PrePostListenerList<BiReturnableListener<Boolean, MouseScrollEvent, Size>, BiConsumer<MouseScrollEvent, Size>> mouseScrollListener = new PrePostListenerList<>();
    @Getter
    private final ListenerList<BiReturnableListener<Boolean, DropEvent, Size>> dropListener = new ListenerList<>();
    @Getter
    private final ListenerList<BiReturnableListener<Boolean, DragOverEvent, Size>> dragOverListener = new ListenerList<>();
    @Getter
    private final ListenerList<NullaryVoidListener> dragLeaveListener = new ListenerList<>();
    @Getter
    private final ListenerList<Consumer<Rectangle>> positionUpdateListener = new ListenerList<>();
    private Rectangle lastAbsoluteBounds;

    public final void setRivet(@Nullable final Rivet rivet, @Nullable final Parent parent) {
        this.lastAbsoluteBounds = null;
        if (rivet == null) {
            if (parent != null) {
                throw new IllegalArgumentException("Parent must be null when detaching from Rivet");
            }
            if (this.rivet == null) {
                throw new IllegalStateException("Component is not attached to any Rivet instance");
            }
            if (this.rivet.focusedComponent() == this) {
                this.rivet.focusedComponent(null);
            }
            this.removedListener.call(Runnable::run);
            this.onComponentRemoved();
            this.rivet = null;
            this.parent = null;
        } else {
            if (parent == null) {
                throw new IllegalArgumentException("Parent must not be null when attaching to Rivet");
            }
            if (this.rivet != null) {
                throw new IllegalStateException("Component is already attached to a Rivet instance");
            }
            this.rivet = rivet;
            this.parent = parent;
            this.addedListener.call(Runnable::run);
            this.onComponentAdded();
        }
    }

    public final Component minSize(final float width, final float height) {
        return this.minSize(new Size(width, height));
    }

    public final Component minSize(final Size minSize) {
        if (!this.minSize.equals(minSize)) {
            this.minSize = minSize;
            if (this.parent != null) this.parent.requestLayoutRecalculation();
        }
        return this;
    }

    public final Component maxSize(final float width, final float height) {
        return this.maxSize(new Size(width, height));
    }

    public final Component maxSize(final Size maxSize) {
        if (!this.maxSize.equals(maxSize)) {
            this.maxSize = maxSize;
            if (this.parent != null) this.parent.requestLayoutRecalculation();
        }
        return this;
    }

    public final Component fixedSize(final float width, final float height) {
        Size newMinSize = this.minSize;
        Size newMaxSize = this.maxSize;
        if (width >= 0) {
            newMinSize = newMinSize.withWidth(width);
            newMaxSize = newMaxSize.withWidth(width);
        }
        if (height >= 0) {
            newMinSize = newMinSize.withHeight(height);
            newMaxSize = newMaxSize.withHeight(height);
        }
        if (!this.minSize.equals(newMinSize) || !this.maxSize.equals(newMaxSize)) {
            this.minSize = newMinSize;
            this.maxSize = newMaxSize;
            if (this.parent != null) this.parent.requestLayoutRecalculation();
        }
        return this;
    }

    public final Component layoutOptions(final LayoutOptions layoutOptions) {
        if (this.layoutOptions == null || !this.layoutOptions.equals(layoutOptions)) {
            this.layoutOptions = layoutOptions;
            if (this.parent != null) this.parent.requestLayoutRecalculation();
        }
        return this;
    }

    public final Component disabled(final boolean disabled) {
        if (this.disabled == disabled) return this;
        this.disabled = disabled;
        if (disabled) {
            if (this.rivet != null && this.rivet.focusedComponent() == this) {
                this.rivet.focusedComponent(null);
            }
            this.disabledListener.call(Runnable::run);
            this.onComponentDisabled();
        } else {
            this.enabledListener.call(Runnable::run);
            this.onComponentEnabled();
        }
        return this;
    }

    public final Rectangle relativeBounds() {
        if (this.parent == null) return Rectangle.EMPTY;
        return this.parent.childBounds(this);
    }

    public final Rectangle absoluteBounds() {
        if (this.lastAbsoluteBounds != null) return this.lastAbsoluteBounds;
        if (this.parent == null) return Rectangle.EMPTY;
        Rectangle parentBounds = this.parent.absoluteBounds();
        Rectangle relative = this.relativeBounds();
        return new Rectangle(parentBounds.x() + relative.x(), parentBounds.y() + relative.y(), relative.width(), relative.height());
    }

    public final boolean isDescendantOf(final Component parent) {
        Component current = this;
        while (true) {
            if (current == parent) {
                return true;
            } else if (current.parent() instanceof Component p) {
                current = p;
            } else {
                return false;
            }
        }
    }


    protected void onComponentAdded() {
    }

    protected void onComponentRemoved() {
    }

    protected void onComponentDisabled() {
    }

    protected void onComponentEnabled() {
    }

    public final void onThemeChanged() {
        this.themeChangedListener.call(Runnable::run, Runnable::run, this::onComponentThemeChanged);
    }

    protected void onComponentThemeChanged() {
    }

    public final void onFocusGained() {
        this.focusGainedListener.callVoid(NullaryVoidListener::accept, Runnable::run, this::onComponentFocusGained);
    }

    protected void onComponentFocusGained() {
    }

    public final void onFocusLost() {
        this.focusLostListener.callVoid(NullaryVoidListener::accept, Runnable::run, this::onComponentFocusLost);
    }

    protected void onComponentFocusLost() {
    }

    public final boolean onKeyDown(final KeyEvent event) {
        if (!this.capabilities.keyboardInput) return false;
        if (this.disabled) return false;
        return this.keyDownListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event),
                listener -> listener.accept(event),
                () -> this.onComponentKeyDown(event)
        );
    }

    protected boolean onComponentKeyDown(final KeyEvent event) {
        return false;
    }

    public final boolean onKeyUp(final KeyEvent event) {
        if (!this.capabilities.keyboardInput) return false;
        if (this.disabled) return false;
        return this.keyUpListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event),
                listener -> listener.accept(event),
                () -> this.onComponentKeyUp(event)
        );
    }

    protected boolean onComponentKeyUp(final KeyEvent event) {
        return false;
    }

    public final boolean onCharTyped(final CharEvent event) {
        if (!this.capabilities.keyboardInput) return false;
        if (this.disabled) return false;
        return this.charTypedListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event),
                listener -> listener.accept(event),
                () -> this.onComponentCharTyped(event)
        );
    }

    protected boolean onComponentCharTyped(final CharEvent event) {
        return false;
    }

    public final void onMouseEnter() {
        if (!this.capabilities.mouseHover) return;
        if (this.disabled) return;
        this.mouseEnterListener.callVoid(NullaryVoidListener::accept, this::onComponentMouseEnter);
    }

    protected void onComponentMouseEnter() {
    }

    public final void onMouseLeave() {
        if (!this.capabilities.mouseHover) return;
        if (this.disabled) return;
        this.mouseLeaveListener.callVoid(NullaryVoidListener::accept, this::onComponentMouseLeave);
    }

    protected void onComponentMouseLeave() {
    }

    public final boolean onMouseDown(final MouseButtonEvent event, final Size size) {
        if (!this.capabilities.mouseInput) return false;
        if (this.disabled) return false;
        return this.mouseDownListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event, size),
                (listener) -> listener.accept(event, size),
                () -> this.onComponentMouseDown(event, size)
        );
    }

    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
        return true;
    }

    public final boolean onMouseUp(final MouseButtonEvent event, final Size size) {
        if (!this.capabilities.mouseInput) return false;
        if (this.disabled) return false;
        return this.mouseUpListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event, size),
                (listener) -> listener.accept(event, size),
                () -> this.onComponentMouseUp(event, size)
        );
    }

    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        return true;
    }

    public final boolean onMouseMove(final MouseMoveEvent event, final Size size) {
        if (!this.capabilities.mouseHover) return false;
        if (this.disabled) return false;
        return this.mouseMoveListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event, size),
                (listener) -> listener.accept(event, size),
                () -> this.onComponentMouseMove(event, size)
        );
    }

    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
        return true;
    }

    public final boolean onMouseScroll(final MouseScrollEvent event, final Size size) {
        if (!this.capabilities.mouseInput) return false;
        if (this.disabled) return false;
        return this.mouseScrollListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event, size),
                (listener) -> listener.accept(event, size),
                () -> this.onComponentMouseScroll(event, size)
        );
    }

    protected boolean onComponentMouseScroll(final MouseScrollEvent event, final Size size) {
        return false;
    }

    public final boolean onDrop(final DropEvent event, final Size size) {
        if (!this.capabilities.dragAndDrop) return false;
        if (this.disabled) return false;
        return this.dropListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event, size),
                () -> this.onComponentDrop(event, size)
        );
    }

    protected boolean onComponentDrop(final DropEvent event, final Size size) {
        return false;
    }

    public final boolean onDragOver(final DragOverEvent event, final Size size) {
        if (!this.capabilities.dragAndDrop) return false;
        if (this.disabled) return false;
        return this.dragOverListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event, size),
                () -> this.onComponentDragOver(event, size)
        );
    }

    protected boolean onComponentDragOver(final DragOverEvent event, final Size size) {
        return false;
    }

    public final void onDragLeave() {
        if (!this.capabilities.dragAndDrop) return;
        if (this.disabled) return;
        this.dragLeaveListener.callVoid(NullaryVoidListener::accept, this::onComponentDragLeave);
    }

    protected void onComponentDragLeave() {
    }

    public final void updatePosition(final float x, final float y, final float width, final float height) {
        if (this.lastAbsoluteBounds != null) {
            Rectangle absoluteBounds = this.lastAbsoluteBounds;
            if (absoluteBounds.x() == x && absoluteBounds.y() == y && absoluteBounds.width() == width && absoluteBounds.height() == height) {
                return;
            }
        }
        this.lastAbsoluteBounds = new Rectangle(x, y, width, height);
        this.positionUpdateListener.call(l -> l.accept(this.lastAbsoluteBounds));
        this.updateComponentPosition(this.lastAbsoluteBounds);
    }

    protected void updateComponentPosition(final Rectangle absoluteBounds) {
    }

    public final void render(final Renderer renderer, final Size size) {
        this.updatePosition(renderer.xOffset(), renderer.yOffset(), size.width(), size.height());
        this.renderComponent(renderer, size);
    }

    protected void renderComponent(final Renderer renderer, final Size size) {
    }

    public abstract Size computeIdealSize(final Size constraints);

    public void computeLayout(final Size size) {
    }


    @Getter
    @Setter
    @Accessors(fluent = true, chain = true, makeFinal = true)
    public static class Capabilities {
        private boolean keyboardInput = true;
        private boolean mouseInput = true;
        private boolean mouseHover = true;
        private boolean dragAndDrop = true;

        public boolean hasAny() {
            return this.keyboardInput || this.mouseInput || this.mouseHover || this.dragAndDrop;
        }

        public Capabilities all(final boolean value) {
            this.keyboardInput = value;
            this.mouseInput = value;
            this.mouseHover = value;
            this.dragAndDrop = value;
            return this;
        }
    }

}
