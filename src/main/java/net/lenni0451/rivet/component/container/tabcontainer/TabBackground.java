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
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import org.jetbrains.annotations.ApiStatus;

@Getter
@Accessors(fluent = true, chain = true, makeFinal = true)
public class TabBackground extends Component {

    private final Runnable clickListener;
    private final ThemeOption<Corners> cornerRadius = new ThemeOption<>(this, Theme.Tab.CORNER_RADIUS);
    private final ThemeOption<Float> outlineWidth = new ThemeOption<>(this, Theme.Tab.OUTLINE_WIDTH);
    private final ThemeOption<Color> inactiveColor = new ThemeOption<>(this, Theme.Tab.INACTIVE_COLOR);
    private final ThemeOption<Color> inactiveOutlineColor = new ThemeOption<>(this, Theme.Tab.INACTIVE_OUTLINE_COLOR);
    private final ThemeOption<Color> activeColor = new ThemeOption<>(this, Theme.Tab.ACTIVE_COLOR);
    private final ThemeOption<Color> activeOutlineColor = new ThemeOption<>(this, Theme.Tab.ACTIVE_OUTLINE_COLOR);
    private final ThemeOption<Color> hoverColor = new ThemeOption<>(this, Theme.Tab.HOVER_COLOR);
    private final ThemeOption<Color> hoverOutlineColor = new ThemeOption<>(this, Theme.Tab.HOVER_OUTLINE_COLOR);
    private final ThemeOption<Padding> innerPadding = new ThemeOption<>(this, Theme.Tab.INNER_PADDING);
    private final ThemeOption<AnimationConfig> hoverAnimationConfig = new ThemeOption<>(this, Theme.Tab.HOVER_ANIMATION);
    private final ThemeOption<AnimationConfig> activeAnimationConfig = new ThemeOption<>(this, Theme.Tab.ACTIVE_ANIMATION);
    private final ThemeOption<ClickOn> selectOn = new ThemeOption<>(this, Theme.Tab.SELECT_ON);
    private boolean hovered = false;
    private boolean active = false;

    private StateTransition<Color, State> backgroundColor;
    private StateTransition<Color, State> outlineColor;

    public TabBackground(final Runnable clickListener) {
        this.clickListener = clickListener;

        this.innerPadding.initListener().add(padding -> {
            if (this.parent() instanceof DecoratedContainer decoratedContainer) {
                decoratedContainer.innerPadding(padding);
                decoratedContainer.requestLayoutRecalculation();
            }
        });
    }

    @ApiStatus.Internal
    public final void activate() {
        this.active = true;
    }

    @ApiStatus.Internal
    public final void deactivate() {
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
    protected void onAddedInternal() {
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
    protected void onMouseEnterInternal() {
        this.hovered = true;
    }

    @Override
    protected void onMouseLeaveInternal() {
        this.hovered = false;
    }

    @Override
    protected boolean onMouseDownInternal(final MouseButtonEvent event, final Size size) {
        if (!this.active && event.button().equals(MouseButton.LEFT)) {
            if (this.selectOn.value().equals(ClickOn.DOWN) || this.selectOn.value().equals(ClickOn.BOTH)) {
                this.clickListener.run();
                this.active = true;
            }
        }
        return true;
    }

    @Override
    protected boolean onMouseUpInternal(final MouseButtonEvent event, final Size size) {
        if (!this.active && this.hovered && event.button().equals(MouseButton.LEFT)) {
            if (this.selectOn.value().equals(ClickOn.UP) || this.selectOn.value().equals(ClickOn.BOTH)) {
                this.clickListener.run();
                this.active = true;
            }
        }
        return true;
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
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
