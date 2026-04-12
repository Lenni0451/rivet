package net.lenni0451.rivet.component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.layout.LayoutOptions;
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


    public void onFocusGained() {
    }

    public void onFocusLost() {
    }

    public abstract void computeIdealSize();

    public void computeLayout(final Size size) {
    }

}
