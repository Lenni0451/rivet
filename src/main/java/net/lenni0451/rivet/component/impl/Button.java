package net.lenni0451.rivet.component.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Font;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.ShapedTextBuffer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.MouseListener;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.TextBuffer;
import net.lenni0451.rivet.text.TextRun;
import net.lenni0451.rivet.text.TextSegment;
import org.joml.Matrix4fStack;
import org.joml.primitives.Rectanglef;

import java.util.function.IntConsumer;

public class Button extends Component implements Renderable, MouseListener {

    private final String text;
    private final IntConsumer onClick;
    private boolean hovered = false;
    private ShapedTextBuffer shapedText;

    public Button(final String text, final IntConsumer onClick) {
        this.text = text;
        this.onClick = onClick;
    }

    @Override
    public void onMouseEnter() {
        this.hovered = true;
    }

    @Override
    public void onMouseLeave() {
        this.hovered = false;
    }

    @Override
    public void render(Renderer renderer, Matrix4fStack positionMatrix, Size size) {
        /*if (this.hovered) {
            renderer.filledRectangle(positionMatrix, 0, 0, size.width(), size.height(), Color.GREEN);
        } else {
            renderer.filledRectangle(positionMatrix, 0, 0, size.width(), size.height(), Color.RED);
        }*/
        if (this.shapedText == null || true) {
//            this.shapedText = this.rivet.getBackend().shapeTextBuffer(TextBuffer.fromString(this.rivet.getDefaultFonts(), this.text));
            Font font = this.rivet.getDefaultFonts().getMainFont();
            Color outlineColor = Color.YELLOW;
            int flags = TextSegment.STYLE_SHADOW_BIT | TextSegment.STYLE_UNDERLINE_BIT | TextSegment.STYLE_STRIKETHROUGH_BIT | TextSegment.STYLE_ITALIC_BIT | TextSegment.STYLE_BOLD_BIT;
//            int flags = TextSegment.STYLE_SHADOW_BIT;
            this.shapedText = this.rivet.getBackend().shapeTextBuffer(new TextBuffer(
                    new TextRun(font).addSegment(new TextSegment("Hello", Color.RED, flags, outlineColor)),
                    new TextRun(font, 50, 50).addSegment(new TextSegment(" World", Color.GREEN, flags)),
                    new TextRun(font).addSegment(new TextSegment("!", Color.BLUE, flags, outlineColor))
            ));
        }
        final Rectanglef textBounds = this.shapedText.bounds();
        renderer.filledRectangle(positionMatrix, 0, 0, textBounds.lengthX(), textBounds.lengthY(), Color.GRAY.withAlpha(100));
        renderer.filledRectangle(positionMatrix, 0, -textBounds.minY, textBounds.lengthX(), 1, Color.BLUE);
        renderer.filledRectangle(positionMatrix, 0, textBounds.maxY, textBounds.lengthX(), 1, Color.RED);
        renderer.text(positionMatrix, this.shapedText, 0, 0);
    }

    @Override
    protected void computePreferredSize() {
        //TODO: Measure the text size and set the preferred size accordingly
        this.preferredSize.set(100, 30);
    }

}
