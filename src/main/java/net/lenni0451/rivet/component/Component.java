package net.lenni0451.rivet.component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.LayoutOptions;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
public abstract class Component {

    @Getter
    protected final Rivet rivet;
    @Getter
    private Size minSize = Size.EMPTY;
    @Getter
    protected Size idealSize = Size.EMPTY;
    @Getter
    private Size maxSize = new Size(Float.MAX_VALUE, Float.MAX_VALUE);
    @Getter
    private LayoutOptions layoutOptions;
    @Getter
    @Setter
    private boolean interactive = true;

    private final List<BooleanSupplier> focusGainedListeners = new ArrayList<>();
    private final List<BooleanSupplier> focusLostListeners = new ArrayList<>();
    private final List<Predicate<KeyEvent>> keyDownListeners = new ArrayList<>();
    private final List<Predicate<KeyEvent>> keyUpListeners = new ArrayList<>();
    private final List<Predicate<CharEvent>> charTypedListeners = new ArrayList<>();
    private final List<BooleanSupplier> mouseEnterListeners = new ArrayList<>();
    private final List<BooleanSupplier> mouseLeaveListeners = new ArrayList<>();
    private final List<BiPredicate<MouseButtonEvent, Rectangle>> mouseDownListeners = new ArrayList<>();
    private final List<BiPredicate<MouseButtonEvent, Rectangle>> mouseUpListeners = new ArrayList<>();
    private final List<BiPredicate<MouseMoveEvent, Rectangle>> mouseMoveListeners = new ArrayList<>();
    private final List<BiPredicate<MouseScrollEvent, Rectangle>> mouseScrollListeners = new ArrayList<>();

    public final Component minSize(final Size minSize) {
        if (!this.minSize.equals(minSize)) {
            this.minSize = minSize;
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public final Component maxSize(final Size maxSize) {
        if (!this.maxSize.equals(maxSize)) {
            this.maxSize = maxSize;
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public final Component layoutOptions(final LayoutOptions layoutOptions) {
        if (this.layoutOptions == null || !this.layoutOptions.equals(layoutOptions)) {
            this.layoutOptions = layoutOptions;
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public final Component addFocusGainedListener(final BooleanSupplier listener) {
        this.focusGainedListeners.add(listener);
        return this;
    }

    public final Component removeFocusGainedListener(final BooleanSupplier listener) {
        this.focusGainedListeners.remove(listener);
        return this;
    }

    public final Component addFocusLostListener(final BooleanSupplier listener) {
        this.focusLostListeners.add(listener);
        return this;
    }

    public final Component removeFocusLostListener(final BooleanSupplier listener) {
        this.focusLostListeners.remove(listener);
        return this;
    }

    public final Component addKeyDownListener(final Predicate<KeyEvent> listener) {
        this.keyDownListeners.add(listener);
        return this;
    }

    public final Component removeKeyDownListener(final Predicate<KeyEvent> listener) {
        this.keyDownListeners.remove(listener);
        return this;
    }

    public final Component addKeyUpListener(final Predicate<KeyEvent> listener) {
        this.keyUpListeners.add(listener);
        return this;
    }

    public final Component removeKeyUpListener(final Predicate<KeyEvent> listener) {
        this.keyUpListeners.remove(listener);
        return this;
    }

    public final Component addCharTypedListener(final Predicate<CharEvent> listener) {
        this.charTypedListeners.add(listener);
        return this;
    }

    public final Component removeCharTypedListener(final Predicate<CharEvent> listener) {
        this.charTypedListeners.remove(listener);
        return this;
    }

    public final Component addMouseEnterListener(final BooleanSupplier listener) {
        this.mouseEnterListeners.add(listener);
        return this;
    }

    public final Component removeMouseEnterListener(final BooleanSupplier listener) {
        this.mouseEnterListeners.remove(listener);
        return this;
    }

    public final Component addMouseLeaveListener(final BooleanSupplier listener) {
        this.mouseLeaveListeners.add(listener);
        return this;
    }

    public final Component removeMouseLeaveListener(final BooleanSupplier listener) {
        this.mouseLeaveListeners.remove(listener);
        return this;
    }

    public final Component addMouseDownListener(final BiPredicate<MouseButtonEvent, Rectangle> listener) {
        this.mouseDownListeners.add(listener);
        return this;
    }

    public final Component removeMouseDownListener(final BiPredicate<MouseButtonEvent, Rectangle> listener) {
        this.mouseDownListeners.remove(listener);
        return this;
    }

    public final Component addMouseUpListener(final BiPredicate<MouseButtonEvent, Rectangle> listener) {
        this.mouseUpListeners.add(listener);
        return this;
    }

    public final Component removeMouseUpListener(final BiPredicate<MouseButtonEvent, Rectangle> listener) {
        this.mouseUpListeners.remove(listener);
        return this;
    }

    public final Component addMouseMoveListener(final BiPredicate<MouseMoveEvent, Rectangle> listener) {
        this.mouseMoveListeners.add(listener);
        return this;
    }

    public final Component removeMouseMoveListener(final BiPredicate<MouseMoveEvent, Rectangle> listener) {
        this.mouseMoveListeners.remove(listener);
        return this;
    }

    public final Component addMouseScrollListener(final BiPredicate<MouseScrollEvent, Rectangle> listener) {
        this.mouseScrollListeners.add(listener);
        return this;
    }

    public final Component removeMouseScrollListener(final BiPredicate<MouseScrollEvent, Rectangle> listener) {
        this.mouseScrollListeners.remove(listener);
        return this;
    }


    public final void onFocusGained() {
        for (BooleanSupplier listener : this.focusGainedListeners) {
            if (listener.getAsBoolean()) {
                return;
            }
        }
        this.onComponentFocusGained();
    }

    protected void onComponentFocusGained() {
    }

    public final void onFocusLost() {
        for (BooleanSupplier listener : this.focusLostListeners) {
            if (listener.getAsBoolean()) {
                return;
            }
        }
        this.onComponentFocusLost();
    }

    protected void onComponentFocusLost() {
    }

    public final boolean onKeyDown(final KeyEvent event) {
        for (Predicate<KeyEvent> listener : this.keyDownListeners) {
            if (listener.test(event)) {
                return true;
            }
        }
        return this.onComponentKeyDown(event);
    }

    protected boolean onComponentKeyDown(final KeyEvent event) {
        return false;
    }

    public final boolean onKeyUp(final KeyEvent event) {
        for (Predicate<KeyEvent> listener : this.keyUpListeners) {
            if (listener.test(event)) {
                return true;
            }
        }
        return this.onComponentKeyUp(event);
    }

    protected boolean onComponentKeyUp(final KeyEvent event) {
        return false;
    }

    public final boolean onCharTyped(final CharEvent event) {
        for (Predicate<CharEvent> listener : this.charTypedListeners) {
            if (listener.test(event)) {
                return true;
            }
        }
        return this.onComponentCharTyped(event);
    }

    protected boolean onComponentCharTyped(final CharEvent event) {
        return false;
    }

    public final void onMouseEnter() {
        for (BooleanSupplier listener : this.mouseEnterListeners) {
            if (listener.getAsBoolean()) {
                return;
            }
        }
        this.onComponentMouseEnter();
    }

    protected void onComponentMouseEnter() {
    }

    public final void onMouseLeave() {
        for (BooleanSupplier listener : this.mouseLeaveListeners) {
            if (listener.getAsBoolean()) {
                return;
            }
        }
        this.onComponentMouseLeave();
    }

    protected void onComponentMouseLeave() {
    }

    public final boolean onMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
        for (BiPredicate<MouseButtonEvent, Rectangle> listener : this.mouseDownListeners) {
            if (listener.test(event, bounds)) {
                return true;
            }
        }
        return this.onComponentMouseDown(event, bounds);
    }

    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
        return false;
    }

    public final boolean onMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
        for (BiPredicate<MouseButtonEvent, Rectangle> listener : this.mouseUpListeners) {
            if (listener.test(event, bounds)) {
                return true;
            }
        }
        return this.onComponentMouseUp(event, bounds);
    }

    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
        return false;
    }

    public final boolean onMouseMove(final MouseMoveEvent event, final Rectangle bounds) {
        for (BiPredicate<MouseMoveEvent, Rectangle> listener : this.mouseMoveListeners) {
            if (listener.test(event, bounds)) {
                return true;
            }
        }
        return this.onComponentMouseMove(event, bounds);
    }

    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Rectangle bounds) {
        return false;
    }

    public final boolean onMouseScroll(final MouseScrollEvent event, final Rectangle bounds) {
        for (BiPredicate<MouseScrollEvent, Rectangle> listener : this.mouseScrollListeners) {
            if (listener.test(event, bounds)) {
                return true;
            }
        }
        return this.onComponentMouseScroll(event, bounds);
    }

    protected boolean onComponentMouseScroll(final MouseScrollEvent event, final Rectangle bounds) {
        return false;
    }

    public void render(final Renderer renderer, final Rectangle bounds) {
    }

    public abstract void computeIdealSize(final Size constraints);

    public void computeLayout(final Size size) {
    }

}
