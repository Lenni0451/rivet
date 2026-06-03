package net.lenni0451.rivet.backend.thingl.render;

import lombok.RequiredArgsConstructor;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.render.RenderCommand;
import net.lenni0451.rivet.backend.render.RenderElement;
import net.lenni0451.rivet.backend.render.RenderList;
import net.lenni0451.rivet.backend.render.TransformCommand;
import net.lenni0451.rivet.backend.thingl.ThinGLTexture;
import net.lenni0451.rivet.backend.thingl.text.ThinGLShapedText;
import net.lenni0451.rivet.backend.thingl.text.ThinGLShapedTextBlock;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.renderer.impl.Renderer2D;
import net.raphimc.thingl.gl.renderer.impl.RendererText;
import org.joml.Matrix4fStack;

@RequiredArgsConstructor
public class ThinGLRenderer {

    public static void renderList(final Matrix4fStack matrixStack, final RenderList renderList) {
        boolean matrixPushed = false;
        int scissorPushed = 0;
        for (TransformCommand transform : renderList.transforms()) {
            switch (transform) {
                case TransformCommand.Scale scale -> {
                    if (!matrixPushed) {
                        matrixPushed = true;
                        matrixStack.pushMatrix();
                    }
                    matrixStack.scaleXY(
                            scale.x(),
                            scale.y()
                    );
                }
                case TransformCommand.ComponentBounds bounds -> {
                    scissorPushed++;
                    ThinGL.scissorStack().pushIntersection(
                            matrixStack,
                            MathUtils.floorInt(bounds.x()),
                            MathUtils.floorInt(bounds.y()),
                            MathUtils.ceilInt(bounds.x() + bounds.width()),
                            MathUtils.ceilInt(bounds.y() + bounds.height())
                    );
                }
                case TransformCommand.Scissor scissor -> {
                    scissorPushed++;
                    ThinGL.scissorStack().pushIntersection(
                            matrixStack,
                            MathUtils.floorInt(scissor.x()),
                            MathUtils.floorInt(scissor.y()),
                            MathUtils.ceilInt(scissor.x() + scissor.width()),
                            MathUtils.ceilInt(scissor.y() + scissor.height())
                    );
                }
                case TransformCommand.Translate translate -> {
                    if (!matrixPushed) {
                        matrixPushed = true;
                        matrixStack.pushMatrix();
                    }
                    matrixStack.translate(
                            translate.x(),
                            translate.y(),
                            0
                    );
                }
            }
        }
        for (RenderElement element : renderList.elements()) {
            switch (element) {
                case RenderCommand command -> renderCommand(matrixStack, command);
                case RenderList subList -> renderList(matrixStack, subList);
            }
        }
        while (scissorPushed-- > 0) ThinGL.scissorStack().pop();
        if (matrixPushed) matrixStack.popMatrix();
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
            case RenderCommand.Text text -> {
                switch (text.shapedText()) {
                    case ThinGLShapedText shapedText -> ThinGL.rendererText().textLine(
                            matrixStack,
                            shapedText.shapedTextLine(),
                            text.x(), text.y(),
                            RendererText.VerticalOrigin.BASELINE,
                            RendererText.HorizontalOrigin.LOGICAL_LEFT
                    );
                    case ThinGLShapedTextBlock shapedTextBlock -> ThinGL.rendererText().textBlock(
                            matrixStack,
                            shapedTextBlock.shapedTextBlock(),
                            text.x(), text.y(),
                            RendererText.VerticalOrigin.BASELINE,
                            RendererText.HorizontalOrigin.LOGICAL_LEFT
                    );
                    default -> throw new UnsupportedOperationException(text.shapedText().getClass().getName());
                }
            }
            case RenderCommand.Image image -> {
                ThinGLTexture texture = (ThinGLTexture) image.texture();
                if (image.color().equals(Color.WHITE)) {
                    ThinGL.renderer2D().texture(
                            matrixStack,
                            texture.texture(),
                            image.x(), image.y(),
                            image.width(), image.height(),
                            texture.view().minX, texture.view().minY,
                            texture.view().lengthX(), texture.view().lengthY()
                    );
                } else if (image.color().getRed() == 255 && image.color().getGreen() == 255 && image.color().getBlue() == 255) {
                    ThinGL.renderer2D().coloredTexture(
                            matrixStack,
                            texture.texture(),
                            image.x(), image.y(),
                            image.width(), image.height(),
                            texture.view().minX, texture.view().minY,
                            texture.view().lengthX(), texture.view().lengthY(),
                            image.color()
                    );
                } else {
                    ThinGL.renderer2D().colorizedTexture(
                            matrixStack,
                            texture.texture(),
                            image.x(), image.y(),
                            image.width(), image.height(),
                            texture.view().minX, texture.view().minY,
                            texture.view().lengthX(), texture.view().lengthY(),
                            image.color()
                    );
                }
            }
            case RenderCommand.FillGradientRect fillGradientRect -> ThinGL.renderer2D().filledRectangle(
                    matrixStack,
                    fillGradientRect.x(), fillGradientRect.y(),
                    fillGradientRect.x() + fillGradientRect.width(), fillGradientRect.y() + fillGradientRect.height(),
                    fillGradientRect.cbl(), fillGradientRect.cbr(), fillGradientRect.ctr(), fillGradientRect.ctl()
            );
            case RenderCommand.CustomRenderCommand customRenderCommand -> {
                customRenderCommand.action().accept(matrixStack);
            }
        }
    }

}
