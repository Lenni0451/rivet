package net.lenni0451.rivet.backend.render.deferred;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.render.CheckedRenderer;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.text.model.TextOrigin;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public final class DeferredRenderer extends CheckedRenderer {

    private final Stack<IncompleteRenderList> currentRenderList = new Stack<>();
    @Getter
    private float xOffset = 0;
    @Getter
    private float yOffset = 0;

    public DeferredRenderer() {
        this.currentRenderList.push(new IncompleteRenderList());
    }

    private void checkClosed() {
        if (this.currentRenderList.isEmpty()) {
            throw new IllegalStateException("Renderer has been closed");
        }
    }

    public RenderList complete() {
        this.checkClosed();
        if (this.currentRenderList.size() > 1) {
            throw new IllegalStateException("Not all render lists have been completed (" + this.currentRenderList.size() + ")");
        }
        return this.currentRenderList.pop().complete();
    }


    @Override
    public void doTranslate(final float x, final float y, final Runnable renderer) {
        float previousXOffset = this.xOffset;
        float previousYOffset = this.yOffset;
        this.xOffset += x;
        this.yOffset += y;
        this.transform(new ModifierCommand.Translate(x, y), renderer);
        this.xOffset = previousXOffset;
        this.yOffset = previousYOffset;
    }

    @Override
    public void doComponentBounds(final float x, final float y, final float width, final float height, final Runnable renderer) {
        this.transform(new ModifierCommand.ComponentBounds(x, y, width, height), renderer);
    }

    @Override
    public void doScissor(final float x, final float y, final float width, final float height, final Runnable renderer) {
        this.transform(new ModifierCommand.Scissor(x, y, width, height), renderer);
    }

    @Override
    public void doScale(final float x, final float y, final Runnable renderer) {
        this.transform(new ModifierCommand.Scale(x, y), renderer);
    }

    @Override
    public void doStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        DeferredRenderer mask = new DeferredRenderer();
        maskRenderer.accept(mask);
        this.transform(new ModifierCommand.Stencil(mask.complete(), false), renderer);
    }

    @Override
    public void doInverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        DeferredRenderer mask = new DeferredRenderer();
        maskRenderer.accept(mask);
        this.transform(new ModifierCommand.Stencil(mask.complete(), true), renderer);
    }

    @Override
    public void custom(final ModifierCommand.Custom command, final Runnable renderer) {
        this.transform(command, renderer);
    }

    private void transform(final ModifierCommand command, final Runnable renderer) {
        this.checkClosed();
        IncompleteRenderList newRenderList = new IncompleteRenderList();
        newRenderList.add(command);
        this.currentRenderList.push(newRenderList);
        renderer.run();
        IncompleteRenderList subList = this.currentRenderList.pop();
        RenderList completedSubList = subList.complete();
        if (!completedSubList.elements().isEmpty()) {
            this.currentRenderList.peek().add(completedSubList);
        }
    }


    @Override
    public void doFillCircle(final float x, final float y, final float radius, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.FillCircle(x, y, radius, color));
    }

    @Override
    public void doOutlineCircle(final float x, final float y, final float radius, final float outlineWidth, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.OutlineCircle(x, y, radius, outlineWidth, color));
    }

    @Override
    public void doFillTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.FillTriangle(x1, y1, x2, y2, x3, y3, color));
    }

    @Override
    public void doFillRect(final float x, final float y, final float width, final float height, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.FillRect(x, y, width, height, color));
    }

    @Override
    public void doOutlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.OutlineRect(x, y, width, height, outlineWidth, color));
    }

    @Override
    public void doFillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.FillRoundedRect(x, y, width, height, rtl, rbl, rbr, rtr, color));
    }

    @Override
    public void doOutlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.OutlineRoundedRect(x, y, width, height, rtl, rbl, rbr, rtr, outlineWidth, color));
    }

    @Override
    public void doFillPolygon(final Point[] points, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.FillPolygon(points, color));
    }

    @Override
    public void doLine(final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.Line(x1, y1, x2, y2, width, color));
    }

    @Override
    public void doPolyLine(final Point[] points, final float width, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.PolyLine(points, width, color));
    }

    @Override
    public void doFillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.FillGradientRect(x, y, width, height, ctl, cbl, cbr, ctr));
    }

    @Override
    public void doText(final ShapedText shapedText, final float x, final float y, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.Text(shapedText, x + shapedText.offset(horizontalOrigin), y + shapedText.offset(verticalOrigin)));
    }

    @Override
    public void doImage(final Texture texture, final float x, final float y, final float width, final float height, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.Image(texture, x, y, width, height, color));
    }

    @Override
    public void custom(final RenderCommand.Custom renderCommand) {
        this.checkClosed();
        this.currentRenderList.peek().add(renderCommand);
    }


    private static class IncompleteRenderList {
        private final List<ModifierCommand> transform = new ArrayList<>();
        private final List<RenderElement> elements = new ArrayList<>();
        private boolean closed;

        public void add(final ModifierCommand command) {
            this.checkClosed();
            this.transform.add(command);
        }

        public void add(final RenderElement element) {
            this.checkClosed();
            this.elements.add(element);
        }

        public RenderList complete() {
            this.checkClosed();
            this.closed = true;
            if (this.elements.size() == 1 && this.elements.get(0) instanceof RenderList subList) {
                this.elements.clear();
                if (subList.elements().isEmpty()) {
                    this.transform.clear();
                } else {
                    this.transform.addAll(subList.modifiers());
                    this.elements.addAll(subList.elements());
                }
            }
            return new RenderList(this.transform, this.elements);
        }

        private void checkClosed() {
            if (this.closed) {
                throw new IllegalStateException("Render list has been closed");
            }
        }
    }

}
