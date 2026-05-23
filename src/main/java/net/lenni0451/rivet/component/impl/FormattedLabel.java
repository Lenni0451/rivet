package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.backend.text.ShapedTextBlock;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.TextParser;
import net.lenni0451.rivet.text.TextWrapper;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextLine;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;

@Accessors(fluent = true, chain = true)
public class FormattedLabel extends Component {

    @Getter
    private String text;
    @Getter
    private TextLine line;
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
        this.line = TextParser.parse(text, defaultFormat);
    }

    public FormattedLabel(final Rivet rivet, final TextLine line) {
        super(rivet);
        this.text = null;
        this.line = line;
    }

    public FormattedLabel text(final String text) {
        return this.text(text, TextFormat.DEFAULT.withColor(this.rivet.theme().get(Theme.TEXT_COLOR)));
    }

    public FormattedLabel text(final String text, final TextFormat defaultFormat) {
        if (this.text == null || !this.text.equals(text)) {
            this.text = text;
            this.line = TextParser.parse(text, defaultFormat);
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public FormattedLabel sections(final TextLine line) {
        this.text = null;
        this.line = line;
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
    public void computeIdealSize(final Size constraints) {
        ShapedTextBlock shapedTextBlock = TextWrapper.wrapLine(this.rivet.backend(), this.line, constraints.width());
        this.idealSize = new Size(
                shapedTextBlock.visualBounds().width(),
                shapedTextBlock.logicalBounds().height()
        );
    }

    @Override
    public void computeLayout(final Size size) {
        this.shapedText = TextWrapper.wrapLine(this.rivet.backend(), this.line, size.width());
    }

}
