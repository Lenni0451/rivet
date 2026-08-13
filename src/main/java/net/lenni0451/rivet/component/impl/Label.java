package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.text.Font;
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

@Accessors(fluent = true, chain = true, makeFinal = true)
public class Label extends Component {

    @Getter
    @Nullable
    private Font font;
    @Getter
    private String text;
    private ShapedText shapedText;
    private ShapedTextBlock shapedTextBlock;
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
        if (this.font != font) {
            this.font = font;
            this.reshape = true;
            if (this.parent() != null) {
                this.parent().requestLayoutRecalculation();
            }
        }
        return this;
    }

    public final Label text(final String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            this.reshape = true;
            if (this.parent() != null) {
                this.parent().requestLayoutRecalculation();
            }
        }
        return this;
    }

    public final Label scale(final float scale) {
        if (this.scale != scale) {
            this.scale = scale;
            if (this.parent() != null) {
                this.parent().requestLayoutRecalculation();
            }
        }
        return this;
    }

    private void shapeText() {
        if (this.reshape) {
            Color textColor = this.disabled() ? this.disabledTextColor.value() : this.textColor.value();
            this.shapedText = this.usedFont().shapeText(this.text, textColor);
            this.reshape = false;
        }
    }

    private TextLine createTextLine() {
        Color textColor = this.disabled() ? this.disabledTextColor.value() : this.textColor.value();
        TextFormat format = TextFormat.DEFAULT.withColor(textColor);
        return new TextLine(new TextSection(this.text, format));
    }

    protected final Font usedFont() {
        return this.font != null ? this.font : this.rivet().backend().font();
    }

    @Override
    protected void onComponentAdded() {
        this.reshape = true;
    }

    @Override
    protected void onComponentDisabled() {
        this.reshape = true;
    }

    @Override
    protected void onComponentEnabled() {
        this.reshape = true;
    }

    @Override
    protected void onComponentThemeChanged() {
        this.reshape = true;
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
    protected void renderComponent(final Renderer renderer, final Size size) {
        if (this.overflowBehavior.value().equals(OverflowBehavior.WRAP)) {
            if (this.shapedTextBlock == null) {
                this.computeLayout(size);
            }
            float x = this.horizontalOrigin.position(size.width() / this.scale);
            float y = this.verticalOrigin.position(size.height() / this.scale);
            renderer.scale(this.scale, () -> renderer.text(this.shapedTextBlock, x, y, this.horizontalOrigin, this.verticalOrigin, this.lineAlignment));
        } else {
            this.shapeText();

            float scale;
            if (this.overflowBehavior.value().equals(OverflowBehavior.SCALE)) {
                float widthRatio = size.width() / (this.shapedText.visualBounds().width() * this.scale);
                float heightRatio = size.height() / (this.shapedText.logicalBounds().height() * this.scale);
                float ratio = Math.min(widthRatio, heightRatio);
                scale = ratio > 1 ? this.scale : ratio;
            } else {
                scale = this.scale;
            }

            float x = this.horizontalOrigin.position(size.width() / scale);
            float y = this.verticalOrigin.position(size.height() / scale);
            renderer.scale(scale, () -> renderer.text(this.shapedText, x, y, this.horizontalOrigin, this.verticalOrigin));
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        if (this.overflowBehavior.value().equals(OverflowBehavior.WRAP)) {
            ShapedTextBlock block = TextWrapper.wrapLine(this.usedFont(), this.createTextLine(), constraints.width() / this.scale);
            return new Size(
                    block.visualBounds().width() * this.scale,
                    block.logicalBounds().height() * this.scale
            );
        } else {
            this.shapeText();
            return new Size(
                    this.shapedText.visualBounds().width() * this.scale,
                    this.shapedText.logicalBounds().height() * this.scale
            );
        }
    }

    @Override
    public void computeLayout(final Size size) {
        if (this.overflowBehavior.value().equals(OverflowBehavior.WRAP)) {
            this.shapedTextBlock = TextWrapper.wrapLine(this.usedFont(), this.createTextLine(), size.width() / this.scale);
        }
    }


    public enum OverflowBehavior {
        CLIP, SCALE, WRAP
    }

}
