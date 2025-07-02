package net.lenni0451.rivet.backend.opengl;

import net.lenni0451.rivet.backend.text.ShapedTextBuffer;
import net.raphimc.thingl.text.font.Font;
import net.raphimc.thingl.text.shaper.ShapedTextRun;
import net.raphimc.thingl.text.shaper.ShapedTextSegment;
import org.joml.primitives.Rectanglef;

public record ThinGLShapedTextBuffer(net.raphimc.thingl.text.shaper.ShapedTextBuffer shapedTextBuffer, float extendedWidth) implements ShapedTextBuffer {

    public ThinGLShapedTextBuffer(final net.raphimc.thingl.text.shaper.ShapedTextBuffer shapedTextBuffer) {
        this(shapedTextBuffer, calculateExtendedWidth(shapedTextBuffer));
    }

    @Override
    public Rectanglef bounds() {
        return this.shapedTextBuffer.bounds();
    }

    private static float calculateExtendedWidth(final net.raphimc.thingl.text.shaper.ShapedTextBuffer shapedTextBuffer) {
        if (shapedTextBuffer.runs().isEmpty()) {
            return 0F;
        }
        final ShapedTextRun lastRun = shapedTextBuffer.runs().get(shapedTextBuffer.runs().size() - 1);
        if (lastRun.segments().isEmpty()) {
            return 0F;
        }
        final ShapedTextSegment lastSegment = lastRun.segments().get(lastRun.segments().size() - 1);
        if (lastSegment.glyphs().isEmpty()) {
            return 0F;
        }
        final Font.Glyph lastGlyph = lastSegment.glyphs().get(lastSegment.glyphs().size() - 1).fontGlyph();
        return shapedTextBuffer.bounds().lengthX() - lastGlyph.width() - lastGlyph.bearingX() + lastGlyph.xAdvance();
    }

}
