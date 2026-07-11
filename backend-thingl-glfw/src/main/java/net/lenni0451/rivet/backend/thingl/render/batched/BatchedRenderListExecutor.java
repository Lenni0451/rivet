package net.lenni0451.rivet.backend.thingl.render.batched;

import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.render.deferred.RenderList;
import net.lenni0451.rivet.backend.render.deferred.RenderListExecutor;
import net.lenni0451.rivet.backend.thingl.render.ThinGLRenderer;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.rendering.dataholder.ImmediateMultiDrawBatchDataHolder;
import net.raphimc.thingl.rendering.dataholder.MultiDrawBatchDataHolder;
import org.lwjgl.opengl.GL43C;

import java.util.List;

public class BatchedRenderListExecutor extends RenderListExecutor {

    public static final BatchedRenderListExecutor INSTANCE = new BatchedRenderListExecutor();

    @Override
    public void renderList(final Renderer renderer, final RenderList renderList) {
        this.renderLayers((ThinGLRenderer) renderer, BatchedRenderListBuilder.buildLayers(renderList));
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
                if (layer.blurStrength() > 0) {
                    ThinGL.programs().getGaussianBlur().bindInput();
                }
                if (layer.stencilMaskMode() != null) {
                    ThinGL.stencilStack().push(layer.stencilMaskMode());
                    this.renderLayers(renderer, layer.stencilMaskLayers());
                    ThinGL.stencilStack().set();
                }
                if (!layer.scissor().equals(BatchedRenderListBuilder.FULL_SIZE_RECT)) {
                    ThinGL.scissorStack().pushIntersection(renderer.positionMatrix(), MathUtils.floorInt(layer.scissor().minX), MathUtils.floorInt(layer.scissor().minY), MathUtils.ceilInt(layer.scissor().maxX), MathUtils.ceilInt(layer.scissor().maxY));
                }
                for (BatchedRenderListBuilder.Layer.CommandState commandState : layer.commandStates()) {
                    renderer.positionMatrix().pushMatrix();
                    renderer.positionMatrix().mul(commandState.matrix());
                    this.renderCommand(renderer, commandState.renderCommand());
                    renderer.positionMatrix().popMatrix();
                }
                multiDrawBatchDataHolder.draw();
                if (!layer.scissor().equals(BatchedRenderListBuilder.FULL_SIZE_RECT)) {
                    ThinGL.scissorStack().pop();
                }
                if (layer.stencilMaskMode() != null) {
                    ThinGL.stencilStack().pop();
                }
                if (layer.blurStrength() > 0) {
                    ThinGL.programs().getGaussianBlur().unbindInput();
                    ThinGL.programs().getGaussianBlur().configureParameters(layer.blurStrength());
                    ThinGL.programs().getGaussianBlur().render(renderer.positionMatrix(), layer.bounds().minX, layer.bounds().minY, layer.bounds().maxX, layer.bounds().maxY);
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

}
