package net.lenni0451.rivet.component.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.TextOrigin;

public class Label extends Component implements Renderable {

    private String text;
    private ShapedText shapedText;
    private TextOrigin.Horizontal horizontalOrigin = TextOrigin.Horizontal.VISUAL_CENTER;
    private TextOrigin.Vertical verticalOrigin = TextOrigin.Vertical.LOGICAL_CENTER;

    public Label(final Rivet rivet, final String text) {
        super(rivet);
        this.text = text;
        this.shapedText = rivet.getBackend().shapeText(text);
    }

    public String text() {
        return this.text;
    }

    public Label setText(final String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            this.shapedText = this.rivet.getBackend().shapeText(text);
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public TextOrigin.Horizontal horizontalOrigin() {
        return this.horizontalOrigin;
    }

    public Label setHorizontalOrigin(final TextOrigin.Horizontal horizontalOrigin) {
        this.horizontalOrigin = horizontalOrigin;
        return this;
    }

    public TextOrigin.Vertical verticalOrigin() {
        return this.verticalOrigin;
    }

    public Label setVerticalOrigin(final TextOrigin.Vertical verticalOrigin) {
        this.verticalOrigin = verticalOrigin;
        return this;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        renderer.renderText(this.shapedText, switch (this.horizontalOrigin) {
            case LOGICAL_LEFT -> 0;
            case VISUAL_LEFT -> 0;
            case VISUAL_CENTER -> size.width() / 2F;
            case VISUAL_RIGHT -> size.width();
        }, switch (this.verticalOrigin) {
            case BASELINE -> size.height() / 2F;
            case LOGICAL_TOP -> 0;
            case LOGICAL_CENTER -> size.height() / 2F;
            case LOGICAL_BOTTOM -> size.height();
            case VISUAL_TOP -> 0;
            case VISUAL_CENTER -> size.height() / 2F;
            case VISUAL_BOTTOM -> size.height();
        }, this.horizontalOrigin, this.verticalOrigin);
    }

    @Override
    public void computeIdealSize() {
        this.idealSize = this.shapedText.visualSize();
    }

    @Override
    public void computeLayout(final Size size) {
    }

}
