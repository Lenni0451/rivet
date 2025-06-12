package net.lenni0451.rivet.renderer.awt;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.renderer.Renderer;
import org.joml.Matrix4f;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.function.Supplier;

public class Graphics2DRenderer implements Renderer {

    private final Supplier<Graphics2D> g2dSupplier;

    public Graphics2DRenderer(final Graphics2D g2d) {
        this.g2dSupplier = () -> g2d;
    }

    public Graphics2DRenderer(final Supplier<Graphics2D> g2d) {
        this.g2dSupplier = g2d;
    }

    @Override
    public void filledRectangle(Matrix4f positionMatrix, float x, float y, float width, float height, Color color) {
        Graphics2D g2d = this.g2dSupplier.get();
        this.applyColor(g2d, color);
        this.applyTransform(g2d, positionMatrix);
        g2d.fill(new Rectangle2D.Float(x, y, width, height));
    }

    @Override
    public void beginBatch() {
    }

    @Override
    public void endBatch() {
    }

    private void applyColor(final Graphics2D g2d, final Color color) {
        g2d.setColor(new java.awt.Color(color.toARGB(), true));
    }

    private void applyTransform(final Graphics2D g2d, final Matrix4f positionMatrix) {
        g2d.setTransform(new AffineTransform(
                positionMatrix.m00(), positionMatrix.m10(),
                positionMatrix.m10(), positionMatrix.m11(),
                positionMatrix.m30(), positionMatrix.m31()
        ));
    }

}
