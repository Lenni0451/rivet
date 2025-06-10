package net.lenni0451.rivet.component;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.event.ComponentEvent;
import net.lenni0451.rivet.event.ComponentListener;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.math.impl.ExtendedVector2f;
import net.lenni0451.rivet.math.impl.FloatPadding;

import java.util.ArrayList;
import java.util.List;

public abstract class Component {

    protected final ExtendedVector2f minSize = new ExtendedVector2f(0, 0);
    protected final ExtendedVector2f preferredSize = new ExtendedVector2f(0, 0);
    protected final ExtendedVector2f maxSize = new ExtendedVector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    protected final FloatPadding padding = new FloatPadding();
    protected final List<ComponentListener> eventListeners = new ArrayList<>();

    public Component() {
        this.computePreferredSize();
    }

    public Size getMinSize() {
        return this.minSize;
    }

    public void setMinSize(final float width, final float height) {
        Size oldSize = new ExtendedVector2f(this.minSize);
        this.minSize.set(width, height);
        this.fireEvent(new MinSizeChangedEvent(this, oldSize, this.minSize));
    }

    public ExtendedVector2f getPreferredSize() {
        return this.preferredSize;
    }

    public Size getMaxSize() {
        return this.maxSize;
    }

    public void setMaxSize(final float width, final float height) {
        Size oldSize = new ExtendedVector2f(this.maxSize);
        this.maxSize.set(width, height);
        this.fireEvent(new MaxSizeChangedEvent(this, oldSize, this.maxSize));
    }

    public Padding getPadding() {
        return this.padding;
    }

    public void setPadding(final float left, final float top, final float right, final float bottom) {
        Padding oldPadding = new FloatPadding(this.padding);
        this.padding.set(left, top, right, bottom);
        this.fireEvent(new PaddingChangedEvent(this, oldPadding, this.padding));
    }

    public void addListener(final ComponentListener listener) {
        this.eventListeners.add(listener);
    }

    public boolean removeListener(final ComponentListener listener) {
        return this.eventListeners.remove(listener);
    }

    protected abstract void computePreferredSize();

    protected void fireEvent(final ComponentEvent event) {
        for (ComponentListener listener : this.eventListeners) {
            try {
                listener.handle(event);
            } catch (Throwable t) {
                Rivet.LOGGER.error("Unhandled exception in component listener", t);
            }
        }
    }


    public static class MinSizeChangedEvent extends ComponentEvent {
        private final Size oldSize;
        private final Size newSize;

        public MinSizeChangedEvent(final Component owner, final Size oldSize, final Size newSize) {
            super(owner);
            this.oldSize = oldSize;
            this.newSize = newSize;
        }

        public Size oldSize() {
            return this.oldSize;
        }

        public Size newSize() {
            return this.newSize;
        }
    }

    public static class MaxSizeChangedEvent extends ComponentEvent {
        private final Size oldSize;
        private final Size newSize;

        public MaxSizeChangedEvent(final Component owner, final Size oldSize, final Size newSize) {
            super(owner);
            this.oldSize = oldSize;
            this.newSize = newSize;
        }

        public Size oldSize() {
            return this.oldSize;
        }

        public Size newSize() {
            return this.newSize;
        }
    }

    public static class PaddingChangedEvent extends ComponentEvent {
        private final Padding oldPadding;
        private final Padding newPadding;

        public PaddingChangedEvent(final Component owner, final Padding oldPadding, final Padding newPadding) {
            super(owner);
            this.oldPadding = oldPadding;
            this.newPadding = newPadding;
        }

        public Padding oldPadding() {
            return this.oldPadding;
        }

        public Padding newPadding() {
            return this.newPadding;
        }
    }

}
