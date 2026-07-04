package net.lenni0451.rivet.backend.thingl.render;

import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderElement;
import net.lenni0451.rivet.backend.render.deferred.RenderList;
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
            switch (modifiers.next()) {
                case ModifierCommand.Scale scale -> renderer.scale(scale.x(), scale.y(), () -> this.renderList(renderer, elements, modifiers));
                case ModifierCommand.ComponentBounds bounds -> renderer.componentBounds(bounds.x(), bounds.y(), bounds.width(), bounds.height(), () -> this.renderList(renderer, elements, modifiers));
                case ModifierCommand.Scissor scissor -> renderer.scissor(scissor.x(), scissor.y(), scissor.width(), scissor.height(), () -> this.renderList(renderer, elements, modifiers));
                case ModifierCommand.Translate translate -> renderer.translate(translate.x(), translate.y(), () -> this.renderList(renderer, elements, modifiers));
                case ModifierCommand.Stencil stencil -> {
                    if (!stencil.inverse()) {
                        renderer.stencil(maskRenderer -> this.renderList(maskRenderer, stencil.mask()), () -> this.renderList(renderer, elements, modifiers));
                    } else {
                        renderer.inverseStencil(maskRenderer -> this.renderList(maskRenderer, stencil.mask()), () -> this.renderList(renderer, elements, modifiers));
                    }
                }
                case ModifierCommand.Custom custom -> renderer.custom(custom, () -> this.renderList(renderer, elements, modifiers));
            }
            return;
        }
        for (RenderElement element : elements) {
            switch (element) {
                case RenderCommand command -> this.renderCommand(renderer, command);
                case RenderList subList -> this.renderList(renderer, subList.elements(), subList.modifiers().iterator());
            }
        }
    }

    private void renderCommand(final Renderer renderer, final RenderCommand command) {
        switch (command) {
            case RenderCommand.FillCircle fillCircle -> renderer.fillCircle(fillCircle.x(), fillCircle.y(), fillCircle.radius(), fillCircle.color());
            case RenderCommand.FillRect fillRect -> renderer.fillRect(fillRect.x(), fillRect.y(), fillRect.width(), fillRect.height(), fillRect.color());
            case RenderCommand.FillRoundedRect fillRoundedRect -> renderer.fillRoundedRect(
                    fillRoundedRect.x(), fillRoundedRect.y(),
                    fillRoundedRect.width(), fillRoundedRect.height(),
                    fillRoundedRect.rbl(), fillRoundedRect.rbr(),
                    fillRoundedRect.rtr(), fillRoundedRect.rtl(),
                    fillRoundedRect.color()
            );
            case RenderCommand.FillTriangle fillTriangle -> renderer.fillTriangle(
                    fillTriangle.x1(), fillTriangle.y1(),
                    fillTriangle.x2(), fillTriangle.y2(),
                    fillTriangle.x3(), fillTriangle.y3(),
                    fillTriangle.color()
            );
            case RenderCommand.OutlineCircle outlineCircle -> renderer.outlineCircle(outlineCircle.x(), outlineCircle.y(), outlineCircle.radius(), outlineCircle.outlineWidth(), outlineCircle.color());
            case RenderCommand.OutlineRect outlineRect -> renderer.outlineRect(
                    outlineRect.x(), outlineRect.y(),
                    outlineRect.width(), outlineRect.height(),
                    outlineRect.outlineWidth(),
                    outlineRect.color()
            );
            case RenderCommand.OutlineRoundedRect outlineRoundedRect -> renderer.outlineRoundedRect(
                    outlineRoundedRect.x(), outlineRoundedRect.y(),
                    outlineRoundedRect.width(), outlineRoundedRect.height(),
                    outlineRoundedRect.rbl(), outlineRoundedRect.rbr(),
                    outlineRoundedRect.rtr(), outlineRoundedRect.rtl(),
                    outlineRoundedRect.outlineWidth(),
                    outlineRoundedRect.color()
            );
            case RenderCommand.FillPolygon fillPolygon -> renderer.fillPolygon(fillPolygon.points(), fillPolygon.color());
            case RenderCommand.Line line -> renderer.line(line.x1(), line.y1(), line.x2(), line.y2(), line.width(), line.color());
            case RenderCommand.PolyLine polyLine -> renderer.polyLine(polyLine.points(), polyLine.width(), polyLine.color());
            case RenderCommand.Text text -> renderer.text(text.shapedText(), text.x(), text.y(), TextOrigin.Horizontal.LOGICAL_LEFT, TextOrigin.Vertical.BASELINE);
            case RenderCommand.Image image -> renderer.image(image.texture(), image.x(), image.y(), image.width(), image.height(), image.color());
            case RenderCommand.FillGradientRect fillGradientRect -> renderer.fillGradientRect(
                    fillGradientRect.x(), fillGradientRect.y(),
                    fillGradientRect.width(), fillGradientRect.height(),
                    fillGradientRect.ctl(), fillGradientRect.cbl(), fillGradientRect.cbr(), fillGradientRect.ctr()
            );
            case RenderCommand.Custom custom -> renderer.custom(custom);
        }
    }

}
