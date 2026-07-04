package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class ProgressBar extends Component {

    @Getter
    private float progress;
    @Getter
    private final ThemeOption<String> textFormat;
    @Getter
    private final ThemeOption<TextPosition> textPosition;
    @Getter
    private final ThemeOption<Float> textPadding;
    @Getter
    private final ThemeOption<Float> trackCornerRadius;
    @Getter
    private final ThemeOption<Float> indicatorCornerRadius;
    @Getter
    private final ThemeOption<Color> trackColor;
    @Getter
    private final ThemeOption<Color> indicatorColor;
    @Getter
    private final ThemeOption<Color> borderColor;
    @Getter
    private final ThemeOption<Float> borderWidth;
    @Getter
    private final ThemeOption<Color> textColor;

    private String currentText = null;
    private ShapedText currentShapedText = null;

    public ProgressBar() {
        this(0.5F);
    }

    public ProgressBar(final float progress) {
        this.progress = MathUtils.clamp(progress, 0, 1);

        this.textFormat = new ThemeOption<>(this, Theme.PROGRESS_BAR_TEXT_FORMAT);
        this.textPosition = new ThemeOption<>(this, Theme.PROGRESS_BAR_TEXT_POSITION);
        this.textPadding = new ThemeOption<>(this, Theme.PROGRESS_BAR_TEXT_PADDING);
        this.trackCornerRadius = new ThemeOption<>(this, Theme.PROGRESS_BAR_TRACK_CORNER_RADIUS);
        this.indicatorCornerRadius = new ThemeOption<>(this, Theme.PROGRESS_BAR_INDICATOR_CORNER_RADIUS);
        this.trackColor = new ThemeOption<>(this, Theme.PROGRESS_BAR_TRACK_COLOR);
        this.indicatorColor = new ThemeOption<>(this, Theme.PROGRESS_BAR_INDICATOR_COLOR);
        this.borderColor = new ThemeOption<>(this, Theme.PROGRESS_BAR_BORDER_COLOR);
        this.borderWidth = new ThemeOption<>(this, Theme.PROGRESS_BAR_BORDER_WIDTH);
        this.textColor = new ThemeOption<>(this, Theme.PROGRESS_BAR_TEXT_COLOR);

        this.textFormat.changeListener().add(f -> this.currentText = null);
        this.textColor.changeListener().add(c -> this.currentText = null);
    }

    public ProgressBar progress(final float progress) {
        this.progress = MathUtils.clamp(progress, 0, 1);
        return this;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        renderer.optimizedFillRoundedRect(0, 0, size.width(), size.height(), this.trackCornerRadius.value(), this.trackColor.value());
        if (this.trackCornerRadius.value() < this.indicatorCornerRadius.value()) {
            renderer.optimizedFillRoundedRect(0, 0, size.width() * this.progress, size.height(), this.indicatorCornerRadius.value(), this.indicatorColor.value());
        } else {
            renderer.stencil(maskRenderer -> {
                maskRenderer.optimizedFillRoundedRect(0, 0, size.width(), size.height(), this.trackCornerRadius.value(), Color.RED);
            }, () -> {
                renderer.optimizedFillRoundedRect(0, 0, size.width() * this.progress, size.height(), this.indicatorCornerRadius.value(), this.indicatorColor.value());
            });
        }
        renderer.optimizedOutlineRoundedRect(0, 0, size.width(), size.height(), this.trackCornerRadius.value(), this.borderWidth.value(), this.borderColor.value());

        if (!this.textPosition.value().equals(TextPosition.NONE)) {
            String text = String.format(this.textFormat.value(), this.progress * 100);
            if (!text.equals(this.currentText)) {
                this.currentText = text;
                this.currentShapedText = this.rivet().backend().font().shapeText(text, this.textColor.value());
            }

            switch (this.textPosition.value()) {
                case LEFT -> renderer.text(
                        this.currentShapedText,
                        this.textPadding.value(), size.height() / 2F,
                        TextOrigin.Horizontal.VISUAL_LEFT, TextOrigin.Vertical.LOGICAL_CENTER
                );
                case CENTER -> renderer.text(
                        this.currentShapedText,
                        size.width() / 2F, size.height() / 2F,
                        TextOrigin.Horizontal.VISUAL_CENTER, TextOrigin.Vertical.LOGICAL_CENTER
                );
                case RIGHT -> renderer.text(
                        this.currentShapedText,
                        size.width() - this.textPadding.value(), size.height() / 2F,
                        TextOrigin.Horizontal.VISUAL_RIGHT, TextOrigin.Vertical.LOGICAL_CENTER
                );
                case FOLLOW_LEFT -> renderer.text(
                        this.currentShapedText,
                        Math.max(size.width() * this.progress - this.textPadding.value(), this.textPadding.value() + this.currentShapedText.visualBounds().width()), size.height() / 2F,
                        TextOrigin.Horizontal.VISUAL_RIGHT, TextOrigin.Vertical.LOGICAL_CENTER
                );
                case FOLLOW_CENTER -> renderer.text(
                        this.currentShapedText,
                        MathUtils.clamp(size.width() * this.progress, this.textPadding.value() + this.currentShapedText.visualBounds().width() / 2F, size.width() - this.textPadding.value() - this.currentShapedText.visualBounds().width() / 2), size.height() / 2F,
                        TextOrigin.Horizontal.VISUAL_CENTER, TextOrigin.Vertical.LOGICAL_CENTER
                );
                case FOLLOW_RIGHT -> renderer.text(
                        this.currentShapedText,
                        Math.min(size.width() * this.progress + this.textPadding.value(), size.width() - this.textPadding.value() - this.currentShapedText.visualBounds().width()), size.height() / 2F,
                        TextOrigin.Horizontal.VISUAL_LEFT, TextOrigin.Vertical.LOGICAL_CENTER
                );
                case PROGRESS_CENTER -> renderer.text(
                        this.currentShapedText,
                        Math.max(size.width() * this.progress / 2, this.textPadding.value() + this.currentShapedText.visualBounds().width() / 2), size.height() / 2F,
                        TextOrigin.Horizontal.VISUAL_CENTER, TextOrigin.Vertical.LOGICAL_CENTER
                );
            }
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        float fontSize = this.rivet().backend().font().height();
        return new Size(fontSize * 10, fontSize);
    }


    public enum TextPosition {
        NONE,
        LEFT, CENTER, RIGHT,
        FOLLOW_LEFT, FOLLOW_CENTER, FOLLOW_RIGHT,
        PROGRESS_CENTER
    }

}
