package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedTextBlock;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.TextParser;
import net.lenni0451.rivet.text.TextWrapper;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextLine;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class FormattedLabel extends Component {

    @Getter
    private Font font;
    @Getter
    @Nullable
    private String text;
    @Getter
    @Nullable
    private TextFormat format;
    @Getter
    @Nullable
    private TextLine line;
    private ShapedTextBlock shapedText;
    @Getter
    @Setter
    private TextOrigin.Horizontal horizontalOrigin = TextOrigin.Horizontal.VISUAL_CENTER;
    @Getter
    @Setter
    private TextOrigin.Vertical verticalOrigin = TextOrigin.Vertical.LOGICAL_CENTER;
    @Getter
    @Setter
    private ShapedTextBlock.LineAlignment lineAlignment = ShapedTextBlock.LineAlignment.LEFT;
    @Getter
    private float scale = 1F;
    @Getter
    private final ThemeOption<Color> textColor = new ThemeOption<>(this, Theme.TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> disabledTextColor = new ThemeOption<>(this, Theme.DISABLED_TEXT_COLOR);

    public FormattedLabel(@Nonnull final String text) {
        this(text, null);
    }

    public FormattedLabel(@Nonnull final String text, @Nullable final TextFormat defaultFormat) {
        this.text = text;
        this.format = defaultFormat;
    }

    public FormattedLabel(@Nonnull final TextLine line) {
        this.line = line;
    }

    public final FormattedLabel font(final Font font) {
        if (this.font != font) {
            this.font = font;
            if (this.parent() != null) {
                this.parent().requestLayoutRecalculation();
            }
        }
        return this;
    }

    public final FormattedLabel text(final String text) {
        this.text = text;
        this.line = null;
        if (this.parent() != null) {
            this.parent().requestLayoutRecalculation();
        }
        return this;
    }

    public final FormattedLabel text(final String text, final TextFormat defaultFormat) {
        this.text = text;
        this.format = defaultFormat;
        this.line = null;
        if (this.parent() != null) {
            this.parent().requestLayoutRecalculation();
        }
        return this;
    }

    public final FormattedLabel text(final TextLine line) {
        this.text = null;
        this.line = line;
        if (this.parent() != null) {
            this.parent().requestLayoutRecalculation();
        }
        return this;
    }

    public final FormattedLabel scale(final float scale) {
        if (this.scale != scale) {
            this.scale = scale;
            if (this.parent() != null) {
                this.parent().requestLayoutRecalculation();
            }
        }
        return this;
    }

    private void parseLine() {
        if (this.line == null) {
            boolean disabled = this.disabled();
            Color color;
            if (this.format != null) {
                if (disabled && this.format.color().equals(this.textColor.value())) {
                    color = this.disabledTextColor.value();
                } else {
                    color = this.format.color();
                }
            } else {
                color = disabled ? this.disabledTextColor.value() : this.textColor.value();
            }
            TextFormat format = TextFormat.DEFAULT.withColor(color);
            this.line = TextParser.parse(this.text, format);
        }
    }

    protected final Font usedFont() {
        return this.font != null ? this.font : this.rivet().backend().font();
    }

    @Override
    protected void onComponentAdded() {
        if (this.text != null) {
            this.line = null;
        }
    }

    @Override
    protected void onComponentDisabled() {
        if (this.text != null) {
            this.line = null;
        }
    }

    @Override
    protected void onComponentEnabled() {
        if (this.text != null) {
            this.line = null;
        }
    }

    @Override
    protected void onComponentThemeChanged() {
        if (this.text != null) {
            this.line = null;
        }
    }

    @Override
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
        return false;
    }

    @Override
    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
        return false;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float x = this.horizontalOrigin.position(size.width() / this.scale);
        float y = this.verticalOrigin.position(size.height() / this.scale);
        renderer.scale(this.scale, () -> {
            renderer.text(this.shapedText, x, y, this.horizontalOrigin, this.verticalOrigin, this.lineAlignment);
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        this.parseLine();
        ShapedTextBlock shapedTextBlock = TextWrapper.wrapLine(this.usedFont(), this.line, constraints.width() / this.scale);
        return new Size(
                shapedTextBlock.visualBounds().width() * this.scale,
                shapedTextBlock.logicalBounds().height() * this.scale
        );
    }

    @Override
    public void computeLayout(final Size size) {
        this.parseLine();
        this.shapedText = TextWrapper.wrapLine(this.usedFont(), this.line, size.width() / this.scale);
    }

}
