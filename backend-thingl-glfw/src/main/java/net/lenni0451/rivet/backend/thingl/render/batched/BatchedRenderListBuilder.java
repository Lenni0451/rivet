package net.lenni0451.rivet.backend.thingl.render.batched;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderElement;
import net.lenni0451.rivet.backend.render.deferred.RenderList;
import net.lenni0451.rivet.backend.thingl.render.ThinGLModifierCommand;
import net.lenni0451.rivet.backend.thingl.util.MathUtil;
import net.lenni0451.rivet.math.Rectangle;
import net.raphimc.thingl.gl.wrapper.StencilStack;
import net.raphimc.thingl.util.RenderMathUtil;
import org.joml.Matrix4f;
import org.joml.primitives.Rectanglef;

import java.util.ArrayList;
import java.util.List;

public class BatchedRenderListBuilder {

    private static final Rectanglef EMPTY_RECT = new Rectanglef(Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    public static final Rectanglef FULL_SIZE_RECT = new Rectanglef(-Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

    public static Layers buildLayers(final RenderList renderList) {
        final Layers layers = new Layers();
        layers.addRenderList(renderList, RenderMathUtil.getIdentityMatrix(), FULL_SIZE_RECT);
        return layers;
    }

    public static class Layers extends ArrayList<Layer> {

        public void addRenderList(final RenderList renderList, Matrix4f matrix, Rectanglef scissor) {
            matrix = new Matrix4f(matrix);
            scissor = new Rectanglef(scissor);
            StencilStack.Mode stencilMaskMode = null;
            final Layers stencilMaskLayers = new Layers();
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
                            case ThinGLModifierCommand.Blur blurCommand -> blurStrength += blurCommand.strength();
                        }
                    }
                    case ModifierCommand.Stencil stencilCommand -> {
                        stencilMaskMode = stencilCommand.inverse() ? StencilStack.Mode.NOT_EQUAL : StencilStack.Mode.EQUAL_INTERSECTION;
                        stencilMaskLayers.addRenderList(stencilCommand.mask(), matrix, scissor);
                    }
                    case ModifierCommand.Custom _ -> throw new UnsupportedOperationException("Custom modifier commands are not supported in BatchedThinGLRenderer");
                }
            }
            for (RenderElement renderElement : renderList.elements()) {
                switch (renderElement) {
                    case RenderCommand renderCommand -> this.addRenderCommand(renderCommand, matrix, scissor, stencilMaskMode, stencilMaskLayers, blurStrength);
                    case RenderList subRenderList -> this.addRenderList(subRenderList, matrix, scissor);
                }
            }
        }

        public void addRenderCommand(final RenderCommand renderCommand, final Matrix4f matrix, final Rectanglef scissor, final StencilStack.Mode stencilMaskMode, final Layers stencilMaskLayers, final int blurStrength) {
            final Rectanglef bounds = MathUtil.transform(MathUtil.convert(renderCommand.bounds()), matrix);
            int insertionIndex = this.size();
            for (int i = this.size() - 1; i >= 0; i--) {
                final Layer layer = this.get(i);
                if (layer.isCompatible(bounds, scissor, stencilMaskMode, stencilMaskLayers, blurStrength)) {
                    insertionIndex = i;
                } else if (layer.intersectsRectangle(bounds)) {
                    if (insertionIndex <= i) {
                        insertionIndex = i + 1;
                    }
                    break;
                }
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
        private final StencilStack.Mode stencilMaskMode;
        private final Layers stencilMaskLayers = new Layers();
        private final ReferenceSet<Layers> addedStencilMaskLayers = new ReferenceOpenHashSet<>();
        private final int blurStrength;

        public Layer(final Rectanglef scissor, final StencilStack.Mode stencilMaskMode, final int blurStrength) {
            this.scissor = scissor;
            this.stencilMaskMode = stencilMaskMode;
            this.blurStrength = blurStrength;
        }

        public boolean isCompatible(final Rectanglef renderBounds, final Rectanglef scissor, final StencilStack.Mode stencilMaskMode, final Layers stencilMaskLayers, final int blurStrength) {
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

        public boolean intersectsRectangle(final Rectanglef bounds) {
            if (this.bounds.intersectsRectangle(bounds)) {
                for (CommandState commandState : this.commandStates) {
                    if (commandState.bounds.intersectsRectangle(bounds)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean intersectsLayer(final Layer otherLayer) {
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

        public void addCommandState(final CommandState commandState) {
            this.commandStates.add(commandState);
            this.bounds.union(commandState.bounds);
        }

        public void addStencilMaskLayers(final Layers layers) {
            if (!layers.isEmpty() && this.addedStencilMaskLayers.add(layers)) {
                for (Layer layer : layers) {
                    for (CommandState commandState : layer.commandStates) {
                        this.stencilMaskLayers.addRenderCommand(commandState.renderCommand(), commandState.matrix(), layer.scissor, layer.stencilMaskMode, layer.stencilMaskLayers, 0);
                    }
                }
            }
        }

        public List<CommandState> commandStates() {
            return this.commandStates;
        }

        public Rectanglef bounds() {
            return this.bounds;
        }

        public Rectanglef scissor() {
            return this.scissor;
        }

        public StencilStack.Mode stencilMaskMode() {
            return this.stencilMaskMode;
        }

        public Layers stencilMaskLayers() {
            return this.stencilMaskLayers;
        }

        public int blurStrength() {
            return this.blurStrength;
        }

        public record CommandState(Rectanglef bounds, Matrix4f matrix, RenderCommand renderCommand) {
        }

    }

}
