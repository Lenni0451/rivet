package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.Interpolator;
import net.lenni0451.rivet.animation.StateTransition;
import net.lenni0451.rivet.animation.Transition;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.input.mouse.ClickOn;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class ToggleSwitch extends Component {

    @Getter
    private boolean toggled;
    @Getter
    private final ListenerList<Consumer<Boolean>> toggleListener = new ListenerList<>();
    private boolean hovered = false;
    private boolean pressed = false;

    @Getter
    private final ThemeOption<Float> cornerRadius = new ThemeOption<>(this, Theme.ToggleSwitch.CORNER_RADIUS);
    @Getter
    private final ThemeOption<Float> outlineWidth = new ThemeOption<>(this, Theme.ToggleSwitch.OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Color> inactiveColor = new ThemeOption<>(this, Theme.ToggleSwitch.INACTIVE_COLOR);
    @Getter
    private final ThemeOption<Color> inactiveOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.INACTIVE_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> activeColor = new ThemeOption<>(this, Theme.ToggleSwitch.ACTIVE_COLOR);
    @Getter
    private final ThemeOption<Color> activeOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.ACTIVE_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> inactiveThumbColor = new ThemeOption<>(this, Theme.ToggleSwitch.INACTIVE_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> activeThumbColor = new ThemeOption<>(this, Theme.ToggleSwitch.ACTIVE_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> inactiveThumbOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.INACTIVE_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> activeThumbOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.ACTIVE_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Float> thumbOutlineWidth = new ThemeOption<>(this, Theme.ToggleSwitch.THUMB_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Float> thumbRatio = new ThemeOption<>(this, Theme.ToggleSwitch.THUMB_RATIO);
    @Getter
    private final ThemeOption<Color> hoverInactiveColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_INACTIVE_COLOR);
    @Getter
    private final ThemeOption<Color> hoverActiveColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_ACTIVE_COLOR);
    @Getter
    private final ThemeOption<Color> hoverInactiveOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_INACTIVE_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> hoverActiveOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_ACTIVE_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> hoverInactiveThumbColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_INACTIVE_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> hoverActiveThumbColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_ACTIVE_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> hoverInactiveThumbOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_INACTIVE_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> hoverActiveThumbOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_ACTIVE_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledInactiveColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_INACTIVE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledActiveColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_ACTIVE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledInactiveOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_INACTIVE_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledActiveOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_ACTIVE_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledInactiveThumbColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_INACTIVE_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> disabledActiveThumbColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_ACTIVE_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> disabledInactiveThumbOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_INACTIVE_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledActiveThumbOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_ACTIVE_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<AnimationConfig> hoverAnimationConfig = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_ANIMATION);
    @Getter
    private final ThemeOption<AnimationConfig> toggleAnimationConfig = new ThemeOption<>(this, Theme.ToggleSwitch.TOGGLE_ANIMATION);
    @Getter
    private final ThemeOption<ClickOn> clickOn = new ThemeOption<>(this, Theme.ToggleSwitch.CLICK_ON);
    @Getter
    private final ThemeOption<Boolean> thumbEncased = new ThemeOption<>(this, Theme.ToggleSwitch.THUMB_ENCASED);
    @Getter
    private final ThemeOption<Float> railRatio = new ThemeOption<>(this, Theme.ToggleSwitch.RAIL_RATIO);

    private StateTransition<Color, VisualState> backgroundColorTransition;
    private StateTransition<Color, VisualState> outlineColorTransition;
    private StateTransition<Color, VisualState> thumbColorTransition;
    private StateTransition<Color, VisualState> thumbOutlineColorTransition;
    private Transition<Float> toggleProgress;

    public ToggleSwitch() {
        this(false);
    }

    public ToggleSwitch(final boolean toggled) {
        this.toggled = toggled;
    }

    public final ToggleSwitch toggled(final boolean toggled) {
        if (this.toggled != toggled) {
            this.toggled = toggled;
            this.toggleListener.callVoid(c -> c.accept(this.toggled));
        }
        return this;
    }

    private VisualState visualState() {
        return VisualState.get(this.toggled, this.disabled(), this.hovered);
    }

    @Override
    protected void onComponentAdded() {
        this.backgroundColorTransition = new StateTransition<>(
                this,
                this::visualState,
                (start, target) -> {
                    if (start.toggled != target.toggled) {
                        return this.toggleAnimationConfig.value();
                    } else {
                        return this.hoverAnimationConfig.value();
                    }
                },
                () -> switch (this.visualState()) {
                    case INACTIVE_OFF -> this.inactiveColor.value();
                    case INACTIVE_ON -> this.activeColor.value();
                    case HOVERED_OFF -> this.hoverInactiveColor.value();
                    case HOVERED_ON -> this.hoverActiveColor.value();
                    case DISABLED_OFF -> this.disabledInactiveColor.value();
                    case DISABLED_ON -> this.disabledActiveColor.value();
                },
                Interpolator.COLOR
        );
        this.outlineColorTransition = new StateTransition<>(
                this,
                this::visualState,
                (start, target) -> {
                    if (start.toggled != target.toggled) {
                        return this.toggleAnimationConfig.value();
                    } else {
                        return this.hoverAnimationConfig.value();
                    }
                },
                () -> switch (this.visualState()) {
                    case INACTIVE_OFF -> this.inactiveOutlineColor.value();
                    case INACTIVE_ON -> this.activeOutlineColor.value();
                    case HOVERED_OFF -> this.hoverInactiveOutlineColor.value();
                    case HOVERED_ON -> this.hoverActiveOutlineColor.value();
                    case DISABLED_OFF -> this.disabledInactiveOutlineColor.value();
                    case DISABLED_ON -> this.disabledActiveOutlineColor.value();
                },
                Interpolator.COLOR
        );
        this.thumbColorTransition = new StateTransition<>(
                this,
                this::visualState,
                (start, target) -> {
                    if (start.toggled != target.toggled) {
                        return this.toggleAnimationConfig.value();
                    } else {
                        return this.hoverAnimationConfig.value();
                    }
                },
                () -> switch (this.visualState()) {
                    case INACTIVE_OFF -> this.inactiveThumbColor.value();
                    case INACTIVE_ON -> this.activeThumbColor.value();
                    case HOVERED_OFF -> this.hoverInactiveThumbColor.value();
                    case HOVERED_ON -> this.hoverActiveThumbColor.value();
                    case DISABLED_OFF -> this.disabledInactiveThumbColor.value();
                    case DISABLED_ON -> this.disabledActiveThumbColor.value();
                },
                Interpolator.COLOR
        );
        this.thumbOutlineColorTransition = new StateTransition<>(
                this,
                this::visualState,
                (start, target) -> {
                    if (start.toggled != target.toggled) {
                        return this.toggleAnimationConfig.value();
                    } else {
                        return this.hoverAnimationConfig.value();
                    }
                },
                () -> switch (this.visualState()) {
                    case INACTIVE_OFF -> this.inactiveThumbOutlineColor.value();
                    case INACTIVE_ON -> this.activeThumbOutlineColor.value();
                    case HOVERED_OFF -> this.hoverInactiveThumbOutlineColor.value();
                    case HOVERED_ON -> this.hoverActiveThumbOutlineColor.value();
                    case DISABLED_OFF -> this.disabledInactiveThumbOutlineColor.value();
                    case DISABLED_ON -> this.disabledActiveThumbOutlineColor.value();
                },
                Interpolator.COLOR
        );
        this.toggleProgress = new Transition<>(
                this,
                () -> this.toggled ? 1F : 0F,
                this.toggleAnimationConfig::value,
                Interpolator.FLOAT
        );
    }

    @Override
    protected void onComponentRemoved() {
        this.hovered = false;
        this.pressed = false;
    }

    @Override
    protected void onComponentDisabled() {
        this.onComponentRemoved();
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
            this.pressed = true;
            if (this.clickOn.value().equals(ClickOn.DOWN) || this.clickOn.value().equals(ClickOn.BOTH)) {
                this.toggled(!this.toggled);
            }
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            boolean wasPressed = this.pressed;
            this.pressed = false;
            if (this.hovered && wasPressed && (this.clickOn.value().equals(ClickOn.UP) || this.clickOn.value().equals(ClickOn.BOTH))) {
                this.toggled(!this.toggled);
            }
        }
        return true;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float cornerRadius = this.cornerRadius.value();
        float outlineWidth = this.outlineWidth.value();
        boolean encased = this.thumbEncased.value();

        float progress = this.toggleProgress.value();
        float thumbRadius = size.height() / 2 * this.thumbRatio.value();
        float thumbX;
        if (encased) {
            thumbX = MathUtils.lerp(size.height() / 2, size.width() - size.height() / 2, progress);
        } else {
            thumbX = MathUtils.lerp(thumbRadius, size.width() - thumbRadius, progress);
        }
        float thumbY = size.height() / 2;

        float railHeight = encased ? size.height() : size.height() * this.railRatio.value();
        float railWidth = encased ? size.width() : size.width() - 2 * thumbRadius + railHeight;
        float railX = encased ? 0 : thumbRadius - railHeight / 2;
        float railY = encased ? 0 : (size.height() - railHeight) / 2;

        renderer.optimizedFillRoundedRect(railX, railY, railWidth, railHeight, cornerRadius, this.backgroundColorTransition.value());
        if (outlineWidth > 0) {
            renderer.optimizedOutlineRoundedRect(railX, railY, railWidth, railHeight, cornerRadius, outlineWidth, this.outlineColorTransition.value());
        }

        renderer.fillCircle(thumbX, thumbY, thumbRadius, this.thumbColorTransition.value());
        float thumbOutlineWidth = this.thumbOutlineWidth.value();
        if (thumbOutlineWidth > 0) {
            renderer.outlineCircle(thumbX, thumbY, thumbRadius, thumbOutlineWidth, this.thumbOutlineColorTransition.value());
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        float fontSize = this.rivet().backend().font().height();
        return new Size(fontSize * 2, fontSize);
    }


    @RequiredArgsConstructor
    private enum VisualState {
        INACTIVE_OFF(false), INACTIVE_ON(true),
        HOVERED_OFF(false), HOVERED_ON(true),
        DISABLED_OFF(false), DISABLED_ON(true);

        private final boolean toggled;

        public static VisualState get(final boolean toggled, final boolean disabled, final boolean hovered) {
            if (disabled) {
                return toggled ? DISABLED_ON : DISABLED_OFF;
            } else if (hovered) {
                return toggled ? HOVERED_ON : HOVERED_OFF;
            } else {
                return toggled ? INACTIVE_ON : INACTIVE_OFF;
            }
        }
    }

}
