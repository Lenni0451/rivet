package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.Interpolator;
import net.lenni0451.rivet.animation.StateTransition;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.input.mouse.ClickOn;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class Button extends Component implements Parent {

    @Getter
    private final Component child;
    @Getter
    private final Set<MouseButton> handledButtons = EnumSet.of(MouseButton.LEFT);
    @Getter
    private final ListenerList<ClickListener> clickListener = new ListenerList<>();
    @Getter
    private final ThemeOption<Float> cornerRadius = new ThemeOption<>(this, Theme.Button.CORNER_RADIUS);
    @Getter
    private final ThemeOption<Float> outlineWidth = new ThemeOption<>(this, Theme.Button.OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Color> backgroundColor = new ThemeOption<>(this, Theme.Button.BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> outlineColor = new ThemeOption<>(this, Theme.Button.OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> hoverBackgroundColor = new ThemeOption<>(this, Theme.Button.HOVER_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> hoverOutlineColor = new ThemeOption<>(this, Theme.Button.HOVER_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> clickBackgroundColor = new ThemeOption<>(this, Theme.Button.CLICK_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> clickOutlineColor = new ThemeOption<>(this, Theme.Button.CLICK_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledBackgroundColor = new ThemeOption<>(this, Theme.Button.DISABLED_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOutlineColor = new ThemeOption<>(this, Theme.Button.DISABLED_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<AnimationConfig> hoverAnimationConfig = new ThemeOption<>(this, Theme.Button.HOVER_ANIMATION);
    @Getter
    private final ThemeOption<AnimationConfig> clickAnimationConfig = new ThemeOption<>(this, Theme.Button.CLICK_ANIMATION);
    @Getter
    private final ThemeOption<Padding> innerPadding = new ThemeOption<>(this, Theme.Button.INNER_PADDING);
    @Getter
    private final ThemeOption<ClickOn> clickOn = new ThemeOption<>(this, Theme.Button.CLICK_ON);
    private boolean hovered = false;
    private final Set<MouseButton> pressed = new HashSet<>();
    private StateTransition<Color, State> backgroundColorTransition;
    private StateTransition<Color, State> outlineColorTransition;

    public Button(final String text) {
        this(text, () -> {});
    }

    public Button(final Component child) {
        this(child, () -> {});
    }

    public Button(final String text, final Runnable clickListener) {
        this(text, event -> clickListener.run());
    }

    public Button(final String text, final ClickListener clickListener) {
        this(new Label(text), clickListener);
    }

    public Button(final Component child, final Runnable clickListener) {
        this(child, event -> clickListener.run());
    }

    public Button(final Component child, final ClickListener clickListener) {
        this(child, c -> {}, clickListener);
    }

    public <C extends Component> Button(final C child, final Consumer<C> initializer, final Runnable clickListener) {
        this(child, initializer, event -> clickListener.run());
    }

    public <C extends Component> Button(final C child, final Consumer<C> initializer, final ClickListener clickListener) {
        this.child = child;
        initializer.accept(child);
        this.clickListener.add(clickListener);
    }

    private State state() {
        if (this.disabled()) {
            return State.DISABLED;
        } else if (!this.pressed.isEmpty()) {
            return State.PRESSED;
        } else {
            return this.hovered ? State.HOVERED : State.NORMAL;
        }
    }

    @Override
    protected void onComponentAdded() {
        this.child.setRivet(this.rivet(), this);

        this.backgroundColorTransition = new StateTransition<>(
                this,
                this::state,
                (start, target) -> {
                    if (start.equals(State.PRESSED) || target.equals(State.PRESSED)) {
                        return this.clickAnimationConfig.value();
                    } else {
                        return this.hoverAnimationConfig.value();
                    }
                },
                () -> switch (this.state()) {
                    case NORMAL -> this.backgroundColor.value();
                    case HOVERED -> this.hoverBackgroundColor.value();
                    case PRESSED -> this.clickBackgroundColor.value();
                    case DISABLED -> this.disabledBackgroundColor.value();
                },
                Interpolator.COLOR
        );
        this.outlineColorTransition = new StateTransition<>(
                this,
                this::state,
                (start, target) -> {
                    if (start.equals(State.PRESSED) || target.equals(State.PRESSED)) {
                        return this.clickAnimationConfig.value();
                    } else {
                        return this.hoverAnimationConfig.value();
                    }
                },
                () -> switch (this.state()) {
                    case NORMAL -> this.outlineColor.value();
                    case HOVERED -> this.hoverOutlineColor.value();
                    case PRESSED -> this.clickOutlineColor.value();
                    case DISABLED -> this.disabledOutlineColor.value();
                },
                Interpolator.COLOR
        );
    }

    @Override
    protected void onComponentRemoved() {
        this.child.setRivet(null, null);
        this.hovered = false;
        this.pressed.clear();
    }

    @Override
    protected void onComponentDisabled() {
        this.child.disabled(true);
        this.hovered = false;
        this.pressed.clear();
    }

    @Override
    protected void onComponentEnabled() {
        this.child.disabled(false);
    }

    @Override
    protected void onComponentThemeChanged() {
        this.child.onThemeChanged();
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
        if (this.handledButtons.contains(event.button())) {
            this.pressed.add(event.button());
            if (this.clickOn.value().equals(ClickOn.DOWN) || this.clickOn.value().equals(ClickOn.BOTH)) {
                this.clickListener.call(listener -> listener.onClick(event));
            }
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        this.pressed.remove(event.button());
        if (this.hovered && this.handledButtons.contains(event.button()) && (this.clickOn.value().equals(ClickOn.UP) || this.clickOn.value().equals(ClickOn.BOTH))) {
            this.clickListener.call(listener -> listener.onClick(event));
        }
        return true;
    }

    @Override
    protected void renderComponent(final Renderer renderer, final Size size) {
        float cornerRadius = Math.min(this.cornerRadius.value(), Math.min(size.width(), size.height()) / 2F);
        float outlineWidth = this.outlineWidth.value();
        renderer.optimizedFillRoundedRect(0, 0, size.width(), size.height(), cornerRadius, this.backgroundColorTransition.value());
        if (outlineWidth > 0) {
            renderer.optimizedOutlineRoundedRect(0, 0, size.width(), size.height(), cornerRadius, outlineWidth, this.outlineColorTransition.value());
        }

        Size innerSize = size.minus(this.innerPadding.value()).clamp(this.child);
        renderer.translate(this.innerPadding.value().left(), this.innerPadding.value().top(), () -> {
            renderer.componentBounds(0, 0, innerSize.width(), innerSize.height(), () -> {
                this.child.render(renderer, innerSize);
            });
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return this.child.computeIdealSize(constraints.minus(this.innerPadding.value())).clamp(this.child).plus(this.innerPadding.value());
    }

    @Override
    public void computeLayout(final Size size) {
        this.child.computeLayout(size.minus(this.innerPadding.value()).clamp(this.child));
    }

    @Override
    public void requestLayoutRecalculation() {
        if (this.parent() != null) this.parent().requestLayoutRecalculation();
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
        if (this.child == component) {
            Size containerSize = this.relativeBounds().size();
            Size innerSize = containerSize.minus(this.innerPadding.value()).clamp(this.child);
            return new Rectangle(
                    this.innerPadding.value().left(),
                    this.innerPadding.value().top(),
                    innerSize
            );
        }
        return Rectangle.EMPTY;
    }


    @FunctionalInterface
    public interface ClickListener {
        void onClick(final MouseButtonEvent event);
    }

    private enum State {
        NORMAL, HOVERED, PRESSED, DISABLED
    }

}
