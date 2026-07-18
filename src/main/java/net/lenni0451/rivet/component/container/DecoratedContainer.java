package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.component.ParentContainer;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.utils.ContainerMouseHandler;

import java.util.List;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class DecoratedContainer extends ParentContainer {

    @Getter
    private final Component background;
    @Getter
    private final Component child;
    private final MouseHandler mouseHandler = new MouseHandler();
    @Getter
    @Setter
    private Padding innerPadding;

    public DecoratedContainer(final Component background, final Component child) {
        this(background, c -> {}, child, c -> {});
    }

    public <B extends Component, C extends Component> DecoratedContainer(final B background, final Consumer<B> backgroundInitializer, final C child, final Consumer<C> childInitializer) {
        this.background = background;
        backgroundInitializer.accept(background);
        this.child = child;
        childInitializer.accept(child);

        this.innerPadding = Padding.EMPTY;
    }

    @Override
    protected ContainerMouseHandler<?> mouseHandler() {
        return this.mouseHandler;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        this.background.updatePosition(new Rectangle(renderer.xOffset(), renderer.yOffset(), size));
        this.background.render(renderer, size);

        float width = size.width() - this.innerPadding.horizontal();
        float height = size.height() - this.innerPadding.vertical();
        renderer.translate(this.innerPadding.left(), this.innerPadding.top(), () -> {
            this.child.updatePosition(new Rectangle(renderer.xOffset(), renderer.yOffset(), width, height));
            renderer.componentBounds(0, 0, width, height, () -> {
                this.child.render(renderer, new Size(width, height));
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
        this.background.computeLayout(size);
        this.child.computeLayout(size.minus(this.innerPadding.horizontal(), this.innerPadding.vertical()));
    }

    @Override
    public Size contentSize() {
        if (this.child instanceof Parent parent) {
            Size parentContentSize = parent.contentSize();
            if (!parentContentSize.equals(Size.EMPTY)) {
                return parentContentSize.plus(this.innerPadding.horizontal(), this.innerPadding.vertical());
            }
        }
        return Size.EMPTY;
    }

    @Override
    public List<Component> children() {
        return List.of(this.background, this.child);
    }

    @Override
    public Rectangle childBounds(final Component component) {
        Rectangle bounds = this.relativeBounds();
        if (component == this.background) {
            return new Rectangle(bounds.size());
        } else if (component == this.child) {
            return new Rectangle(
                    this.innerPadding.left(), this.innerPadding.top(),
                    bounds.width() - this.innerPadding.horizontal(),
                    bounds.height() - this.innerPadding.vertical()
            );
        }
        return Rectangle.EMPTY;
    }


    private class MouseHandler extends ContainerMouseHandler<Component> {
        @Override
        protected Component map(final Component element) {
            return element;
        }

        @Override
        protected Rectangle relativeBounds(final Size containerBounds, final Component element) {
            if (element == DecoratedContainer.this.child) {
                Padding padding = DecoratedContainer.this.innerPadding;
                return new Rectangle(
                        padding.left(), padding.top(),
                        containerBounds.width() - padding.horizontal(), containerBounds.height() - padding.vertical()
                );
            } else {
                return new Rectangle(0, 0, containerBounds.width(), containerBounds.height());
            }
        }

        @Override
        protected List<Component> elementsAt(final float x, final float y, final Size containerBounds) {
            if (x < 0 || x >= containerBounds.width() || y < 0 || y >= containerBounds.height()) return List.of();
            Padding padding = DecoratedContainer.this.innerPadding;
            if (x >= padding.left() && x < containerBounds.width() - padding.right() && y >= padding.top() && y < containerBounds.height() - padding.bottom()) {
                return List.of(DecoratedContainer.this.child, DecoratedContainer.this.background);
            } else {
                return List.of(DecoratedContainer.this.background);
            }
        }
    }

}
