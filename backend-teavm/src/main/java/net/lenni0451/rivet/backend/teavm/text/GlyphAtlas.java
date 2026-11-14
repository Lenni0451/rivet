package net.lenni0451.rivet.backend.teavm.text;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.teavm.utils.JsUtils;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.canvas.TextMetrics;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.webgl.WebGL2RenderingContext;
import org.teavm.jso.webgl.WebGLRenderingContext;
import org.teavm.jso.webgl.WebGLTexture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rasterizes glyphs using the browser's canvas text renderer and packs them into texture pages.<br>
 * Glyphs are rasterized at their final device pixel size, so text is always rendered 1:1 without scaling artifacts.
 */
@Accessors(fluent = true, chain = true, makeFinal = true)
public final class GlyphAtlas {

    private static final int PAGE_SIZE = 1024;
    private static final int MAX_PAGES = 8;
    private static final int GAP = 1;
    private static final int RGBA8 = 0x8058;

    private final WebGL2RenderingContext gl;
    private final List<Page> pages = new ArrayList<>();
    private final Map<String, GlyphSet> glyphSets = new HashMap<>();
    private final HTMLCanvasElement canvas;
    private final CanvasRenderingContext2D context;
    @Getter
    private int generation;

    public GlyphAtlas(final WebGL2RenderingContext gl) {
        this.gl = gl;
        this.canvas = (HTMLCanvasElement) HTMLDocument.current().createElement("canvas");
        this.context = JsUtils.getSoftwareContext2D(this.canvas);
    }

    public void beginFrame() {
        if (this.pages.size() > MAX_PAGES) {
            this.clear();
        }
    }

    public void clear() {
        for (Page page : this.pages) {
            this.gl.deleteTexture(page.texture);
        }
        this.pages.clear();
        this.glyphSets.clear();
        this.generation++;
    }

    public GlyphSet glyphSet(final String css, final float outlineWidth) {
        String key = outlineWidth > 0 ? css + "|" + outlineWidth : css;
        GlyphSet glyphSet = this.glyphSets.get(key);
        if (glyphSet == null) {
            glyphSet = new GlyphSet(css, outlineWidth);
            this.glyphSets.put(key, glyphSet);
        }
        return glyphSet;
    }

    private Glyph rasterize(final String css, final float outlineWidth, final int codePoint) {
        String text = new String(Character.toChars(codePoint));
        this.context.setFont(css);
        TextMetrics metrics = this.context.measureText(text);
        int left = (int) Math.ceil(metrics.getActualBoundingBoxLeft());
        int right = (int) Math.ceil(metrics.getActualBoundingBoxRight());
        int ascent = (int) Math.ceil(metrics.getActualBoundingBoxAscent());
        int descent = (int) Math.ceil(metrics.getActualBoundingBoxDescent());
        if (left + right <= 0 || ascent + descent <= 0) return Glyph.EMPTY;

        int padding = 1 + (int) Math.ceil(outlineWidth / 2F);
        int width = left + right + padding * 2;
        int height = ascent + descent + padding * 2;
        if (width > PAGE_SIZE || height > PAGE_SIZE) return Glyph.EMPTY;

        // Resizing the canvas also resets its state and clears it
        this.canvas.setWidth(width);
        this.canvas.setHeight(height);
        this.context.setFont(css);
        this.context.setTextBaseline("alphabetic");
        if (outlineWidth > 0) {
            this.context.setLineWidth(outlineWidth);
            this.context.setLineCap("round");
            this.context.setLineJoin("round");
            this.context.setStrokeStyle("#fff");
            this.context.strokeText(text, padding + left, padding + ascent);
        }
        this.context.setFillStyle("#fff");
        this.context.fillText(text, padding + left, padding + ascent);

        Page page = this.allocate(width, height);
        this.gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, page.texture);
        this.gl.pixelStorei(WebGLRenderingContext.UNPACK_PREMULTIPLY_ALPHA_WEBGL, 1);
        this.gl.texSubImage2D(WebGLRenderingContext.TEXTURE_2D, 0, page.allocX, page.allocY, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, this.canvas);
        this.gl.pixelStorei(WebGLRenderingContext.UNPACK_PREMULTIPLY_ALPHA_WEBGL, 0);
        return new Glyph(
                page.texture,
                (float) page.allocX / PAGE_SIZE, (float) page.allocY / PAGE_SIZE,
                (float) (page.allocX + width) / PAGE_SIZE, (float) (page.allocY + height) / PAGE_SIZE,
                -(left + padding), -(ascent + padding), width, height
        );
    }

    private Page allocate(final int width, final int height) {
        if (!this.pages.isEmpty()) {
            Page page = this.pages.get(this.pages.size() - 1);
            if (page.allocate(width, height)) return page;
        }
        Page page = new Page(this.createPageTexture());
        this.pages.add(page);
        if (!page.allocate(width, height)) throw new IllegalStateException("Glyph does not fit into an empty atlas page");
        return page;
    }

    private WebGLTexture createPageTexture() {
        WebGLTexture texture = this.gl.createTexture();
        this.gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, texture);
        this.gl.texStorage2D(WebGLRenderingContext.TEXTURE_2D, 1, RGBA8, PAGE_SIZE, PAGE_SIZE);
        this.gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
        this.gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
        this.gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_S, WebGLRenderingContext.CLAMP_TO_EDGE);
        this.gl.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_T, WebGLRenderingContext.CLAMP_TO_EDGE);
        return texture;
    }


    /**
     * {@code left}/{@code top} are the offsets (in raster pixels) of the bitmap relative to the pen position on the baseline.
     */
    public record Glyph(WebGLTexture texture, float u0, float v0, float u1, float v1, float left, float top, float width, float height) {
        public static final Glyph EMPTY = new Glyph(null, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public final class GlyphSet {
        private final String css;
        private final float outlineWidth;
        private final Map<Integer, Glyph> glyphs = new HashMap<>();

        private GlyphSet(final String css, final float outlineWidth) {
            this.css = css;
            this.outlineWidth = outlineWidth;
        }

        public Glyph get(final int codePoint) {
            Glyph glyph = this.glyphs.get(codePoint);
            if (glyph == null) {
                glyph = GlyphAtlas.this.rasterize(this.css, this.outlineWidth, codePoint);
                this.glyphs.put(codePoint, glyph);
            }
            return glyph;
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Page {
        private final WebGLTexture texture;
        private int shelfY;
        private int shelfHeight;
        private int cursorX;
        private int allocX;
        private int allocY;

        private boolean allocate(final int width, final int height) {
            if (this.cursorX + width > PAGE_SIZE) {
                this.shelfY += this.shelfHeight + GAP;
                this.shelfHeight = 0;
                this.cursorX = 0;
            }
            if (this.shelfY + height > PAGE_SIZE) return false;
            this.allocX = this.cursorX;
            this.allocY = this.shelfY;
            this.cursorX += width + GAP;
            this.shelfHeight = Math.max(this.shelfHeight, height);
            return true;
        }
    }

}
