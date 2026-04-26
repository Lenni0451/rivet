package net.lenni0451.rivet.backend.thingl;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.backend.render.RenderCommand;
import net.lenni0451.rivet.backend.render.RenderElement;
import net.lenni0451.rivet.backend.render.RenderList;
import net.lenni0451.rivet.backend.render.TransformCommand;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.renderer.impl.Renderer2D;
import net.raphimc.thingl.gl.renderer.impl.RendererText;
import org.joml.Matrix4fStack;

@RequiredArgsConstructor
public class ThinGLRenderer {

    public static void renderList(final Matrix4fStack matrixStack, final RenderList renderList) {
        switch (renderList.transform()) {
            case TransformCommand.Scale scale -> {
                matrixStack.pushMatrix();
                matrixStack.scaleXY(
                        scale.x(),
                        scale.y()
                );
            }
            case TransformCommand.ComponentBounds bounds -> ThinGL.scissorStack().pushIntersection(
                    matrixStack,
                    bounds.x(),
                    bounds.y(),
                    bounds.x() + bounds.width(),
                    bounds.y() + bounds.height()
            );
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
        for (RenderElement element : renderList.elements()) {
            switch (element) {
                case RenderCommand command -> renderCommand(matrixStack, command);
                case RenderList subList -> renderList(matrixStack, subList);
            }
        }
        switch (renderList.transform()) {
            case TransformCommand.Scale _ -> matrixStack.popMatrix();
            case TransformCommand.ComponentBounds _ -> ThinGL.scissorStack().pop();
            case TransformCommand.Scissor _ -> ThinGL.scissorStack().pop();
            case TransformCommand.Translate _ -> matrixStack.popMatrix();
            case null -> {
            }
        }
    }

    public static void renderCommand(final Matrix4fStack matrixStack, final RenderCommand command) {
        switch (command) {
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
            case RenderCommand.Text text -> ThinGL.rendererText().textLine(
                    matrixStack,
                    ((ThinGLShapedText) text.shapedText()).shapedTextLine(),
                    text.x(), text.y(),
                    RendererText.VerticalOrigin.BASELINE,
                    RendererText.HorizontalOrigin.LOGICAL_LEFT
            );
            case RenderCommand.FillGradientRect fillGradientRect -> ThinGL.renderer2D().filledRectangle(
                    matrixStack,
                    fillGradientRect.x(), fillGradientRect.y(),
                    fillGradientRect.x() + fillGradientRect.width(), fillGradientRect.y() + fillGradientRect.height(),
                    fillGradientRect.cbl(), fillGradientRect.cbr(), fillGradientRect.ctr(), fillGradientRect.ctl()
            );
        }
    }

}
