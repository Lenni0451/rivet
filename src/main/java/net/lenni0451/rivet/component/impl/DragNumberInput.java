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
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.component.ParentContainer;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class DragNumberInput extends ParentContainer {

    @Getter
    private final Component child;
    @Nullable
    private UpdatedLabel updatedLabel;
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

    private boolean dragging = false;
    private boolean hovered = false;
    private float mouseDownX = 0;
    private float mouseDownY = 0;
    private double dragStartValue = 0;

    @Getter
    private final ThemeOption<Color> backgroundColor = new ThemeOption<>(this, Theme.DragNumberInput.BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> outlineColor = new ThemeOption<>(this, Theme.DragNumberInput.OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Float> outlineWidth = new ThemeOption<>(this, Theme.DragNumberInput.OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Float> cornerRadius = new ThemeOption<>(this, Theme.DragNumberInput.CORNER_RADIUS);
    @Getter
    private final ThemeOption<Padding> innerPadding = new ThemeOption<>(this, Theme.DragNumberInput.INNER_PADDING);
    @Getter
    private final ThemeOption<String> valueFormat = new ThemeOption<>(this, Theme.DragNumberInput.VALUE_FORMAT);
    @Getter
    private final ThemeOption<Color> hoverBackgroundColor = new ThemeOption<>(this, Theme.DragNumberInput.HOVER_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> hoverOutlineColor = new ThemeOption<>(this, Theme.DragNumberInput.HOVER_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> clickBackgroundColor = new ThemeOption<>(this, Theme.DragNumberInput.CLICK_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> clickOutlineColor = new ThemeOption<>(this, Theme.DragNumberInput.CLICK_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledBackgroundColor = new ThemeOption<>(this, Theme.DragNumberInput.DISABLED_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOutlineColor = new ThemeOption<>(this, Theme.DragNumberInput.DISABLED_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<AnimationConfig> hoverAnimationConfig = new ThemeOption<>(this, Theme.DragNumberInput.HOVER_ANIMATION);
    @Getter
    private final ThemeOption<AnimationConfig> clickAnimationConfig = new ThemeOption<>(this, Theme.DragNumberInput.CLICK_ANIMATION);

    private StateTransition<Color, State> backgroundColorTransition;
    private StateTransition<Color, State> outlineColorTransition;

    public DragNumberInput(final double min, final double max, final double value) {
        this(min, max, 1, value);
    }

    public DragNumberInput(final double min, final double max, final double step, final double value) {
        this(new UpdatedLabel(), min, max, step, value);
    }

    public DragNumberInput(final Component child, final double min, final double max, final double value) {
        this(child, min, max, 1, value);
    }

    public DragNumberInput(final Component child, final double min, final double max, final double step, final double value) {
        this.child = child;
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;

        if (child instanceof UpdatedLabel label) {
            this.registerUpdatedLabel(label);
        }
        this.valueFormat.initListener().add(format -> {
            if (this.updatedLabel != null) {
                this.updatedLabel.valueFormat = format;
                this.updatedLabel.cachedFormatString = null;
            }
        });
    }

    public <C extends Component> DragNumberInput(final C child, final BiConsumer<C, Double> valueUpdater, final double min, final double max, final double value) {
        this(child, valueUpdater, min, max, 1, value);
    }

    public <C extends Component> DragNumberInput(final C child, final BiConsumer<C, Double> valueUpdater, final double min, final double max, final double step, final double value) {
        this(child, min, max, step, value);
        this.valueChangeListener.add(val -> valueUpdater.accept(child, val));
        valueUpdater.accept(child, this.value);
    }

    public final DragNumberInput step(final double step) {
        this.step = step;
        if (this.updatedLabel != null) {
            this.updatedLabel.step = step;
            this.updatedLabel.cachedFormatString = null;
        }
        return this;
    }

    public final DragNumberInput value(final double value) {
        double newValue = MathUtils.clamp(value, this.min, this.max);
        if (this.value != newValue) {
            this.value = newValue;
            if (this.updatedLabel != null) {
                this.updatedLabel.update(this.value);
            }
            this.valueChangeListener.callVoid(c -> c.accept(this.value));
        }
        return this;
    }

    public final DragNumberInput registerUpdatedLabel(@Nullable final UpdatedLabel updatedLabel) {
        this.updatedLabel = updatedLabel;
        if (updatedLabel != null) {
            updatedLabel.step = this.step;
            if (this.rivet() != null) {
                updatedLabel.valueFormat = this.valueFormat.value();
                updatedLabel.cachedFormatString = null;
                updatedLabel.update(this.value);
            }
        }
        return this;
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
        super.onComponentAdded();
        if (this.updatedLabel != null) {
            this.updatedLabel.update(this.value);
        }

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
        super.onComponentRemoved();
        this.dragging = false;
        this.hovered = false;
    }

    @Override
    protected void onComponentDisabled() {
        super.onComponentDisabled();
        this.dragging = false;
        this.hovered = false;
    }

    @Override
    protected void onComponentThemeChanged() {
        super.onComponentThemeChanged();
        if (this.updatedLabel != null) {
            this.updatedLabel.cachedFormatString = null;
        }
    }

    @Override
    protected void onComponentMouseEnter() {
        this.hovered = true;
    }

    @Override
    protected void onComponentMouseLeave() {
        super.onComponentMouseLeave();
        this.hovered = false;
    }

    @Override
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
        if (!super.onComponentMouseDown(event, size)) {
            if (event.button().equals(MouseButton.LEFT)) {
                this.dragging = true;
                this.mouseDownX = event.x();
                this.mouseDownY = event.y();
                this.dragStartValue = this.value;
            }
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        super.onComponentMouseUp(event, size);
        if (event.button().equals(MouseButton.LEFT)) {
            this.dragging = false;
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
        super.onComponentMouseMove(event, size);
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
            renderer.componentBounds(0, 0, width, height, () -> {
                this.child.render(renderer, new Size(width, height));
            });
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        Padding padding = this.innerPadding.value();
        return this.child.computeIdealSize(constraints.minus(padding)).plus(padding);
    }

    @Override
    public void computeLayout(final Size size) {
        Padding padding = this.innerPadding.value();
        this.child.computeLayout(size.minus(padding));
        this.updateChildPositions();
    }

    @Override
    public Size contentSize() {
        if (this.child instanceof Parent parent) {
            Size parentContentSize = parent.contentSize();
            if (!parentContentSize.equals(Size.EMPTY)) {
                return parentContentSize.plus(this.innerPadding.value());
            }
        }
        return Size.EMPTY;
    }

    @Override
    public List<Component> children() {
        return List.of(this.child);
    }

    @Override
    public Rectangle childBounds(final Component component) {
        if (component == this.child) {
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


    public static class UpdatedLabel extends Label {
        private double step;
        private String valueFormat;
        private String cachedFormatString = null;

        public UpdatedLabel() {
            this("Not initialized");
        }

        public UpdatedLabel(final String text) {
            super(text);
        }

        public final void update(final double value) {
            this.text(this.formatValue(value));
        }

        private String formatValue(final double value) {
            if (this.cachedFormatString == null) {
                this.cachedFormatString = FormatUtils.formatDecimalString(this.valueFormat, this.step);
            }
            try {
                return String.format(this.cachedFormatString, value);
            } catch (Throwable t) {
                return Double.toString(value);
            }
        }
    }

    private enum State {
        INACTIVE, HOVERED, DRAGGED, DISABLED
    }

}
