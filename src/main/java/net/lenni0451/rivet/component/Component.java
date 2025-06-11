package net.lenni0451.rivet.component;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.container.Container;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.math.impl.ExtendedVector2f;
import net.lenni0451.rivet.math.impl.FloatPadding;
import org.jetbrains.annotations.ApiStatus;

public abstract class Component {

    protected Rivet rivet;
    protected Container parent;
    private final ExtendedVector2f minSize = new ExtendedVector2f(0, 0);
    protected final ExtendedVector2f preferredSize = new ExtendedVector2f(); //Is immediately computed in the constructor
    private final ExtendedVector2f maxSize = new ExtendedVector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    private final FloatPadding padding = new FloatPadding();

    public Component() {
        this.computePreferredSize();
    }

    public Size getMinSize() {
        return this.minSize;
    }

    public void setMinSize(final float width, final float height) {
        this.minSize.set(width, height);
    }

    public ExtendedVector2f getPreferredSize() {
        return this.preferredSize;
    }

    public Size getMaxSize() {
        return this.maxSize;
    }

    public void setMaxSize(final float width, final float height) {
        this.maxSize.set(width, height);
    }

    public Padding getPadding() {
        return this.padding;
    }

    public void setPadding(final float left, final float top, final float right, final float bottom) {
        this.padding.set(left, top, right, bottom);
    }

    public void onFocusGained() {
    }

    public void onFocusLost() {
    }

    protected abstract void computePreferredSize();

    @ApiStatus.Internal
    public void onAdded(final Rivet rivet, final Container parent) {
        this.rivet = rivet;
        this.parent = parent;
    }

}
