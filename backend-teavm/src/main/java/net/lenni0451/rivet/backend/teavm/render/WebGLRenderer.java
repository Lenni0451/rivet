package net.lenni0451.rivet.backend.teavm.render;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.render.batched.BatchedRenderListBuilder;
import net.lenni0451.rivet.backend.render.batched.StencilMode;
import net.lenni0451.rivet.backend.render.deferred.RenderList;
import net.lenni0451.rivet.backend.teavm.text.GlyphAtlas;
import net.lenni0451.rivet.backend.teavm.utils.JsUtils;
import net.lenni0451.rivet.math.Aabb;
import org.teavm.jso.typedarrays.Float32Array;
import org.teavm.jso.typedarrays.Int32Array;
import org.teavm.jso.typedarrays.Uint8Array;
import org.teavm.jso.webgl.*;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Renders {@link RenderList}s using WebGL 2.<br>
 * A frame is flattened into non-overlapping layers (see {@link BatchedRenderListBuilder}), all primitives are written into a single
 * instance buffer and every layer is drawn with as few instanced draw calls as possible.
 */
@Accessors(fluent = true, chain = true, makeFinal = true)
public class WebGLRenderer {

    private static final int FLOAT_STRIDE = InstanceBuffer.FLOATS_PER_INSTANCE * 4;
    private static final int COLOR_STRIDE = InstanceBuffer.COLORS_PER_INSTANCE * 4;

    private final WebGL2RenderingContext gl;
    @Getter
    private final GlyphAtlas glyphAtlas;
    private final InstanceBuffer instances = new InstanceBuffer();
    private final FrameBuilder frameBuilder;
    private final WebGLProgram program;
    private final WebGLUniformLocation viewportUniform;
    private final WebGLUniformLocation maskUniform;
    private final WebGLVertexArrayObject vao;
    private final WebGLBuffer floatBuffer;
    private final WebGLBuffer colorBuffer;
    private final WebGLTexture whiteTexture;
    private final Deque<StencilMode> stencilStack = new ArrayDeque<>();
    private int floatBufferCapacity;
    private int colorBufferCapacity;
    @Getter
    private int lastDrawCalls;
    @Getter
    private int lastInstanceCount;

    public WebGLRenderer(final WebGL2RenderingContext gl) {
        this.gl = gl;
        this.glyphAtlas = new GlyphAtlas(gl);
        this.frameBuilder = new FrameBuilder(this.instances, this.glyphAtlas);
        this.program = this.createProgram(Shaders.VERTEX, Shaders.FRAGMENT);
        this.viewportUniform = gl.getUniformLocation(this.program, "u_viewport");
        this.maskUniform = gl.getUniformLocation(this.program, "u_mask");
        gl.useProgram(this.program);
        gl.uniform1i(gl.getUniformLocation(this.program, "u_texture"), 0);

        this.vao = gl.createVertexArray();
        gl.bindVertexArray(this.vao);
        for (int i = 0; i <= 8; i++) {
            gl.enableVertexAttribArray(i);
            gl.vertexAttribDivisor(i, 1);
        }
        gl.bindVertexArray(null);
        this.floatBuffer = gl.createBuffer();
        this.colorBuffer = gl.createBuffer();

        this.whiteTexture = gl.createTexture();
        gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, this.whiteTexture);
        Uint8Array white = new Uint8Array(4);
        for (int i = 0; i < 4; i++) white.set(i, (short) 255);
        gl.texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, 1, 1, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, white);
    }

    public void render(final RenderList renderList, final int width, final int height, final Color backgroundColor) {
        this.glyphAtlas.beginFrame();
        this.frameBuilder.build(BatchedRenderListBuilder.build(renderList));

        this.gl.viewport(0, 0, width, height);
        this.gl.disable(WebGLRenderingContext.SCISSOR_TEST);
        this.gl.disable(WebGLRenderingContext.STENCIL_TEST);
        this.gl.colorMask(true, true, true, true);
        this.gl.clearColor(backgroundColor.getRedF(), backgroundColor.getGreenF(), backgroundColor.getBlueF(), 1F);
        this.gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT);

        this.lastInstanceCount = this.instances.count();
        this.lastDrawCalls = 0;
        if (this.instances.count() == 0) return;
        this.upload();

        this.gl.useProgram(this.program);
        this.gl.bindVertexArray(this.vao);
        this.gl.uniform2f(this.viewportUniform, width, height);
        this.gl.uniform1i(this.maskUniform, 0);
        this.gl.enable(WebGLRenderingContext.BLEND);
        this.gl.blendFunc(WebGLRenderingContext.ONE, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
        this.gl.activeTexture(WebGLRenderingContext.TEXTURE0);

        Aabb currentScissor = null;
        WebGLTexture currentTexture = null;
        boolean textureBound = false;
        this.stencilStack.clear();
        for (FrameBuilder.Op op : this.frameBuilder.ops()) {
            if (op instanceof FrameBuilder.Op.Draw draw) {
                WebGLTexture texture = draw.texture() == null ? this.whiteTexture : draw.texture();
                if (!textureBound || texture != currentTexture) {
                    this.gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, texture);
                    currentTexture = texture;
                    textureBound = true;
                }
                this.draw(draw.first(), draw.count());
            } else if (op instanceof FrameBuilder.Op.Scissor scissor) {
                if (!scissor.bounds().equals(currentScissor)) {
                    currentScissor = scissor.bounds();
                    this.applyScissor(currentScissor, width, height);
                }
            } else if (op instanceof FrameBuilder.Op.StencilPush push) {
                if (this.stencilStack.isEmpty()) {
                    // The stencil clear is affected by the scissor test
                    this.gl.disable(WebGLRenderingContext.SCISSOR_TEST);
                    currentScissor = null;
                    this.gl.clear(WebGLRenderingContext.STENCIL_BUFFER_BIT);
                    this.gl.enable(WebGLRenderingContext.STENCIL_TEST);
                }
                this.stencilStack.push(push.mode());
                this.gl.colorMask(false, false, false, false);
                this.gl.uniform1i(this.maskUniform, 1);
                this.beginStencil(push.mode(), this.stencilStack.size());
            } else if (op == FrameBuilder.Op.StencilSet.INSTANCE) {
                this.setStencil();
            } else if (op == FrameBuilder.Op.StencilPop.INSTANCE) {
                this.stencilStack.pop();
                if (this.stencilStack.isEmpty()) {
                    this.gl.disable(WebGLRenderingContext.STENCIL_TEST);
                    this.gl.colorMask(true, true, true, true);
                    this.gl.uniform1i(this.maskUniform, 0);
                } else {
                    this.setStencil();
                }
            }
        }
        this.gl.bindVertexArray(null);
        this.gl.disable(WebGLRenderingContext.SCISSOR_TEST);
        this.gl.disable(WebGLRenderingContext.STENCIL_TEST);
        this.gl.colorMask(true, true, true, true);
    }

    public void close() {
        this.glyphAtlas.clear();
        this.gl.deleteTexture(this.whiteTexture);
        this.gl.deleteBuffer(this.floatBuffer);
        this.gl.deleteBuffer(this.colorBuffer);
        this.gl.deleteVertexArray(this.vao);
        this.gl.deleteProgram(this.program);
    }

    private void upload() {
        int count = this.instances.count();
        int floatBytes = count * FLOAT_STRIDE;
        int colorBytes = count * COLOR_STRIDE;

        this.gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, this.floatBuffer);
        if (floatBytes > this.floatBufferCapacity) {
            this.floatBufferCapacity = Math.max(floatBytes, this.floatBufferCapacity * 2);
            this.gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, this.floatBufferCapacity, WebGLRenderingContext.DYNAMIC_DRAW);
        }
        JsUtils.bufferSubData(this.gl, WebGLRenderingContext.ARRAY_BUFFER, Float32Array.fromJavaArray(this.instances.floats()), count * InstanceBuffer.FLOATS_PER_INSTANCE);

        this.gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, this.colorBuffer);
        if (colorBytes > this.colorBufferCapacity) {
            this.colorBufferCapacity = Math.max(colorBytes, this.colorBufferCapacity * 2);
            this.gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, this.colorBufferCapacity, WebGLRenderingContext.DYNAMIC_DRAW);
        }
        JsUtils.bufferSubData(this.gl, WebGLRenderingContext.ARRAY_BUFFER, Int32Array.fromJavaArray(this.instances.colors()), count * InstanceBuffer.COLORS_PER_INSTANCE);
    }

    private void draw(final int first, final int count) {
        // WebGL 2 has no base instance, so the attribute pointers are moved to the first instance instead
        int floatOffset = first * FLOAT_STRIDE;
        this.gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, this.floatBuffer);
        for (int i = 0; i < 5; i++) {
            this.gl.vertexAttribPointer(i, 4, WebGLRenderingContext.FLOAT, false, FLOAT_STRIDE, floatOffset + i * 16);
        }
        int colorOffset = first * COLOR_STRIDE;
        this.gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, this.colorBuffer);
        for (int i = 0; i < 4; i++) {
            this.gl.vertexAttribPointer(5 + i, 4, WebGLRenderingContext.UNSIGNED_BYTE, true, COLOR_STRIDE, colorOffset + i * 4);
        }
        this.gl.drawArraysInstanced(WebGLRenderingContext.TRIANGLE_STRIP, 0, 4, count);
        this.lastDrawCalls++;
    }

    private void applyScissor(final Aabb bounds, final int width, final int height) {
        if (bounds.isFull()) {
            this.gl.disable(WebGLRenderingContext.SCISSOR_TEST);
        } else {
            int minX = MathUtils.clamp((int) Math.floor(bounds.minX()), 0, width);
            int minY = MathUtils.clamp((int) Math.floor(bounds.minY()), 0, height);
            int maxX = MathUtils.clamp((int) Math.ceil(bounds.maxX()), 0, width);
            int maxY = MathUtils.clamp((int) Math.ceil(bounds.maxY()), 0, height);
            this.gl.enable(WebGLRenderingContext.SCISSOR_TEST);
            this.gl.scissor(minX, height - Math.max(minY, maxY), Math.max(0, maxX - minX), Math.max(0, maxY - minY));
        }
    }

    private void beginStencil(final StencilMode mode, final int depth) {
        if (mode == StencilMode.EQUAL_INTERSECTION) {
            this.gl.stencilFunc(WebGLRenderingContext.EQUAL, depth - 1, 0xFF);
            this.gl.stencilOp(WebGLRenderingContext.KEEP, WebGLRenderingContext.KEEP, WebGLRenderingContext.INCR);
        } else {
            this.gl.stencilFunc(WebGLRenderingContext.ALWAYS, 1, 0xFF);
            this.gl.stencilOp(WebGLRenderingContext.REPLACE, WebGLRenderingContext.REPLACE, WebGLRenderingContext.REPLACE);
        }
    }

    private void setStencil() {
        this.gl.colorMask(true, true, true, true);
        this.gl.uniform1i(this.maskUniform, 0);
        int depth = this.stencilStack.size();
        if (this.stencilStack.peek() == StencilMode.EQUAL_INTERSECTION) {
            this.gl.stencilFunc(WebGLRenderingContext.LEQUAL, depth, 0xFF);
        } else {
            this.gl.stencilFunc(WebGLRenderingContext.EQUAL, 0, 0xFF);
        }
        this.gl.stencilOp(WebGLRenderingContext.KEEP, WebGLRenderingContext.KEEP, WebGLRenderingContext.KEEP);
    }

    private WebGLProgram createProgram(final String vertexSource, final String fragmentSource) {
        WebGLShader vertexShader = this.compileShader(WebGLRenderingContext.VERTEX_SHADER, vertexSource);
        WebGLShader fragmentShader = this.compileShader(WebGLRenderingContext.FRAGMENT_SHADER, fragmentSource);
        WebGLProgram program = this.gl.createProgram();
        this.gl.attachShader(program, vertexShader);
        this.gl.attachShader(program, fragmentShader);
        this.gl.linkProgram(program);
        if (!this.gl.getProgramParameterb(program, WebGLRenderingContext.LINK_STATUS)) {
            throw new IllegalStateException("Failed to link shader program: " + this.gl.getProgramInfoLog(program));
        }
        this.gl.deleteShader(vertexShader);
        this.gl.deleteShader(fragmentShader);
        return program;
    }

    private WebGLShader compileShader(final int type, final String source) {
        WebGLShader shader = this.gl.createShader(type);
        this.gl.shaderSource(shader, source);
        this.gl.compileShader(shader);
        if (!this.gl.getShaderParameterb(shader, WebGLRenderingContext.COMPILE_STATUS)) {
            throw new IllegalStateException("Failed to compile shader: " + this.gl.getShaderInfoLog(shader));
        }
        return shader;
    }

}
