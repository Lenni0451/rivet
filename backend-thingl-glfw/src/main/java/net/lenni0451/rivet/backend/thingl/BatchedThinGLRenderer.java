package net.lenni0451.rivet.backend.thingl;

import net.lenni0451.rivet.backend.render.RenderCommand;
import net.lenni0451.rivet.backend.render.RenderElement;
import net.lenni0451.rivet.backend.render.RenderList;
import net.lenni0451.rivet.backend.render.TransformCommand;
import net.lenni0451.rivet.backend.thingl.util.MathUtil;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.renderer.impl.Renderer2D;
import net.raphimc.thingl.gl.renderer.impl.RendererText;
import net.raphimc.thingl.gl.rendering.dataholder.ImmediateMultiDrawBatchDataHolder;
import net.raphimc.thingl.rendering.dataholder.MultiDrawBatchDataHolder;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.primitives.Rectanglef;
import org.lwjgl.opengl.GL43C;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BatchedThinGLRenderer {

    public static void render(final Matrix4fStack matrixStack, final RenderList renderList) {
        render(matrixStack, buildLayerList(renderList));
    }

    public static List<Layer> buildLayerList(final RenderList renderList) {
        final List<Layer> layers = new ArrayList<>();
        final Matrix4fStack matrixStack = new Matrix4fStack(32);
        buildLayerList(layers, matrixStack, renderList);
        return layers;
    }

    public static void render(final Matrix4fStack matrixStack, final List<Layer> layers) {
        final MultiDrawBatchDataHolder multiDrawBatchDataHolder = new ImmediateMultiDrawBatchDataHolder();
        ThinGL.renderer2D().beginBuffering(multiDrawBatchDataHolder);
        ThinGL.rendererText().beginBuffering(multiDrawBatchDataHolder);
        GL43C.glPushDebugGroup(GL43C.GL_DEBUG_SOURCE_APPLICATION, 0, "Rivet");
        for (int i = 0; i < layers.size(); i++) {
            final Layer layer = layers.get(i);
            for (Layer.CommandState commandState : layer.commandStates()) {
                matrixStack.pushMatrix();
                matrixStack.mul(commandState.matrix());
                switch (commandState.command()) {
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
                matrixStack.popMatrix();
            }
            GL43C.glPushDebugGroup(GL43C.GL_DEBUG_SOURCE_APPLICATION, 0, "Layer " + i);
            multiDrawBatchDataHolder.draw();
            GL43C.glPopDebugGroup();
        }
        GL43C.glPopDebugGroup();
        ThinGL.renderer2D().endBuffering();
        ThinGL.rendererText().endBuffering();
    }


    private static void buildLayerList(final List<Layer> layers, final Matrix4fStack matrixStack, final RenderList renderList) {
        switch (renderList.transform()) {
            case TransformCommand.Scale scale -> {
                matrixStack.pushMatrix();
                matrixStack.scaleXY(scale.x(), scale.y());
            }
            case TransformCommand.Scissor scissor -> {
                // TODO: Implement
            }
            case TransformCommand.Translate translate -> {
                matrixStack.pushMatrix();
                matrixStack.translate(translate.x(), translate.y(), 0F);
            }
            case null -> {
            }
        }
        for (RenderElement element : renderList.elements()) {
            switch (element) {
                case RenderCommand command -> {
                    final Rectanglef bounds = MathUtil.transform(MathUtil.convert(command.bounds()), matrixStack);
                    int insertionIndex = 0;
                    for (int i = layers.size() - 1; i >= 0; i--) {
                        final Layer layer = layers.get(i);
                        if (layer.intersectsRectangle(bounds)) {
                            insertionIndex = i + 1;
                            break;
                        }
                    }
                    if (insertionIndex >= layers.size()) {
                        layers.add(new Layer());
                    }
                    layers.get(insertionIndex).commandStates.add(new Layer.CommandState(bounds, new Matrix4f(matrixStack), command));
                }
                case RenderList subRenderList -> buildLayerList(layers, matrixStack, subRenderList);
            }
        }
        switch (renderList.transform()) {
            case TransformCommand.Scale _ -> matrixStack.popMatrix();
            case TransformCommand.Scissor _ -> {
                // TODO: Implement
            }
            case TransformCommand.Translate _ -> matrixStack.popMatrix();
            case null -> {
            }
        }
    }

    public static class Layer {

        private final List<CommandState> commandStates = new ArrayList<>();

        private Layer() {
        }

        private boolean intersectsRectangle(final Rectanglef bounds) {
            for (CommandState commandState : this.commandStates) {
                if (commandState.bounds.intersectsRectangle(bounds)) {
                    return true;
                }
            }
            return false;
        }

        public List<CommandState> commandStates() {
            return Collections.unmodifiableList(this.commandStates);
        }

        public record CommandState(Rectanglef bounds, Matrix4f matrix, RenderCommand command) {
        }

    }

}
