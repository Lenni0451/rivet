package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.text.TextOrigin;
import net.lenni0451.rivet.theme.Theme;

@Accessors(fluent = true, chain = true)
public class Label extends Component implements Renderable {

    @Getter
    private String text;
    private ShapedText shapedText;
    @Getter
    @Setter
    private TextOrigin.Horizontal horizontalOrigin = TextOrigin.Horizontal.VISUAL_CENTER;
    @Getter
    @Setter
    private TextOrigin.Vertical verticalOrigin = TextOrigin.Vertical.LOGICAL_CENTER;

    public Label(final Rivet rivet, final String text) {
        super(rivet);
        this.text = text;
        this.shapedText = rivet.backend().shapeText(text, rivet.theme().get(Theme.TEXT_COLOR));
    }

    public Label text(final String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            this.shapedText = this.rivet.backend().shapeText(text, this.rivet.theme().get(Theme.TEXT_COLOR));
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    @Override
    public void render(final Renderer renderer, final Rectangle bounds) {
        float x = switch (this.horizontalOrigin) {
            case LOGICAL_LEFT -> 0;
            case VISUAL_LEFT -> 0;
            case VISUAL_CENTER -> bounds.width() / 2F;
            case VISUAL_RIGHT -> bounds.width();
        };
        float y = switch (this.verticalOrigin) {
            case BASELINE -> bounds.height() / 2F;
            case LOGICAL_TOP -> 0;
            case LOGICAL_CENTER -> bounds.height() / 2F;
            case LOGICAL_BOTTOM -> bounds.height();
            case VISUAL_TOP -> 0;
            case VISUAL_CENTER -> bounds.height() / 2F;
            case VISUAL_BOTTOM -> bounds.height();
        };
        renderer.renderText(this.shapedText, x, y, this.horizontalOrigin, this.verticalOrigin);
    }

    @Override
    public void computeIdealSize() {
        this.idealSize = this.shapedText.logicalSize();
    }

}
