package net.lenni0451.rivet.math;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public final class WindowScale {

    private final Runnable changeListener;
    @Getter
    private boolean manual = false;
    @Getter
    private float manualScale = 1;
    @Getter
    private float automaticScale = 1;

    public WindowScale manualScale(final float scale) {
        float previousScaleFactor = this.scaleFactor();
        this.manual = true;
        this.manualScale = scale;
        this.notifyChange(previousScaleFactor);
        return this;
    }

    public WindowScale resetManualScale() {
        float previousScaleFactor = this.scaleFactor();
        this.manual = false;
        this.manualScale = 1;
        this.notifyChange(previousScaleFactor);
        return this;
    }

    public WindowScale automaticScale(final float scale) {
        float previousScaleFactor = this.scaleFactor();
        this.automaticScale = scale;
        this.notifyChange(previousScaleFactor);
        return this;
    }

    public float scaleFactor() {
        return this.manual ? this.manualScale : this.automaticScale;
    }

    public Size scale(final Size size) {
        float scaleFactor = this.scaleFactor();
        return new Size(
                (int) (size.width() / scaleFactor),
                (int) (size.height() / scaleFactor)
        );
    }

    public float scale(final float f) {
        return f / this.scaleFactor();
    }

    private void notifyChange(final float previousScaleFactor) {
        if (this.scaleFactor() != previousScaleFactor) {
            this.changeListener.run();
        }
    }

}
