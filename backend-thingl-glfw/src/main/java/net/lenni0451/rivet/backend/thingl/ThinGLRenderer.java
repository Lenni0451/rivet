package net.lenni0451.rivet.backend.thingl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Renderer;
import net.raphimc.thingl.ThinGL;
import org.joml.Matrix4fStack;

public class ThinGLRenderer implements Renderer {

    private final Matrix4fStack matrixStack;

    public ThinGLRenderer(final Matrix4fStack matrixStack) {
        this.matrixStack = matrixStack;
    }

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

}
