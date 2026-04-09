package net.lenni0451.rivet.component;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.layout.LayoutOptions;
import net.lenni0451.rivet.math.Size;

public abstract class Component {

    protected final Rivet rivet;
    private Size minSize = Size.EMPTY;
    protected Size idealSize = Size.EMPTY;
    private Size maxSize = new Size(Float.MAX_VALUE, Float.MAX_VALUE);
    private LayoutOptions layoutOptions;

    public Component(final Rivet rivet) {
        this.rivet = rivet;
    }

    public Rivet rivet() {
        return this.rivet;
    }

    public Size minSize() {
        return this.minSize;
    }

    public Component setMinSize(final Size minSize) {
        if (!this.minSize.equals(minSize)) {
            this.minSize = minSize;
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public Size idealSize() {
        return this.idealSize;
    }

    public Size maxSize() {
        return this.maxSize;
    }

    public Component setMaxSize(final Size maxSize) {
        if (!this.maxSize.equals(maxSize)) {
            this.maxSize = maxSize;
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public LayoutOptions layoutOptions() {
        return this.layoutOptions;
    }

    public Component setLayoutOptions(final LayoutOptions layoutOptions) {
        if (this.layoutOptions == null || !this.layoutOptions.equals(layoutOptions)) {
            this.layoutOptions = layoutOptions;
            this.rivet.recalculateNextFrame();
        }
        return this;
    }


    public void onFocusGained() {
    }

    public void onFocusLost() {
    }

    public abstract void computeIdealSize();

    public void computeLayout(final Size size) {
    }

}
