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
import net.lenni0451.rivet.text.TextFormat;
import net.lenni0451.rivet.text.TextOrigin;
import net.lenni0451.rivet.text.TextParser;
import net.lenni0451.rivet.text.TextSection;
import net.lenni0451.rivet.theme.Theme;

import java.util.List;

@Accessors(fluent = true, chain = true)
public class FormattedLabel extends Component implements Renderable {

    @Getter
    private String text;
    @Getter
    private List<TextSection> sections;
    private ShapedText shapedText;
    @Getter
    @Setter
    private TextOrigin.Horizontal horizontalOrigin = TextOrigin.Horizontal.VISUAL_CENTER;
    @Getter
    @Setter
    private TextOrigin.Vertical verticalOrigin = TextOrigin.Vertical.LOGICAL_CENTER;

    public FormattedLabel(final Rivet rivet, final String text) {
        this(rivet, text, TextFormat.DEFAULT.withColor(rivet.theme().get(Theme.TEXT_COLOR)));
    }

    public FormattedLabel(final Rivet rivet, final String text, final TextFormat defaultFormat) {
        super(rivet);
        this.text = text;
        this.sections = TextParser.parse(text, defaultFormat);
        this.shapedText = rivet.backend().shapeText(this.sections);
    }

    public FormattedLabel(final Rivet rivet, final List<TextSection> sections) {
        super(rivet);
        this.text = null;
        this.sections = sections;
        this.shapedText = rivet.backend().shapeText(this.sections);
    }

    public FormattedLabel text(final String text) {
        return this.text(text, TextFormat.DEFAULT.withColor(this.rivet.theme().get(Theme.TEXT_COLOR)));
    }

    public FormattedLabel text(final String text, final TextFormat defaultFormat) {
        if (this.text == null || !this.text.equals(text)) {
            this.text = text;
            this.sections = TextParser.parse(text, defaultFormat);
            this.shapedText = this.rivet.backend().shapeText(this.sections);
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public FormattedLabel sections(final List<TextSection> sections) {
        this.text = null;
        this.sections = sections;
        this.shapedText = this.rivet.backend().shapeText(this.sections);
        this.rivet.recalculateNextFrame();
        return this;
    }

    @Override
    public void render(final Renderer renderer, final Rectangle bounds) {
        float x = this.horizontalOrigin.offset(bounds.width());
        float y = this.verticalOrigin.offset(bounds.height());
        renderer.renderText(this.shapedText, x, y, this.horizontalOrigin, this.verticalOrigin);
    }

    @Override
    public void computeIdealSize() {
        this.idealSize = this.shapedText.logicalBounds().size();
    }

}
