package net.lenni0451.rivet.backend.render.batched;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderElement;
import net.lenni0451.rivet.backend.render.deferred.RenderList;
import net.lenni0451.rivet.math.Aabb;
import net.lenni0451.rivet.math.Transform2D;

import javax.annotation.Nullable;
import java.util.*;

@UtilityClass
public class BatchedRenderListBuilder {

    public static Layers build(final RenderList renderList) {
        return build(renderList, CustomModifierHandler.UNSUPPORTED);
    }

    public static Layers build(final RenderList renderList, final CustomModifierHandler customModifierHandler) {
        final Layers layers = new Layers(customModifierHandler);
        layers.addRenderList(renderList, Transform2D.IDENTITY, Aabb.FULL, null, new Layers(customModifierHandler), null);
        return layers;
    }


    @FunctionalInterface
    public interface CustomModifierHandler {
        CustomModifierHandler UNSUPPORTED = (command, state) -> {
            throw new UnsupportedOperationException("Unsupported custom modifier command: " + command.getClass().getName());
        };

        @Nullable
        Object apply(final ModifierCommand.Custom command, @Nullable final Object state);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Layers extends ArrayList<Layer> {
        private final CustomModifierHandler customModifierHandler;

        public Aabb coarseBounds() {
            Aabb coarseBounds = Aabb.EMPTY;
            for (Layer layer : this) {
                coarseBounds = coarseBounds.union(layer.coarseBounds);
            }
            return coarseBounds;
        }

        private void addRenderList(final RenderList renderList, final Transform2D parentMatrix, final Aabb parentScissor, @Nullable final StencilMode parentStencilMode, final Layers parentStencilMaskLayers, @Nullable final Object parentCustomState) {
            Transform2D matrix = parentMatrix;
            Aabb scissor = parentScissor;
            StencilMode stencilMode = parentStencilMode;
            Layers stencilMaskLayers = parentStencilMaskLayers;
            Object customState = parentCustomState;
            for (ModifierCommand modifierCommand : renderList.modifiers()) {
                if (modifierCommand instanceof ModifierCommand.Translate translate) {
                    matrix = matrix.translate(translate.x(), translate.y());
                } else if (modifierCommand instanceof ModifierCommand.Scissor scissorCommand) {
                    scissor = scissor.intersect(matrix.transformBounds(
                            scissorCommand.x(), scissorCommand.y(),
                            scissorCommand.x() + scissorCommand.width(), scissorCommand.y() + scissorCommand.height()
                    ));
                } else if (modifierCommand instanceof ModifierCommand.Scale scale) {
                    matrix = matrix.scale(scale.x(), scale.y());
                } else if (modifierCommand instanceof ModifierCommand.Rotate rotate) {
                    matrix = matrix.rotateDegrees(rotate.degrees());
                } else if (modifierCommand instanceof ModifierCommand.Stencil stencil) {
                    stencilMode = stencil.inverse() ? StencilMode.NOT_EQUAL : StencilMode.EQUAL_INTERSECTION;
                    stencilMaskLayers = new Layers(this.customModifierHandler);
                    stencilMaskLayers.addRenderList(stencil.mask(), matrix, scissor, null, new Layers(this.customModifierHandler), null);
                } else if (modifierCommand instanceof ModifierCommand.Custom custom) {
                    customState = this.customModifierHandler.apply(custom, customState);
                }
                // Component bounds are not enforced to allow better batching
            }
            for (RenderElement renderElement : renderList.elements()) {
                if (renderElement instanceof RenderCommand renderCommand) {
                    this.addRenderCommand(renderCommand, matrix, scissor, stencilMode, stencilMaskLayers, customState);
                } else if (renderElement instanceof RenderList subRenderList) {
                    this.addRenderList(subRenderList, matrix, scissor, stencilMode, stencilMaskLayers, customState);
                }
            }
        }

        private void addRenderCommand(final RenderCommand renderCommand, final Transform2D matrix, final Aabb scissor, @Nullable final StencilMode stencilMode, final Layers stencilMaskLayers, @Nullable final Object customState) {
            if (scissor.isEmpty()) return; // Everything is clipped away
            Aabb bounds = matrix.transformBounds(renderCommand.bounds());
            int insertionIndex = this.size();
            for (int i = this.size() - 1; i >= 0; i--) {
                Layer layer = this.get(i);
                if (layer.isCompatible(bounds, scissor, stencilMode, stencilMaskLayers, customState)) {
                    insertionIndex = i;
                } else if (layer.intersects(bounds)) {
                    if (insertionIndex <= i) {
                        insertionIndex = i + 1;
                    }
                    break;
                }
            }
            if (insertionIndex >= this.size()) {
                this.add(new Layer(scissor, stencilMode, customState, new Layers(this.customModifierHandler)));
            }
            Layer layer = this.get(insertionIndex);
            layer.addCommand(new CommandState(bounds, matrix, renderCommand));
            layer.addStencilMaskLayers(stencilMaskLayers);
        }

        private boolean intersectsLayers(final Layers layers) {
            for (Layer layer : this) {
                for (Layer otherLayer : layers) {
                    if (layer.intersectsLayer(otherLayer)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Accessors(fluent = true, chain = true, makeFinal = true)
    public static final class Layer {
        private final List<CommandState> commands = new ArrayList<>();
        private Aabb coarseBounds = Aabb.EMPTY;
        private final RectangleQuadTree exactBounds = new RectangleQuadTree();
        private final Aabb scissor;
        @Nullable
        private final StencilMode stencilMode;
        @Nullable
        private final Object customState;
        private final Layers stencilMaskLayers;
        private final Set<Layers> addedStencilMaskLayers = Collections.newSetFromMap(new IdentityHashMap<>());

        private boolean isCompatible(final Aabb renderBounds, final Aabb scissor, @Nullable final StencilMode stencilMode, final Layers stencilMaskLayers, @Nullable final Object customState) {
            if (this.intersects(renderBounds)) return false;
            if (!this.scissor.equals(scissor)) return false;
            if (!Objects.equals(this.customState, customState)) return false;
            if (this.stencilMode != stencilMode) return false;
            if (this.stencilMaskLayers.isEmpty() != stencilMaskLayers.isEmpty()) return false;
            return this.addedStencilMaskLayers.contains(stencilMaskLayers)
                    || !(stencilMaskLayers.intersectsLayers(this.stencilMaskLayers)
                    || renderBounds.intersects(this.stencilMaskLayers.coarseBounds())
                    || this.coarseBounds.intersects(stencilMaskLayers.coarseBounds()));
        }

        private boolean intersects(final Aabb bounds) {
            return this.coarseBounds.intersects(bounds) && this.exactBounds.intersects(bounds);
        }

        private boolean intersectsLayer(final Layer otherLayer) {
            if (this.coarseBounds.intersects(otherLayer.coarseBounds)) {
                Layer smaller = this.commands.size() <= otherLayer.commands.size() ? this : otherLayer;
                Layer larger = smaller == this ? otherLayer : this;
                for (CommandState command : smaller.commands) {
                    if (larger.exactBounds.intersects(command.bounds)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void addCommand(final CommandState command) {
            this.commands.add(command);
            this.coarseBounds = this.coarseBounds.union(command.bounds);
            this.exactBounds.add(command.bounds);
        }

        private void addStencilMaskLayers(final Layers layers) {
            if (!layers.isEmpty() && this.addedStencilMaskLayers.add(layers)) {
                for (Layer layer : layers) {
                    for (CommandState command : layer.commands) {
                        this.stencilMaskLayers.addRenderCommand(command.command(), command.matrix(), layer.scissor, layer.stencilMode, layer.stencilMaskLayers, null);
                    }
                }
            }
        }
    }

    public record CommandState(Aabb bounds, Transform2D matrix, RenderCommand command) {
    }

}
