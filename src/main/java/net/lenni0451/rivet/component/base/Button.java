package net.lenni0451.rivet.component.base;

import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseListener;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.function.Consumer;

public class Button extends Component implements MouseListener, Renderable {

    private final Component child;
    private final Consumer<MouseButtonEvent> clickListener;
    private final ThemeOption<Integer> cornerRadius;
    private final ThemeOption<Integer> outlineWidth;
    private final ThemeOption<Color> inactiveColor;
    private final ThemeOption<Color> inactiveOutlineColor;
    private final ThemeOption<Color> activeColor;
    private final ThemeOption<Color> activeOutlineColor;
    private final ThemeOption<Integer> animationDuration;
    private Padding innerPadding = new Padding(20, 5, 20, 5);
    private boolean hovered = false;
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
        this.animationDuration = new ThemeOption<>(rivet, Theme.BUTTON_ANIMATION_DURATION);
    }

    public ThemeOption<Integer> cornerRadius() {
        return this.cornerRadius;
    }

    public ThemeOption<Integer> outlineWidth() {
        return this.outlineWidth;
    }

    public ThemeOption<Color> inactiveColor() {
        return this.inactiveColor;
    }

    public ThemeOption<Color> inactiveOutlineColor() {
        return this.inactiveOutlineColor;
    }

    public ThemeOption<Color> activeColor() {
        return this.activeColor;
    }

    public ThemeOption<Color> activeOutlineColor() {
        return this.activeOutlineColor;
    }

    public ThemeOption<Integer> animationDuration() {
        return this.animationDuration;
    }

    public Padding innerPadding() {
        return this.innerPadding;
    }

    public Button setInnerPadding(final Padding padding) {
        if (!this.innerPadding.equals(padding)) {
            this.innerPadding = padding;
            this.rivet.recalculateNextFrame();
        }
        return this;
    }

    public boolean isHovered() {
        return this.hovered;
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
    public void onMouseUp(final MouseButtonEvent event, final Size size) {
        this.clickListener.accept(event);
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float cornerRadius = Math.min(this.cornerRadius.value(), Math.min(size.width(), size.height()) / 2F);
        float outlineWidth = this.outlineWidth.value();
        float animationProgress = MathUtils.clamp((System.currentTimeMillis() - this.hoverStateChange) / (float) this.animationDuration.value(), 0, 1);
        Color color = this.hovered ? Color.interpolate(animationProgress, this.inactiveColor.value(), this.activeColor.value()) : Color.interpolate(animationProgress, this.activeColor.value(), this.inactiveColor.value());
        Color outlineColor = this.hovered ? Color.interpolate(animationProgress, this.inactiveOutlineColor.value(), this.activeOutlineColor.value()) : Color.interpolate(animationProgress, this.activeOutlineColor.value(), this.inactiveOutlineColor.value());
        if (cornerRadius > 0) {
            renderer.fillRoundedRect(0, 0, size.width(), size.height(), cornerRadius, color);
        } else {
            renderer.fillRect(0, 0, size.width(), size.height(), color);
        }
        if (outlineWidth > 0) {
            if (cornerRadius > 0) {
                renderer.outlineRoundedRect(0, 0, size.width(), size.height(), cornerRadius, outlineWidth, outlineColor);
            } else {
                renderer.outlineRect(0, 0, size.width(), size.height(), outlineWidth, outlineColor);
            }
        }

        if (this.child instanceof Renderable renderable) {
            float width = size.width() - this.innerPadding.left() - this.innerPadding.right();
            float height = size.height() - this.innerPadding.top() - this.innerPadding.bottom();

            renderer.push();
            renderer.translate(this.innerPadding.left(), this.innerPadding.top());
            renderer.pushScissor(0, 0, width, height);
            renderable.render(renderer, new Size(width, height));
            renderer.popScissor();
            renderer.pop();
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
    }

}
