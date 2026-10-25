package net.lenni0451.rivet.backend.awt.render;

import lombok.RequiredArgsConstructor;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.awt.text.AWTShapedText;
import net.lenni0451.rivet.backend.awt.texture.AWTTexture;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RescaleOp;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class AWTRenderer extends AbstractAWTRenderer {

    private static final float INVERSE_CLIP_SIZE = 100_000_000F;

    private final Graphics2D graphics;

    @Override
    public void doTranslate(final float x, final float y, final Runnable renderer) {
        AffineTransform previous = new AffineTransform(this.graphics.getTransform());
        this.graphics.translate(x, y);
        renderer.run();
        this.graphics.setTransform(previous);
    }

    @Override
    public void doScissor(final float x, final float y, final float width, final float height, final Runnable renderer) {
        Rectangle2D bounds = new Rectangle2D.Float(x, y, width, height);
        this.clip(bounds, renderer);
    }

    @Override
    public void doScale(final float x, final float y, final Runnable renderer) {
        AffineTransform previous = new AffineTransform(this.graphics.getTransform());
        this.graphics.scale(x, y);
        renderer.run();
        this.graphics.setTransform(previous);
    }

    @Override
    public void doRotate(final float degrees, final Runnable renderer) {
        AffineTransform previous = new AffineTransform(this.graphics.getTransform());
        this.graphics.rotate(Math.toRadians(degrees));
        renderer.run();
        this.graphics.setTransform(previous);
    }

    @Override
    public void doStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        maskRenderer.accept(shapeRenderer);
        this.clip(shapeRenderer.shape(), renderer);
    }

    @Override
    public void doInverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        maskRenderer.accept(shapeRenderer);
        this.inverseClip(shapeRenderer.shape(), renderer);
    }

    @Override
    public void custom(final ModifierCommand.Custom command, final Runnable renderer) {
    }

    private void clip(final Shape shape, final Runnable renderer) {
        Shape clip = this.graphics.getClip();
        if (clip == null) {
            this.graphics.setClip(shape);
        } else {
            Area clipArea = new Area(clip);
            clipArea.intersect(shape instanceof Area area ? area : new Area(shape));
            this.graphics.setClip(clipArea);
        }
        renderer.run();
        this.graphics.setClip(clip);
    }

    private void inverseClip(final Shape shape, final Runnable renderer) {
        Shape clip = this.graphics.getClip();
        Area clipArea;
        if (clip == null) {
            clipArea = new Area(new Rectangle2D.Float(-INVERSE_CLIP_SIZE, -INVERSE_CLIP_SIZE, INVERSE_CLIP_SIZE, INVERSE_CLIP_SIZE));
        } else {
            clipArea = new Area(clip);
        }
        clipArea.subtract(shape instanceof Area area ? area : new Area(shape));
        this.graphics.setClip(clipArea);
        renderer.run();
        this.graphics.setClip(clip);
    }

    @Override
    public void doFillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr) {
        if (ctl.equals(ctr) && cbl.equals(cbr)) {
            this.graphics.setPaint(new GradientPaint(x, y, ctl.toAWT(), x, y + height, cbl.toAWT()));
            this.graphics.fill(new Rectangle2D.Float(x, y, width, height));
        } else if (ctl.equals(cbl) && ctr.equals(cbr)) {
            this.graphics.setPaint(new GradientPaint(x, y, ctl.toAWT(), x + width, y, ctr.toAWT()));
            this.graphics.fill(new Rectangle2D.Float(x, y, width, height));
        } else {
            this.graphics.setPaint(new GradientPaint(x, y, ctl.toAWT(), x + width, y, ctr.toAWT()));
            this.graphics.fill(new Rectangle2D.Float(x, y, width, height));
            this.graphics.setPaint(new GradientPaint(x, y, Color.TRANSPARENT.toAWT(), x, y + height, cbl.toAWT()));
            this.graphics.fill(new Rectangle2D.Float(x, y, width, height));
        }
    }

    @Override
    protected void renderShapedText(final AWTShapedText shapedText, final float originX, final float originY) {
        Stroke previousStroke = this.graphics.getStroke();
        java.awt.Color previousColor = this.graphics.getColor();
        super.renderShapedText(shapedText, originX, originY);
        this.graphics.setStroke(previousStroke);
        this.graphics.setColor(previousColor);
    }

    @Override
    public void doImage(final Texture texture, final float x, final float y, final float width, final float height, final Color color) {
        if (texture instanceof AWTTexture awtTexture) {
            float scaleX = width / awtTexture.width();
            float scaleY = height / awtTexture.height();

            AffineTransform transform = new AffineTransform();
            transform.translate(x, y);
            transform.scale(scaleX, scaleY);
            if (color.equals(Color.WHITE)) {
                this.graphics.drawImage(awtTexture.image(), transform, null);
            } else if (color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255) {
                Composite previousComposite = this.graphics.getComposite();
                if (previousComposite instanceof AlphaComposite alphaComposite) {
                    this.graphics.setComposite(alphaComposite.derive(alphaComposite.getAlpha() * color.getAlphaF()));
                } else {
                    this.graphics.setComposite(AlphaComposite.SrcOver.derive(color.getAlphaF()));
                }
                this.graphics.drawImage(awtTexture.image(), transform, null);
                this.graphics.setComposite(previousComposite);
            } else {
                BufferedImage image = awtTexture.image();
                BufferedImage tintedImage = this.applyColor(image, color);
                this.graphics.drawImage(tintedImage, transform, null);
            }
        } else {
            throw new UnsupportedOperationException(texture.getClass().getName());
        }
    }

    private BufferedImage applyColor(final BufferedImage image, final Color color) {
        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        float a = color.getAlphaF();

        if (!(image.getColorModel() instanceof IndexColorModel)) {
            int bandCount = image.getRaster().getNumBands();
            if (bandCount >= 4) {
                RescaleOp rescaleOp = new RescaleOp(new float[]{r, g, b, a}, new float[]{0F, 0F, 0F, 0F}, null);
                return rescaleOp.filter(image, null);
            } else if (bandCount == 3 && a == 1F) {
                RescaleOp rescaleOp = new RescaleOp(new float[]{r, g, b}, new float[]{0F, 0F, 0F, 0F}, null);
                return rescaleOp.filter(image, null);
            }
        }

        BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = converted.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        RescaleOp rescaleOp = new RescaleOp(new float[]{r, g, b, a}, new float[]{0F, 0F, 0F, 0F}, null);
        return rescaleOp.filter(converted, null);
    }

    @Override
    public void custom(final RenderCommand.Custom renderCommand) {
    }

    @Override
    protected void fillShape(final Shape shape, final Color color) {
        this.graphics.setColor(color.toAWT());
        this.graphics.fill(shape);
    }

    @Override
    protected void strokeShape(final Shape shape, final Stroke stroke, final Color color) {
        this.graphics.setStroke(stroke);
        this.graphics.setColor(color.toAWT());
        this.graphics.draw(shape);
    }

    @Override
    protected void drawGlyphs(final GlyphVector glyphVector, final float x, final float y, final Color color) {
        this.graphics.setColor(color.toAWT());
        this.graphics.drawGlyphVector(glyphVector, x, y);
    }

    @Override
    protected void strokeTextOutline(final Shape outlineShape, final Stroke stroke, final float x, final float y, final Color color) {
        this.graphics.setColor(color.toAWT());
        this.graphics.setStroke(stroke);
        this.graphics.translate(x, y);
        this.graphics.draw(outlineShape);
        this.graphics.translate(-x, -y);
    }

}
