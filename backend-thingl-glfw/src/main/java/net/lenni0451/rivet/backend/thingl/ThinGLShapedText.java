package net.lenni0451.rivet.backend.thingl;

import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.math.Size;
import net.raphimc.thingl.text.shaping.ShapedTextLine;
import org.joml.primitives.Rectanglef;

public record ThinGLShapedText(ShapedTextLine shapedTextLine) implements ShapedText {

    @Override
    public Size visualSize() {
        Rectanglef bounds = this.shapedTextLine.visualBounds();
        return new Size(bounds.lengthX(), bounds.lengthY());
    }

    @Override
    public Size logicalSize() {
        Rectanglef bounds = this.shapedTextLine.logicalBounds();
        return new Size(bounds.lengthX(), bounds.lengthY());
    }

}
