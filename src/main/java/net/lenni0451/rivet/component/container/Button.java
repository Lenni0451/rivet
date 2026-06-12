package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.Interpolator;
import net.lenni0451.rivet.animation.Transition;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.component.impl.Label;
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
    private final ListenerList<ClickListener> clickListener;
    @Getter
    private final ThemeOption<Float> cornerRadius;
    @Getter
    private final ThemeOption<Float> outlineWidth;
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
    private final ThemeOption<Color> disabledColor;
    @Getter
    private final ThemeOption<Color> disabledOutlineColor;
    @Getter
    private final ThemeOption<AnimationConfig> animationConfig;
    @Getter
    private final ThemeOption<Padding> innerPadding;
    @Getter
    private final ThemeOption<ClickOn> clickOn;
    private boolean hovered = false;
    private final Set<MouseButton> pressed = new HashSet<>();
    private Transition<Color> color;
    private Transition<Color> outlineColor;

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
        this.clickListener = new ListenerList<>();
        this.clickListener.add(clickListener);

        this.cornerRadius = new ThemeOption<>(this, Theme.BUTTON_CORNER_RADIUS);
        this.outlineWidth = new ThemeOption<>(this, Theme.BUTTON_OUTLINE_WIDTH);
        this.inactiveColor = new ThemeOption<>(this, Theme.BUTTON_INACTIVE_COLOR);
        this.inactiveOutlineColor = new ThemeOption<>(this, Theme.BUTTON_INACTIVE_OUTLINE_COLOR);
        this.activeColor = new ThemeOption<>(this, Theme.BUTTON_ACTIVE_COLOR);
        this.activeOutlineColor = new ThemeOption<>(this, Theme.BUTTON_ACTIVE_OUTLINE_COLOR);
        this.clickColor = new ThemeOption<>(this, Theme.BUTTON_CLICK_COLOR);
        this.clickOutlineColor = new ThemeOption<>(this, Theme.BUTTON_CLICK_OUTLINE_COLOR);
        this.disabledColor = new ThemeOption<>(this, Theme.BUTTON_DISABLED_COLOR);
        this.disabledOutlineColor = new ThemeOption<>(this, Theme.BUTTON_DISABLED_OUTLINE_COLOR);
        this.animationConfig = new ThemeOption<>(this, Theme.BUTTON_HOVER_ANIMATION);
        this.innerPadding = new ThemeOption<>(this, Theme.BUTTON_INNER_PADDING);
        this.clickOn = new ThemeOption<>(this, Theme.BUTTON_CLICK_ON);
    }

    @Override
    protected void onComponentAdded() {
        this.child.setRivet(this.rivet(), this);

        this.color = new Transition<>(
                this,
                () -> {
                    if (this.disabled()) {
                        return this.disabledColor.value();
                    } else if (this.pressed.isEmpty()) {
                        return this.hovered ? this.activeColor.value() : this.inactiveColor.value();
                    } else {
                        return this.clickColor.value();
                    }
                },
                this.animationConfig::value,
                Interpolator.COLOR
        );
        this.outlineColor = new Transition<>(
                this,
                () -> {
                    if (this.disabled()) {
                        return this.disabledOutlineColor.value();
                    } else if (this.pressed.isEmpty()) {
                        return this.hovered ? this.activeOutlineColor.value() : this.inactiveOutlineColor.value();
                    } else {
                        return this.clickOutlineColor.value();
                    }
                },
                this.animationConfig::value,
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
    public void onThemeChanged() {
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
                this.clickListener.callVoid(listener -> listener.onClick(event));
            }
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        this.pressed.remove(event.button());
        if (this.hovered && this.handledButtons.contains(event.button()) && (this.clickOn.value().equals(ClickOn.UP) || this.clickOn.value().equals(ClickOn.BOTH))) {
            this.clickListener.callVoid(listener -> listener.onClick(event));
        }
        return true;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float cornerRadius = Math.min(this.cornerRadius.value(), Math.min(size.width(), size.height()) / 2F);
        float outlineWidth = this.outlineWidth.value();
        renderer.optimizedFillRoundedRect(0, 0, size.width(), size.height(), cornerRadius, this.color.getValue());
        if (outlineWidth > 0) {
            renderer.optimizedOutlineRoundedRect(0, 0, size.width(), size.height(), cornerRadius, outlineWidth, this.outlineColor.getValue());
        }

        float width = size.width() - this.innerPadding.value().horizontal();
        float height = size.height() - this.innerPadding.value().vertical();
        renderer.translate(this.innerPadding.value().left(), this.innerPadding.value().top(), () -> {
            renderer.componentBounds(0, 0, width, height, () -> {
                this.child.render(renderer, new Size(width, height));
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

    @Override
    public void requestLayoutRecalculation() {
        if (this.parent() != null) this.parent().requestLayoutRecalculation();
    }

    @Override
    public Size contentSize() {
        if (this.child instanceof Parent parent) {
            return parent.contentSize().plus(this.innerPadding.value().horizontal(), this.innerPadding.value().vertical());
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
            Rectangle bounds = this.relativeBounds();
            return new Rectangle(
                    this.innerPadding.value().left(),
                    this.innerPadding.value().top(),
                    bounds.width() - this.innerPadding.value().horizontal(),
                    bounds.height() - this.innerPadding.value().vertical()
            );
        }
        return Rectangle.EMPTY;
    }


    @FunctionalInterface
    public interface ClickListener {
        void onClick(final MouseButtonEvent event);
    }

    public enum ClickOn {
        DOWN, UP, BOTH
    }

}
