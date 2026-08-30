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
import net.lenni0451.rivet.utils.MathUtils;

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

    public PaddedContainer(final float padding, final Component child) {
        this(new Padding(padding), child);
    }

    public PaddedContainer(final Padding padding, final Component child) {
        this(padding, child, c -> {});
    }

    public <C extends Component> PaddedContainer(final Padding padding, final C child, final Consumer<C> initializer) {
        this.padding = padding;
        this.child = child;
        initializer.accept(child);
    }

    public final PaddedContainer padding(final Padding padding) {
        this.padding = padding;
        this.requestLayoutRecalculation();
        return this;
    }


    @Override
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        Size innerSize = size.minus(this.padding).clamp(this.child);
        renderer.translate(this.padding.left(), this.padding.top(), () -> {
            Runnable renderChild = () -> this.child.render(
                    renderer,
                    innerSize,
                    MathUtils.relativizeVisibleArea(visibleArea, this.padding.left(), this.padding.top(), innerSize)
            );
            if (this.cropChild) {
                renderer.componentBounds(0, 0, innerSize.width(), innerSize.height(), renderChild);
            } else {
                renderChild.run();
            }
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return this.child.computeIdealSize(constraints.minus(this.padding)).clamp(this.child).plus(this.padding);
    }

    @Override
    public void computeLayout(final Size size) {
        this.child.computeLayout(size.minus(this.padding).clamp(this.child));
    }


    @Override
    public Size contentSize() {
        if (this.child instanceof Parent parent) {
            Size parentContentSize = parent.contentSize();
            if (!parentContentSize.equals(Size.EMPTY)) {
                return parentContentSize.plus(this.padding);
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
            Size containerSize = this.relativeBounds().size();
            Size innerSize = containerSize.minus(this.padding).clamp(this.child);
            return new Rectangle(this.padding.left(), this.padding.top(), innerSize);
        }
        return Rectangle.EMPTY;
    }

}
