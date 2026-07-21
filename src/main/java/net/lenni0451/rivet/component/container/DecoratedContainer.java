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

import java.util.List;
import java.util.function.Consumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class DecoratedContainer extends ParentContainer {

    @Getter
    private final Component background;
    @Getter
    private final Component child;
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
    public void render(final Renderer renderer, final Size size) {
        this.background.render(renderer, size);

        float width = size.width() - this.innerPadding.horizontal();
        float height = size.height() - this.innerPadding.vertical();
        renderer.translate(this.innerPadding.left(), this.innerPadding.top(), () -> {
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
        this.updateChildPositions();
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

}
