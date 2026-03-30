package net.lenni0451.rivet.component.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.TextFormat;
import net.lenni0451.rivet.text.TextParser;
import net.lenni0451.rivet.text.TextSection;

import java.util.List;

public class FormattedLabel extends Component implements Renderable {

    private String text;
    private List<TextSection> sections;
    private ShapedText shapedText;

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

    public void setText(final String text) {
        this.setText(text, TextFormat.DEFAULT);
    }

    public void setText(final String text, final TextFormat defaultFormat) {
        if (this.text == null || !this.text.equals(text)) {
            this.text = text;
            this.sections = TextParser.parse(text, defaultFormat);
            this.shapedText = this.rivet.getBackend().shapeText(this.sections);
            this.rivet.recalculateNextFrame();
        }
    }

    public List<TextSection> sections() {
        return this.sections;
    }

    public void setSections(final List<TextSection> sections) {
        this.text = null;
        this.sections = sections;
        this.shapedText = this.rivet.getBackend().shapeText(this.sections);
        this.rivet.recalculateNextFrame();
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
