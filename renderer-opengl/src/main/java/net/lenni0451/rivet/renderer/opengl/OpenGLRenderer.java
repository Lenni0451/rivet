package net.lenni0451.rivet.renderer.opengl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.renderer.Renderer;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.ImmediateMultiDrawBatchDataHolder;
import net.raphimc.thingl.drawbuilder.drawbatchdataholder.MultiDrawBatchDataHolder;
import org.joml.Matrix4f;

public class OpenGLRenderer implements Renderer {

    private final MultiDrawBatchDataHolder immediateMultiDrawBatchDataHolder = new ImmediateMultiDrawBatchDataHolder();
    private MultiDrawBatchDataHolder targetMultiDrawBatchDataHolder;

    @Override
    public void filledRectangle(final Matrix4f positionMatrix, final float x, final float y, final float width, final float height, final Color color) {
        if (this.targetMultiDrawBatchDataHolder != null) {
            ThinGL.renderer2D().beginBuffering(this.targetMultiDrawBatchDataHolder);
        }
        ThinGL.renderer2D().filledRectangle(positionMatrix, x, y, width, height, color);
        if (this.targetMultiDrawBatchDataHolder != null) {
            ThinGL.renderer2D().endBuffering();
        }
    }

    @Override
    public void beginBatch() {
        this.targetMultiDrawBatchDataHolder = this.immediateMultiDrawBatchDataHolder;
    }

    @Override
    public void endBatch() {
        this.immediateMultiDrawBatchDataHolder.draw();
        this.targetMultiDrawBatchDataHolder = null;
    }

}
