package net.lenni0451.rivet.component.impl.tabcontainer;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.DecoratedContainer;
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
    private boolean hovered = false;
    private boolean active = false;

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

    @Override
    protected void onComponentAdded() {
        if (this.parent() instanceof DecoratedContainer decoratedContainer) {
            decoratedContainer.innerPadding(this.innerPadding.value());
        }
    }

    @Override
    public void onThemeChanged() {
        if (this.parent() instanceof DecoratedContainer decoratedContainer) {
            decoratedContainer.innerPadding(this.innerPadding.value());
            decoratedContainer.requestLayoutRecalculation();
        }
    }

    @Override
    protected boolean onComponentMouseEnter() {
        this.hovered = true;
        return true;
    }

    @Override
    protected void onComponentMouseLeave() {
        this.hovered = false;
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        if (!this.active && this.hovered && event.button().equals(MouseButton.LEFT)) {
            this.clickListener.run();
            this.active = true;
        }
        return true;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        Color color;
        Color outlineColor;
        if (this.active) {
            color = this.activeColor.value();
            outlineColor = this.activeOutlineColor.value();
        } else if (this.hovered) {
            color = this.hoverColor.value();
            outlineColor = this.hoverOutlineColor.value();
        } else {
            color = this.inactiveColor.value();
            outlineColor = this.inactiveOutlineColor.value();
        }
        Corners corners = this.cornerRadius.value();
        renderer.fillRoundedRect(0, 0, size.width(), size.height(), corners.topLeft(), corners.bottomLeft(), corners.bottomRight(), corners.topRight(), color);
        float outlineWidth = this.outlineWidth.value();
        if (outlineWidth > 0) {
            renderer.outlineRoundedRect(0, 0, size.width(), size.height(), corners.topLeft(), corners.bottomLeft(), corners.bottomRight(), corners.topRight(), outlineWidth, outlineColor);
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return Size.EMPTY;
    }

}
