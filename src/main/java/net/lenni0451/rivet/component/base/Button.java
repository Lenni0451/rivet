package net.lenni0451.rivet.component.base;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.animation.Animation;
import net.lenni0451.commons.animation.AnimationDirection;
import net.lenni0451.commons.animation.EasingBehavior;
import net.lenni0451.commons.animation.easing.EasingFunction;
import net.lenni0451.commons.animation.easing.EasingMode;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public class Button extends Component {

    @Getter
    private final Component child;
    @Getter
    @Setter
    private Consumer<MouseButtonEvent> clickListener;
    @Getter
    private final ThemeOption<Integer> cornerRadius;
    @Getter
    private final ThemeOption<Integer> outlineWidth;
    @Getter
    private final ThemeOption<Color> inactiveColor;
    @Getter
    private final ThemeOption<Color> inactiveOutlineColor;
    @Getter
    private final ThemeOption<Color> activeColor;
    @Getter
    private final ThemeOption<Color> activeOutlineColor;
    @Getter
    private final ThemeOption<Color> clickColor;
    @Getter
    private final ThemeOption<Color> clickOutlineColor;
    @Getter
    private final ThemeOption<Integer> animationDuration;
    @Getter
    private final ThemeOption<Padding> innerPadding;
    private boolean hovered = false;
    private final Set<MouseButton> pressed = new HashSet<>();
    private final Animation hoverAnimation;

    public Button(final Rivet rivet, final Component child, final Consumer<MouseButtonEvent> clickListener) {
        this(rivet, child, c -> {}, clickListener);
    }

    public <C extends Component> Button(final Rivet rivet, final C child, final Consumer<C> childConsumer, final Consumer<MouseButtonEvent> initializer) {
        super(rivet);
        this.child = child;
        childConsumer.accept(child);
        this.clickListener = initializer;

        this.cornerRadius = new ThemeOption<>(rivet, Theme.BUTTON_CORNER_RADIUS);
        this.outlineWidth = new ThemeOption<>(rivet, Theme.BUTTON_OUTLINE_WIDTH);
        this.inactiveColor = new ThemeOption<>(rivet, Theme.BUTTON_INACTIVE_COLOR);
        this.inactiveOutlineColor = new ThemeOption<>(rivet, Theme.BUTTON_INACTIVE_OUTLINE_COLOR);
        this.activeColor = new ThemeOption<>(rivet, Theme.BUTTON_ACTIVE_COLOR);
        this.activeOutlineColor = new ThemeOption<>(rivet, Theme.BUTTON_ACTIVE_OUTLINE_COLOR);
        this.clickColor = new ThemeOption<>(rivet, Theme.BUTTON_CLICK_COLOR);
        this.clickOutlineColor = new ThemeOption<>(rivet, Theme.BUTTON_CLICK_OUTLINE_COLOR);
        this.animationDuration = new ThemeOption<>(rivet, Theme.BUTTON_ANIMATION_DURATION);
        this.innerPadding = new ThemeOption<>(rivet, Theme.BUTTON_INNER_PADDING);

        this.hoverAnimation = new Animation()
                .frame(EasingFunction.SINE, EasingMode.EASE_IN_OUT, 0, 1, this.animationDuration.value(), EasingBehavior.KEEP)
                .finish(AnimationDirection.BACKWARDS);
    }

    @Override
    protected void onComponentMouseEnter() {
        this.hovered = true;
        this.hoverAnimation.runInDirection(AnimationDirection.FORWARDS);
    }

    @Override
    protected void onComponentMouseLeave() {
        this.hovered = false;
        this.hoverAnimation.runInDirection(AnimationDirection.BACKWARDS);
    }

    @Override
    public boolean onComponentMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
        this.pressed.add(event.button());
        return true;
    }

    @Override
    public boolean onComponentMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
        if (this.hovered) {
            this.clickListener.accept(event);
        }
        this.pressed.remove(event.button());
        return true;
    }

    @Override
    public void render(final Renderer renderer, final Rectangle bounds) {
        float cornerRadius = Math.min(this.cornerRadius.value(), Math.min(bounds.width(), bounds.height()) / 2F);
        float outlineWidth = this.outlineWidth.value();
        float animationProgress = this.hoverAnimation.getValue();
        Color color;
        Color outlineColor;
        if (this.pressed.isEmpty()) {
            color = Color.interpolate(animationProgress, this.inactiveColor.value(), this.activeColor.value());
            outlineColor = Color.interpolate(animationProgress, this.inactiveOutlineColor.value(), this.activeOutlineColor.value());
        } else {
            color = this.clickColor.value();
            outlineColor = this.clickOutlineColor.value();
        }
        renderer.fillOptimizedRoundedRect(0, 0, bounds.width(), bounds.height(), cornerRadius, color);
        if (outlineWidth > 0) {
            renderer.outlineOptimizedRoundedRect(0, 0, bounds.width(), bounds.height(), cornerRadius, outlineWidth, outlineColor);
        }

        float width = bounds.width() - this.innerPadding.value().horizontal();
        float height = bounds.height() - this.innerPadding.value().vertical();
        renderer.translate(this.innerPadding.value().left(), this.innerPadding.value().top(), () -> {
            renderer.componentBounds(0, 0, width, height, () -> {
                this.child.render(renderer, new Rectangle(
                        bounds.x() + this.innerPadding.value().left(), bounds.y() + this.innerPadding.value().top(),
                        width, height
                ));
            });
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return this.child.computeIdealSize(constraints.minus(this.innerPadding.value().horizontal(), this.innerPadding.value().vertical()))
                .plus(this.innerPadding.value().horizontal(), this.innerPadding.value().vertical());
    }

    @Override
    public void computeLayout(final Size size) {
        this.child.computeLayout(size.minus(this.innerPadding.value().horizontal(), this.innerPadding.value().vertical()));
    }

}
