package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.Interpolator;
import net.lenni0451.rivet.animation.Transition;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class Checkbox extends Component {

    @Getter
    private Font font;
    @Getter
    private boolean checked;
    @Getter
    private final ListenerList<Consumer<Boolean>> toggleListener = new ListenerList<>();
    @Getter
    private String text;
    private ShapedText shapedText;
    private boolean hovered = false;

    @Getter
    private final ThemeOption<Float> cornerRadius = new ThemeOption<>(this, Theme.CHECKBOX_CORNER_RADIUS);
    @Getter
    private final ThemeOption<Float> outlineWidth = new ThemeOption<>(this, Theme.CHECKBOX_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Color> backgroundColor = new ThemeOption<>(this, Theme.CHECKBOX_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> outlineColor = new ThemeOption<>(this, Theme.CHECKBOX_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> checkColor = new ThemeOption<>(this, Theme.CHECKBOX_CHECK_COLOR);
    @Getter
    private final ThemeOption<Float> checkWidth = new ThemeOption<>(this, Theme.CHECKBOX_CHECK_WIDTH);
    @Getter
    private final ThemeOption<Float> textGap = new ThemeOption<>(this, Theme.CHECKBOX_TEXT_GAP);
    @Getter
    private final ThemeOption<Color> hoverBackgroundColor = new ThemeOption<>(this, Theme.CHECKBOX_HOVER_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> hoverOutlineColor = new ThemeOption<>(this, Theme.CHECKBOX_HOVER_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledBackgroundColor = new ThemeOption<>(this, Theme.CHECKBOX_DISABLED_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOutlineColor = new ThemeOption<>(this, Theme.CHECKBOX_DISABLED_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledCheckColor = new ThemeOption<>(this, Theme.CHECKBOX_DISABLED_CHECK_COLOR);
    @Getter
    private final ThemeOption<AnimationConfig> hoverAnimationConfig = new ThemeOption<>(this, Theme.CHECKBOX_HOVER_ANIMATION);
    @Getter
    private final ThemeOption<AnimationConfig> checkAnimationConfig = new ThemeOption<>(this, Theme.CHECKBOX_CHECK_ANIMATION);

    private Transition<Color> backgroundColorTransition;
    private Transition<Color> outlineColorTransition;
    private Transition<Float> checkProgress;

    public Checkbox() {
        this(false);
    }

    public Checkbox(final boolean checked) {
        this("", checked);
    }

    public Checkbox(final String text, final boolean checked) {
        this.checked = checked;
        this.text = text;
    }

    public Checkbox font(final Font font) {
        if (this.font != font) {
            this.font = font;
            if (this.rivet() != null) {
                this.shapeText();
                if (this.parent() != null) {
                    this.parent().requestLayoutRecalculation();
                }
            }
        }
        return this;
    }

    public Checkbox checked(final boolean checked) {
        if (this.checked != checked) {
            this.checked = checked;
            this.toggleListener.callVoid(c -> c.accept(this.checked));
        }
        return this;
    }

    public Checkbox text(final String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            if (this.rivet() != null) {
                this.shapeText();
                this.parent().requestLayoutRecalculation();
            }
        }
        return this;
    }

    private void shapeText() {
        if (this.rivet() != null) {
            Font font = this.font != null ? this.font : this.rivet().backend().font();
            Color textColor = this.disabled() ? this.rivet().theme().get(Theme.DISABLED_TEXT_COLOR) : this.rivet().theme().get(Theme.TEXT_COLOR);
            this.shapedText = font.shapeText(this.text, textColor);
        }
    }

    private State state() {
        if (this.disabled()) {
            return State.DISABLED;
        } else if (this.hovered) {
            return State.HOVERED;
        } else {
            return State.INACTIVE;
        }
    }

    @Override
    protected void onComponentAdded() {
        this.shapeText();
        this.backgroundColorTransition = new Transition<>(
                this,
                () -> switch (this.state()) {
                    case INACTIVE -> this.backgroundColor.value();
                    case HOVERED -> this.hoverBackgroundColor.value();
                    case DISABLED -> this.disabledBackgroundColor.value();
                },
                this.hoverAnimationConfig::value,
                Interpolator.COLOR
        );
        this.outlineColorTransition = new Transition<>(
                this,
                () -> switch (this.state()) {
                    case INACTIVE -> this.outlineColor.value();
                    case HOVERED -> this.hoverOutlineColor.value();
                    case DISABLED -> this.disabledOutlineColor.value();
                },
                this.hoverAnimationConfig::value,
                Interpolator.COLOR
        );
        this.checkProgress = new Transition<>(
                this,
                () -> this.checked ? 1F : 0F,
                this.checkAnimationConfig::value,
                Interpolator.FLOAT
        );
    }

    @Override
    protected void onComponentRemoved() {
        this.hovered = false;
    }

    @Override
    protected void onComponentDisabled() {
        this.onComponentRemoved();
        this.shapeText();
    }

    @Override
    protected void onComponentEnabled() {
        this.shapeText();
    }

    @Override
    protected void onComponentThemeChanged() {
        if (this.rivet() != null) {
            this.shapeText();
        }
    }

    @Override
    protected void onComponentMouseEnter() {
        this.hovered = true;
    }

    @Override
    protected void onComponentMouseLeave() {
        this.hovered = false;
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        if (this.hovered && event.button().equals(MouseButton.LEFT)) {
            this.checked(!this.checked);
        }
        return true;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float boxSize = size.height() * 0.8F;
        float offset = (size.height() - boxSize) / 2F;

        renderer.optimizedFillRoundedRect(offset, offset, boxSize, boxSize, this.cornerRadius.value(), this.backgroundColorTransition.value());
        if (this.outlineWidth.value() > 0) {
            renderer.optimizedOutlineRoundedRect(offset, offset, boxSize, boxSize, this.cornerRadius.value(), this.outlineWidth.value(), this.outlineColorTransition.value());
        }

        float progress = this.checkProgress.value();
        if (progress > 0) {
            float padding = boxSize * 0.2F;
            float checkWidth = this.checkWidth.value();
            Color checkColor = this.disabled() ? this.disabledCheckColor.value() : this.checkColor.value();
            checkColor = checkColor.withAlphaF(checkColor.getAlpha() / 255F * progress);

            renderer.polyLine(new Point[]{
                    new Point(offset + padding, offset + boxSize / 2F),
                    new Point(offset + boxSize / 2.5F, offset + boxSize - padding),
                    new Point(offset + boxSize - padding, offset + padding),
            }, checkWidth, checkColor);
        }

        if (!this.text.isEmpty()) {
            renderer.text(this.shapedText, size.height() + this.textGap.value(), size.height() / 2F, TextOrigin.Horizontal.VISUAL_LEFT, TextOrigin.Vertical.LOGICAL_CENTER);
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        float height = this.shapedText.logicalBounds().height();
        if (height <= 0) height = this.rivet().backend().font().height();
        float width = height;
        if (!this.text.isEmpty()) {
            width += this.textGap.value() + this.shapedText.visualBounds().width();
        }
        return new Size(width, height);
    }


    private enum State {
        INACTIVE, HOVERED, DISABLED
    }

}
