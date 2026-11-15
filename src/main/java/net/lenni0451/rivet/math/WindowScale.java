package net.lenni0451.rivet.math;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WindowScale {

    private final Runnable changeListener;
    private boolean manual = false;
    private float manualScale = 1;
    private float automaticScale = 1;

    public WindowScale(final Runnable changeListener) {
        this.changeListener = changeListener;
    }

    public boolean manual() {
        return this.manual;
    }

    public float manualScale() {
        return this.manualScale;
    }

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

    public float automaticScale() {
        return this.automaticScale;
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
