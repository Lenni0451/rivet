package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import net.lenni0451.rivet.utils.FormatUtils;

import java.util.List;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class NumberPicker extends Component implements Parent {
    private final TextField field = new TextField();

    @Getter
    private Font font;

    @Getter
    private String suffix = "";
    @Getter
    private double min;
    @Getter
    private double max;
    @Getter
    private double step;
    @Getter
    private double value;

    private float prevMouseX = -Float.MAX_VALUE;
    private boolean over;
    private boolean holding, editing;
    private boolean changed;
    private ShapedText shapedText;

    @Getter
    private final ThemeOption<Color> textColor = new ThemeOption<>(this, Theme.NUMBER_PICKER_TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> holdingTextColor = new ThemeOption<>(this, Theme.NUMBER_PICKER_HOLDING_TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> highlightTextColor = new ThemeOption<>(this, Theme.NUMBER_PICKER_HIGHLIGHT_TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> disabledTextColor = new ThemeOption<>(this, Theme.NUMBER_PICKER_DISABLED_TEXT_COLOR);

    @Getter
    private final ThemeOption<Color> disabledBackgroundColor = new ThemeOption<>(this, Theme.NUMBER_PICKER_DISABLED_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> holdingBackgroundColor = new ThemeOption<>(this, Theme.NUMBER_PICKER_HOLDING_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> highlightedBackgroundColor = new ThemeOption<>(this, Theme.NUMBER_PICKER_HIGHLIGHT_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> backgroundColor = new ThemeOption<>(this, Theme.NUMBER_PICKER_BACKGROUND_COLOR);

    @Getter
    private final ThemeOption<Float> outlineWidth = new ThemeOption<>(this, Theme.NUMBER_PICKER_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Float> cornerRadius = new ThemeOption<>(this, Theme.NUMBER_PICKER_CORNER_RADIUS);

    @Getter
    private final ThemeOption<Color> outlineColor = new ThemeOption<>(this, Theme.NUMBER_PICKER_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> holdingOutlineColor = new ThemeOption<>(this, Theme.NUMBER_PICKER_HOLDING_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> highlightOutlineColor = new ThemeOption<>(this, Theme.NUMBER_PICKER_HIGHLIGHT_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOutlineColor = new ThemeOption<>(this, Theme.NUMBER_PICKER_DISABLED_OUTLINE_COLOR);

    @Getter
    private final ThemeOption<String> numberFormat = new ThemeOption<>(this, Theme.NUMBER_PICKER_FORMAT);


    public NumberPicker(final double min, final double max, final double value) {
        this(min, max, 1, value);
    }

    public NumberPicker(final double min, final double max, final double step, final double value) {
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;

        field.text(String.valueOf(value));
    }

    public ThemeOption<Padding> innerPadding() {
        return this.field.innerPadding();
    }

    public NumberPicker font(final Font font) {
        if (this.font != font) {
            this.font = font;
            if (this.rivet() != null) {
                this.updateShapedText();
                if (this.parent() != null) {
                    this.parent().requestLayoutRecalculation();
                }
            }
        }
        this.field.font(font);
        return this;
    }

    public NumberPicker suffix(final String suffix) {
        if (this.suffix.equals(suffix)) {
            return this;
        }

        this.suffix = suffix;
        this.updateShapedText();
        return this;
    }


    public NumberPicker value(final double value) {
        this.value = MathUtils.clamp(value, min, max);
        this.updateShapedText();
        this.field.text(String.valueOf(this.value));
        return this;
    }

    public NumberPicker min(final double min) {
        this.min = min;
        return this;
    }

    public NumberPicker max(final double max) {
        this.max = max;
        return this;
    }

    public NumberPicker step(final double step) {
        this.step = step;
        return this;
    }

    @Override
    public void render(Renderer renderer, Size size) {
        if (editing) {
            field.render(renderer, size);
            return;
        }

        Color backgroundColor = this.disabled() ? this.disabledBackgroundColor.value() : holding ? this.holdingBackgroundColor.value() : over ? this.highlightedBackgroundColor.value() : this.backgroundColor.value();
        renderer.optimizedFillRoundedRect(0, 0, size.width(), size.height(), this.cornerRadius.value(), backgroundColor);
        renderer.optimizedOutlineRoundedRect(0, 0, size.width(), size.height(), this.cornerRadius.value(), this.outlineWidth.value(), disabled() ? this.disabledOutlineColor.value() : holding ? this.holdingOutlineColor.value() : over ? this.highlightOutlineColor.value() : this.outlineColor.value());

        renderer.text(shapedText, size.width() / 2f, size.height() / 2f, TextOrigin.Horizontal.VISUAL_CENTER, TextOrigin.Vertical.LOGICAL_CENTER);
    }

    @Override
    protected void onComponentMouseEnter() {
        over = true;
        updateShapedText();
    }

    @Override
    protected boolean onComponentKeyDown(KeyEvent event) {
        if (event.key() == Key.ENTER) {
            this.editing = false;
            try {
                value(Double.parseDouble(this.field.text()));
            } catch (Exception ignored) {}
        }

        if (this.editing) {
            this.field.onComponentKeyDown(event);
        }
        return this.editing;
    }

    @Override
    protected void onComponentFocusGained() {
        if (this.editing) {
            this.field.onComponentFocusGained();
        }
    }

    @Override
    protected void onComponentFocusLost() {
        if (this.editing) {
            this.field.onComponentFocusLost();
        }
    }

    @Override
    protected boolean onComponentCharTyped(CharEvent event) {
        if (this.editing) {
            this.field.onComponentCharTyped(event);
        }

        return this.editing;
    }

    @Override
    protected boolean onComponentMouseDown(MouseButtonEvent event, Size size) {
        if (this.editing) {
            this.field.onComponentMouseDown(event, size);
        }
        if (event.button() != MouseButton.LEFT) {
            return false;
        }

        this.holding = true;
        this.prevMouseX = -Float.MAX_VALUE;
        updateShapedText();
        return true;
    }

    @Override
    protected boolean onComponentMouseMove(MouseMoveEvent event, Size size) {
        if (this.editing) {
            this.field.onComponentMouseMove(event, size);
        } else if (this.holding) {
            if (this.prevMouseX != -Float.MAX_VALUE) {
                float delta = event.x() - this.prevMouseX;

                value(value + step * Math.signum(delta));
                this.updateShapedText();
                changed = true;
            }
            this.prevMouseX = event.x();
        }

        return this.editing;
    }

    @Override
    protected boolean onComponentMouseUp(MouseButtonEvent event, Size size) {
        this.holding = false;
        updateShapedText();

        if (changed) {
            changed = false;
            return true;
        }

        this.editing = true;
        this.field.onComponentFocusGained();
        this.field.cursor(String.valueOf(this.value).length());
        this.field.onComponentMouseUp(event, size);
        return true;
    }

    @Override
    protected void onComponentMouseLeave() {
        over = false;
    }

    @Override
    protected void onComponentRemoved() {
        this.field.onComponentRemoved();
    }

    protected void onComponentAdded() {
        this.field.setRivet(this.rivet(), this);
        this.updateShapedText();
    }

    private void updateShapedText() {
        if (rivet() == null) {
            return;
        }

        String text = String.format(FormatUtils.formatDecimalString(this.numberFormat.value(), this.step), value) + " " + suffix;
        Color textColor;
        if (this.disabled()) textColor = this.disabledTextColor.value();
        else textColor = holding ? holdingTextColor.value() : over ? highlightTextColor.value() : this.textColor.value();
        this.shapedText = this.usedFont().shapeText(text, textColor);
    }


    private Font usedFont() {
        return this.font != null ? this.font : this.rivet().backend().font();
    }

    @Override
    public Size computeIdealSize(Size constraints) {
        float textHeight = this.usedFont().height();
        return new Size(
                textHeight * 10 + this.field.innerPadding().value().horizontal(),
                textHeight + this.field.innerPadding().value().vertical()
        );
    }

    @Override
    public void requestLayoutRecalculation() {
        if (this.parent() != null) this.parent().requestLayoutRecalculation();
    }

    @Override
    public Size contentSize() {
        if (this.field instanceof Parent parent) {
            return parent.contentSize();
        }
        return Size.EMPTY;
    }

    @Override
    public List<Component> children() {
        return List.of(this.field);
    }

    @Override
    public Rectangle childBounds(Component component) {
        if (this.field == component) {
            Rectangle bounds = this.relativeBounds();
            return new Rectangle(0, 0, bounds.width(), bounds.height());
        }
        return Rectangle.EMPTY;
    }
}
