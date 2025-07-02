package net.lenni0451.rivet.backend.opengl;

import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedTextBuffer;
import net.lenni0451.rivet.text.TextBuffer;
import net.lenni0451.rivet.text.TextRun;
import net.lenni0451.rivet.text.TextSegment;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.resource.texture.AbstractTexture;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ThinGLBackend implements Backend {

    public static final Cleaner CLEANER = Cleaner.create();

    private final Renderer renderer = new ThinGLRenderer();
    private final Supplier<String> clipboardSupplier;
    private final Consumer<String> clipboardConsumer;

    public ThinGLBackend(final Supplier<String> clipboardSupplier, final Consumer<String> clipboardConsumer) {
        this.clipboardSupplier = clipboardSupplier;
        this.clipboardConsumer = clipboardConsumer;
    }

    @Override
    public boolean isOnRenderThread() {
        return ThinGL.get().isOnRenderThread();
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

    @Override
    public Texture loadTexture(final byte[] pngData) {
        return new ThinGLTexture(new net.raphimc.thingl.resource.texture.Texture2D(AbstractTexture.InternalFormat.RGBA8, pngData));
    }

    @Override
    public String getClipboardText() {
        return this.clipboardSupplier.get();
    }

    @Override
    public void setClipboardText(final String text) {
        this.clipboardConsumer.accept(text);
    }

}
