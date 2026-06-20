package net.lenni0451.rivet.component.container.tabcontainer;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.Interpolator;
import net.lenni0451.rivet.animation.StateTransition;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.DecoratedContainer;
import net.lenni0451.rivet.input.mouse.ClickOn;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import org.jetbrains.annotations.ApiStatus;

@Getter
@Accessors(fluent = true, chain = true, makeFinal = true)
public class TabBackground extends Component {

    private final Runnable clickListener;
    private final ThemeOption<Corners> cornerRadius;
    private final ThemeOption<Float> outlineWidth;
    private final ThemeOption<Color> inactiveColor;
    private final ThemeOption<Color> inactiveOutlineColor;
    private final ThemeOption<Color> activeColor;
    private final ThemeOption<Color> activeOutlineColor;
    private final ThemeOption<Color> hoverColor;
    private final ThemeOption<Color> hoverOutlineColor;
    private final ThemeOption<Padding> innerPadding;
    private final ThemeOption<AnimationConfig> hoverAnimationConfig;
    private final ThemeOption<AnimationConfig> activeAnimationConfig;
    private final ThemeOption<ClickOn> clickOn;
    private boolean hovered = false;
    private boolean active = false;

    private StateTransition<Color, State> backgroundColor;
    private StateTransition<Color, State> outlineColor;

    public TabBackground(final Runnable clickListener) {
        this.clickListener = clickListener;
        this.cornerRadius = new ThemeOption<>(this, Theme.TAB_CORNER_RADIUS);
        this.outlineWidth = new ThemeOption<>(this, Theme.TAB_OUTLINE_WIDTH);
        this.inactiveColor = new ThemeOption<>(this, Theme.TAB_INACTIVE_COLOR);
        this.inactiveOutlineColor = new ThemeOption<>(this, Theme.TAB_INACTIVE_OUTLINE_COLOR);
        this.activeColor = new ThemeOption<>(this, Theme.TAB_ACTIVE_COLOR);
        this.activeOutlineColor = new ThemeOption<>(this, Theme.TAB_ACTIVE_OUTLINE_COLOR);
        this.hoverColor = new ThemeOption<>(this, Theme.TAB_HOVER_COLOR);
        this.hoverOutlineColor = new ThemeOption<>(this, Theme.TAB_HOVER_OUTLINE_COLOR);
        this.innerPadding = new ThemeOption<>(this, Theme.TAB_INNER_PADDING);
        this.hoverAnimationConfig = new ThemeOption<>(this, Theme.TAB_HOVER_ANIMATION);
        this.activeAnimationConfig = new ThemeOption<>(this, Theme.TAB_ACTIVE_ANIMATION);
        this.clickOn = new ThemeOption<>(this, Theme.TAB_CLICK_ON);

        this.innerPadding.changeListener().add(padding -> {
            if (this.parent() instanceof DecoratedContainer decoratedContainer) {
                decoratedContainer.innerPadding(padding);
                decoratedContainer.requestLayoutRecalculation();
            }
        });
    }

    @ApiStatus.Internal
    public void activate() {
        this.active = true;
    }

    @ApiStatus.Internal
    public void deactivate() {
        this.active = false;
    }

    private State state() {
        if (this.active) {
            return State.ACTIVE;
        } else {
            return this.hovered ? State.HOVERED : State.INACTIVE;
        }
    }

    @Override
    protected void onComponentAdded() {
        if (this.parent() instanceof DecoratedContainer decoratedContainer) {
            decoratedContainer.innerPadding(this.innerPadding.value());
        }
        this.backgroundColor = new StateTransition<>(
                this,
                this::state,
                (start, target) -> {
                    if (start.equals(State.ACTIVE) || target.equals(State.ACTIVE)) {
                        return this.activeAnimationConfig.value();
                    } else {
                        return this.hoverAnimationConfig.value();
                    }
                },
                () -> switch (this.state()) {
                    case INACTIVE -> this.inactiveColor.value();
                    case HOVERED -> this.hoverColor.value();
                    case ACTIVE -> this.activeColor.value();
                },
                Interpolator.COLOR
        );
        this.outlineColor = new StateTransition<>(
                this,
                this::state,
                (start, target) -> {
                    if (start.equals(State.ACTIVE) || target.equals(State.ACTIVE)) {
                        return this.activeAnimationConfig.value();
                    } else {
                        return this.hoverAnimationConfig.value();
                    }
                },
                () -> switch (this.state()) {
                    case INACTIVE -> this.inactiveOutlineColor.value();
                    case HOVERED -> this.hoverOutlineColor.value();
                    case ACTIVE -> this.activeOutlineColor.value();
                },
                Interpolator.COLOR
        );
    }

    @Override
    public void onThemeChanged() {
        if (this.parent() instanceof DecoratedContainer decoratedContainer) {
            decoratedContainer.innerPadding(this.innerPadding.value());
            decoratedContainer.requestLayoutRecalculation();
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
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
        if (!this.active && event.button().equals(MouseButton.LEFT)) {
            if (this.clickOn.value().equals(ClickOn.DOWN) || this.clickOn.value().equals(ClickOn.BOTH)) {
                this.clickListener.run();
                this.active = true;
            }
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        if (!this.active && this.hovered && event.button().equals(MouseButton.LEFT)) {
            if (this.clickOn.value().equals(ClickOn.UP) || this.clickOn.value().equals(ClickOn.BOTH)) {
                this.clickListener.run();
                this.active = true;
            }
        }
        return true;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        Corners corners = this.cornerRadius.value();
        renderer.fillRoundedRect(
                0, 0, size.width(), size.height(),
                corners.topLeft(), corners.bottomLeft(), corners.bottomRight(), corners.topRight(),
                this.backgroundColor.value()
        );
        float outlineWidth = this.outlineWidth.value();
        if (outlineWidth > 0) {
            renderer.outlineRoundedRect(
                    0, 0, size.width(), size.height(),
                    corners.topLeft(), corners.bottomLeft(), corners.bottomRight(), corners.topRight(),
                    outlineWidth,
                    this.outlineColor.value()
            );
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return Size.EMPTY;
    }


    private enum State {
        INACTIVE, HOVERED, ACTIVE
    }

}
