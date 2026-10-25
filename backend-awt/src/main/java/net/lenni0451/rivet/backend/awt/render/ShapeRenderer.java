package net.lenni0451.rivet.backend.awt.render;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = true, makeFinal = true)
public class ShapeRenderer extends AbstractAWTRenderer {

    @Getter
    private Area shape;
    @Getter
    private AffineTransform transform;

    public ShapeRenderer() {
        this(new Area(), new AffineTransform());
    }

    public ShapeRenderer(final AffineTransform transform) {
        this(new Area(), transform);
    }

    public ShapeRenderer add(final Shape shape) {
        Shape transformed = this.transform.isIdentity() ? shape : this.transform.createTransformedShape(shape);
        this.shape.add(transformed instanceof Area area ? area : new Area(transformed));
        return this;
    }

    @Override
    public void doTranslate(final float x, final float y, final Runnable renderer) {
        AffineTransform previous = new AffineTransform(this.transform);
        this.transform.translate(x, y);
        renderer.run();
        this.transform = previous;
    }

    @Override
    public void doScissor(final float x, final float y, final float width, final float height, final Runnable renderer) {
        Shape bounds = new Rectangle2D.Float(x, y, width, height);
        Shape transformedBounds = this.transform.isIdentity() ? bounds : this.transform.createTransformedShape(bounds);
        Area boundsArea = transformedBounds instanceof Area area ? area : new Area(transformedBounds);

        Area previousShape = this.shape;
        this.shape = new Area();
        renderer.run();
        this.shape.intersect(boundsArea);
        previousShape.add(this.shape);
        this.shape = previousShape;
    }

    @Override
    public void doScale(final float x, final float y, final Runnable renderer) {
        AffineTransform previous = new AffineTransform(this.transform);
        this.transform.scale(x, y);
        renderer.run();
        this.transform = previous;
    }

    @Override
    public void doRotate(final float degrees, final Runnable renderer) {
        AffineTransform previous = new AffineTransform(this.transform);
        this.transform.rotate(Math.toRadians(degrees));
        renderer.run();
        this.transform = previous;
    }

    @Override
    public void doStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        ShapeRenderer maskShapeRenderer = new ShapeRenderer(new AffineTransform(this.transform));
        maskRenderer.accept(maskShapeRenderer);
        Area maskArea = maskShapeRenderer.shape();

        Area previousShape = this.shape;
        this.shape = new Area();
        renderer.run();
        this.shape.intersect(maskArea);
        previousShape.add(this.shape);
        this.shape = previousShape;
    }

    @Override
    public void doInverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        ShapeRenderer maskShapeRenderer = new ShapeRenderer(new AffineTransform(this.transform));
        maskRenderer.accept(maskShapeRenderer);
        Area maskArea = maskShapeRenderer.shape();

        Area previousShape = this.shape;
        this.shape = new Area();
        renderer.run();
        this.shape.subtract(maskArea);
        previousShape.add(this.shape);
        this.shape = previousShape;
    }

    @Override
    public void custom(final ModifierCommand.Custom command, final Runnable renderer) {
        throw new UnsupportedOperationException("Custom modifier commands are not supported in ShapeRenderer");
    }

    @Override
    public void doFillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr) {
        this.add(new Rectangle2D.Float(x, y, width, height));
    }

    @Override
    public void doImage(final Texture texture, final float x, final float y, final float width, final float height, final Color color) {
        this.add(new Rectangle2D.Float(x, y, width, height));
    }

    @Override
    public void custom(final RenderCommand.Custom renderCommand) {
        throw new UnsupportedOperationException("Custom render commands are not supported in ShapeRenderer");
    }

    @Override
    protected void fillShape(final Shape shape, final Color color) {
        this.add(shape);
    }

    @Override
    protected void strokeShape(final Shape shape, final Stroke stroke, final Color color) {
        this.add(stroke.createStrokedShape(shape));
    }

    @Override
    protected void drawGlyphs(final GlyphVector glyphVector, final float x, final float y, final Color color) {
        this.add(glyphVector.getOutline(x, y));
    }

    @Override
    protected void strokeTextOutline(final Shape outlineShape, final Stroke stroke, final float x, final float y, final Color color) {
        AffineTransform outlineTransform = AffineTransform.getTranslateInstance(x, y);
        this.add(outlineTransform.createTransformedShape(stroke.createStrokedShape(outlineShape)));
    }

}
