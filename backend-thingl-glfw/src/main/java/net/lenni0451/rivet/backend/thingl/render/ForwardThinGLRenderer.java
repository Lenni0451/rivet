package net.lenni0451.rivet.backend.thingl.render;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.backend.thingl.ThinGLTexture;
import net.lenni0451.rivet.backend.thingl.text.ThinGLShapedText;
import net.lenni0451.rivet.backend.thingl.text.ThinGLShapedTextBlock;
import net.lenni0451.rivet.backend.thingl.util.MathUtil;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.gl.renderer.impl.Renderer2D;
import net.raphimc.thingl.gl.renderer.impl.RendererText;
import net.raphimc.thingl.gl.wrapper.StencilStack;
import org.joml.Matrix4fStack;

import java.util.function.Consumer;

@Getter
@Accessors(fluent = true, chain = true)
public class ForwardThinGLRenderer implements Renderer {

    @Setter
    private Matrix4fStack positionMatrix;

    public ForwardThinGLRenderer() {
        this.positionMatrix = new Matrix4fStack(32);
    }

    public ForwardThinGLRenderer(final Matrix4fStack positionMatrix) {
        this.positionMatrix = positionMatrix;
    }

    @Override
    public float xOffset() {
        return this.positionMatrix.m30();
    }

    @Override
    public float yOffset() {
        return this.positionMatrix.m31();
    }

    @Override
    public void translate(final float x, final float y, final Runnable renderer) {
        if (x == 0 && y == 0) {
            renderer.run();
        } else {
            this.positionMatrix.pushMatrix();
            this.positionMatrix.translate(x, y, 0);
            renderer.run();
            this.positionMatrix.popMatrix();
        }
    }

    @Override
    public void componentBounds(final float x, final float y, final float width, final float height, final Runnable renderer) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height (" + width + ", " + height + ") must be non-negative");
        } else if (width > 0 && height > 0) {
            ThinGL.scissorStack().pushIntersection(this.positionMatrix, MathUtils.floorInt(x), MathUtils.floorInt(y), MathUtils.ceilInt(x + width), MathUtils.ceilInt(y + height));
            renderer.run();
            ThinGL.scissorStack().pop();
        }
    }

    @Override
    public void scissor(final float x, final float y, final float width, final float height, final Runnable renderer) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height (" + width + ", " + height + ") must be non-negative");
        } else if (width > 0 && height > 0) {
            ThinGL.scissorStack().pushIntersection(this.positionMatrix, MathUtils.floorInt(x), MathUtils.floorInt(y), MathUtils.ceilInt(x + width), MathUtils.ceilInt(y + height));
            renderer.run();
            ThinGL.scissorStack().pop();
        }
    }

    @Override
    public void scale(final float x, final float y, final Runnable renderer) {
        if (x == 1 && y == 1) {
            renderer.run();
        } else if (x != 0 && y != 0) {
            this.positionMatrix.pushMatrix();
            this.positionMatrix.scaleXY(x, y);
            renderer.run();
            this.positionMatrix.popMatrix();
        }
    }

    @Override
    public void stencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        ThinGL.stencilStack().push(StencilStack.Mode.EQUAL_INTERSECTION);
        maskRenderer.accept(this);
        ThinGL.stencilStack().set();
        renderer.run();
        ThinGL.stencilStack().pop();
    }

    @Override
    public void inverseStencil(final Consumer<Renderer> maskRenderer, final Runnable renderer) {
        ThinGL.stencilStack().push(StencilStack.Mode.NOT_EQUAL);
        maskRenderer.accept(this);
        ThinGL.stencilStack().set();
        renderer.run();
        ThinGL.stencilStack().pop();
    }

    @Override
    public void custom(final ModifierCommand.Custom command, final Runnable renderer) {
        if (command instanceof ThinGLModifierCommand thinGLModifierCommand) {
            switch (thinGLModifierCommand) {
                case ThinGLModifierCommand.Blur blur -> {
                    ThinGL.programs().getGaussianBlur().bindInput();
                    renderer.run();
                    ThinGL.programs().getGaussianBlur().unbindInput();
                    ThinGL.programs().getGaussianBlur().configureParameters(blur.strength());
                    ThinGL.programs().getGaussianBlur().renderFullscreen();
                    ThinGL.programs().getGaussianBlur().clearInput();
                }
            }
        }
    }


    @Override
    public void fillCircle(final float x, final float y, final float radius, final Color color) {
        if (radius < 0) {
            throw new IllegalArgumentException("Radius (" + radius + ") must be non-negative");
        } else if (radius > 0 && color.getAlpha() > 0) {
            ThinGL.renderer2D().filledCircle(this.positionMatrix, x, y, radius, color);
        }
    }

    @Override
    public void outlineCircle(final float x, final float y, final float radius, final float outlineWidth, final Color color) {
        if (radius < 0) {
            throw new IllegalArgumentException("Radius (" + radius + ") must be non-negative");
        } else if (outlineWidth < 0) {
            throw new IllegalArgumentException("Outline width (" + outlineWidth + ") must be non-negative");
        } else if (radius > 0 && outlineWidth > 0 && color.getAlpha() > 0) {
            ThinGL.renderer2D().outlinedCircle(this.positionMatrix, x, y, radius, color, outlineWidth, Renderer2D.OUTLINE_STYLE_INNER_BIT);
        }
    }

    @Override
    public void fillTriangle(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3, final Color color) {
        if (color.getAlpha() > 0) {
            ThinGL.renderer2D().filledTriangle(this.positionMatrix, x1, y1, x2, y2, x3, y3, color);
        }
    }

    @Override
    public void fillRect(final float x, final float y, final float width, final float height, final Color color) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height (" + width + ", " + height + ") must be non-negative");
        } else if (width > 0 && height > 0 && color.getAlpha() > 0) {
            ThinGL.renderer2D().filledRectangle(this.positionMatrix, x, y, x + width, y + height, color);
        }
    }

    @Override
    public void outlineRect(final float x, final float y, final float width, final float height, final float outlineWidth, final Color color) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height (" + width + ", " + height + ") must be non-negative");
        } else if (outlineWidth < 0) {
            throw new IllegalArgumentException("Outline width (" + outlineWidth + ") must be non-negative");
        } else if (width > 0 && height > 0 && outlineWidth > 0 && color.getAlpha() > 0) {
            ThinGL.renderer2D().outlinedRectangle(this.positionMatrix, x, y, x + width, y + height, color, outlineWidth, Renderer2D.OUTLINE_STYLE_INNER_BIT);
        }
    }

    @Override
    public void fillRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final Color color) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height (" + width + ", " + height + ") must be non-negative");
        } else if (rtl < 0 || rbl < 0 || rbr < 0 || rtr < 0) {
            throw new IllegalArgumentException("Corner radii (" + rtl + ", " + rbl + ", " + rbr + ", " + rtr + ") must be non-negative");
        } else if (width > 0 && height > 0 && color.getAlpha() > 0) {
            ThinGL.renderer2D().filledRoundedRectangle(this.positionMatrix, x, y, x + width, y + height, rbl, rbr, rtr, rtl, color);
        }
    }

    @Override
    public void outlineRoundedRect(final float x, final float y, final float width, final float height, final float rtl, final float rbl, final float rbr, final float rtr, final float outlineWidth, final Color color) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height (" + width + ", " + height + ") must be non-negative");
        } else if (rtl < 0 || rbl < 0 || rbr < 0 || rtr < 0) {
            throw new IllegalArgumentException("Corner radii (" + rtl + ", " + rbl + ", " + rbr + ", " + rtr + ") must be non-negative");
        } else if (outlineWidth < 0) {
            throw new IllegalArgumentException("Outline width (" + outlineWidth + ") must be non-negative");
        } else if (width > 0 && height > 0 && outlineWidth > 0 && color.getAlpha() > 0) {
            ThinGL.renderer2D().outlinedRoundedRectangle(this.positionMatrix, x, y, x + width, y + height, rbl, rbr, rtr, rtl, color, outlineWidth, Renderer2D.OUTLINE_STYLE_INNER_BIT);
        }
    }

    @Override
    public void fillPolygon(final Point[] points, final Color color) {
        if (points.length > 0 && points.length < 3) {
            throw new IllegalArgumentException("Polygon must have at least 3 points");
        } else if (points.length >= 3 && color.getAlpha() != 0) {
            ThinGL.renderer2D().filledPolygon(this.positionMatrix, MathUtil.convert(points), color);
        }
    }

    @Override
    public void line(final float x1, final float y1, final float x2, final float y2, final float width, final Color color) {
        if (width < 0) {
            throw new IllegalArgumentException("Line width (" + width + ") must be non-negative");
        } else if (width != 0 && color.getAlpha() != 0 && (x1 != x2 || y1 != y2)) {
            ThinGL.renderer2D().line(this.positionMatrix, x1, y1, x2, y2, width, color);
        }
    }

    @Override
    public void polyLine(final Point[] points, final float width, final Color color) {
        if (points.length == 1) {
            throw new IllegalArgumentException("Polyline must have at least 2 points");
        } else if (width < 0) {
            throw new IllegalArgumentException("Line width (" + width + ") must be non-negative");
        } else if (points.length >= 2 && color.getAlpha() != 0) {
            ThinGL.renderer2D().polyLine(this.positionMatrix, MathUtil.convert(points), width, color);
        }
    }

    @Override
    public void fillGradientRect(final float x, final float y, final float width, final float height, final Color ctl, final Color cbl, final Color cbr, final Color ctr) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height (" + width + ", " + height + ") must be non-negative");
        } else if (width != 0 && height != 0 && (ctl.getAlpha() != 0 || cbl.getAlpha() != 0 || cbr.getAlpha() != 0 || ctr.getAlpha() != 0)) {
            ThinGL.renderer2D().filledRectangle(this.positionMatrix, x, y, x + width, y + height, cbl, cbr, ctr, ctl);
        }
    }

    @Override
    public void text(final ShapedText shapedText, final float x, final float y, final TextOrigin.Horizontal horizontalOrigin, final TextOrigin.Vertical verticalOrigin) {
        if (shapedText.visualBounds().width() > 0 && shapedText.visualBounds().height() > 0) {
            float tx = x + shapedText.offset(horizontalOrigin);
            float ty = y + shapedText.offset(verticalOrigin);
            switch (shapedText) {
                case ThinGLShapedText thinGLShapedText -> ThinGL.rendererText().textLine(
                        this.positionMatrix,
                        thinGLShapedText.shapedTextLine(),
                        tx, ty,
                        RendererText.VerticalOrigin.BASELINE,
                        RendererText.HorizontalOrigin.LOGICAL_LEFT
                );
                case ThinGLShapedTextBlock thinGLShapedTextBlock -> ThinGL.rendererText().textBlock(
                        this.positionMatrix,
                        thinGLShapedTextBlock.shapedTextBlock(),
                        tx, ty,
                        RendererText.VerticalOrigin.BASELINE,
                        RendererText.HorizontalOrigin.LOGICAL_LEFT
                );
                default -> throw new UnsupportedOperationException(shapedText.getClass().getName());
            }
        }
    }

    @Override
    public void image(final Texture texture, final float x, final float y, final float width, final float height, final Color color) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height (" + width + ", " + height + ") must be non-negative");
        } else if (width != 0 && height != 0 && texture.width() > 0 && texture.height() > 0 && color.getAlpha() != 0) {
            ThinGLTexture thinGLTexture = (ThinGLTexture) texture;
            if (color.equals(Color.WHITE)) {
                ThinGL.renderer2D().texture(this.positionMatrix, thinGLTexture.texture(), x, y, width, height, thinGLTexture.view().minX, thinGLTexture.view().minY, thinGLTexture.view().lengthX(), thinGLTexture.view().lengthY());
            } else {
                ThinGL.renderer2D().coloredTexture(this.positionMatrix, thinGLTexture.texture(), x, y, width, height, thinGLTexture.view().minX, thinGLTexture.view().minY, thinGLTexture.view().lengthX(), thinGLTexture.view().lengthY(), color);
            }
        }
    }

    @Override
    public void custom(final RenderCommand.Custom renderCommand) {
    }

}
