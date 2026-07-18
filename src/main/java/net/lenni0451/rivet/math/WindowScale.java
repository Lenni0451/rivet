package net.lenni0451.rivet.math;

public final class WindowScale {

    private boolean manual = false;
    private float manualScale = 1;
    private float automaticScale = 1;

    public boolean manual() {
        return this.manual;
    }

    public float manualScale() {
        return this.manualScale;
    }

    public WindowScale manualScale(final float scale) {
        this.manual = true;
        this.manualScale = scale;
        return this;
    }

    public WindowScale resetManualScale() {
        this.manual = false;
        this.manualScale = 1;
        return this;
    }

    public float automaticScale() {
        return this.automaticScale;
    }

    public WindowScale automaticScale(final float scale) {
        this.automaticScale = scale;
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

}
