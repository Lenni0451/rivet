package net.lenni0451.rivet.component.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.Shaped;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.backend.text.ShapedTextBlock;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.TextWrapper;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextLine;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.text.model.TextSection;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import javax.annotation.Nullable;
import java.util.Objects;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class Label extends Component {

    @Getter
    @Nullable
    private Font font;
    private Text text;
    private Shaped shaped;
    private boolean reshape;
    @Getter
    private final ThemeOption<Color> textColor = new ThemeOption<>(this, Theme.General.TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> disabledTextColor = new ThemeOption<>(this, Theme.General.DISABLED_TEXT_COLOR);
    @Getter
    private final ThemeOption<OverflowBehavior> overflowBehavior = new ThemeOption<>(this, Theme.Label.OVERFLOW_BEHAVIOR);
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

    public Label(final String text) {
        this(text, null);
    }

    public Label(final String text, @Nullable final TextFormat format) {
        this(new StringText(text, format));
    }

    public Label(final TextLine line) {
        this(new TextLineText(line));
    }

    private Label(final Text text) {
        this.text = text;

        this.textColor.changeListener().add(c -> this.reshape = true);
        this.disabledTextColor.changeListener().add(c -> this.reshape = true);
        this.overflowBehavior.changeListener().add(o -> {
            this.reshape = true;
            if (this.parent() != null) {
                this.parent().requestLayoutRecalculation();
            }
        });
    }

    public final Label font(@Nullable final Font font) {
        if (!Objects.equals(this.font, font)) {
            this.font = font;
            this.reshape = true;
            if (this.parent() != null) {
                this.parent().requestLayoutRecalculation();
            }
        }
        return this;
    }

    @Nullable
    public final String text() {
        if (this.text instanceof StringText stringText) {
            return stringText.text;
        }
        return null;
    }

    @Nullable
    public final TextLine textLine() {
        if (this.text instanceof TextLineText textLineText) {
            return textLineText.line;
        }
        return null;
    }

    public final Label text(final String text) {
        if (this.text instanceof StringText stringText) {
            if (!stringText.text.equals(text)) {
                stringText.text = text;
                this.markReshape();
            }
        } else {
            this.text = new StringText(text, TextFormat.DEFAULT);
            this.markReshape();
        }
        return this;
    }

    public final Label text(final String text, @Nullable final TextFormat textFormat) {
        if (!(this.text instanceof StringText stringText) || !stringText.text.equals(text) || !Objects.equals(stringText.format, textFormat)) {
            this.text = new StringText(text, textFormat);
            this.markReshape();
        }
        return this;
    }

    public final Label text(final TextLine line) {
        if (!(this.text instanceof TextLineText textLineText) || !textLineText.line.equals(line)) {
            this.text = new TextLineText(line);
            this.markReshape();
        }
        return this;
    }

    @Nullable
    public final TextFormat textFormat() {
        if (this.text instanceof StringText stringText) {
            return stringText.format;
        }
        return null;
    }

    public final Label textFormat(final TextFormat textFormat) {
        if (this.text instanceof StringText stringText) {
            if (!Objects.equals(stringText.format, textFormat)) {
                this.text = new StringText(stringText.text, textFormat);
                this.markReshape();
            }
        }
        return this;
    }

    public Label scale(final float scale) {
        if (this.scale != scale) {
            this.scale = scale;
            if (this.parent() != null) {
                this.parent().requestLayoutRecalculation();
            }
        }
        return this;
    }

    private void markReshape() {
        this.reshape = true;
        if (this.parent() != null) {
            this.parent().requestLayoutRecalculation();
        }
    }

    private void shapeText(final Size size) {
        if (this.reshape) {
            Font font = this.font != null ? this.font : this.rivet().backend().font();
            TextLine line = this.createTextLine();
            if (this.overflowBehavior.value().equals(OverflowBehavior.WRAP)) {
                this.shaped = TextWrapper.wrapLine(font, line, size.width() / this.scale);
            } else {
                this.shaped = font.shapeText(line);
            }
            this.reshape = false;
        }
    }

    private TextLine createTextLine() {
        if (this.text instanceof StringText stringText) {
            TextFormat format;
            Color color;
            if (stringText.format != null) {
                format = stringText.format;
                if (this.disabled() && stringText.format.color().equals(this.textColor.value())) {
                    color = this.disabledTextColor.value();
                } else {
                    color = stringText.format.color();
                }
            } else {
                format = TextFormat.DEFAULT;
                color = this.disabled() ? this.disabledTextColor.value() : this.textColor.value();
            }
            return this.createTextLine(stringText.text, format.withColor(color));
        } else if (this.text instanceof TextLineText textLineText) {
            return textLineText.line;
        } else {
            throw new IllegalStateException("Unknown text type: " + this.text.getClass().getName());
        }
    }

    protected TextLine createTextLine(final String text, final TextFormat format) {
        return new TextLine(new TextSection(text, format));
    }

    @Override
    protected void onAddedInternal() {
        this.reshape = true;
    }

    @Override
    protected void onDisabledInternal() {
        this.reshape = true;
    }

    @Override
    protected void onEnabledInternal() {
        this.reshape = true;
    }

    @Override
    protected void onThemeChangedInternal() {
        this.reshape = true;
    }

    @Override
    protected boolean onMouseDownInternal(final MouseButtonEvent event, final Size size) {
        return false;
    }

    @Override
    protected boolean onMouseMoveInternal(final MouseMoveEvent event, final Size size) {
        return false;
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size) {
        this.shapeText(size);
        if (this.shaped instanceof ShapedTextBlock shapedTextBlock) {
            float x = this.horizontalOrigin.position(size.width() / this.scale);
            float y = this.verticalOrigin.position(size.height() / this.scale);
            renderer.scale(this.scale, () -> renderer.text(shapedTextBlock, x, y, this.horizontalOrigin, this.verticalOrigin, this.lineAlignment));
        } else if (this.shaped instanceof ShapedText shapedText) {
            float scale;
            if (this.overflowBehavior.value().equals(OverflowBehavior.SCALE)) {
                float widthRatio = size.width() / (this.shaped.visualBounds().width() * this.scale);
                float heightRatio = size.height() / (this.shaped.logicalBounds().height() * this.scale);
                float ratio = Math.min(widthRatio, heightRatio);
                scale = ratio > 1 ? this.scale : ratio;
            } else {
                scale = this.scale;
            }

            float x = this.horizontalOrigin.position(size.width() / scale);
            float y = this.verticalOrigin.position(size.height() / scale);
            renderer.scale(scale, () -> renderer.text(shapedText, x, y, this.horizontalOrigin, this.verticalOrigin));
        } else {
            throw new IllegalStateException("Unknown shaped type: " + this.shaped.getClass().getName());
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        this.shapeText(constraints);
        return new Size(
                this.shaped.visualBounds().width() * this.scale,
                this.shaped.logicalBounds().height() * this.scale
        );
    }

    @Override
    public void computeLayout(final Size size) {
        if (this.overflowBehavior.value().equals(OverflowBehavior.WRAP)) {
            this.shapeText(size);
        }
    }


    public enum OverflowBehavior {
        CLIP, SCALE, WRAP
    }

    private sealed interface Text permits StringText, TextLineText {
    }

    @AllArgsConstructor
    private static final class StringText implements Text {
        public String text;
        @Nullable
        public TextFormat format;
    }

    @AllArgsConstructor
    private static final class TextLineText implements Text {
        public TextLine line;
    }

}
