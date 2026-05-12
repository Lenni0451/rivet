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

    public Component minSize(final Size minSize) {
        if (!this.minSize.equals(minSize)) {
            this.minSize = minSize;
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public Component maxSize(final Size maxSize) {
        if (!this.maxSize.equals(maxSize)) {
            this.maxSize = maxSize;
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public Component layoutOptions(final LayoutOptions layoutOptions) {
        if (this.layoutOptions == null || !this.layoutOptions.equals(layoutOptions)) {
            this.layoutOptions = layoutOptions;
            this.rivet.recalculateNextFrame();
        }
        return this;
    }


    public final void onFocusGained() {
        this.onComponentFocusGained();
    }

    protected void onComponentFocusGained() {
    }

    public final void onFocusLost() {
        this.onComponentFocusLost();
    }

    protected void onComponentFocusLost() {
    }

    public final boolean onKeyDown(final KeyEvent event) {
        return this.onComponentKeyDown(event);
    }

    protected boolean onComponentKeyDown(final KeyEvent event) {
        return false;
    }

    public final boolean onKeyUp(final KeyEvent event) {
        return this.onComponentKeyUp(event);
    }

    protected boolean onComponentKeyUp(final KeyEvent event) {
        return false;
    }

    public final boolean onCharTyped(final CharEvent event) {
        return this.onComponentCharTyped(event);
    }

    protected boolean onComponentCharTyped(final CharEvent event) {
        return false;
    }

    public final void onMouseEnter() {
        this.onComponentMouseEnter();
    }

    protected void onComponentMouseEnter() {
    }

    public final void onMouseLeave() {
        this.onComponentMouseLeave();
    }

    protected void onComponentMouseLeave() {
    }

    public final boolean onMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
        return this.onComponentMouseDown(event, bounds);
    }

    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
        return false;
    }

    public final boolean onMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
        return this.onComponentMouseUp(event, bounds);
    }

    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
        return false;
    }

    public final boolean onMouseMove(final MouseMoveEvent event, final Rectangle bounds) {
        return this.onComponentMouseMove(event, bounds);
    }

    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Rectangle bounds) {
        return false;
    }

    public final boolean onMouseScroll(final MouseScrollEvent event, final Rectangle bounds) {
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
