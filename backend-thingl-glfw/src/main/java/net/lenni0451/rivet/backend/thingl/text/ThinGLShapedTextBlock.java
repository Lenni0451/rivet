package net.lenni0451.rivet.backend.thingl.text;

import net.lenni0451.rivet.math.Rectangle;
import net.raphimc.thingl.text.shaping.ShapedTextBlock;
import org.joml.primitives.Rectanglef;

public record ThinGLShapedTextBlock(ShapedTextBlock shapedTextBlock) implements net.lenni0451.rivet.backend.text.ShapedTextBlock {

    @Override
    public Rectangle visualBounds() {
        Rectanglef bounds = this.shapedTextBlock.visualBounds();
        return new Rectangle(bounds.minX, bounds.minY, bounds.lengthX(), bounds.lengthY());
    }

    @Override
    public Rectangle logicalBounds() {
        Rectanglef bounds = this.shapedTextBlock.logicalBounds();
        return new Rectangle(bounds.minX, bounds.minY, bounds.lengthX(), bounds.lengthY());
    }

    @Override
    public float cursorPosition(final int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int index(final float x) {
        throw new UnsupportedOperationException();
    }

}
