package net.lenni0451.rivet.backend.thingl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.ShapedText;
import net.raphimc.thingl.text.TextLine;
import net.raphimc.thingl.text.font.FontSet;
import net.raphimc.thingl.text.shaping.ShapedTextLine;

@RequiredArgsConstructor
public class ThinGLBackend implements Backend {

    @Getter
    private final FontSet fontSet;

    @Override
    public ShapedText shapeText(String text) {
        ShapedTextLine shapedTextLine = TextLine.fromString(this.fontSet, text).shape();
        return new ThinGLShapedText(shapedTextLine);
    }

}
