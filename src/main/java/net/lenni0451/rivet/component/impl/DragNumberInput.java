package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.Interpolator;
import net.lenni0451.rivet.animation.StateTransition;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.component.ParentContainer;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.property.DoubleProperty;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import net.lenni0451.rivet.utils.FormatUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class DragNumberInput extends ParentContainer {

    @Getter
    private final Component child;
    @Nullable
    private UpdatedLabel updatedLabel;
    @Getter
    private final DoubleProperty min;
    @Getter
    private final DoubleProperty max;
    @Getter
    private final DoubleProperty step;
    @Getter
    private final DoubleProperty value;

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
    private final ThemeOption<Corners> cornerRadius = new ThemeOption<>(this, Theme.DragNumberInput.CORNER_RADIUS);
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
        this.min = new DoubleProperty(min);
        this.max = new DoubleProperty(max);
        this.step = new DoubleProperty(step);
        this.value = new DoubleProperty(value);

        this.step.updateListener().add(v -> {
            if (this.updatedLabel != null) {
                this.updatedLabel.step = v;
                this.updatedLabel.cachedFormatString = null;
            }
        });
        this.value.addValidator(v -> MathUtils.clamp(v, this.min.get(), this.max.get()));
        this.value.updateListener().add(v -> {
            if (this.updatedLabel != null) {
                this.updatedLabel.update(v);
            }
        });
        this.min.updateListener().add(m -> this.value.revalidate());
        this.max.updateListener().add(m -> this.value.revalidate());

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
        this.value.changeListener().add(val -> valueUpdater.accept(child, val));
        valueUpdater.accept(child, this.value.get());
    }

    public final DragNumberInput registerUpdatedLabel(@Nullable final UpdatedLabel updatedLabel) {
        this.updatedLabel = updatedLabel;
        if (updatedLabel != null) {
            updatedLabel.step = this.step.get();
            if (this.rivet() != null) {
                updatedLabel.valueFormat = this.valueFormat.value();
                updatedLabel.cachedFormatString = null;
                updatedLabel.update(this.value.get());
            }
        }
        return this;
    }

    private State state() {
        if (this.disabled().get()) {
            return State.DISABLED;
        } else if (this.dragging) {
            return State.DRAGGED;
        } else {
            return this.hovered ? State.HOVERED : State.INACTIVE;
        }
    }

    @Override
    protected void onAddedInternal() {
        super.onAddedInternal();
        if (this.updatedLabel != null) {
            this.updatedLabel.update(this.value.get());
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
    protected void onRemovedInternal() {
        super.onRemovedInternal();
        this.dragging = false;
        this.hovered = false;
    }

    @Override
    protected void onDisabledInternal() {
        super.onDisabledInternal();
        this.dragging = false;
        this.hovered = false;
    }

    @Override
    protected void onThemeChangedInternal() {
        super.onThemeChangedInternal();
        if (this.updatedLabel != null) {
            this.updatedLabel.cachedFormatString = null;
        }
    }

    @Override
    protected void onMouseEnterInternal() {
        this.hovered = true;
    }

    @Override
    protected void onMouseLeaveInternal() {
        super.onMouseLeaveInternal();
        this.hovered = false;
    }

    @Override
    protected boolean onMouseDownInternal(final MouseButtonEvent event, final Size size) {
        if (!super.onMouseDownInternal(event, size)) {
            if (event.button().equals(MouseButton.LEFT) && !this.value.readOnly()) {
                this.dragging = true;
                this.mouseDownX = event.x();
                this.mouseDownY = event.y();
                this.dragStartValue = this.value.get();
            }
        }
        return true;
    }

    @Override
    protected boolean onMouseUpInternal(final MouseButtonEvent event, final Size size) {
        super.onMouseUpInternal(event, size);
        if (event.button().equals(MouseButton.LEFT)) {
            this.dragging = false;
        }
        return true;
    }

    @Override
    protected boolean onMouseMoveInternal(final MouseMoveEvent event, final Size size) {
        super.onMouseMoveInternal(event, size);
        if (this.dragging) {
            double step = this.step.get();
            float deltaX = event.x() - this.mouseDownX;
            float deltaY = event.y() - this.mouseDownY;
            double deltaValue = (deltaX - deltaY) * step;
            double newValue = this.dragStartValue + deltaValue;
            newValue = net.lenni0451.rivet.utils.MathUtils.snap(newValue, this.min.get(), this.max.get(), step);
            this.value.set(newValue);
        }
        return true;
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        Padding padding = this.innerPadding.value();
        Color background = this.backgroundColorTransition.value();
        Color outline = this.outlineColorTransition.value();
        float outlineWidth = this.outlineWidth.value();
        Corners cornerRadius = this.cornerRadius.value();

        renderer.optimizedFillRoundedRect(0, 0, size.width(), size.height(), cornerRadius, background);
        if (outlineWidth > 0) {
            renderer.optimizedOutlineRoundedRect(0, 0, size.width(), size.height(), cornerRadius, outlineWidth, outline);
        }

        renderer.translate(padding.left(), padding.top(), () -> {
            Size innerSize = size.minus(padding).clamp(this.child);
            renderer.componentBounds(0, 0, innerSize.width(), innerSize.height(), () -> {
                this.child.render(
                        renderer,
                        innerSize,
                        net.lenni0451.rivet.utils.MathUtils.relativizeVisibleArea(visibleArea, padding.left(), padding.top(), innerSize)
                );
            });
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        Padding padding = this.innerPadding.value();
        return this.child.computeIdealSize(constraints.minus(padding)).clamp(this.child).plus(padding);
    }

    @Override
    public void computeLayout(final Size size) {
        Padding padding = this.innerPadding.value();
        this.child.computeLayout(size.minus(padding).clamp(this.child));
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
            Size containerSize = this.relativeBounds().size();
            Padding padding = this.innerPadding.value();
            Size innerSize = containerSize.minus(padding).clamp(this.child);
            return new Rectangle(
                    padding.left(), padding.top(),
                    innerSize
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
