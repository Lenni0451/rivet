package net.lenni0451.rivet.backend.thingl;

import lombok.RequiredArgsConstructor;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.ShapedText;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.renderer.impl.RendererText;
import org.joml.Matrix4fStack;

@RequiredArgsConstructor
public class ThinGLRenderer implements Renderer {

    private final Matrix4fStack matrixStack;

    @Override
    public void push() {
        this.matrixStack.pushMatrix();
    }

    @Override
    public void translate(float x, float y) {
        this.matrixStack.translate(x, y, 0);
    }

    @Override
    public void pushScissor(float x, float y, float width, float height) {
        ThinGL.scissorStack().pushIntersection(this.matrixStack, x, y, x + width, y + height);
    }

    @Override
    public void popScissor() {
        ThinGL.scissorStack().pop();
    }

    @Override
    public void scale(float x, float y) {
        this.matrixStack.scale(x, y, 1);
    }

    @Override
    public void pop() {
        this.matrixStack.popMatrix();
    }

    @Override
    public void fillRect(float x, float y, float width, float height, Color color) {
        ThinGL.renderer2D().filledRectangle(this.matrixStack, x, y, x + width, y + height, color);
    }

    @Override
    public void renderText(ShapedText shapedText, float x, float y, HorizontalOrigin horizontalOrigin, VerticalOrigin verticalOrigin) {
        RendererText.VerticalOrigin vOrigin = switch (verticalOrigin) {
            case BASELINE -> RendererText.VerticalOrigin.BASELINE;
            case LOGICAL_TOP -> RendererText.VerticalOrigin.LOGICAL_TOP;
            case LOGICAL_CENTER -> RendererText.VerticalOrigin.LOGICAL_CENTER;
            case LOGICAL_BOTTOM -> RendererText.VerticalOrigin.LOGICAL_BOTTOM;
            case VISUAL_TOP -> RendererText.VerticalOrigin.VISUAL_TOP;
            case VISUAL_CENTER -> RendererText.VerticalOrigin.VISUAL_CENTER;
            case VISUAL_BOTTOM -> RendererText.VerticalOrigin.VISUAL_BOTTOM;
        };
        RendererText.HorizontalOrigin hOrigin = switch (horizontalOrigin) {
            case LOGICAL_LEFT -> RendererText.HorizontalOrigin.LOGICAL_LEFT;
            case VISUAL_LEFT -> RendererText.HorizontalOrigin.VISUAL_LEFT;
            case VISUAL_CENTER -> RendererText.HorizontalOrigin.VISUAL_CENTER;
            case VISUAL_RIGHT -> RendererText.HorizontalOrigin.VISUAL_RIGHT;
        };
        ThinGL.rendererText().textLine(this.matrixStack, ((ThinGLShapedText) shapedText).shapedTextLine(), x, y, vOrigin, hOrigin);
    }

}
