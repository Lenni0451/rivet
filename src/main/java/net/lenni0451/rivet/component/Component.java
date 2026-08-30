package net.lenni0451.rivet.component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import java.util.function.BooleanSupplier;
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
    private final ListenerList<PositionUpdateListener> positionUpdateListener = new ListenerList<>();

    public final void setRivet(@Nullable final Rivet rivet, @Nullable final Parent parent) {
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
            this.onRemovedInternal();
            this.rivet = null;
            this.parent = null;
            for (PositionUpdateListener listener : this.positionUpdateListener.listeners()) {
                listener.lastPosition = null;
            }
        } else {
            if (parent == null) {
                throw new IllegalArgumentException("Parent must not be null when attaching to Rivet");
            }
            if (this.rivet != null) {
                throw new IllegalStateException("Component is already attached to a Rivet instance");
            }
            this.rivet = rivet;
            this.parent = parent;
            for (PositionUpdateListener listener : this.positionUpdateListener.listeners()) {
                listener.lastPosition = null;
            }
            this.addedListener.call(Runnable::run);
            this.onAddedInternal();
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

    public final Component capabilities(final Capabilities capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    public final Component capabilities(final Consumer<Capabilities> capabilities) {
        capabilities.accept(this.capabilities);
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
            this.onDisabledInternal();
        } else {
            this.enabledListener.call(Runnable::run);
            this.onEnabledInternal();
        }
        return this;
    }

    public final Rectangle relativeBounds() {
        if (this.parent == null) return Rectangle.EMPTY;
        return this.parent.childBounds(this);
    }

    public final Rectangle absoluteBounds() {
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


    protected void onAddedInternal() {
    }

    protected void onRemovedInternal() {
    }

    protected void onDisabledInternal() {
    }

    protected void onEnabledInternal() {
    }

    public final void onThemeChanged() {
        this.themeChangedListener.call(Runnable::run, Runnable::run, this::onThemeChangedInternal);
    }

    protected void onThemeChangedInternal() {
    }

    public final void onFocusGained() {
        this.focusGainedListener.callVoid(NullaryVoidListener::accept, Runnable::run, this::onFocusGainedInternal);
    }

    protected void onFocusGainedInternal() {
    }

    public final void onFocusLost() {
        this.focusLostListener.callVoid(NullaryVoidListener::accept, Runnable::run, this::onFocusLostInternal);
    }

    protected void onFocusLostInternal() {
    }

    public final boolean onKeyDown(final KeyEvent event) {
        if (!this.capabilities.keyboardInput) return false;
        if (this.disabled) return false;
        return this.keyDownListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event),
                listener -> listener.accept(event),
                () -> this.onKeyDownInternal(event)
        );
    }

    protected boolean onKeyDownInternal(final KeyEvent event) {
        return false;
    }

    public final boolean onKeyUp(final KeyEvent event) {
        if (!this.capabilities.keyboardInput) return false;
        if (this.disabled) return false;
        return this.keyUpListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event),
                listener -> listener.accept(event),
                () -> this.onKeyUpInternal(event)
        );
    }

    protected boolean onKeyUpInternal(final KeyEvent event) {
        return false;
    }

    public final boolean onCharTyped(final CharEvent event) {
        if (!this.capabilities.keyboardInput) return false;
        if (this.disabled) return false;
        return this.charTypedListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event),
                listener -> listener.accept(event),
                () -> this.onCharTypedInternal(event)
        );
    }

    protected boolean onCharTypedInternal(final CharEvent event) {
        return false;
    }

    public final void onMouseEnter() {
        if (!this.capabilities.mouseHover) return;
        if (this.disabled) return;
        this.mouseEnterListener.callVoid(NullaryVoidListener::accept, this::onMouseEnterInternal);
    }

    protected void onMouseEnterInternal() {
    }

    public final void onMouseLeave() {
        if (!this.capabilities.mouseHover) return;
        if (this.disabled) return;
        this.mouseLeaveListener.callVoid(NullaryVoidListener::accept, this::onMouseLeaveInternal);
    }

    protected void onMouseLeaveInternal() {
    }

    public final boolean onMouseDown(final MouseButtonEvent event, final Size size) {
        if (!this.capabilities.mouseInput) return false;
        if (this.disabled) return false;
        return this.mouseDownListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event, size),
                (listener) -> listener.accept(event, size),
                () -> this.onMouseDownInternal(event, size)
        );
    }

    protected boolean onMouseDownInternal(final MouseButtonEvent event, final Size size) {
        return true;
    }

    public final boolean onMouseUp(final MouseButtonEvent event, final Size size) {
        if (!this.capabilities.mouseInput) return false;
        if (this.disabled) return false;
        return this.mouseUpListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event, size),
                (listener) -> listener.accept(event, size),
                () -> this.onMouseUpInternal(event, size)
        );
    }

    protected boolean onMouseUpInternal(final MouseButtonEvent event, final Size size) {
        return true;
    }

    public final boolean onMouseMove(final MouseMoveEvent event, final Size size) {
        if (!this.capabilities.mouseHover) return false;
        if (this.disabled) return false;
        return this.mouseMoveListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event, size),
                (listener) -> listener.accept(event, size),
                () -> this.onMouseMoveInternal(event, size)
        );
    }

    protected boolean onMouseMoveInternal(final MouseMoveEvent event, final Size size) {
        return true;
    }

    public final boolean onMouseScroll(final MouseScrollEvent event, final Size size) {
        if (!this.capabilities.mouseInput) return false;
        if (this.disabled) return false;
        return this.mouseScrollListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event, size),
                (listener) -> listener.accept(event, size),
                () -> this.onMouseScrollInternal(event, size)
        );
    }

    protected boolean onMouseScrollInternal(final MouseScrollEvent event, final Size size) {
        return false;
    }

    public final boolean onDrop(final DropEvent event, final Size size) {
        if (!this.capabilities.dragAndDrop) return false;
        if (this.disabled) return false;
        return this.dropListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event, size),
                () -> this.onDropInternal(event, size)
        );
    }

    protected boolean onDropInternal(final DropEvent event, final Size size) {
        return false;
    }

    public final boolean onDragOver(final DragOverEvent event, final Size size) {
        if (!this.capabilities.dragAndDrop) return false;
        if (this.disabled) return false;
        return this.dragOverListener.callWithReturnValue(
                (listener, ctx) -> listener.accept(ctx, event, size),
                () -> this.onDragOverInternal(event, size)
        );
    }

    protected boolean onDragOverInternal(final DragOverEvent event, final Size size) {
        return false;
    }

    public final void onDragLeave() {
        if (!this.capabilities.dragAndDrop) return;
        if (this.disabled) return;
        this.dragLeaveListener.callVoid(NullaryVoidListener::accept, this::onDragLeaveInternal);
    }

    protected void onDragLeaveInternal() {
    }

    public final void updatePosition() {
        if (!this.positionUpdateListener.isEmpty()) {
            Rectangle absoluteBounds = null;
            for (PositionUpdateListener listener : this.positionUpdateListener.listeners()) {
                if (listener.trackPosition.getAsBoolean()) {
                    if (absoluteBounds == null) absoluteBounds = this.absoluteBounds();
                    listener.check(absoluteBounds);
                }
            }
        }
    }

    public final void render(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        this.updatePosition();
        this.renderInternal(renderer, size, visibleArea);
    }

    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
    }

    public abstract Size computeIdealSize(final Size constraints);

    public void computeLayout(final Size size) {
    }


    @Getter
    @Setter
    @Accessors(fluent = true, chain = true, makeFinal = true)
    public static final class Capabilities {
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

    @RequiredArgsConstructor
    public static final class PositionUpdateListener {
        private final BooleanSupplier trackPosition;
        private final Consumer<Rectangle> updatePosition;
        private Rectangle lastPosition;

        public PositionUpdateListener(final Consumer<Rectangle> updatePosition) {
            this.trackPosition = () -> true;
            this.updatePosition = updatePosition;
        }

        private void check(final Rectangle absoluteBounds) {
            if (this.lastPosition == null || !this.lastPosition.equals(absoluteBounds)) {
                this.lastPosition = absoluteBounds;
                this.updatePosition.accept(absoluteBounds);
            }
        }
    }

}
