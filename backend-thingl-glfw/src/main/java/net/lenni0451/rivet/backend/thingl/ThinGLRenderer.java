package net.lenni0451.rivet.backend.thingl;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.backend.render.RenderCommand;
import net.lenni0451.rivet.backend.render.RenderList;
import net.lenni0451.rivet.backend.render.TransformCommand;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.renderer.impl.Renderer2D;
import net.raphimc.thingl.gl.renderer.impl.RendererText;
import org.joml.Matrix4fStack;

@RequiredArgsConstructor
public class ThinGLRenderer {

    public static void render(final Matrix4fStack matrixStack, final RenderList renderList) {
        switch (renderList.transform()) {
            case TransformCommand.Scale scale -> {
                matrixStack.pushMatrix();
                matrixStack.scale(
                        scale.x(),
                        scale.y(),
                        1
                );
            }
            case TransformCommand.Scissor scissor -> ThinGL.scissorStack().pushIntersection(
                    matrixStack,
                    scissor.x(),
                    scissor.y(),
                    scissor.x() + scissor.width(),
                    scissor.y() + scissor.height()
            );
            case TransformCommand.Translate translate -> {
                matrixStack.pushMatrix();
                matrixStack.translate(
                        translate.x(),
                        translate.y(),
                        0
                );
            }
            case null -> {
            }
        }
        for (RenderCommand render : renderList.renders()) {
            switch (render) {
                case RenderCommand.FillCircle fillCircle -> ThinGL.renderer2D().filledCircle(
                        matrixStack,
                        fillCircle.x(), fillCircle.y(),
                        fillCircle.radius(),
                        fillCircle.color()
                );
                case RenderCommand.FillRect fillRect -> ThinGL.renderer2D().filledRectangle(
                        matrixStack,
                        fillRect.x(), fillRect.y(),
                        fillRect.x() + fillRect.width(), fillRect.y() + fillRect.height(),
                        fillRect.color()
                );
                case RenderCommand.FillRoundedRect fillRoundedRect -> ThinGL.renderer2D().filledRoundedRectangle(
                        matrixStack,
                        fillRoundedRect.x(), fillRoundedRect.y(),
                        fillRoundedRect.x() + fillRoundedRect.width(), fillRoundedRect.y() + fillRoundedRect.height(),
                        fillRoundedRect.cornerRadius(),
                        fillRoundedRect.color()
                );
                case RenderCommand.FillTriangle fillTriangle -> ThinGL.renderer2D().filledTriangle(
                        matrixStack,
                        fillTriangle.x1(), fillTriangle.y1(),
                        fillTriangle.x2(), fillTriangle.y2(),
                        fillTriangle.x3(), fillTriangle.y3(),
                        fillTriangle.color()
                );
                case RenderCommand.OutlineCircle outlineCircle -> ThinGL.renderer2D().outlinedCircle(
                        matrixStack,
                        outlineCircle.x(), outlineCircle.y(),
                        outlineCircle.radius(),
                        outlineCircle.color(),
                        outlineCircle.outlineWidth(),
                        Renderer2D.OUTLINE_STYLE_INNER_BIT
                );
                case RenderCommand.OutlineRect outlineRect -> ThinGL.renderer2D().outlinedRectangle(
                        matrixStack,
                        outlineRect.x(), outlineRect.y(),
                        outlineRect.x() + outlineRect.width(), outlineRect.y() + outlineRect.height(),
                        outlineRect.color(),
                        outlineRect.outlineWidth(),
                        Renderer2D.OUTLINE_STYLE_INNER_BIT
                );
                case RenderCommand.OutlineRoundedRect outlineRoundedRect -> ThinGL.renderer2D().outlinedRoundedRectangle(
                        matrixStack,
                        outlineRoundedRect.x(), outlineRoundedRect.y(),
                        outlineRoundedRect.x() + outlineRoundedRect.width(), outlineRoundedRect.y() + outlineRoundedRect.height(),
                        outlineRoundedRect.cornerRadius(),
                        outlineRoundedRect.color(),
                        outlineRoundedRect.outlineWidth(),
                        Renderer2D.OUTLINE_STYLE_INNER_BIT
                );
                case RenderCommand.Line line -> ThinGL.renderer2D().line(
                        matrixStack,
                        line.x1(), line.y1(),
                        line.x2(), line.y2(),
                        line.width(),
                        line.color()
                );
                case RenderCommand.Text text -> {
                    RendererText.VerticalOrigin verticalOrigin = switch (text.verticalOrigin()) {
                        case BASELINE -> RendererText.VerticalOrigin.BASELINE;
                        case LOGICAL_TOP -> RendererText.VerticalOrigin.LOGICAL_TOP;
                        case LOGICAL_CENTER -> RendererText.VerticalOrigin.LOGICAL_CENTER;
                        case LOGICAL_BOTTOM -> RendererText.VerticalOrigin.LOGICAL_BOTTOM;
                        case VISUAL_TOP -> RendererText.VerticalOrigin.VISUAL_TOP;
                        case VISUAL_CENTER -> RendererText.VerticalOrigin.VISUAL_CENTER;
                        case VISUAL_BOTTOM -> RendererText.VerticalOrigin.VISUAL_BOTTOM;
                    };
                    RendererText.HorizontalOrigin horizontalOrigin = switch (text.horizontalOrigin()) {
                        case LOGICAL_LEFT -> RendererText.HorizontalOrigin.LOGICAL_LEFT;
                        case VISUAL_LEFT -> RendererText.HorizontalOrigin.VISUAL_LEFT;
                        case VISUAL_CENTER -> RendererText.HorizontalOrigin.VISUAL_CENTER;
                        case VISUAL_RIGHT -> RendererText.HorizontalOrigin.VISUAL_RIGHT;
                    };
                    ThinGL.rendererText().textLine(
                            matrixStack,
                            ((ThinGLShapedText) text.shapedText()).shapedTextLine(),
                            text.x(), text.y(),
                            verticalOrigin,
                            horizontalOrigin
                    );
                }
                case RenderCommand.FillGradientRect fillGradientRect -> ThinGL.renderer2D().filledRectangle(
                        matrixStack,
                        fillGradientRect.x(), fillGradientRect.y(),
                        fillGradientRect.x() + fillGradientRect.width(), fillGradientRect.y() + fillGradientRect.height(),
                        fillGradientRect.cbl(), fillGradientRect.cbr(), fillGradientRect.ctr(), fillGradientRect.ctl()
                );
            }
        }
        for (RenderList subList : renderList.subLists()) {
            render(matrixStack, subList);
        }
        switch (renderList.transform()) {
            case TransformCommand.Scale _ -> matrixStack.popMatrix();
            case TransformCommand.Scissor _ -> ThinGL.scissorStack().pop();
            case TransformCommand.Translate _ -> matrixStack.popMatrix();
            case null -> {
            }
        }
    }

}
