package net.lenni0451.rivet.backend.teavm.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.batched.BatchedRenderListBuilder;
import net.lenni0451.rivet.backend.render.batched.StencilMode;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.backend.teavm.text.FontStyle;
import net.lenni0451.rivet.backend.teavm.text.GlyphAtlas;
import net.lenni0451.rivet.backend.teavm.text.TeaVMFont;
import net.lenni0451.rivet.backend.teavm.text.TeaVMShapedText;
import net.lenni0451.rivet.backend.teavm.texture.TeaVMTexture;
import net.lenni0451.rivet.backend.text.TextDecorations;
import net.lenni0451.rivet.math.Aabb;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.math.Transform2D;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextOrigin;
import org.maplibre.earcut4j.Earcut;
import org.teavm.jso.webgl.WebGLTexture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts the layers of a frame into instances and a list of GPU operations.<br>
 * All instances of a frame end up in a single buffer, so the whole frame needs only one upload.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Accessors(fluent = true, chain = true, makeFinal = true)
final class FrameBuilder {

    private static final int MAX_RASTER_SIZE = 512;

    private final InstanceBuffer instances;
    private final GlyphAtlas atlas;
    @Getter(AccessLevel.PACKAGE)
    private final List<Op> ops = new ArrayList<>();
    private final List<BatchedRenderListBuilder.CommandState> sortedCommands = new ArrayList<>();
    private final Map<TeaVMTexture, List<BatchedRenderListBuilder.CommandState>> imageCommands = new LinkedHashMap<>();
    private final SectionPainter sectionPainter = new SectionPainter();
    private int drawStart;
    private WebGLTexture drawTexture;
    private boolean drawTextureSet;

    void build(final BatchedRenderListBuilder.Layers layers) {
        this.instances.reset();
        this.ops.clear();
        this.drawStart = 0;
        this.drawTexture = null;
        this.drawTextureSet = false;
        this.emitLayers(layers);
    }

    private void emitLayers(final BatchedRenderListBuilder.Layers layers) {
        for (BatchedRenderListBuilder.Layer layer : layers) {
            StencilMode stencilMode = layer.stencilMode();
            if (stencilMode != null) {
                this.ops.add(new Op.StencilPush(stencilMode));
                this.emitLayers(layer.stencilMaskLayers());
                this.ops.add(Op.StencilSet.INSTANCE);
            }
            this.ops.add(new Op.Scissor(layer.scissor()));
            for (BatchedRenderListBuilder.CommandState command : this.sortByTexture(layer.commands())) {
                this.emitCommand(command.matrix(), command.command());
            }
            this.flushDraw();
            if (stencilMode != null) {
                this.ops.add(Op.StencilPop.INSTANCE);
            }
        }
    }

    /**
     * Commands inside a layer never overlap, so they can be reordered freely.<br>
     * Images are grouped by texture to minimize the amount of draw calls.
     */
    private List<BatchedRenderListBuilder.CommandState> sortByTexture(final List<BatchedRenderListBuilder.CommandState> commands) {
        this.sortedCommands.clear();
        this.imageCommands.clear();
        for (BatchedRenderListBuilder.CommandState command : commands) {
            if (command.command() instanceof RenderCommand.Image image && image.texture() instanceof TeaVMTexture texture) {
                this.imageCommands.computeIfAbsent(texture.root(), t -> new ArrayList<>()).add(command);
            } else {
                this.sortedCommands.add(command);
            }
        }
        if (this.imageCommands.isEmpty()) return commands;
        for (List<BatchedRenderListBuilder.CommandState> images : this.imageCommands.values()) {
            this.sortedCommands.addAll(images);
        }
        return this.sortedCommands;
    }

    private void useTexture(final WebGLTexture texture) {
        if (!this.drawTextureSet) {
            this.drawTexture = texture;
            this.drawTextureSet = true;
        } else if (this.drawTexture != texture) {
            this.flushDraw();
            this.drawTexture = texture;
            this.drawTextureSet = true;
        }
    }

    private void flushDraw() {
        int count = this.instances.count() - this.drawStart;
        if (count > 0) {
            this.ops.add(new Op.Draw(this.drawStart, count, this.drawTexture));
        }
        this.drawStart = this.instances.count();
        this.drawTexture = null;
        this.drawTextureSet = false;
    }

    private void emitCommand(final Transform2D m, final RenderCommand command) {
        InstanceBuffer b = this.instances;
        if (command instanceof RenderCommand.FillRect c) {
            int color = InstanceBuffer.pack(c.color());
            b.rect(m, c.x(), c.y(), c.width(), c.height(), color, color, color, color);
        } else if (command instanceof RenderCommand.OutlineRect c) {
            int color = InstanceBuffer.pack(c.color());
            b.shape(m, c.x(), c.y(), c.width(), c.height(), 0, 0, 0, 0, c.outlineWidth(), color, color, color, color);
        } else if (command instanceof RenderCommand.FillRoundedRect c) {
            int color = InstanceBuffer.pack(c.color());
            b.shape(m, c.x(), c.y(), c.width(), c.height(), c.rtl(), c.rbl(), c.rbr(), c.rtr(), 0, color, color, color, color);
        } else if (command instanceof RenderCommand.OutlineRoundedRect c) {
            int color = InstanceBuffer.pack(c.color());
            b.shape(m, c.x(), c.y(), c.width(), c.height(), c.rtl(), c.rbl(), c.rbr(), c.rtr(), c.outlineWidth(), color, color, color, color);
        } else if (command instanceof RenderCommand.FillCircle c) {
            int color = InstanceBuffer.pack(c.color());
            float r = c.radius();
            b.shape(m, c.x() - r, c.y() - r, r * 2, r * 2, r, r, r, r, 0, color, color, color, color);
        } else if (command instanceof RenderCommand.OutlineCircle c) {
            int color = InstanceBuffer.pack(c.color());
            float r = c.radius();
            b.shape(m, c.x() - r, c.y() - r, r * 2, r * 2, r, r, r, r, c.outlineWidth(), color, color, color, color);
        } else if (command instanceof RenderCommand.FillGradientRect c) {
            b.rect(
                    m, c.x(), c.y(), c.width(), c.height(),
                    InstanceBuffer.pack(c.ctl()), InstanceBuffer.pack(c.cbl()), InstanceBuffer.pack(c.cbr()), InstanceBuffer.pack(c.ctr())
            );
        } else if (command instanceof RenderCommand.FillTriangle c) {
            b.triangle(m, c.x1(), c.y1(), c.x2(), c.y2(), c.x3(), c.y3(), true, InstanceBuffer.pack(c.color()));
        } else if (command instanceof RenderCommand.FillPolygon c) {
            Point[] points = c.points();
            int color = InstanceBuffer.pack(c.color());
            double[] coords = new double[points.length * 2];
            for (int i = 0; i < points.length; i++) {
                coords[i * 2] = points[i].x();
                coords[i * 2 + 1] = points[i].y();
            }
            List<Integer> indices = Earcut.earcut(coords, null, 2);
            for (int i = 0; i + 2 < indices.size(); i += 3) {
                Point p1 = points[indices.get(i)];
                Point p2 = points[indices.get(i + 1)];
                Point p3 = points[indices.get(i + 2)];
                b.triangle(m, p1.x(), p1.y(), p2.x(), p2.y(), p3.x(), p3.y(), false, color);
            }
        } else if (command instanceof RenderCommand.Line c) {
            this.emitLine(m, c.x1(), c.y1(), c.x2(), c.y2(), c.width(), InstanceBuffer.pack(c.color()));
        } else if (command instanceof RenderCommand.PolyLine c) {
            this.emitPolyLine(m, c.points(), c.width(), InstanceBuffer.pack(c.color()));
        } else if (command instanceof RenderCommand.Image c) {
            this.emitImage(m, c);
        } else if (command instanceof RenderCommand.Text c) {
            this.emitText(m, c);
        } else {
            throw new UnsupportedOperationException("Unsupported render command: " + command.getClass().getName());
        }
    }

    private void emitLine(final Transform2D m, final float x1, final float y1, final float x2, final float y2, final float width, final int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0 || width <= 0) return;
        this.instances.rotatedRect(m, x1, y1, dx / length, dy / length, length, width, color);
    }

    private void emitPolyLine(final Transform2D m, final Point[] points, final float width, final int color) {
        if (width <= 0) return;
        for (int i = 0; i < points.length - 1; i++) {
            this.emitLine(m, points[i].x(), points[i].y(), points[i + 1].x(), points[i + 1].y(), width, color);
        }
        // Round joins to close the gaps between the segments
        float radius = width / 2F;
        for (int i = 1; i < points.length - 1; i++) {
            Point point = points[i];
            this.instances.shape(m, point.x() - radius, point.y() - radius, width, width, radius, radius, radius, radius, 0, color, color, color, color);
        }
    }

    private void emitImage(final Transform2D m, final RenderCommand.Image image) {
        if (!(image.texture() instanceof TeaVMTexture texture)) {
            throw new UnsupportedOperationException("Unsupported texture type: " + image.texture().getClass().getName());
        }
        this.useTexture(texture.glTexture());
        this.instances.texture(m, image.x(), image.y(), image.width(), image.height(), texture.u0(), texture.v0(), texture.u1(), texture.v1(), InstanceBuffer.pack(image.color()));
    }

    private void emitText(final Transform2D m, final RenderCommand.Text text) {
        if (!(text.shapedText() instanceof TeaVMShapedText shapedText)) {
            throw new UnsupportedOperationException("Unsupported shaped text type: " + text.shapedText().getClass().getName());
        }
        float scale = m.averageScale();
        if (scale <= 0) return;
        float originX = shapedText.alignAnchorTo(text.x(), text.horizontalOrigin(), TextOrigin.Horizontal.LOGICAL_LEFT);
        float originY = shapedText.alignAnchorTo(text.y(), text.verticalOrigin(), TextOrigin.Vertical.BASELINE);

        for (TeaVMShapedText.Section section : shapedText.sections()) {
            if (section.text().isEmpty()) continue;
            FontStyle style = section.style();
            TextFormat format = section.format();
            int deviceSize = Math.round(style.size() * scale);
            int rasterSize = Math.max(1, Math.min(MAX_RASTER_SIZE, deviceSize));
            SectionPainter painter = this.sectionPainter;
            painter.matrix = m;
            painter.section = section;
            // Glyphs are drawn 1:1, unless the raster size had to be limited. Then they have to be scaled to their actual size
            painter.scale = rasterSize == deviceSize ? scale : (float) rasterSize / style.size();
            painter.glyphs = section.glyphs(this.atlas, rasterSize, false);
            painter.outlineGlyphs = TextDecorations.hasOutline(format) ? section.glyphs(this.atlas, rasterSize, true) : null;
            TextDecorations.render(format, style.size(), originX + section.x(), originY, painter);
        }
    }

    private void emitGlyphs(final Transform2D m, final TeaVMShapedText.Section section, final GlyphAtlas.Glyph[] glyphs, final float x, final float y, final float scale, final Color color) {
        if (color.getAlpha() <= 0) return;
        int packedColor = InstanceBuffer.pack(color);
        float[] positions = section.positions();
        boolean axisAligned = m.isAxisAligned() && m.m00() != 0 && m.m11() != 0;
        float baseline = y;
        if (axisAligned) {
            float deviceY = m.m11() * baseline + m.m12();
            baseline += (Math.round(deviceY) - deviceY) / m.m11();
        }
        for (int i = 0; i < glyphs.length; i++) {
            GlyphAtlas.Glyph glyph = glyphs[i];
            if (glyph == null || glyph.width() <= 0) continue;
            float penX = x + positions[i];
            if (axisAligned) {
                // Snap glyphs to the device pixel grid to keep them crisp
                float deviceX = m.m00() * penX + m.m02();
                penX += (Math.round(deviceX) - deviceX) / m.m00();
            }
            this.useTexture(glyph.texture());
            this.instances.texture(
                    m,
                    penX + glyph.left() / scale, baseline + glyph.top() / scale,
                    glyph.width() / scale, glyph.height() / scale,
                    glyph.u0(), glyph.v0(), glyph.u1(), glyph.v1(),
                    packedColor
            );
        }
    }

    private void emitTextLines(final Transform2D m, final TeaVMShapedText.Section section, final float x, final float y, final Color color) {
        TextFormat format = section.format();
        if (section.advance() <= 0 || color.getAlpha() <= 0) return;
        int size = section.style().size();
        float thickness = TeaVMFont.lineThickness(size, format);
        int packedColor = InstanceBuffer.pack(color);
        if (format.underlined()) {
            float lineY = y + TeaVMFont.UNDERLINE_OFFSET_FACTOR * size;
            this.instances.shape(m, x, lineY - thickness / 2, section.advance(), thickness, 0, 0, 0, 0, 0, packedColor, packedColor, packedColor, packedColor);
        }
        if (format.strikethrough()) {
            float lineY = y + TeaVMFont.STRIKETHROUGH_OFFSET_FACTOR * size;
            this.instances.shape(m, x, lineY - thickness / 2, section.advance(), thickness, 0, 0, 0, 0, 0, packedColor, packedColor, packedColor, packedColor);
        }
    }


    private final class SectionPainter implements TextDecorations.Painter {
        private Transform2D matrix;
        private TeaVMShapedText.Section section;
        private float scale;
        private GlyphAtlas.Glyph[] glyphs;
        private GlyphAtlas.Glyph[] outlineGlyphs;

        @Override
        public void outline(final float x, final float y, final Color color) {
            FrameBuilder.this.emitGlyphs(this.matrix, this.section, this.outlineGlyphs, x, y, this.scale, color);
        }

        @Override
        public void glyphs(final float x, final float y, final Color color) {
            FrameBuilder.this.emitGlyphs(this.matrix, this.section, this.glyphs, x, y, this.scale, color);
        }

        @Override
        public void lines(final float x, final float y, final Color color) {
            FrameBuilder.this.emitTextLines(this.matrix, this.section, x, y, color);
        }
    }

    sealed interface Op {
        record Scissor(Aabb bounds) implements Op {
        }

        record StencilPush(StencilMode mode) implements Op {
        }

        enum StencilSet implements Op {
            INSTANCE
        }

        enum StencilPop implements Op {
            INSTANCE
        }

        record Draw(int first, int count, WebGLTexture texture) implements Op {
        }
    }

}
