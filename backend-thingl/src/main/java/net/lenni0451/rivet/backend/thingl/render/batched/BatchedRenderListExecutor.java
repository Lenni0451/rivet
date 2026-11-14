package net.lenni0451.rivet.backend.thingl.render.batched;

import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.render.batched.BatchedRenderListBuilder;
import net.lenni0451.rivet.backend.render.batched.StencilMode;
import net.lenni0451.rivet.backend.render.deferred.RenderList;
import net.lenni0451.rivet.backend.render.deferred.RenderListExecutor;
import net.lenni0451.rivet.backend.thingl.render.ThinGLModifierCommand;
import net.lenni0451.rivet.backend.thingl.render.ThinGLRenderer;
import net.lenni0451.rivet.backend.thingl.util.MathUtil;
import net.lenni0451.rivet.math.Aabb;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.rendering.dataholder.ImmediateMultiDrawBatchDataHolder;
import net.raphimc.thingl.gl.wrapper.StencilStack;
import net.raphimc.thingl.rendering.dataholder.MultiDrawBatchDataHolder;
import org.lwjgl.opengl.GL43C;

import java.util.List;

public class BatchedRenderListExecutor extends RenderListExecutor {

    public static final BatchedRenderListExecutor INSTANCE = new BatchedRenderListExecutor();
    private static final BatchedRenderListBuilder.CustomModifierHandler CUSTOM_MODIFIER_HANDLER = (command, state) -> {
        if (command instanceof ThinGLModifierCommand.Blur blur) {
            return blurStrength(state) + blur.strength();
        }
        return BatchedRenderListBuilder.CustomModifierHandler.UNSUPPORTED.apply(command, state);
    };

    @Override
    public void renderList(final Renderer renderer, final RenderList renderList) {
        this.renderLayers((ThinGLRenderer) renderer, BatchedRenderListBuilder.build(renderList, CUSTOM_MODIFIER_HANDLER));
    }

    public void renderLayers(final ThinGLRenderer renderer, final List<BatchedRenderListBuilder.Layer> layers) {
        final MultiDrawBatchDataHolder multiDrawBatchDataHolder = new ImmediateMultiDrawBatchDataHolder();
        final MultiDrawBatchDataHolder previousMultiDrawBatchDataHolder2D = ThinGL.renderer2D().getTargetMultiDrawBatchDataHolder();
        final MultiDrawBatchDataHolder previousMultiDrawBatchDataHolderText = ThinGL.rendererText().getTargetMultiDrawBatchDataHolder();
        try {
            ThinGL.renderer2D().beginBuffering(multiDrawBatchDataHolder);
            ThinGL.rendererText().beginBuffering(multiDrawBatchDataHolder);
            GL43C.glPushDebugGroup(GL43C.GL_DEBUG_SOURCE_APPLICATION, 0, "Rivet");
            for (int i = 0; i < layers.size(); i++) {
                GL43C.glPushDebugGroup(GL43C.GL_DEBUG_SOURCE_APPLICATION, 0, "Layer " + i);
                final BatchedRenderListBuilder.Layer layer = layers.get(i);
                final int blurStrength = blurStrength(layer.customState());
                if (blurStrength > 0) {
                    ThinGL.programs().getGaussianBlur().bindInput();
                }
                if (layer.stencilMode() != null) {
                    ThinGL.stencilStack().push(toThinGL(layer.stencilMode()));
                    this.renderLayers(renderer, layer.stencilMaskLayers());
                    ThinGL.stencilStack().set();
                }
                final Aabb scissor = layer.scissor();
                if (!scissor.isFull()) {
                    ThinGL.scissorStack().pushIntersection(renderer.positionMatrix(), MathUtils.floorInt(scissor.minX()), MathUtils.floorInt(scissor.minY()), MathUtils.ceilInt(scissor.maxX()), MathUtils.ceilInt(scissor.maxY()));
                }
                for (BatchedRenderListBuilder.CommandState commandState : layer.commands()) {
                    renderer.positionMatrix().pushMatrix();
                    renderer.positionMatrix().mul(MathUtil.toMatrix(commandState.matrix()));
                    this.renderCommand(renderer, commandState.command());
                    renderer.positionMatrix().popMatrix();
                }
                multiDrawBatchDataHolder.draw();
                if (!scissor.isFull()) {
                    ThinGL.scissorStack().pop();
                }
                if (layer.stencilMode() != null) {
                    ThinGL.stencilStack().pop();
                }
                if (blurStrength > 0) {
                    final Aabb bounds = layer.coarseBounds();
                    ThinGL.programs().getGaussianBlur().unbindInput();
                    ThinGL.programs().getGaussianBlur().configureParameters(blurStrength);
                    ThinGL.programs().getGaussianBlur().render(renderer.positionMatrix(), bounds.minX(), bounds.minY(), bounds.maxX(), bounds.maxY());
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

    private static int blurStrength(final Object customState) {
        return customState instanceof Integer strength ? strength : 0;
    }

    private static StencilStack.Mode toThinGL(final StencilMode mode) {
        return switch (mode) {
            case EQUAL_INTERSECTION -> StencilStack.Mode.EQUAL_INTERSECTION;
            case NOT_EQUAL -> StencilStack.Mode.NOT_EQUAL;
        };
    }

}
