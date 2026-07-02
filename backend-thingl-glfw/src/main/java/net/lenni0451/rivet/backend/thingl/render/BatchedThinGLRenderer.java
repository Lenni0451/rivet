package net.lenni0451.rivet.backend.thingl.render;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.render.ModifierCommand;
import net.lenni0451.rivet.backend.render.RenderCommand;
import net.lenni0451.rivet.backend.render.RenderElement;
import net.lenni0451.rivet.backend.render.RenderList;
import net.lenni0451.rivet.backend.thingl.util.MathUtil;
import net.lenni0451.rivet.math.Rectangle;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.rendering.dataholder.ImmediateMultiDrawBatchDataHolder;
import net.raphimc.thingl.gl.wrapper.StencilStack;
import net.raphimc.thingl.rendering.dataholder.MultiDrawBatchDataHolder;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.primitives.Rectanglef;
import org.lwjgl.opengl.GL43C;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BatchedThinGLRenderer extends ThinGLRenderer {

    private static final Rectanglef EMPTY_RECT = new Rectanglef(Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    private static final Rectanglef FULL_SIZE_RECT = new Rectanglef(-Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

    @Override
    public void renderList(final Matrix4fStack matrixStack, final RenderList renderList) {
        this.renderLayers(matrixStack, this.buildLayers(renderList));
    }

    public List<Layer> buildLayers(final RenderList renderList) {
        final Layers layers = new Layers();
        layers.addRenderList(renderList, new Matrix4f(), FULL_SIZE_RECT);
        return layers;
    }

    public void renderLayers(final Matrix4fStack matrixStack, final List<Layer> layers) {
        final MultiDrawBatchDataHolder multiDrawBatchDataHolder = new ImmediateMultiDrawBatchDataHolder();
        final MultiDrawBatchDataHolder previousMultiDrawBatchDataHolder2D = ThinGL.renderer2D().getTargetMultiDrawBatchDataHolder();
        final MultiDrawBatchDataHolder previousMultiDrawBatchDataHolderText = ThinGL.rendererText().getTargetMultiDrawBatchDataHolder();
        try {
            ThinGL.renderer2D().beginBuffering(multiDrawBatchDataHolder);
            ThinGL.rendererText().beginBuffering(multiDrawBatchDataHolder);
            GL43C.glPushDebugGroup(GL43C.GL_DEBUG_SOURCE_APPLICATION, 0, "Rivet");
            for (int i = 0; i < layers.size(); i++) {
                GL43C.glPushDebugGroup(GL43C.GL_DEBUG_SOURCE_APPLICATION, 0, "Layer " + i);
                final Layer layer = layers.get(i);
                for (Layer.CommandState commandState : layer.commandStates()) {
                    matrixStack.pushMatrix();
                    matrixStack.mul(commandState.matrix());
                    this.renderCommand(matrixStack, commandState.renderCommand());
                    matrixStack.popMatrix();
                }
                if (layer.blurStrength > 0) {
                    ThinGL.programs().getGaussianBlur().bindInput();
                }
                if (!layer.stencilMaskLayers.isEmpty()) {
                    ThinGL.stencilStack().push(layer.stencilMaskMode);
                    this.renderLayers(matrixStack, layer.stencilMaskLayers);
                    ThinGL.stencilStack().set();
                }
                if (!layer.scissor.equals(FULL_SIZE_RECT)) {
                    ThinGL.scissorStack().pushOverwrite(matrixStack, MathUtils.floorInt(layer.scissor.minX), MathUtils.floorInt(layer.scissor.minY), MathUtils.ceilInt(layer.scissor.maxX), MathUtils.ceilInt(layer.scissor.maxY));
                }
                multiDrawBatchDataHolder.draw();
                if (!layer.scissor.equals(FULL_SIZE_RECT)) {
                    ThinGL.scissorStack().pop();
                }
                if (!layer.stencilMaskLayers.isEmpty()) {
                    ThinGL.stencilStack().pop();
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
        } finally {
            ThinGL.renderer2D().beginBuffering(previousMultiDrawBatchDataHolder2D);
            ThinGL.rendererText().beginBuffering(previousMultiDrawBatchDataHolderText);
        }
    }

    public static class Layers extends ArrayList<Layer> {

        public void addRenderList(final RenderList renderList, Matrix4f matrix, Rectanglef scissor) {
            matrix = new Matrix4f(matrix);
            scissor = new Rectanglef(scissor);
            final Layers stencilMaskLayers = new Layers();
            StencilStack.Mode stencilMaskMode = null;
            int blurStrength = 0;
            for (ModifierCommand modifierCommand : renderList.modifiers()) {
                switch (modifierCommand) {
                    case ModifierCommand.Translate translateCommand -> matrix.translate(translateCommand.x(), translateCommand.y(), 0F);
                    case ModifierCommand.ComponentBounds _ -> {
                    }
                    case ModifierCommand.Scissor scissorCommand -> {
                        final Rectangle scissorRect = new Rectangle(scissorCommand.x(), scissorCommand.y(), scissorCommand.width(), scissorCommand.height());
                        scissor.intersection(MathUtil.transform(MathUtil.convert(scissorRect), matrix));
                    }
                    case ModifierCommand.Scale scaleCommand -> matrix.scaleXY(scaleCommand.x(), scaleCommand.y());
                    case ThinGLModifierCommand thinGlModifierCommand -> {
                        switch (thinGlModifierCommand) {
                            case ThinGLModifierCommand.Blur blur -> blurStrength += blur.strength();
                        }
                    }
                    case ModifierCommand.Stencil stencil -> {
                        stencilMaskLayers.addRenderList(stencil.mask(), matrix, scissor);
                        stencilMaskMode = stencil.inverse() ? StencilStack.Mode.NOT_EQUAL : StencilStack.Mode.EQUAL_INTERSECTION;
                    }
                    case ModifierCommand.Custom _ -> throw new UnsupportedOperationException("Custom modifier commands are not supported in BatchedThinGLRenderer");
                }
            }
            for (RenderElement renderElement : renderList.elements()) {
                switch (renderElement) {
                    case RenderCommand renderCommand -> this.addRenderCommand(renderCommand, matrix, scissor, stencilMaskLayers, stencilMaskMode, blurStrength);
                    case RenderList subRenderList -> this.addRenderList(subRenderList, matrix, scissor);
                }
            }
        }

        public void addRenderCommand(final RenderCommand renderCommand, final Matrix4f matrix, final Rectanglef scissor, final Layers stencilMaskLayers, final StencilStack.Mode stencilMaskMode, final int blurStrength) {
            final Rectanglef bounds = MathUtil.transform(MathUtil.convert(renderCommand.bounds()), matrix);
            int insertionIndex = this.size();
            if (!(renderCommand instanceof RenderCommand.Custom)) {
                for (int i = this.size() - 1; i >= 0; i--) {
                    final Layer layer = this.get(i);
                    if (layer.isCompatible(bounds, scissor, stencilMaskLayers, stencilMaskMode, blurStrength)) {
                        insertionIndex = i;
                    } else if (layer.intersectsRectangle(bounds)) {
                        if (insertionIndex <= i) {
                            insertionIndex = i + 1;
                        }
                        break;
                    }
                }
            } else {
                bounds.set(FULL_SIZE_RECT);
            }
            if (insertionIndex >= this.size()) {
                this.add(new Layer(scissor, stencilMaskMode, blurStrength));
            }
            final Layer layer = this.get(insertionIndex);
            layer.addCommandState(new Layer.CommandState(bounds, matrix, renderCommand));
            layer.addStencilMaskLayers(stencilMaskLayers);
        }

        public boolean intersectsLayers(final Layers layers) {
            for (Layer layer : this) {
                for (Layer otherLayer : layers) {
                    if (layer.intersectsLayer(otherLayer)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Rectanglef bounds() {
            final Rectanglef bounds = new Rectanglef(EMPTY_RECT);
            for (Layer layer : this) {
                bounds.union(layer.bounds);
            }
            return bounds;
        }

    }

    public static class Layer {

        private final List<CommandState> commandStates = new ArrayList<>();
        private final Rectanglef bounds = new Rectanglef(EMPTY_RECT);
        private final Rectanglef scissor;
        private final Layers stencilMaskLayers = new Layers();
        private final ReferenceSet<Layers> addedStencilMaskLayers = new ReferenceOpenHashSet<>();
        private final StencilStack.Mode stencilMaskMode;
        private final int blurStrength;

        private Layer(final Rectanglef scissor, final StencilStack.Mode stencilMaskMode, final int blurStrength) {
            this.scissor = scissor;
            this.stencilMaskMode = stencilMaskMode;
            this.blurStrength = blurStrength;
        }

        private boolean isCompatible(final Rectanglef renderBounds, final Rectanglef scissor, final Layers stencilMaskLayers, final StencilStack.Mode stencilMaskMode, final int blurStrength) {
            if (this.intersectsRectangle(renderBounds)) {
                return false;
            }
            if (!this.scissor.equals(scissor)) {
                return false;
            }
            if (this.blurStrength != blurStrength) {
                return false;
            }
            if (this.stencilMaskMode != stencilMaskMode) {
                return false;
            }
            if (this.stencilMaskLayers.isEmpty() != stencilMaskLayers.isEmpty()) {
                return false;
            }
            if (!this.addedStencilMaskLayers.contains(stencilMaskLayers) && (stencilMaskLayers.intersectsLayers(this.stencilMaskLayers) || renderBounds.intersectsRectangle(this.stencilMaskLayers.bounds()) || this.bounds.intersectsRectangle(stencilMaskLayers.bounds()))) {
                return false;
            }
            return true;
        }

        private boolean intersectsRectangle(final Rectanglef bounds) {
            if (this.bounds.intersectsRectangle(bounds)) {
                for (CommandState commandState : this.commandStates) {
                    if (commandState.bounds.intersectsRectangle(bounds)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean intersectsLayer(final Layer otherLayer) {
            if (this.bounds.intersectsRectangle(otherLayer.bounds)) {
                for (CommandState commandState : this.commandStates) {
                    for (CommandState otherCommandState : otherLayer.commandStates) {
                        if (commandState.bounds.intersectsRectangle(otherCommandState.bounds)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private void addCommandState(final CommandState commandState) {
            this.commandStates.add(commandState);
            this.bounds.union(commandState.bounds);
        }

        private void addStencilMaskLayers(final Layers layers) {
            if (!layers.isEmpty() && this.addedStencilMaskLayers.add(layers)) {
                for (Layer layer : layers) {
                    for (CommandState commandState : layer.commandStates()) {
                        this.stencilMaskLayers.addRenderCommand(commandState.renderCommand(), commandState.matrix(), layer.scissor, layer.stencilMaskLayers, layer.stencilMaskMode, 0);
                    }
                }
            }
        }

        public List<CommandState> commandStates() {
            return Collections.unmodifiableList(this.commandStates);
        }

        public record CommandState(Rectanglef bounds, Matrix4f matrix, RenderCommand renderCommand) {
        }

    }

}
