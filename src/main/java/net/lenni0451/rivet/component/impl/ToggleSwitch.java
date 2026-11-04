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
import net.lenni0451.rivet.input.mouse.ClickOn;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.property.BooleanProperty;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;


@Accessors(fluent = true, chain = true, makeFinal = true)
public class ToggleSwitch extends Component {

    @Getter
    private final BooleanProperty toggled;
    private boolean hovered = false;
    private boolean pressed = false;

    @Getter
    private final ThemeOption<Corners> cornerRadius = new ThemeOption<>(this, Theme.ToggleSwitch.CORNER_RADIUS);
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
        this.toggled = new BooleanProperty(toggled);
    }

    public final ToggleSwitch toggled(final boolean toggled) {
        this.toggled.set(toggled);
        return this;
    }

    private VisualState visualState() {
        return VisualState.get(this.toggled.get(), this.disabled().get(), this.hovered);
    }

    @Override
    protected void onAddedInternal() {
        this.backgroundColorTransition = new StateTransition<>(
                this,
                this::visualState,
                (start, target) -> {
                    if (start.toggled != target.toggled) {
                        return this.toggleAnimationConfig.get();
                    } else {
                        return this.hoverAnimationConfig.get();
                    }
                },
                () -> switch (this.visualState()) {
                    case NORMAL_OFF -> this.offColor.get();
                    case NORMAL_ON -> this.onColor.get();
                    case HOVERED_OFF -> this.hoverOffColor.get();
                    case HOVERED_ON -> this.hoverOnColor.get();
                    case DISABLED_OFF -> this.disabledOffColor.get();
                    case DISABLED_ON -> this.disabledOnColor.get();
                },
                Interpolator.COLOR
        );
        this.outlineColorTransition = new StateTransition<>(
                this,
                this::visualState,
                (start, target) -> {
                    if (start.toggled != target.toggled) {
                        return this.toggleAnimationConfig.get();
                    } else {
                        return this.hoverAnimationConfig.get();
                    }
                },
                () -> switch (this.visualState()) {
                    case NORMAL_OFF -> this.offOutlineColor.get();
                    case NORMAL_ON -> this.onOutlineColor.get();
                    case HOVERED_OFF -> this.hoverOffOutlineColor.get();
                    case HOVERED_ON -> this.hoverOnOutlineColor.get();
                    case DISABLED_OFF -> this.disabledOffOutlineColor.get();
                    case DISABLED_ON -> this.disabledOnOutlineColor.get();
                },
                Interpolator.COLOR
        );
        this.thumbColorTransition = new StateTransition<>(
                this,
                this::visualState,
                (start, target) -> {
                    if (start.toggled != target.toggled) {
                        return this.toggleAnimationConfig.get();
                    } else {
                        return this.hoverAnimationConfig.get();
                    }
                },
                () -> switch (this.visualState()) {
                    case NORMAL_OFF -> this.offThumbColor.get();
                    case NORMAL_ON -> this.onThumbColor.get();
                    case HOVERED_OFF -> this.hoverOffThumbColor.get();
                    case HOVERED_ON -> this.hoverOnThumbColor.get();
                    case DISABLED_OFF -> this.disabledOffThumbColor.get();
                    case DISABLED_ON -> this.disabledOnThumbColor.get();
                },
                Interpolator.COLOR
        );
        this.thumbOutlineColorTransition = new StateTransition<>(
                this,
                this::visualState,
                (start, target) -> {
                    if (start.toggled != target.toggled) {
                        return this.toggleAnimationConfig.get();
                    } else {
                        return this.hoverAnimationConfig.get();
                    }
                },
                () -> switch (this.visualState()) {
                    case NORMAL_OFF -> this.offThumbOutlineColor.get();
                    case NORMAL_ON -> this.onThumbOutlineColor.get();
                    case HOVERED_OFF -> this.hoverOffThumbOutlineColor.get();
                    case HOVERED_ON -> this.hoverOnThumbOutlineColor.get();
                    case DISABLED_OFF -> this.disabledOffThumbOutlineColor.get();
                    case DISABLED_ON -> this.disabledOnThumbOutlineColor.get();
                },
                Interpolator.COLOR
        );
        this.toggleProgress = new Transition<>(
                this,
                () -> this.toggled.get() ? 1F : 0F,
                this.toggleAnimationConfig::get,
                Interpolator.FLOAT
        );
    }

    @Override
    protected void onRemovedInternal() {
        this.hovered = false;
        this.pressed = false;
    }

    @Override
    protected void onDisabledInternal() {
        this.onRemovedInternal();
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
        if (event.button().equals(MouseButton.LEFT)) {
            this.pressed = true;
            if (this.toggleOn.get().equals(ClickOn.DOWN) || this.toggleOn.get().equals(ClickOn.BOTH)) {
                this.toggled.update(t -> !t);
            }
        }
        return true;
    }

    @Override
    protected boolean onMouseUpInternal(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            boolean wasPressed = this.pressed;
            this.pressed = false;
            if (this.hovered && wasPressed && (this.toggleOn.get().equals(ClickOn.UP) || this.toggleOn.get().equals(ClickOn.BOTH))) {
                this.toggled.update(t -> !t);
            }
        }
        return true;
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        Corners cornerRadius = this.cornerRadius.get();
        float outlineWidth = this.outlineWidth.get();
        boolean encased = this.thumbEncased.get();

        float progress = this.toggleProgress.value();
        float thumbRadius = size.height() / 2 * this.thumbRatio.get();
        float thumbX;
        if (encased) {
            thumbX = MathUtils.lerp(size.height() / 2, size.width() - size.height() / 2, progress);
        } else {
            thumbX = MathUtils.lerp(thumbRadius, size.width() - thumbRadius, progress);
        }
        float thumbY = size.height() / 2;

        float railHeight = encased ? size.height() : size.height() * this.railRatio.get();
        float railWidth = encased ? size.width() : size.width() - 2 * thumbRadius + railHeight;
        float railX = encased ? 0 : thumbRadius - railHeight / 2;
        float railY = encased ? 0 : (size.height() - railHeight) / 2;

        renderer.optimizedFillRoundedRect(railX, railY, railWidth, railHeight, cornerRadius, this.backgroundColorTransition.value());
        if (outlineWidth > 0) {
            renderer.optimizedOutlineRoundedRect(railX, railY, railWidth, railHeight, cornerRadius, outlineWidth, this.outlineColorTransition.value());
        }

        renderer.fillCircle(thumbX, thumbY, thumbRadius, this.thumbColorTransition.value());
        float thumbOutlineWidth = this.thumbOutlineWidth.get();
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
