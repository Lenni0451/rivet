package net.lenni0451.rivet.component.base;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseListener;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public class Button extends Component implements MouseListener, Renderable {

    private final Component child;
    private final Consumer<MouseButtonEvent> clickListener;
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
    @Setter
    private Padding innerPadding = new Padding(20, 5, 20, 5);
    private boolean hovered = false;
    private final Set<MouseButton> pressed = new HashSet<>();
    private long hoverStateChange = 0;

    public Button(final Rivet rivet, final Component child, final Consumer<MouseButtonEvent> clickListener) {
        super(rivet);
        this.child = child;
        this.clickListener = clickListener;

        this.cornerRadius = new ThemeOption<>(rivet, Theme.BUTTON_CORNER_RADIUS);
        this.outlineWidth = new ThemeOption<>(rivet, Theme.BUTTON_OUTLINE_WIDTH);
        this.inactiveColor = new ThemeOption<>(rivet, Theme.BUTTON_INACTIVE_COLOR);
        this.inactiveOutlineColor = new ThemeOption<>(rivet, Theme.BUTTON_INACTIVE_OUTLINE_COLOR);
        this.activeColor = new ThemeOption<>(rivet, Theme.BUTTON_ACTIVE_COLOR);
        this.activeOutlineColor = new ThemeOption<>(rivet, Theme.BUTTON_ACTIVE_OUTLINE_COLOR);
        this.clickColor = new ThemeOption<>(rivet, Theme.BUTTON_CLICK_COLOR);
        this.clickOutlineColor = new ThemeOption<>(rivet, Theme.BUTTON_CLICK_OUTLINE_COLOR);
        this.animationDuration = new ThemeOption<>(rivet, Theme.BUTTON_ANIMATION_DURATION);
    }

    @Override
    public void onMouseEnter() {
        this.hovered = true;
        this.hoverStateChange = System.currentTimeMillis();
    }

    @Override
    public void onMouseLeave() {
        this.hovered = false;
        this.hoverStateChange = System.currentTimeMillis();
    }

    @Override
    public void onMouseDown(final MouseButtonEvent event, final Size size) {
        this.pressed.add(event.button());
    }

    @Override
    public void onMouseUp(final MouseButtonEvent event, final Size size) {
        if (this.hovered) {
            this.clickListener.accept(event);
        }
        this.pressed.remove(event.button());
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float cornerRadius = Math.min(this.cornerRadius.value(), Math.min(size.width(), size.height()) / 2F);
        float outlineWidth = this.outlineWidth.value();
        float animationProgress = MathUtils.clamp((System.currentTimeMillis() - this.hoverStateChange) / (float) this.animationDuration.value(), 0, 1);
        Color color;
        Color outlineColor;
        if (this.pressed.isEmpty()) {
            color = this.hovered ? Color.interpolate(animationProgress, this.inactiveColor.value(), this.activeColor.value()) : Color.interpolate(animationProgress, this.activeColor.value(), this.inactiveColor.value());
            outlineColor = this.hovered ? Color.interpolate(animationProgress, this.inactiveOutlineColor.value(), this.activeOutlineColor.value()) : Color.interpolate(animationProgress, this.activeOutlineColor.value(), this.inactiveOutlineColor.value());
        } else {
            color = this.clickColor.value();
            outlineColor = this.clickOutlineColor.value();
        }
        renderer.fillOptimizedRoundedRect(0, 0, size.width(), size.height(), cornerRadius, color);
        if (outlineWidth > 0) {
            renderer.outlineOptimizedRoundedRect(0, 0, size.width(), size.height(), cornerRadius, outlineWidth, outlineColor);
        }

        if (this.child instanceof Renderable renderable) {
            float width = size.width() - this.innerPadding.left() - this.innerPadding.right();
            float height = size.height() - this.innerPadding.top() - this.innerPadding.bottom();

            renderer.translate(this.innerPadding.left(), this.innerPadding.top(), () -> {
                renderer.scissor(0, 0, width, height, () -> {
                    renderable.render(renderer, new Size(width, height));
                });
            });
        }
    }

    @Override
    public void computeIdealSize() {
        this.child.computeIdealSize();
        this.idealSize = new Size(
                this.child.idealSize().width() + this.innerPadding.left() + this.innerPadding.right(),
                this.child.idealSize().height() + this.innerPadding.top() + this.innerPadding.bottom()
        );
    }

    @Override
    public void computeLayout(final Size size) {
        this.child.computeLayout(new Size(
                size.width() - this.innerPadding.left() - this.innerPadding.right(),
                size.height() - this.innerPadding.top() - this.innerPadding.bottom()
        ));
    }

}
