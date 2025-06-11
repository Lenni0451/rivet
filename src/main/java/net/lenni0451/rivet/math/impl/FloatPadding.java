package net.lenni0451.rivet.math.impl;

import net.lenni0451.rivet.math.Padding;

import java.util.Objects;

public class FloatPadding implements Padding {

    public float left;
    public float top;
    public float right;
    public float bottom;

    public FloatPadding() {
    }

    public FloatPadding(final FloatPadding padding) {
        this(padding.left, padding.top, padding.right, padding.bottom);
    }

    public FloatPadding(final float left, final float top, final float right, final float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public float left() {
        return this.left;
    }

    @Override
    public float top() {
        return this.top;
    }

    @Override
    public float right() {
        return this.right;
    }

    @Override
    public float bottom() {
        return this.bottom;
    }

    public void set(final float left, final float top, final float right, final float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public String toString() {
        return "FloatPadding{" +
                "left=" + this.left +
                ", top=" + this.top +
                ", right=" + this.right +
                ", bottom=" + this.bottom +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FloatPadding that = (FloatPadding) o;
        return Float.compare(this.left, that.left) == 0 && Float.compare(this.top, that.top) == 0 && Float.compare(this.right, that.right) == 0 && Float.compare(this.bottom, that.bottom) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.left, this.top, this.right, this.bottom);
    }

}
