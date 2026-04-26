package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseListener;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public class Checkbox extends Component implements MouseListener, Renderable {

    @Getter
    private boolean checked;
    @Getter
    @Setter
    private Consumer<Boolean> onToggle;
    @Getter
    private String text;
    private ShapedText shapedText;
    private boolean hovered = false;

    @Getter
    private final ThemeOption<Float> cornerRadius;
    @Getter
    private final ThemeOption<Float> outlineWidth;
    @Getter
    private final ThemeOption<Color> backgroundColor;
    @Getter
    private final ThemeOption<Color> outlineColor;
    @Getter
    private final ThemeOption<Color> checkColor;
    @Getter
    private final ThemeOption<Float> checkWidth;
    @Getter
    private final ThemeOption<Float> textGap;

    public Checkbox(final Rivet rivet) {
        this(rivet, false);
    }

    public Checkbox(final Rivet rivet, final boolean checked) {
        this(rivet, "", checked);
    }

    public Checkbox(final Rivet rivet, final String text, final boolean checked) {
        super(rivet);
        this.checked = checked;
        this.text = text;
        this.shapedText = rivet.backend().shapeText(text, rivet.theme().get(Theme.TEXT_COLOR));

        this.cornerRadius = new ThemeOption<>(rivet, Theme.CHECKBOX_CORNER_RADIUS);
        this.outlineWidth = new ThemeOption<>(rivet, Theme.CHECKBOX_OUTLINE_WIDTH);
        this.backgroundColor = new ThemeOption<>(rivet, Theme.CHECKBOX_BACKGROUND_COLOR);
        this.outlineColor = new ThemeOption<>(rivet, Theme.CHECKBOX_OUTLINE_COLOR);
        this.checkColor = new ThemeOption<>(rivet, Theme.CHECKBOX_CHECK_COLOR);
        this.checkWidth = new ThemeOption<>(rivet, Theme.CHECKBOX_CHECK_WIDTH);
        this.textGap = new ThemeOption<>(rivet, Theme.CHECKBOX_TEXT_GAP);
    }

    public Checkbox checked(final boolean checked) {
        if (this.checked != checked) {
            this.checked = checked;
            if (this.onToggle != null) this.onToggle.accept(this.checked);
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public Checkbox text(final String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            this.shapedText = this.rivet.backend().shapeText(text, this.rivet.theme().get(Theme.TEXT_COLOR));
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    @Override
    public void onMouseEnter() {
        this.hovered = true;
    }

    @Override
    public void onMouseLeave() {
        this.hovered = false;
    }

    @Override
    public void onMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
        if (this.hovered && event.button().equals(MouseButton.LEFT)) {
            this.checked(!this.checked);
        }
    }

    @Override
    public void render(final Renderer renderer, final Rectangle bounds) {
        float boxSize = bounds.height() * 0.8F;
        float offset = (bounds.height() - boxSize) / 2F;

        renderer.fillOptimizedRoundedRect(offset, offset, boxSize, boxSize, this.cornerRadius.value(), this.backgroundColor.value());
        if (this.outlineWidth.value() > 0) {
            renderer.outlineOptimizedRoundedRect(offset, offset, boxSize, boxSize, this.cornerRadius.value(), this.outlineWidth.value(), this.outlineColor.value());
        }

        if (this.checked) {
            float padding = boxSize * 0.2F;
            float checkWidth = this.checkWidth.value();
            Color checkColor = this.checkColor.value();

            renderer.line(offset + padding, offset + boxSize / 2F, offset + boxSize / 2.5F, offset + boxSize - padding, checkWidth, checkColor);
            renderer.line(offset + boxSize / 2.5F, offset + boxSize - padding, offset + boxSize - padding, offset + padding, checkWidth, checkColor);
        }

        if (!this.text.isEmpty()) {
            renderer.renderText(this.shapedText, offset + bounds.height() + this.textGap.value(), bounds.height() / 2F, TextOrigin.Horizontal.VISUAL_LEFT, TextOrigin.Vertical.LOGICAL_CENTER);
        }
    }

    @Override
    public void computeIdealSize() {
        float height = this.rivet.backend().getTextHeight();
        float width = height;
        if (!this.text.isEmpty()) {
            width += this.textGap.value() + this.shapedText.logicalBounds().width();
        }
        this.idealSize = new Size(width, height);
    }

}
