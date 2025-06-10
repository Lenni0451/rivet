package net.lenni0451.rivet.math.impl;

import net.lenni0451.rivet.math.Position;
import net.lenni0451.rivet.math.Size;
import org.joml.Vector2f;

public class ExtendedVector2f extends Vector2f implements Position, Size {

    public ExtendedVector2f() {
    }

    public ExtendedVector2f(final Vector2f vector) {
        super(vector);
    }

    public ExtendedVector2f(final float x, final float y) {
        super(x, y);
    }

    @Override
    public float width() {
        return this.x;
    }

    @Override
    public float height() {
        return this.y;
    }

}
