package net.lenni0451.rivet.backend.render;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.text.model.TextOrigin;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public final class Renderer {

    private final Stack<IncompleteRenderList> currentRenderList = new Stack<>();
    @Getter
    private float xOffset = 0;
    @Getter
    private float yOffset = 0;

    public Renderer() {
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


    public void translate(final float x, final float y, final Runnable renderer) {
        float previousXOffset = this.xOffset;
        float previousYOffset = this.yOffset;
        this.xOffset += x;
        this.yOffset += y;
        this.transform(new ModifierCommand.Translate(x, y), renderer);
        this.xOffset = previousXOffset;
        this.yOffset = previousYOffset;
    }

    public void componentBounds(final float x, final float y, final float width, final float height, final Runnable renderer) {
        this.transform(new ModifierCommand.ComponentBounds(x, y, width, height), renderer);
    }

    public void scissor(final float x, final float y, final float width, final float height, final Runnable renderer) {
        this.transform(new ModifierCommand.Scissor(x, y, width, height), renderer);
    }

    public void scale(final float xy, final Runnable renderer) {
        this.scale(xy, xy, renderer);
    }

    public void scale(final float x, final float y, final Runnable renderer) {
        if (x == 1 && y == 1) {
            renderer.run();
        } else {
            this.transform(new ModifierCommand.Scale(x, y), renderer);
        }
    }

    public void stencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        Renderer mask = new Renderer();
        maskRenderer.accept(mask);
        this.transform(new ModifierCommand.Stencil(mask.complete()), renderer);
    }

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
        this.currentRenderList.peek().add(subList.complete());
    }


    public void fillCircle(final float x, final float y, final float radius, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.FillCircle(x, y, radius, color));
    }

    public void outlineCircle(final float x, final float y, final float radius, final float outlineWidth, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.OutlineCircle(x, y, radius, outlineWidth, color));
    }

    public void fillTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.FillTriangle(x1, y1, x2, y2, x3, y3, color));
    }

    public void fillRect(final float x, final float y, final float width, final float height, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.FillRect(x, y, width, height, color));
    }

    public void outlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.OutlineRect(x, y, width, height, outlineWidth, color));
    }

    public void fillRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.FillRoundedRect(x, y, width, height, cornerRadius, cornerRadius, cornerRadius, cornerRadius, color));
    }

    public void fillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.FillRoundedRect(x, y, width, height, rtl, rbl, rbr, rtr, color));
    }

    public void outlineRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final float outlineWidth, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.OutlineRoundedRect(x, y, width, height, cornerRadius, cornerRadius, cornerRadius, cornerRadius, outlineWidth, color));
    }

    public void outlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.OutlineRoundedRect(x, y, width, height, rtl, rbl, rbr, rtr, outlineWidth, color));
    }

    public void optimizedFillRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final Color color) {
        this.checkClosed();
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

    public void optimizedOutlineRoundedRect(final float x, final float y, final float width, final float height, final float cornerRadius, final float outlineWidth, final Color color) {
        this.checkClosed();
        if (outlineWidth >= width || outlineWidth >= height) {
            this.optimizedFillRoundedRect(x, y, width, height, cornerRadius, color);
        } else {
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
    }

    public void line(final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.Line(x1, y1, x2, y2, width, color));
    }

    public void fillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.FillGradientRect(x, y, width, height, ctl, cbl, cbr, ctr));
    }

    public void text(final ShapedText shapedText, final float x, final float y, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin) {
        this.checkClosed();
        float tx = x + shapedText.offset(horizontalOrigin);
        float ty = y + shapedText.offset(verticalOrigin);
        this.currentRenderList.peek().add(new RenderCommand.Text(shapedText, tx, ty));
    }

    public void image(final Texture texture, final float x, final float y, final float width, final float height, final Color color) {
        this.checkClosed();
        this.currentRenderList.peek().add(new RenderCommand.Image(texture, x, y, width, height, color));
    }

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
