package net.lenni0451.rivet.backend.thingl.render;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.lenni0451.rivet.backend.render.ModifierCommand;
import net.lenni0451.rivet.backend.render.RenderCommand;
import net.lenni0451.rivet.backend.render.RenderElement;
import net.lenni0451.rivet.backend.render.RenderList;
import net.lenni0451.rivet.backend.thingl.util.MathUtil;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.rendering.dataholder.ImmediateMultiDrawBatchDataHolder;
import net.raphimc.thingl.rendering.dataholder.MultiDrawBatchDataHolder;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.primitives.Rectanglef;
import org.lwjgl.opengl.GL43C;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BatchedThinGLRenderer extends ThinGLRenderer {

    @Override
    public void renderList(final Matrix4fStack matrixStack, final RenderList renderList) {
        this.renderLayers(matrixStack, this.buildLayers(renderList));
    }

    public List<Layer> buildLayers(final RenderList renderList) {
        final List<Layer> layers = new ArrayList<>();
        this.buildLayers(layers, new Matrix4fStack(32), new ObjectArrayList<>(), renderList);
        return layers;
    }

    public void renderLayers(final Matrix4fStack matrixStack, final List<Layer> layers) {
        final MultiDrawBatchDataHolder multiDrawBatchDataHolder = new ImmediateMultiDrawBatchDataHolder();
        ThinGL.renderer2D().beginBuffering(multiDrawBatchDataHolder);
        ThinGL.rendererText().beginBuffering(multiDrawBatchDataHolder);
        GL43C.glPushDebugGroup(GL43C.GL_DEBUG_SOURCE_APPLICATION, 0, "Rivet");
        for (int i = 0; i < layers.size(); i++) {
            final Layer layer = layers.get(i);
            for (Layer.CommandState commandState : layer.commandStates()) {
                matrixStack.pushMatrix();
                matrixStack.mul(commandState.matrix());
                this.renderCommand(matrixStack, commandState.command());
                matrixStack.popMatrix();
            }
            GL43C.glPushDebugGroup(GL43C.GL_DEBUG_SOURCE_APPLICATION, 0, "Layer " + i);
            if (layer.scissor != null) {
                ThinGL.scissorStack().pushOverwrite(layer.scissor.minX, layer.scissor.minY, layer.scissor.maxX, layer.scissor.maxY);
            }
            multiDrawBatchDataHolder.draw();
            if (layer.scissor != null) {
                ThinGL.scissorStack().pop();
            }
            GL43C.glPopDebugGroup();
        }
        GL43C.glPopDebugGroup();
        ThinGL.renderer2D().endBuffering();
        ThinGL.rendererText().endBuffering();
    }


    private void buildLayers(final List<Layer> layers, final Matrix4fStack matrixStack, final Stack<Rectanglef> scissorStack, final RenderList renderList) {
        boolean matrixPushed = false;
        boolean scissorPushed = false;
        for (ModifierCommand transform : renderList.modifiers()) {
            switch (transform) {
                case ModifierCommand.Scale scale -> {
                    if (!matrixPushed) {
                        matrixPushed = true;
                        matrixStack.pushMatrix();
                    }
                    matrixStack.scaleXY(scale.x(), scale.y());
                }
                case ModifierCommand.ComponentBounds _ -> {
                }
                case ModifierCommand.Scissor scissor -> {
                    final Vector3f topLeft = matrixStack.transformPosition(new Vector3f(scissor.x(), scissor.y(), 0F));
                    final Vector3f bottomRight = matrixStack.transformPosition(new Vector3f(scissor.x() + scissor.width(), scissor.y() + scissor.height(), 0F));
                    final Rectanglef scissorRect = new Rectanglef(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
                    if (!scissorPushed) {
                        scissorPushed = true;
                        scissorStack.push(scissorRect);
                    } else {
                        scissorStack.push(scissorStack.top().intersection(scissorRect, new Rectanglef()));
                    }
                }
                case ModifierCommand.Translate translate -> {
                    if (!matrixPushed) {
                        matrixPushed = true;
                        matrixStack.pushMatrix();
                    }
                    matrixStack.translate(translate.x(), translate.y(), 0F);
                }
                case ModifierCommand.Custom custom -> {
                    // TODO: Implement
                }
            }
        }
        final Matrix4f currentMatrix = new Matrix4f(matrixStack);
        final Rectanglef currentScissor = !scissorStack.isEmpty() ? scissorStack.top() : null;
        for (RenderElement element : renderList.elements()) {
            switch (element) {
                case RenderCommand command -> {
                    final Rectanglef bounds = MathUtil.transform(MathUtil.convert(command.bounds()), currentMatrix);
                    int insertionIndex = 0;
                    if (!(command instanceof RenderCommand.Custom)) {
                        for (int i = layers.size() - 1; i >= 0; i--) {
                            final Layer layer = layers.get(i);
                            if (layer.intersectsRectangle(bounds) || !Objects.equals(layer.scissor, currentScissor)) {
                                insertionIndex = i + 1;
                                break;
                            }
                        }
                    } else {
                        insertionIndex = layers.size();
                        bounds.setMin(-Float.MAX_VALUE, -Float.MAX_VALUE);
                        bounds.setMax(Float.MAX_VALUE, Float.MAX_VALUE);
                    }
                    if (insertionIndex >= layers.size()) {
                        layers.add(new Layer(currentScissor));
                    }
                    layers.get(insertionIndex).commandStates.add(new Layer.CommandState(bounds, currentMatrix, command));
                }
                case RenderList subRenderList -> this.buildLayers(layers, matrixStack, scissorStack, subRenderList);
            }
        }
        if (matrixPushed) {
            matrixStack.popMatrix();
        }
        if (scissorPushed) {
            scissorStack.pop();
        }
    }

    public static class Layer {

        private final List<CommandState> commandStates = new ArrayList<>();
        private final Rectanglef scissor;

        private Layer(final Rectanglef scissor) {
            this.scissor = scissor;
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
