package net.lenni0451.rivet.backend.thingl.render;

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
import org.joml.primitives.Rectanglef;
import org.lwjgl.opengl.GL43C;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BatchedThinGLRenderer extends ThinGLRenderer {

    public void renderList(final Matrix4fStack matrixStack, final RenderList renderList) {
        this.renderLayers(matrixStack, this.buildLayers(renderList));
    }

    public List<Layer> buildLayers(final RenderList renderList) {
        final List<Layer> layers = new ArrayList<>();
        final Matrix4fStack matrixStack = new Matrix4fStack(32);
        this.buildLayers(layers, matrixStack, renderList);
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
            multiDrawBatchDataHolder.draw();
            GL43C.glPopDebugGroup();
        }
        GL43C.glPopDebugGroup();
        ThinGL.renderer2D().endBuffering();
        ThinGL.rendererText().endBuffering();
    }


    private void buildLayers(final List<Layer> layers, final Matrix4fStack matrixStack, final RenderList renderList) {
        boolean matrixPushed = false;
        for (ModifierCommand transform : renderList.modifiers()) {
            switch (transform) {
                case ModifierCommand.Scale scale -> {
                    if (!matrixPushed) {
                        matrixPushed = true;
                        matrixStack.pushMatrix();
                    }
                    matrixStack.scaleXY(scale.x(), scale.y());
                }
                case ModifierCommand.ComponentBounds bounds -> {
                    // TODO: Implement
                }
                case ModifierCommand.Scissor scissor -> {
                    // TODO: Implement
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
                case RenderList subRenderList -> this.buildLayers(layers, matrixStack, subRenderList);
            }
        }
        if (matrixPushed) matrixStack.popMatrix();
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
