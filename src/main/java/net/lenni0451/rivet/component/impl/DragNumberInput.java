package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.Interpolator;
import net.lenni0451.rivet.animation.StateTransition;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import net.lenni0451.rivet.utils.FormatUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class DragNumberInput extends Component implements Parent {

    @Getter
    @Setter
    private double min;
    @Getter
    @Setter
    private double max;
    @Getter
    private double step;
    @Getter
    private double value;
    @Getter
    private final ListenerList<Consumer<Double>> valueChangeListener = new ListenerList<>();
    private final Label valueLabel;

    private boolean dragging = false;
    private boolean hovered = false;
    private float mouseDownX = 0;
    private float mouseDownY = 0;
    private double dragStartValue = 0;
    private String cachedFormatString = null;

    @Getter
    private final ThemeOption<Color> backgroundColor = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> textColor = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> outlineColor = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Float> outlineWidth = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Float> cornerRadius = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_CORNER_RADIUS);
    @Getter
    private final ThemeOption<Padding> innerPadding = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_INNER_PADDING);
    @Getter
    private final ThemeOption<String> valueFormat = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_VALUE_FORMAT);
    @Getter
    private final ThemeOption<Color> hoverBackgroundColor = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_HOVER_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> hoverOutlineColor = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_HOVER_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> clickBackgroundColor = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_CLICK_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> clickOutlineColor = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_CLICK_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledBackgroundColor = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_DISABLED_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> disabledTextColor = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_DISABLED_TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOutlineColor = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_DISABLED_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<AnimationConfig> hoverAnimationConfig = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_HOVER_ANIMATION);
    @Getter
    private final ThemeOption<AnimationConfig> clickAnimationConfig = new ThemeOption<>(this, Theme.DRAG_NUMBER_INPUT_CLICK_ANIMATION);

    private StateTransition<Color, State> backgroundColorTransition;
    private StateTransition<Color, State> outlineColorTransition;

    public DragNumberInput(final double min, final double max, final double value) {
        this(min, max, 1, value);
    }

    public DragNumberInput(final double min, final double max, final double step, final double value) {
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;

        this.valueLabel = new Label("Not initialized");

        this.textColor.initListener().add(this.valueLabel.textColor()::set);
        this.valueFormat.initListener().add(f -> this.cachedFormatString = null);
        this.disabledTextColor.initListener().add(this.valueLabel.disabledTextColor()::set);
    }

    public Font font() {
        return this.valueLabel.font();
    }

    public DragNumberInput font(@Nullable final Font font) {
        this.valueLabel.font(font);
        return this;
    }

    public DragNumberInput step(final double step) {
        this.step = step;
        this.cachedFormatString = null;
        return this;
    }

    public DragNumberInput value(final double value) {
        double newValue = MathUtils.clamp(value, this.min, this.max);
        if (this.value != newValue) {
            this.value = newValue;
            this.valueLabel.text(this.formatValue(this.value));
            this.valueChangeListener.callVoid(c -> c.accept(this.value));
        }
        return this;
    }

    @Deprecated(forRemoval = true)
    public Label valueLabel() {
        return this.valueLabel;
    }

    private State state() {
        if (this.disabled()) {
            return State.DISABLED;
        } else if (this.dragging) {
            return State.DRAGGED;
        } else {
            return this.hovered ? State.HOVERED : State.INACTIVE;
        }
    }

    @Override
    protected void onComponentAdded() {
        this.valueLabel.setRivet(this.rivet(), this);
        this.valueLabel.text(this.formatValue(this.value));

        this.backgroundColorTransition = new StateTransition<>(
                this,
                this::state,
                (start, target) -> {
                    if (start.equals(State.DRAGGED) || target.equals(State.DRAGGED)) {
                        return this.clickAnimationConfig.value();
                    } else {
                        return this.hoverAnimationConfig.value();
                    }
                },
                () -> switch (this.state()) {
                    case INACTIVE -> this.backgroundColor.value();
                    case HOVERED -> this.hoverBackgroundColor.value();
                    case DRAGGED -> this.clickBackgroundColor.value();
                    case DISABLED -> this.disabledBackgroundColor.value();
                },
                Interpolator.COLOR
        );
        this.outlineColorTransition = new StateTransition<>(
                this,
                this::state,
                (start, target) -> {
                    if (start.equals(State.DRAGGED) || target.equals(State.DRAGGED)) {
                        return this.clickAnimationConfig.value();
                    } else {
                        return this.hoverAnimationConfig.value();
                    }
                },
                () -> switch (this.state()) {
                    case INACTIVE -> this.outlineColor.value();
                    case HOVERED -> this.hoverOutlineColor.value();
                    case DRAGGED -> this.clickOutlineColor.value();
                    case DISABLED -> this.disabledOutlineColor.value();
                },
                Interpolator.COLOR
        );
    }

    @Override
    protected void onComponentRemoved() {
        this.valueLabel.setRivet(null, null);
        this.dragging = false;
        this.hovered = false;
    }

    @Override
    protected void onComponentDisabled() {
        this.valueLabel.disabled(true);
        this.dragging = false;
        this.hovered = false;
    }

    @Override
    protected void onComponentEnabled() {
        this.valueLabel.disabled(false);
    }

    @Override
    protected void onComponentThemeChanged() {
        this.valueLabel.onThemeChanged();
        this.cachedFormatString = null;
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
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            this.dragging = true;
            this.mouseDownX = event.x();
            this.mouseDownY = event.y();
            this.dragStartValue = this.value;
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            this.dragging = false;
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
        if (this.dragging) {
            float deltaX = event.x() - this.mouseDownX;
            float deltaY = event.y() - this.mouseDownY;
            double deltaValue = (deltaX - deltaY) * this.step;
            double newValue = this.dragStartValue + deltaValue;
            newValue = net.lenni0451.rivet.utils.MathUtils.snap(newValue, this.min, this.max, this.step);
            this.value(newValue);
        }
        return true;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        Padding padding = this.innerPadding.value();
        Color background = this.backgroundColorTransition.value();
        Color outline = this.outlineColorTransition.value();
        float outlineWidth = this.outlineWidth.value();
        float cornerRadius = this.cornerRadius.value();

        renderer.optimizedFillRoundedRect(0, 0, size.width(), size.height(), cornerRadius, background);
        if (outlineWidth > 0) {
            renderer.optimizedOutlineRoundedRect(0, 0, size.width(), size.height(), cornerRadius, outlineWidth, outline);
        }

        renderer.translate(padding.left(), padding.top(), () -> {
            float width = size.width() - padding.horizontal();
            float height = size.height() - padding.vertical();
            this.valueLabel.updatePosition(new Rectangle(renderer.xOffset(), renderer.yOffset(), width, height));
            renderer.componentBounds(0, 0, width, height, () -> {
                this.valueLabel.render(renderer, new Size(width, height));
            });
        });
    }

    private String formatValue(final double value) {
        if (this.cachedFormatString == null) {
            this.cachedFormatString = FormatUtils.formatDecimalString(this.valueFormat.value(), this.step);
        }
        try {
            return String.format(this.cachedFormatString, value);
        } catch (Throwable t) {
            return Double.toString(value);
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        Padding padding = this.innerPadding.value();
        return this.valueLabel.computeIdealSize(constraints.minus(padding.horizontal(), padding.vertical())).plus(padding.horizontal(), padding.vertical());
    }

    @Override
    public void computeLayout(final Size size) {
        Padding padding = this.innerPadding.value();
        this.valueLabel.computeLayout(size.minus(padding.horizontal(), padding.vertical()));
    }

    @Override
    public void requestLayoutRecalculation() {
        if (this.parent() != null) this.parent().requestLayoutRecalculation();
    }

    @Override
    public Size contentSize() {
        return Size.EMPTY;
    }

    @Override
    public List<Component> children() {
        return List.of(this.valueLabel);
    }

    @Override
    public Rectangle childBounds(final Component component) {
        if (component == this.valueLabel) {
            Rectangle bounds = this.relativeBounds();
            Padding padding = this.innerPadding.value();
            return new Rectangle(
                    padding.left(), padding.top(),
                    bounds.width() - padding.horizontal(),
                    bounds.height() - padding.vertical()
            );
        }
        return Rectangle.EMPTY;
    }


    private enum State {
        INACTIVE, HOVERED, DRAGGED, DISABLED
    }

}
