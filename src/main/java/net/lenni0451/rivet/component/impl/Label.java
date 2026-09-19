package net.lenni0451.rivet.component.impl;

import lombok.Getter;
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
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.property.FloatProperty;
import net.lenni0451.rivet.property.ObjectProperty;
import net.lenni0451.rivet.text.TextWrapper;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextLine;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.text.model.TextSection;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import javax.annotation.Nullable;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class Label extends Component {

    @Getter
    private final ObjectProperty<Font> font = new ObjectProperty<>();
    @Getter
    private final ObjectProperty<String> text;
    @Getter
    private final ObjectProperty<TextFormat> textFormat;
    @Getter
    private final ObjectProperty<TextLine> textLine;
    @Getter
    private final FloatProperty scale = new FloatProperty(1F);
    @Getter
    private final ObjectProperty<TextOrigin.Horizontal> horizontalOrigin = new ObjectProperty<>(TextOrigin.Horizontal.VISUAL_CENTER);
    @Getter
    private final ObjectProperty<TextOrigin.Vertical> verticalOrigin = new ObjectProperty<>(TextOrigin.Vertical.LOGICAL_CENTER);
    @Getter
    private final ObjectProperty<ShapedTextBlock.LineAlignment> lineAlignment = new ObjectProperty<>(ShapedTextBlock.LineAlignment.LEFT);

    private Shaped shaped;
    private boolean reshape;

    @Getter
    private final ThemeOption<Color> textColor = new ThemeOption<>(this, Theme.General.TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> disabledTextColor = new ThemeOption<>(this, Theme.General.DISABLED_TEXT_COLOR);
    @Getter
    private final ThemeOption<OverflowBehavior> overflowBehavior = new ThemeOption<>(this, Theme.Label.OVERFLOW_BEHAVIOR);

    public Label(final String text) {
        this(text, null);
    }

    public Label(final String text, @Nullable final TextFormat format) {
        this.text = new ObjectProperty<>(text);
        this.textFormat = new ObjectProperty<>(format);
        this.textLine = new ObjectProperty<>();
        this.init();
    }

    public Label(final TextLine line) {
        this.text = new ObjectProperty<>("");
        this.textFormat = new ObjectProperty<>();
        this.textLine = new ObjectProperty<>(line);
        this.init();
    }

    private void init() {
        this.font.updateListener().add(f -> this.reshapeAndLayout());
        this.text.updateListener().add(t -> {
            if (this.textLine.get() != null) {
                this.textLine.set(null);
            }
            this.reshapeAndLayout();
        });
        this.textFormat.updateListener().add(f -> this.reshapeAndLayout());
        this.textLine.updateListener().add(l -> this.reshapeAndLayout());
        this.scale.updateListener().add(s -> this.reshapeAndLayout());

        this.textColor.changeListener().add(c -> this.reshape = true);
        this.disabledTextColor.changeListener().add(c -> this.reshape = true);
        this.overflowBehavior.changeListener().add(o -> this.reshapeAndLayout());
    }

    public final Label font(@Nullable final Font font) {
        this.font.set(font);
        return this;
    }

    public final Label text(final String text) {
        if (this.textLine.get() != null) {
            this.textLine.set(null);
        }
        this.text.set(text);
        return this;
    }

    public final Label text(final String text, @Nullable final TextFormat textFormat) {
        if (this.textLine.get() != null) {
            this.textLine.set(null);
        }
        this.text.set(text);
        this.textFormat.set(textFormat);
        return this;
    }

    public final Label text(final TextLine line) {
        this.textLine.set(line);
        return this;
    }

    public final Label textFormat(@Nullable final TextFormat textFormat) {
        this.textFormat.set(textFormat);
        return this;
    }

    public final Label scale(final float scale) {
        this.scale.set(scale);
        return this;
    }

    public final Label horizontalOrigin(final TextOrigin.Horizontal horizontalOrigin) {
        this.horizontalOrigin.set(horizontalOrigin);
        return this;
    }

    public final Label verticalOrigin(final TextOrigin.Vertical verticalOrigin) {
        this.verticalOrigin.set(verticalOrigin);
        return this;
    }

    public final Label lineAlignment(final ShapedTextBlock.LineAlignment lineAlignment) {
        this.lineAlignment.set(lineAlignment);
        return this;
    }

    private void reshapeAndLayout() {
        this.reshape = true;
        if (this.parent() != null) {
            this.parent().requestLayoutRecalculation();
        }
    }

    private void shapeText(final Size size) {
        if (this.reshape) {
            Font font = this.font.getOrElse(this.rivet().backend().font());
            TextLine line = this.createTextLine();
            if (this.overflowBehavior.value().equals(OverflowBehavior.WRAP)) {
                this.shaped = TextWrapper.wrapLine(font, line, size.width() / this.scale.get());
            } else {
                this.shaped = font.shapeText(line);
            }
            this.reshape = false;
        }
    }

    private TextLine createTextLine() {
        TextLine textLine = this.textLine.get();
        if (textLine != null) {
            return textLine;
        }
        TextFormat format = this.textFormat.get();
        Color color;
        if (format != null) {
            if (format.color().equals(Color.TRANSPARENT)) {
                color = this.disabled().get() ? this.disabledTextColor.value() : this.textColor.value();
            } else {
                if (this.disabled().get() && format.color().equals(this.textColor.value())) {
                    color = this.disabledTextColor.value();
                } else {
                    color = format.color();
                }
            }
        } else {
            format = TextFormat.DEFAULT;
            color = this.disabled().get() ? this.disabledTextColor.value() : this.textColor.value();
        }
        return this.createTextLine(this.text.getOrElse(""), format.withColor(color));
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
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        this.shapeText(size);
        float scale = this.scale.get();
        TextOrigin.Horizontal horizontalOrigin = this.horizontalOrigin.get();
        TextOrigin.Vertical verticalOrigin = this.verticalOrigin.get();
        ShapedTextBlock.LineAlignment lineAlignment = this.lineAlignment.get();

        if (this.shaped instanceof ShapedTextBlock shapedTextBlock) {
            float x = horizontalOrigin.position(size.width() / scale);
            float y = verticalOrigin.position(size.height() / scale);
            renderer.scale(scale, () -> renderer.text(shapedTextBlock, x, y, horizontalOrigin, verticalOrigin, lineAlignment));
        } else if (this.shaped instanceof ShapedText shapedText) {
            float textScale;
            if (this.overflowBehavior.value().equals(OverflowBehavior.SCALE)) {
                float widthRatio = size.width() / (this.shaped.visualBounds().width() * scale);
                float heightRatio = size.height() / (this.shaped.logicalBounds().height() * scale);
                float ratio = Math.min(widthRatio, heightRatio);
                textScale = ratio > 1 ? scale : ratio;
            } else {
                textScale = scale;
            }

            float x = horizontalOrigin.position(size.width() / textScale);
            float y = verticalOrigin.position(size.height() / textScale);
            renderer.scale(textScale, () -> renderer.text(shapedText, x, y, horizontalOrigin, verticalOrigin));
        } else {
            throw new IllegalStateException("Unknown shaped type: " + this.shaped.getClass().getName());
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        this.shapeText(constraints);
        float scale = this.scale.get();
        return new Size(
                this.shaped.visualBounds().width() * scale,
                this.shaped.logicalBounds().height() * scale
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

}
