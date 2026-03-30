package net.lenni0451.rivet.component.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.math.Size;

public class Label extends Component implements Renderable {

    private String text;
    private ShapedText shapedText;

    public Label(final Rivet rivet, final String text) {
        super(rivet);
        this.text = text;
        this.shapedText = rivet.getBackend().shapeText(text);
    }

    public String text() {
        return this.text;
    }

    public void setText(final String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            this.shapedText = this.rivet.getBackend().shapeText(text);
            this.rivet.recalculateNextFrame();
        }
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        renderer.renderText(this.shapedText, size.width() / 2F, size.height() / 2F, Renderer.HorizontalOrigin.VISUAL_CENTER, Renderer.VerticalOrigin.LOGICAL_CENTER);
    }

    @Override
    public void computeIdealSize() {
        this.idealSize = this.shapedText.visualSize();
    }

    @Override
    public void computeLayout(final Size size) {
    }

}
