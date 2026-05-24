package net.lenni0451.rivet.component.base;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.input.ContainerMouseHandler;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.function.Consumer;

@Accessors(fluent = true, chain = true)
public class DecoratedContainer extends Component {

    @Getter
    private final Component child;
    private final ContainerMouseHandler<Component> mouseHandler = new ContainerMouseHandler<>();
    @Getter
    @Setter
    private Padding innerPadding;
    @Getter
    @Setter
    private Color backgroundColor;
    @Getter
    @Setter
    private Color backgroundOutlineColor;
    @Getter
    @Setter
    private float backgroundOutlineWidth;
    @Getter
    @Setter
    private float backgroundCornerRadius;

    public DecoratedContainer(final Rivet rivet, final Component child) {
        this(rivet, child, c -> {});
    }

    public <C extends Component> DecoratedContainer(final Rivet rivet, final C child, final Consumer<C> initializer) {
        super(rivet);
        this.child = child;
        initializer.accept(child);

        this.innerPadding = new Padding(5, 5, 5, 5);
        this.backgroundColor = Color.TRANSPARENT;
        this.backgroundOutlineColor = Color.TRANSPARENT;
        this.backgroundOutlineWidth = rivet.backend().getTextHeight() / 8F;
        this.backgroundCornerRadius = 0;
    }

    @Override
    protected void onComponentMouseLeave() {
        this.mouseHandler.onComponentMouseLeave(Component::onMouseLeave);
    }

    @Override
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Rectangle bounds) {
        Rectangle childBounds = bounds.offset(this.innerPadding);
        return this.mouseHandler.onComponentMouseDown(
                event,
                childBounds.withX(this.innerPadding.left()).withY(this.innerPadding.top()).contains(event.x(), event.y()) ? this.child : null,
                component -> {
                    this.rivet.focusedComponent(component);
                    return component.onMouseDown(event.withX(event.x() - this.innerPadding.left()).withY(event.y() - this.innerPadding.left()), childBounds);
                },
                () -> this.backgroundColor.getAlpha() > 0
        );
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Rectangle bounds) {
        return this.mouseHandler.onComponentMouseUp(
                event,
                component -> {
                    Rectangle childBounds = bounds.offset(this.innerPadding);
                    return component.onMouseUp(event.withX(event.x() - this.innerPadding.left()).withY(event.y() - this.innerPadding.left()), childBounds);
                },
                () -> this.backgroundColor.getAlpha() > 0
        );
    }

    @Override
    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Rectangle bounds) {
        Rectangle childBounds = bounds.offset(this.innerPadding);
        return this.mouseHandler.onComponentMouseMove(
                childBounds.contains(event.x(), event.y()) ? this.child : null,
                Component::onMouseEnter,
                Component::onMouseLeave,
                component -> component.onMouseMove(event.withX(event.x() - this.innerPadding.left()).withY(event.y() - this.innerPadding.top()), childBounds),
                () -> false
        );
    }

    @Override
    protected boolean onComponentMouseScroll(final MouseScrollEvent event, final Rectangle bounds) {
        Rectangle childBounds = bounds.offset(this.innerPadding);
        return this.mouseHandler.onComponentMouseScroll(
                childBounds.contains(event.x(), event.y()),
                () -> this.child.onMouseScroll(event.withX(event.x() - this.innerPadding.left()).withY(event.y() - this.innerPadding.top()), childBounds),
                () -> false
        );
    }

    @Override
    public void render(final Renderer renderer, final Rectangle bounds) {
        if (this.backgroundColor.getAlpha() > 0) {
            renderer.fillOptimizedRoundedRect(0, 0, bounds.width(), bounds.height(), this.backgroundCornerRadius, this.backgroundColor);
        }
        if (this.backgroundOutlineColor.getAlpha() > 0) {
            renderer.outlineOptimizedRoundedRect(0, 0, bounds.width(), bounds.height(), this.backgroundCornerRadius, this.backgroundOutlineWidth, this.backgroundOutlineColor);
        }

        float width = bounds.width() - this.innerPadding.horizontal();
        float height = bounds.height() - this.innerPadding.vertical();
        renderer.translate(this.innerPadding.left(), this.innerPadding.top(), () -> {
            renderer.componentBounds(0, 0, width, height, () -> {
                this.child.render(renderer, new Rectangle(
                        bounds.x() + this.innerPadding.left(), bounds.y() + this.innerPadding.top(),
                        width, height
                ));
            });
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return this.child.computeIdealSize(constraints.minus(this.innerPadding.horizontal(), this.innerPadding.vertical()))
                .plus(this.innerPadding.horizontal(), this.innerPadding.vertical());
    }

    @Override
    public void computeLayout(final Size size) {
        this.child.computeLayout(size.minus(this.innerPadding.horizontal(), this.innerPadding.vertical()));
    }

}
