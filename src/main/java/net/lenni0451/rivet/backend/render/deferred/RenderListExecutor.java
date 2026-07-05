package net.lenni0451.rivet.backend.render.deferred;

import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.text.model.TextOrigin;

import java.util.Iterator;
import java.util.List;

public class RenderListExecutor {

    public static final RenderListExecutor INSTANCE = new RenderListExecutor();

    public void renderList(final Renderer renderer, final RenderList renderList) {
        this.renderList(renderer, renderList.elements(), renderList.modifiers().iterator());
    }

    private void renderList(final Renderer renderer, final List<RenderElement> elements, final Iterator<ModifierCommand> modifiers) {
        if (modifiers.hasNext()) {
            ModifierCommand command = modifiers.next();
            if (command instanceof ModifierCommand.Scale scale) {
                renderer.scale(scale.x(), scale.y(), () -> this.renderList(renderer, elements, modifiers));
            } else if (command instanceof ModifierCommand.ComponentBounds bounds) {
                renderer.componentBounds(bounds.x(), bounds.y(), bounds.width(), bounds.height(), () -> this.renderList(renderer, elements, modifiers));
            } else if (command instanceof ModifierCommand.Scissor scissor) {
                renderer.scissor(scissor.x(), scissor.y(), scissor.width(), scissor.height(), () -> this.renderList(renderer, elements, modifiers));
            } else if (command instanceof ModifierCommand.Translate translate) {
                renderer.translate(translate.x(), translate.y(), () -> this.renderList(renderer, elements, modifiers));
            } else if (command instanceof ModifierCommand.Stencil stencil) {
                if (!stencil.inverse()) {
                    renderer.stencil(maskRenderer -> this.renderList(maskRenderer, stencil.mask()), () -> this.renderList(renderer, elements, modifiers));
                } else {
                    renderer.inverseStencil(maskRenderer -> this.renderList(maskRenderer, stencil.mask()), () -> this.renderList(renderer, elements, modifiers));
                }
            } else if (command instanceof ModifierCommand.Custom custom) {
                renderer.custom(custom, () -> this.renderList(renderer, elements, modifiers));
            }
            return;
        }
        for (RenderElement element : elements) {
            if (element instanceof RenderCommand command) {
                this.renderCommand(renderer, command);
            } else if (element instanceof RenderList subList) {
                this.renderList(renderer, subList.elements(), subList.modifiers().iterator());
            }
        }
    }

    public void renderCommand(final Renderer renderer, final RenderCommand command) {
        if (command instanceof RenderCommand.FillCircle fillCircle) {
            renderer.fillCircle(fillCircle.x(), fillCircle.y(), fillCircle.radius(), fillCircle.color());
        } else if (command instanceof RenderCommand.FillRect fillRect) {
            renderer.fillRect(fillRect.x(), fillRect.y(), fillRect.width(), fillRect.height(), fillRect.color());
        } else if (command instanceof RenderCommand.FillRoundedRect fillRoundedRect) {
            renderer.fillRoundedRect(
                    fillRoundedRect.x(), fillRoundedRect.y(),
                    fillRoundedRect.width(), fillRoundedRect.height(),
                    fillRoundedRect.rbl(), fillRoundedRect.rbr(),
                    fillRoundedRect.rtr(), fillRoundedRect.rtl(),
                    fillRoundedRect.color()
            );
        } else if (command instanceof RenderCommand.FillTriangle fillTriangle) {
            renderer.fillTriangle(
                    fillTriangle.x1(), fillTriangle.y1(),
                    fillTriangle.x2(), fillTriangle.y2(),
                    fillTriangle.x3(), fillTriangle.y3(),
                    fillTriangle.color()
            );
        } else if (command instanceof RenderCommand.OutlineCircle outlineCircle) {
            renderer.outlineCircle(outlineCircle.x(), outlineCircle.y(), outlineCircle.radius(), outlineCircle.outlineWidth(), outlineCircle.color());
        } else if (command instanceof RenderCommand.OutlineRect outlineRect) {
            renderer.outlineRect(
                    outlineRect.x(), outlineRect.y(),
                    outlineRect.width(), outlineRect.height(),
                    outlineRect.outlineWidth(),
                    outlineRect.color()
            );
        } else if (command instanceof RenderCommand.OutlineRoundedRect outlineRoundedRect) {
            renderer.outlineRoundedRect(
                    outlineRoundedRect.x(), outlineRoundedRect.y(),
                    outlineRoundedRect.width(), outlineRoundedRect.height(),
                    outlineRoundedRect.rbl(), outlineRoundedRect.rbr(),
                    outlineRoundedRect.rtr(), outlineRoundedRect.rtl(),
                    outlineRoundedRect.outlineWidth(),
                    outlineRoundedRect.color()
            );
        } else if (command instanceof RenderCommand.FillPolygon fillPolygon) {
            renderer.fillPolygon(fillPolygon.points(), fillPolygon.color());
        } else if (command instanceof RenderCommand.Line line) {
            renderer.line(line.x1(), line.y1(), line.x2(), line.y2(), line.width(), line.color());
        } else if (command instanceof RenderCommand.PolyLine polyLine) {
            renderer.polyLine(polyLine.points(), polyLine.width(), polyLine.color());
        } else if (command instanceof RenderCommand.Text text) {
            renderer.text(text.shapedText(), text.x(), text.y(), TextOrigin.Horizontal.LOGICAL_LEFT, TextOrigin.Vertical.BASELINE);
        } else if (command instanceof RenderCommand.Image image) {
            renderer.image(image.texture(), image.x(), image.y(), image.width(), image.height(), image.color());
        } else if (command instanceof RenderCommand.FillGradientRect fillGradientRect) {
            renderer.fillGradientRect(
                    fillGradientRect.x(), fillGradientRect.y(),
                    fillGradientRect.width(), fillGradientRect.height(),
                    fillGradientRect.ctl(), fillGradientRect.cbl(), fillGradientRect.cbr(), fillGradientRect.ctr()
            );
        } else if (command instanceof RenderCommand.Custom custom) {
            renderer.custom(custom);
        }
    }

}
