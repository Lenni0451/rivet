package net.lenni0451.rivet.component.impl;

import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.text.ShapedTextBuffer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.TextBuffer;
import org.joml.Matrix4fStack;
import org.joml.primitives.Rectanglef;

public class Label extends Component implements Renderable {

    private String text;
    private TextBuffer textBuffer;
    private ShapedTextBuffer shapedText;

    public Label(final String text) {
        this.text = text;
    }

    public Label(final TextBuffer textBuffer) {
        this.textBuffer = textBuffer;
    }

    @Override
    protected void computePreferredSize() {
        this.shapeText();
        Rectanglef bounds = this.shapedText.bounds();
        this.getPreferredSize().set(bounds.lengthX(), bounds.lengthY());
    }

    @Override
    public void render(Renderer renderer, Matrix4fStack positionMatrix, Size size) {
        renderer.text(positionMatrix, this.shapedText,
                (size.width() - this.shapedText.bounds().lengthX()) / 2F,
                (size.height() - this.shapedText.bounds().lengthY()) / 2F,
                false
        );
    }

    private void shapeText() {
        if (this.shapedText == null) {
            if (this.text != null) {
                this.textBuffer = TextBuffer.fromString(this.rivet.getDefaultFonts(), this.text);
            }
            this.shapedText = this.rivet.getBackend().shapeTextBuffer(this.textBuffer);
        }
    }

}
