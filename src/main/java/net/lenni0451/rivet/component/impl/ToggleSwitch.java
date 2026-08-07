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
import net.lenni0451.rivet.event.ListenerList;
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
    private final ThemeOption<Color> offColor = new ThemeOption<>(this, Theme.ToggleSwitch.OFF_COLOR);
    @Getter
    private final ThemeOption<Color> offOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.OFF_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> onColor = new ThemeOption<>(this, Theme.ToggleSwitch.ON_COLOR);
    @Getter
    private final ThemeOption<Color> onOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.ON_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> offThumbColor = new ThemeOption<>(this, Theme.ToggleSwitch.OFF_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> onThumbColor = new ThemeOption<>(this, Theme.ToggleSwitch.ON_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> offThumbOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.OFF_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> onThumbOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.ON_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Float> thumbOutlineWidth = new ThemeOption<>(this, Theme.ToggleSwitch.THUMB_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Float> thumbRatio = new ThemeOption<>(this, Theme.ToggleSwitch.THUMB_RATIO);
    @Getter
    private final ThemeOption<Color> hoverOffColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_OFF_COLOR);
    @Getter
    private final ThemeOption<Color> hoverOnColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_ON_COLOR);
    @Getter
    private final ThemeOption<Color> hoverOffOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_OFF_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> hoverOnOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_ON_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> hoverOffThumbColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_OFF_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> hoverOnThumbColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_ON_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> hoverOffThumbOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_OFF_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> hoverOnThumbOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_ON_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOffColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_OFF_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOnColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_ON_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOffOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_OFF_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOnOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_ON_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOffThumbColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_OFF_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOnThumbColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_ON_THUMB_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOffThumbOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_OFF_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOnThumbOutlineColor = new ThemeOption<>(this, Theme.ToggleSwitch.DISABLED_ON_THUMB_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<AnimationConfig> hoverAnimationConfig = new ThemeOption<>(this, Theme.ToggleSwitch.HOVER_ANIMATION);
    @Getter
    private final ThemeOption<AnimationConfig> toggleAnimationConfig = new ThemeOption<>(this, Theme.ToggleSwitch.TOGGLE_ANIMATION);
    @Getter
    private final ThemeOption<ClickOn> toggleOn = new ThemeOption<>(this, Theme.ToggleSwitch.TOGGLE_ON);
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
        return this.toggled(toggled, true);
    }

    public final ToggleSwitch toggled(final boolean toggled, final boolean fireListeners) {
        if (this.toggled != toggled) {
            this.toggled = toggled;
            if (fireListeners) {
                this.toggleListener.call(c -> c.accept(this.toggled));
            }
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
                    case NORMAL_OFF -> this.offColor.value();
                    case NORMAL_ON -> this.onColor.value();
                    case HOVERED_OFF -> this.hoverOffColor.value();
                    case HOVERED_ON -> this.hoverOnColor.value();
                    case DISABLED_OFF -> this.disabledOffColor.value();
                    case DISABLED_ON -> this.disabledOnColor.value();
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
                    case NORMAL_OFF -> this.offOutlineColor.value();
                    case NORMAL_ON -> this.onOutlineColor.value();
                    case HOVERED_OFF -> this.hoverOffOutlineColor.value();
                    case HOVERED_ON -> this.hoverOnOutlineColor.value();
                    case DISABLED_OFF -> this.disabledOffOutlineColor.value();
                    case DISABLED_ON -> this.disabledOnOutlineColor.value();
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
                    case NORMAL_OFF -> this.offThumbColor.value();
                    case NORMAL_ON -> this.onThumbColor.value();
                    case HOVERED_OFF -> this.hoverOffThumbColor.value();
                    case HOVERED_ON -> this.hoverOnThumbColor.value();
                    case DISABLED_OFF -> this.disabledOffThumbColor.value();
                    case DISABLED_ON -> this.disabledOnThumbColor.value();
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
                    case NORMAL_OFF -> this.offThumbOutlineColor.value();
                    case NORMAL_ON -> this.onThumbOutlineColor.value();
                    case HOVERED_OFF -> this.hoverOffThumbOutlineColor.value();
                    case HOVERED_ON -> this.hoverOnThumbOutlineColor.value();
                    case DISABLED_OFF -> this.disabledOffThumbOutlineColor.value();
                    case DISABLED_ON -> this.disabledOnThumbOutlineColor.value();
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
            if (this.toggleOn.value().equals(ClickOn.DOWN) || this.toggleOn.value().equals(ClickOn.BOTH)) {
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
            if (this.hovered && wasPressed && (this.toggleOn.value().equals(ClickOn.UP) || this.toggleOn.value().equals(ClickOn.BOTH))) {
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
        NORMAL_OFF(false), NORMAL_ON(true),
        HOVERED_OFF(false), HOVERED_ON(true),
        DISABLED_OFF(false), DISABLED_ON(true);

        private final boolean toggled;

        public static VisualState get(final boolean toggled, final boolean disabled, final boolean hovered) {
            if (disabled) {
                return toggled ? DISABLED_ON : DISABLED_OFF;
            } else if (hovered) {
                return toggled ? HOVERED_ON : HOVERED_OFF;
            } else {
                return toggled ? NORMAL_ON : NORMAL_OFF;
            }
        }
    }

}
