package net.lenni0451.rivet.backend.opengl;

import net.lenni0451.rivet.backend.*;
import net.lenni0451.rivet.text.TextBuffer;
import net.lenni0451.rivet.text.TextRun;
import net.lenni0451.rivet.text.TextSegment;

import java.util.ArrayList;
import java.util.List;

public class ThinGLBackend implements Backend {

    private final Renderer renderer;

    public ThinGLBackend() {
        this.renderer = new ThinGLRenderer();
    }

    @Override
    public Renderer getRenderer() {
        return this.renderer;
    }

    @Override
    public Font loadFont(final byte[] fontData, final int size) {
        return new ThinGLFont(new net.raphimc.thingl.text.font.Font(fontData, size));
    }

    @Override
    public FontSet createFontSet(final List<Font> fonts) {
        final List<net.raphimc.thingl.text.font.Font> thinGlFonts = new ArrayList<>(fonts.size());
        for (Font font : fonts) {
            thinGlFonts.add(((ThinGLFont) font).font);
        }
        return new ThinGLFontSet(new net.raphimc.thingl.text.font.FontSet(thinGlFonts));
    }

    @Override
    public ShapedTextBuffer shapeTextBuffer(final TextBuffer textBuffer) {
        final net.raphimc.thingl.text.TextBuffer thinGlTextBuffer = new net.raphimc.thingl.text.TextBuffer();
        for (TextRun run : textBuffer.runs()) {
            final List<net.raphimc.thingl.text.TextSegment> thinGlSegments = new ArrayList<>(run.segments().size());
            for (TextSegment segment : run.segments()) {
                thinGlSegments.add(new net.raphimc.thingl.text.TextSegment(segment.text(), segment.color(), segment.styleFlags()));
            }
            thinGlTextBuffer.add(new net.raphimc.thingl.text.TextRun(((ThinGLFont) run.font()).font, thinGlSegments, run.xOffset(), run.yOffset()));
        }
        return new ThinGLShapedTextBuffer(thinGlTextBuffer.shape());
    }

}
