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
    private Size maxSize = new Size(Float.MAX_VALUE, Float.MAX_VALUE);
    @Getter
    private LayoutOptions layoutOptions;
    @Getter
    @Setter
    private boolean interactive = true;

    @Getter
    private final ListenerList<BooleanSupplier> focusGainedListener = new ListenerList<>();
    @Getter
    private final ListenerList<BooleanSupplier> focusLostListener = new ListenerList<>();
    @Getter
    private final ListenerList<Predicate<KeyEvent>> keyDownListener = new ListenerList<>();
    @Getter
    private final ListenerList<Predicate<KeyEvent>> keyUpListener = new ListenerList<>();
    @Getter
    private final ListenerList<Predicate<CharEvent>> charTypedListener = new ListenerList<>();
    @Getter
    private final ListenerList<BooleanSupplier> mouseEnterListener = new ListenerList<>();
    @Getter
    private final ListenerList<BooleanSupplier> mouseLeaveListener = new ListenerList<>();
    @Getter
    private final ListenerList<BiPredicate<MouseButtonEvent, Rectangle>> mouseDownListener = new ListenerList<>();
    @Getter
    private final ListenerList<BiPredicate<MouseButtonEvent, Rectangle>> mouseUpListener = new ListenerList<>();
    @Getter
    private final ListenerList<BiPredicate<MouseMoveEvent, Rectangle>> mouseMoveListener = new ListenerList<>();
    @Getter
    private final ListenerList<BiPredicate<MouseScrollEvent, Rectangle>> mouseScrollListener = new ListenerList<>();

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

    public final Component fixedSize(final Size size) {
        this.minSize(size);
        this.maxSize(size);
        return this;
    }

    public final Component layoutOptions(final LayoutOptions layoutOptions) {
        if (this.layoutOptions == null || !this.layoutOptions.equals(layoutOptions)) {
            this.layoutOptions = layoutOptions;
            this.rivet.recalculateNextFrame();
        }
        return this;
    }


    public final void onFocusGained() {
        this.focusGainedListener.call(BooleanSupplier::getAsBoolean, () -> {
            this.onComponentFocusGained();
            return false;
        });
    }

    protected void onComponentFocusGained() {
    }

    public final void onFocusLost() {
        this.focusLostListener.call(BooleanSupplier::getAsBoolean, () -> {
            this.onComponentFocusLost();
            return false;
        });
    }

    protected void onComponentFocusLost() {
    }

    public final boolean onKeyDown(final KeyEvent event) {
        return this.keyDownListener.call(l -> l.test(event), () -> this.onComponentKeyDown(event));
    }

    protected boolean onComponentKeyDown(final KeyEvent event) {
        return false;
    }

    public final boolean onKeyUp(final KeyEvent event) {
        return this.keyUpListener.call(l -> l.test(event), () -> this.onComponentKeyUp(event));
    }

    protected boolean onComponentKeyUp(final KeyEvent event) {
        return false;
    }

    public final boolean onCharTyped(final CharEvent event) {
        return this.charTypedListener.call(l -> l.test(event), () -> this.onComponentCharTyped(event));
    }

    protected boolean onComponentCharTyped(final CharEvent event) {
        return false;
    }

    public final void onMouseEnter() {
        this.mouseEnterListener.call(BooleanSupplier::getAsBoolean, () -> {
            this.onComponentMouseEnter();
            return false;
        });
    }

    protected void onComponentMouseEnter() {
    }

    public final void onMouseLeave() {
        this.mouseLeaveListener.call(BooleanSupplier::getAsBoolean, () -> {
            this.onComponentMouseLeave();
            return false;
        });
    }

    protected void onComponentMouseLeave() {
    }

    public final boolean onMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
        return this.mouseDownListener.call(l -> l.test(event, bounds), () -> this.onComponentMouseDown(event, bounds));
    }

    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
        return false;
    }

    public final boolean onMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
        return this.mouseUpListener.call(l -> l.test(event, bounds), () -> this.onComponentMouseUp(event, bounds));
    }

    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
        return false;
    }

    public final boolean onMouseMove(final MouseMoveEvent event, final Rectangle bounds) {
        return this.mouseMoveListener.call(l -> l.test(event, bounds), () -> this.onComponentMouseMove(event, bounds));
    }

    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Rectangle bounds) {
        return false;
    }

    public final boolean onMouseScroll(final MouseScrollEvent event, final Rectangle bounds) {
        return this.mouseScrollListener.call(l -> l.test(event, bounds), () -> this.onComponentMouseScroll(event, bounds));
    }

    protected boolean onComponentMouseScroll(final MouseScrollEvent event, final Rectangle bounds) {
        return false;
    }

    public void render(final Renderer renderer, final Rectangle bounds) {
    }

    public abstract Size computeIdealSize(final Size constraints);

    public void computeLayout(final Size size) {
    }

}
