package net.lenni0451.rivet.component.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.TextFormat;
import net.lenni0451.rivet.text.TextOrigin;
import net.lenni0451.rivet.text.TextParser;
import net.lenni0451.rivet.text.TextSection;

import java.util.List;

public class FormattedLabel extends Component implements Renderable {

    private String text;
    private List<TextSection> sections;
    private ShapedText shapedText;
    private TextOrigin.Horizontal horizontalOrigin = TextOrigin.Horizontal.VISUAL_CENTER;
    private TextOrigin.Vertical verticalOrigin = TextOrigin.Vertical.LOGICAL_CENTER;

    public FormattedLabel(final Rivet rivet, final String text) {
        this(rivet, text, TextFormat.DEFAULT);
    }

    public FormattedLabel(final Rivet rivet, final String text, final TextFormat defaultFormat) {
        super(rivet);
        this.text = text;
        this.sections = TextParser.parse(text, defaultFormat);
        this.shapedText = rivet.getBackend().shapeText(this.sections);
    }

    public FormattedLabel(final Rivet rivet, final List<TextSection> sections) {
        super(rivet);
        this.text = null;
        this.sections = sections;
        this.shapedText = rivet.getBackend().shapeText(this.sections);
    }

    public String text() {
        return this.text;
    }

    public FormattedLabel setText(final String text) {
        return this.setText(text, TextFormat.DEFAULT);
    }

    public FormattedLabel setText(final String text, final TextFormat defaultFormat) {
        if (this.text == null || !this.text.equals(text)) {
            this.text = text;
            this.sections = TextParser.parse(text, defaultFormat);
            this.shapedText = this.rivet.getBackend().shapeText(this.sections);
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public List<TextSection> sections() {
        return this.sections;
    }

    public FormattedLabel setSections(final List<TextSection> sections) {
        this.text = null;
        this.sections = sections;
        this.shapedText = this.rivet.getBackend().shapeText(this.sections);
        this.rivet.recalculateNextFrame();
        return this;
    }

    public TextOrigin.Horizontal horizontalOrigin() {
        return this.horizontalOrigin;
    }

    public FormattedLabel setHorizontalOrigin(final TextOrigin.Horizontal horizontalOrigin) {
        this.horizontalOrigin = horizontalOrigin;
        return this;
    }

    public TextOrigin.Vertical verticalOrigin() {
        return this.verticalOrigin;
    }

    public FormattedLabel setVerticalOrigin(final TextOrigin.Vertical verticalOrigin) {
        this.verticalOrigin = verticalOrigin;
        return this;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float x = switch (this.horizontalOrigin) {
            case LOGICAL_LEFT -> 0;
            case VISUAL_LEFT -> 0;
            case VISUAL_CENTER -> size.width() / 2F;
            case VISUAL_RIGHT -> size.width();
        };
        float y = switch (this.verticalOrigin) {
            case BASELINE -> size.height() / 2F;
            case LOGICAL_TOP -> 0;
            case LOGICAL_CENTER -> size.height() / 2F;
            case LOGICAL_BOTTOM -> size.height();
            case VISUAL_TOP -> 0;
            case VISUAL_CENTER -> size.height() / 2F;
            case VISUAL_BOTTOM -> size.height();
        };
        renderer.renderText(this.shapedText, x, y, this.horizontalOrigin, this.verticalOrigin);
    }

    @Override
    public void computeIdealSize() {
        this.idealSize = this.shapedText.visualSize();
    }

    @Override
    public void computeLayout(final Size size) {
    }

}
