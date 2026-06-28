package net.lenni0451.rivet.backend.thingl.render;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.lenni0451.commons.math.MathUtils;
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
import java.util.Objects;

public class BatchedThinGLRenderer extends ThinGLRenderer {

    private static final Rectanglef FULL_SIZE_RECT = new Rectanglef(-Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

    @Override
    public void renderList(final Matrix4fStack matrixStack, final RenderList renderList) {
        this.renderLayers(matrixStack, this.buildLayers(renderList));
    }

    public List<Layer> buildLayers(final RenderList renderList) {
        final List<Layer> layers = new ArrayList<>();
        this.buildLayers(layers, new Matrix4fStack(32), new ObjectArrayList<>(List.of(FULL_SIZE_RECT)), renderList);
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
            if (layer.blurStrength > 0) {
                ThinGL.programs().getGaussianBlur().bindInput();
            }
            if (!layer.scissor.equals(FULL_SIZE_RECT)) {
                ThinGL.scissorStack().pushOverwrite(matrixStack, MathUtils.floorInt(layer.scissor.minX), MathUtils.floorInt(layer.scissor.minY), MathUtils.ceilInt(layer.scissor.maxX), MathUtils.ceilInt(layer.scissor.maxY));
            }
            multiDrawBatchDataHolder.draw();
            if (!layer.scissor.equals(FULL_SIZE_RECT)) {
                ThinGL.scissorStack().pop();
            }
            if (layer.blurStrength > 0) {
                ThinGL.programs().getGaussianBlur().unbindInput();
                ThinGL.programs().getGaussianBlur().configureParameters(layer.blurStrength);
                ThinGL.programs().getGaussianBlur().render(matrixStack, layer.bounds.minX, layer.bounds.minY, layer.bounds.maxX, layer.bounds.maxY);
                ThinGL.programs().getGaussianBlur().clearInput();
            }
            GL43C.glPopDebugGroup();
        }
        GL43C.glPopDebugGroup();
        ThinGL.renderer2D().endBuffering();
        ThinGL.rendererText().endBuffering();
    }


    private void buildLayers(final List<Layer> layers, final Matrix4fStack matrixStack, final Stack<Rectanglef> scissorStack, final RenderList renderList) {
        matrixStack.pushMatrix();
        scissorStack.push(new Rectanglef(scissorStack.top()));
        int blurStrength = 0;
        for (ModifierCommand transform : renderList.modifiers()) {
            switch (transform) {
                case ModifierCommand.Translate translate -> matrixStack.translate(translate.x(), translate.y(), 0F);
                case ModifierCommand.ComponentBounds _ -> {
                }
                case ModifierCommand.Scissor scissor -> {
                    final Rectanglef scissorRect = new Rectanglef(scissor.x(), scissor.y(), scissor.x() + scissor.width(), scissor.y() + scissor.height());
                    scissorStack.top().intersection(MathUtil.transform(scissorRect, matrixStack));
                }
                case ModifierCommand.Scale scale -> matrixStack.scaleXY(scale.x(), scale.y());
                case ThinGLModifierCommand thinGlModifier -> {
                    switch (thinGlModifier) {
                        case ThinGLModifierCommand.Blur blur -> blurStrength += blur.strength();
                    }
                }
                case ModifierCommand.Custom _ -> throw new UnsupportedOperationException("Custom modifier commands are not supported in BatchedThinGLRenderer");
            }
        }
        final Matrix4f currentMatrix = new Matrix4f(matrixStack);
        final Rectanglef currentScissor = scissorStack.top();
        for (RenderElement element : renderList.elements()) {
            switch (element) {
                case RenderCommand command -> {
                    final Rectanglef bounds = MathUtil.transform(MathUtil.convert(command.bounds()), currentMatrix);
                    int insertionIndex = 0;
                    if (!(command instanceof RenderCommand.Custom)) {
                        for (int i = layers.size() - 1; i >= 0; i--) {
                            final Layer layer = layers.get(i);
                            if (layer.intersectsRectangle(bounds) || !Objects.equals(layer.scissor, currentScissor) || layer.blurStrength != blurStrength) {
                                insertionIndex = i + 1;
                                break;
                            }
                        }
                    } else {
                        insertionIndex = layers.size();
                        bounds.set(FULL_SIZE_RECT);
                    }
                    if (insertionIndex >= layers.size()) {
                        layers.add(new Layer(currentScissor, blurStrength));
                    }
                    layers.get(insertionIndex).addCommandState(new Layer.CommandState(bounds, currentMatrix, command));
                }
                case RenderList subRenderList -> this.buildLayers(layers, matrixStack, scissorStack, subRenderList);
            }
        }
        matrixStack.popMatrix();
        scissorStack.pop();
    }

    public static class Layer {

        private final List<CommandState> commandStates = new ArrayList<>();
        private final Rectanglef bounds = new Rectanglef();
        private final Rectanglef scissor;
        private final int blurStrength;

        private Layer(final Rectanglef scissor, final int blurStrength) {
            this.scissor = scissor;
            this.blurStrength = blurStrength;
        }

        private boolean intersectsRectangle(final Rectanglef bounds) {
            for (CommandState commandState : this.commandStates) {
                if (commandState.bounds.intersectsRectangle(bounds)) {
                    return true;
                }
            }
            return false;
        }

        private void addCommandState(final CommandState commandState) {
            this.commandStates.add(commandState);
            this.bounds.union(commandState.bounds);
        }

        public List<CommandState> commandStates() {
            return Collections.unmodifiableList(this.commandStates);
        }

        public record CommandState(Rectanglef bounds, Matrix4f matrix, RenderCommand command) {
        }

    }

}
