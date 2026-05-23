package net.lenni0451.rivet.backend;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.RenderCommand;
import net.lenni0451.rivet.backend.render.RenderList;
import net.lenni0451.rivet.backend.render.TransformCommand;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.text.model.TextOrigin;

import java.util.Stack;

public final class Renderer {

    private final Stack<RenderList> currentRenderList = new Stack<>();

    public Renderer() {
        this.currentRenderList.push(new RenderList());
    }

    public RenderList renderList() {
        return this.currentRenderList.peek();
    }

    public void translate(final float x, final float y, final Runnable renderer) {
        this.transform(new TransformCommand.Translate(x, y), renderer);
    }

    public void componentBounds(final float x, final float y, final float width, final float height, final Runnable renderer) {
        this.transform(new TransformCommand.ComponentBounds(x, y, width, height), renderer);
    }

    public void scissor(final float x, final float y, final float width, final float height, final Runnable renderer) {
        this.transform(new TransformCommand.Scissor(x, y, width, height), renderer);
    }

    public void scale(final float xy, final Runnable renderer) {
        this.scale(xy, xy, renderer);
    }

    public void scale(final float x, final float y, final Runnable renderer) {
        this.transform(new TransformCommand.Scale(x, y), renderer);
    }

    private void transform(final TransformCommand command, final Runnable renderer) {
        this.currentRenderList.push(new RenderList(command));
        renderer.run();
        RenderList subList = this.currentRenderList.pop();
        this.currentRenderList.peek().render(subList);
    }


    public void fillCircle(final float x, final float y, final float radius, final Color color) {
        this.currentRenderList.peek().render(new RenderCommand.FillCircle(x, y, radius, color));
    }

    public void outlineCircle(final float x, final float y, final float radius, final float outlineWidth, final Color color) {
        this.currentRenderList.peek().render(new RenderCommand.OutlineCircle(x, y, radius, outlineWidth, color));
    }

    public void fillTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3, final Color color) {
        this.currentRenderList.peek().render(new RenderCommand.FillTriangle(x1, y1, x2, y2, x3, y3, color));
    }

    public void fillRect(final float x, final float y, final float width, final float height, final Color color) {
        this.currentRenderList.peek().render(new RenderCommand.FillRect(x, y, width, height, color));
    }

    public void outlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color) {
        this.currentRenderList.peek().render(new RenderCommand.OutlineRect(x, y, width, height, outlineWidth, color));
    }

    public void fillRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final Color color) {
        this.currentRenderList.peek().render(new RenderCommand.FillRoundedRect(x, y, width, height, cornerRadius, color));
    }

    public void outlineRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final float outlineWidth, final Color color) {
        this.currentRenderList.peek().render(new RenderCommand.OutlineRoundedRect(x, y, width, height, cornerRadius, outlineWidth, color));
    }

    public void fillOptimizedRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final Color color) {
        float maxRadius = Math.min(width, height) / 2F;
        float radius = Math.min(cornerRadius, maxRadius);
        if (radius <= 0) {
            this.fillRect(x, y, width, height, color);
        } else if (width == height && radius == maxRadius) {
            this.fillCircle(x + radius, y + radius, radius, color);
        } else {
            this.fillRoundedRect(x, y, width, height, radius, color);
        }
    }

    public void outlineOptimizedRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final float outlineWidth, final Color color) {
        float maxRadius = Math.min(width, height) / 2F;
        float radius = Math.min(cornerRadius, maxRadius);
        if (radius <= 0) {
            this.outlineRect(x, y, width, height, outlineWidth, color);
        } else if (width == height && radius == maxRadius) {
            this.outlineCircle(x + radius, y + radius, radius, outlineWidth, color);
        } else {
            this.outlineRoundedRect(x, y, width, height, radius, outlineWidth, color);
        }
    }

    public void renderText(final ShapedText shapedText, final float x, final float y, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin) {
        float tx = x + shapedText.offset(horizontalOrigin);
        float ty = y + shapedText.offset(verticalOrigin);
        this.currentRenderList.peek().render(new RenderCommand.Text(shapedText, tx, ty));
    }

    public void line(final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        this.currentRenderList.peek().render(new RenderCommand.Line(x1, y1, x2, y2, width, color));
    }

    public void fillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr) {
        this.currentRenderList.peek().render(new RenderCommand.FillGradientRect(x, y, width, height, ctl, cbl, cbr, ctr));
    }

}
