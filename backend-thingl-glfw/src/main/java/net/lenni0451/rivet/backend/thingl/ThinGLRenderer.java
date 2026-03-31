package net.lenni0451.rivet.backend.thingl;

import lombok.RequiredArgsConstructor;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.text.TextOrigin;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.renderer.impl.Renderer2D;
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
    public void translate(final float x, final float y) {
        this.matrixStack.translate(x, y, 0);
    }

    @Override
    public void pushScissor(final float x, final float y, final float width, final float height) {
        ThinGL.scissorStack().pushIntersection(this.matrixStack, x, y, x + width, y + height);
    }

    @Override
    public void popScissor() {
        ThinGL.scissorStack().pop();
    }

    @Override
    public void scale(final float x, final float y) {
        this.matrixStack.scale(x, y, 1);
    }

    @Override
    public void pop() {
        this.matrixStack.popMatrix();
    }

    @Override
    public void fillRect(final float x, final float y, final float width, final float height, final Color color) {
        ThinGL.renderer2D().filledRectangle(this.matrixStack, x, y, x + width, y + height, color);
    }

    @Override
    public void outlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color) {
        ThinGL.renderer2D().outlinedRectangle(this.matrixStack, x, y, x + width, y + height, color, outlineWidth, Renderer2D.OUTLINE_STYLE_INNER_BIT);
    }

    @Override
    public void fillRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final Color color) {
        ThinGL.renderer2D().filledRoundedRectangle(this.matrixStack, x, y, x + width, y + height, cornerRadius, color);
    }

    @Override
    public void outlineRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final float outlineWidth, final Color color) {
        ThinGL.renderer2D().outlinedRoundedRectangle(this.matrixStack, x, y, x + width, y + height, cornerRadius, color, outlineWidth, Renderer2D.OUTLINE_STYLE_INNER_BIT);
    }

    @Override
    public void renderText(final ShapedText shapedText, final float x, final float y, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin) {
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
