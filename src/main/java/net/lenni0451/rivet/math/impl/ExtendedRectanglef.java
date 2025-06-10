package net.lenni0451.rivet.math.impl;

import net.lenni0451.rivet.math.Bounds;
import net.lenni0451.rivet.math.Position;
import net.lenni0451.rivet.math.Size;
import org.joml.primitives.Rectanglef;

public class ExtendedRectanglef extends Rectanglef implements Bounds, Position, Size {

    public ExtendedRectanglef() {
    }

    public ExtendedRectanglef(final Rectanglef rectangle) {
        super(rectangle);
    }

    public ExtendedRectanglef(final float minX, final float minY, final float maxX, final float maxY) {
        super(minX, minY, maxX, maxY);
    }

    @Override
    public float x() {
        return this.minX;
    }

    @Override
    public float y() {
        return this.minY;
    }

    @Override
    public float x1() {
        return this.minX;
    }

    @Override
    public float y1() {
        return this.minY;
    }

    @Override
    public float x2() {
        return this.maxX;
    }

    @Override
    public float y2() {
        return this.maxY;
    }

    @Override
    public float width() {
        return this.lengthX();
    }

    @Override
    public float height() {
        return this.lengthY();
    }

}
