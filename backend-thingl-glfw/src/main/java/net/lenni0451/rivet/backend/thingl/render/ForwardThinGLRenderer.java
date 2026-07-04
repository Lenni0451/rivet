package net.lenni0451.rivet.backend.thingl.render;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.render.CheckedRenderer;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.backend.thingl.ThinGLTexture;
import net.lenni0451.rivet.backend.thingl.text.ThinGLShapedText;
import net.lenni0451.rivet.backend.thingl.text.ThinGLShapedTextBlock;
import net.lenni0451.rivet.backend.thingl.util.MathUtil;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.renderer.impl.Renderer2D;
import net.raphimc.thingl.gl.renderer.impl.RendererText;
import net.raphimc.thingl.gl.wrapper.StencilStack;
import org.joml.Matrix4fStack;

import java.util.function.Consumer;

@Getter
@Accessors(fluent = true, chain = true)
public class ForwardThinGLRenderer extends CheckedRenderer {

    @Setter
    private Matrix4fStack positionMatrix;

    public ForwardThinGLRenderer() {
        this.positionMatrix = new Matrix4fStack(32);
    }

    public ForwardThinGLRenderer(final Matrix4fStack positionMatrix) {
        this.positionMatrix = positionMatrix;
    }

    @Override
    public float xOffset() {
        return this.positionMatrix.m30();
    }

    @Override
    public float yOffset() {
        return this.positionMatrix.m31();
    }

    @Override
    public void doTranslate(final float x, final float y, final Runnable renderer) {
        this.positionMatrix.pushMatrix();
        this.positionMatrix.translate(x, y, 0);
        renderer.run();
        this.positionMatrix.popMatrix();
    }

    @Override
    public void doComponentBounds(final float x, final float y, final float width, final float height, final Runnable renderer) {
        ThinGL.scissorStack().pushIntersection(this.positionMatrix, MathUtils.floorInt(x), MathUtils.floorInt(y), MathUtils.ceilInt(x + width), MathUtils.ceilInt(y + height));
        renderer.run();
        ThinGL.scissorStack().pop();
    }

    @Override
    public void doScissor(final float x, final float y, final float width, final float height, final Runnable renderer) {
        ThinGL.scissorStack().pushIntersection(this.positionMatrix, MathUtils.floorInt(x), MathUtils.floorInt(y), MathUtils.ceilInt(x + width), MathUtils.ceilInt(y + height));
        renderer.run();
        ThinGL.scissorStack().pop();
    }

    @Override
    public void doScale(final float x, final float y, final Runnable renderer) {
        this.positionMatrix.pushMatrix();
        this.positionMatrix.scaleXY(x, y);
        renderer.run();
        this.positionMatrix.popMatrix();
    }

    @Override
    public void doStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        ThinGL.stencilStack().push(StencilStack.Mode.EQUAL_INTERSECTION);
        maskRenderer.accept(this);
        ThinGL.stencilStack().set();
        renderer.run();
        ThinGL.stencilStack().pop();
    }

    @Override
    public void doInverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        ThinGL.stencilStack().push(StencilStack.Mode.NOT_EQUAL);
        maskRenderer.accept(this);
        ThinGL.stencilStack().set();
        renderer.run();
        ThinGL.stencilStack().pop();
    }

    @Override
    public void custom(final ModifierCommand.Custom command, final Runnable renderer) {
        if (command instanceof ThinGLModifierCommand thinGLModifierCommand) {
            switch (thinGLModifierCommand) {
                case ThinGLModifierCommand.Blur blur -> {
                    ThinGL.programs().getGaussianBlur().bindInput();
                    renderer.run();
                    ThinGL.programs().getGaussianBlur().unbindInput();
                    ThinGL.programs().getGaussianBlur().configureParameters(blur.strength());
                    ThinGL.programs().getGaussianBlur().renderFullscreen();
                    ThinGL.programs().getGaussianBlur().clearInput();
                }
            }
        }
    }


    @Override
    public void doFillCircle(final float x, final float y, final float radius, final Color color) {
        ThinGL.renderer2D().filledCircle(this.positionMatrix, x, y, radius, color);
    }

    @Override
    public void doOutlineCircle(final float x, final float y, final float radius, final float outlineWidth, final Color color) {
        ThinGL.renderer2D().outlinedCircle(this.positionMatrix, x, y, radius, color, outlineWidth, Renderer2D.OUTLINE_STYLE_INNER_BIT);
    }

    @Override
    public void doFillTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3, final Color color) {
        ThinGL.renderer2D().filledTriangle(this.positionMatrix, x1, y1, x2, y2, x3, y3, color);
    }

    @Override
    public void doFillRect(final float x, final float y, final float width, final float height, final Color color) {
        ThinGL.renderer2D().filledRectangle(this.positionMatrix, x, y, x + width, y + height, color);
    }

    @Override
    public void doOutlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color) {
        ThinGL.renderer2D().outlinedRectangle(this.positionMatrix, x, y, x + width, y + height, color, outlineWidth, Renderer2D.OUTLINE_STYLE_INNER_BIT);
    }

    @Override
    public void doFillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color) {
        ThinGL.renderer2D().filledRoundedRectangle(this.positionMatrix, x, y, x + width, y + height, rbl, rbr, rtr, rtl, color);
    }

    @Override
    public void doOutlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color) {
        ThinGL.renderer2D().outlinedRoundedRectangle(this.positionMatrix, x, y, x + width, y + height, rbl, rbr, rtr, rtl, color, outlineWidth, Renderer2D.OUTLINE_STYLE_INNER_BIT);
    }

    @Override
    public void doFillPolygon(final Point[] points, final Color color) {
        ThinGL.renderer2D().filledPolygon(this.positionMatrix, MathUtil.convert(points), color);
    }

    @Override
    public void doLine(final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        ThinGL.renderer2D().line(this.positionMatrix, x1, y1, x2, y2, width, color);
    }

    @Override
    public void doPolyLine(final Point[] points, final float width, final Color color) {
        ThinGL.renderer2D().polyLine(this.positionMatrix, MathUtil.convert(points), width, color);
    }

    @Override
    public void doFillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr) {
        ThinGL.renderer2D().filledRectangle(this.positionMatrix, x, y, x + width, y + height, cbl, cbr, ctr, ctl);
    }

    @Override
    public void doText(final ShapedText shapedText, final float x, final float y, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin) {
        float tx = x + shapedText.offset(horizontalOrigin);
        float ty = y + shapedText.offset(verticalOrigin);
        switch (shapedText) {
            case ThinGLShapedText thinGLShapedText -> ThinGL.rendererText().textLine(
                    this.positionMatrix,
                    thinGLShapedText.shapedTextLine(),
                    tx, ty,
                    RendererText.VerticalOrigin.BASELINE,
                    RendererText.HorizontalOrigin.LOGICAL_LEFT
            );
            case ThinGLShapedTextBlock thinGLShapedTextBlock -> ThinGL.rendererText().textBlock(
                    this.positionMatrix,
                    thinGLShapedTextBlock.shapedTextBlock(),
                    tx, ty,
                    RendererText.VerticalOrigin.BASELINE,
                    RendererText.HorizontalOrigin.LOGICAL_LEFT
            );
            default -> throw new UnsupportedOperationException(shapedText.getClass().getName());
        }
    }

    @Override
    public void doImage(final Texture texture, final float x, final float y, final float width, final float height, final Color color) {
        ThinGLTexture thinGLTexture = (ThinGLTexture) texture;
        if (color.equals(Color.WHITE)) {
            ThinGL.renderer2D().texture(this.positionMatrix, thinGLTexture.texture(), x, y, width, height, thinGLTexture.view().minX, thinGLTexture.view().minY, thinGLTexture.view().lengthX(), thinGLTexture.view().lengthY());
        } else {
            ThinGL.renderer2D().coloredTexture(this.positionMatrix, thinGLTexture.texture(), x, y, width, height, thinGLTexture.view().minX, thinGLTexture.view().minY, thinGLTexture.view().lengthX(), thinGLTexture.view().lengthY(), color);
        }
    }

    @Override
    public void custom(final RenderCommand.Custom renderCommand) {
    }

}
