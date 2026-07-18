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
public class PaddedContainer extends ParentContainer {

    @Getter
    private Padding padding;
    @Getter
    private final Component child;
    @Getter
    @Setter
    private boolean cropChild = true;

    public PaddedContainer(final Padding padding, final Component child) {
        this(padding, child, c -> {});
    }

    public <C extends Component> PaddedContainer(final Padding padding, final C child, final Consumer<C> initializer) {
        this.padding = padding;
        this.child = child;
        initializer.accept(child);
    }

    public PaddedContainer padding(final Padding padding) {
        this.padding = padding;
        this.requestLayoutRecalculation();
        return this;
    }


    @Override
    public void render(final Renderer renderer, final Size size) {
        float width = size.width() - this.padding.horizontal();
        float height = size.height() - this.padding.vertical();
        renderer.translate(this.padding.left(), this.padding.top(), () -> {
            this.child.updatePosition(new Rectangle(renderer.xOffset(), renderer.yOffset(), width, height));
            Runnable renderChild = () -> this.child.render(renderer, new Size(width, height));
            if (this.cropChild) {
                renderer.componentBounds(0, 0, width, height, renderChild);
            } else {
                renderChild.run();
            }
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        Size childSize = this.child.computeIdealSize(constraints.minus(this.padding.horizontal(), this.padding.vertical()));
        return childSize.plus(this.padding.horizontal(), this.padding.vertical());
    }

    @Override
    public void computeLayout(final Size size) {
        this.child.computeLayout(size.minus(this.padding.horizontal(), this.padding.vertical()));
    }


    @Override
    public Size contentSize() {
        if (this.child instanceof Parent parent) {
            Size parentContentSize = parent.contentSize();
            if (!parentContentSize.equals(Size.EMPTY)) {
                return parentContentSize.plus(this.padding.horizontal(), this.padding.vertical());
            }
        }
        return Size.EMPTY;
    }

    @Override
    public List<Component> children() {
        return List.of(this.child);
    }

    @Override
    public Rectangle childBounds(final Component component) {
        if (component == this.child) {
            Rectangle bounds = this.relativeBounds();
            return new Rectangle(
                    this.padding.left(), this.padding.top(),
                    bounds.width() - this.padding.horizontal(),
                    bounds.height() - this.padding.vertical()
            );
        }
        return Rectangle.EMPTY;
    }

}
