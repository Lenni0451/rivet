package net.lenni0451.rivet.backend.opengl;

import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.Font;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.ShapedTextBuffer;
import net.lenni0451.rivet.text.TextBuffer;
import net.lenni0451.rivet.text.TextRun;
import net.lenni0451.rivet.text.TextSegment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThinGLBackend implements Backend {

    private final Renderer renderer = new ThinGLRenderer();
    private final Set<net.raphimc.thingl.text.font.Font> loadedFonts = new HashSet<>();

    @Override
    public Renderer getRenderer() {
        return this.renderer;
    }

    @Override
    public Font loadFont(final byte[] fontData, final int size) {
        final net.raphimc.thingl.text.font.Font font = new net.raphimc.thingl.text.font.Font(fontData, size);
        this.loadedFonts.add(font);
        return new ThinGLFont(font);
    }

    @Override
    public ShapedTextBuffer shapeTextBuffer(final TextBuffer textBuffer) {
        final net.raphimc.thingl.text.TextBuffer thinGlTextBuffer = new net.raphimc.thingl.text.TextBuffer(new ArrayList<>(textBuffer.runs().size()));
        for (TextRun run : textBuffer.runs()) {
            final List<net.raphimc.thingl.text.TextSegment> thinGlSegments = new ArrayList<>(run.segments().size());
            for (TextSegment segment : run.segments()) {
                thinGlSegments.add(new net.raphimc.thingl.text.TextSegment(segment.text(), segment.color(), segment.styleFlags(), segment.outlineColor(), segment.xVisualOffset(), segment.yVisualOffset()));
            }
            thinGlTextBuffer.add(new net.raphimc.thingl.text.TextRun(((ThinGLFont) run.font()).font(), thinGlSegments, run.xOffset(), run.yOffset()));
        }
        return new ThinGLShapedTextBuffer(thinGlTextBuffer.shape());
    }

    public void free() {
        for (net.raphimc.thingl.text.font.Font font : this.loadedFonts) {
            font.free();
        }
    }

}
