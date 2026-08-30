package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.component.ParentContainer;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.utils.MathUtils;

import java.util.List;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class DecoratedContainer extends ParentContainer {

    @Getter
    private final Component background;
    @Getter
    private final Component child;
    @Getter
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

    public final DecoratedContainer innerPadding(final Padding padding) {
        this.innerPadding = padding;
        return this;
    }

    public final DecoratedContainer innerPadding(final float padding) {
        this.innerPadding = new Padding(padding);
        return this;
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        Size backgroundSize = size.clamp(this.background);
        this.background.render(renderer, backgroundSize, visibleArea);

        Size innerSize = size.minus(this.innerPadding).clamp(this.child);
        renderer.translate(this.innerPadding.left(), this.innerPadding.top(), () -> {
            renderer.componentBounds(0, 0, innerSize.width(), innerSize.height(), () -> {
                this.child.render(
                        renderer,
                        innerSize,
                        MathUtils.relativizeVisibleArea(visibleArea, this.innerPadding.left(), this.innerPadding.top(), innerSize)
                );
            });
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return this.child.computeIdealSize(constraints.minus(this.innerPadding)).clamp(this.child).plus(this.innerPadding);
    }

    @Override
    public void computeLayout(final Size size) {
        this.background.computeLayout(size.clamp(this.background));
        this.child.computeLayout(size.minus(this.innerPadding).clamp(this.child));
    }

    @Override
    public Size contentSize() {
        if (this.child instanceof Parent parent) {
            Size parentContentSize = parent.contentSize();
            if (!parentContentSize.equals(Size.EMPTY)) {
                return parentContentSize.plus(this.innerPadding);
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
        Size containerSize = this.relativeBounds().size();
        if (component == this.background) {
            return new Rectangle(containerSize.clamp(this.background));
        } else if (component == this.child) {
            return new Rectangle(
                    this.innerPadding.left(), this.innerPadding.top(),
                    containerSize.minus(this.innerPadding).clamp(this.child)
            );
        }
        return Rectangle.EMPTY;
    }

}
