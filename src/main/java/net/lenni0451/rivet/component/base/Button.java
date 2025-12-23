package net.lenni0451.rivet.component.base;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseListener;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.List;
import java.util.function.Consumer;

public class Button extends Component implements MouseListener, Renderable {

    private final Component child;
    private final Consumer<MouseButtonEvent> clickListener;
    private Padding innerPadding = new Padding(5, 5, 5, 5);
    private Rectangle childBounds;
    private boolean hovered = false;

    public Button(final Rivet rivet, final Component child, final Consumer<MouseButtonEvent> clickListener) {
        super(rivet);
        this.child = child;
        this.clickListener = clickListener;
    }

    public Padding innerPadding() {
        return this.innerPadding;
    }

    public void setInnerPadding(final Padding padding) {
        if (!this.innerPadding.equals(padding)) {
            this.innerPadding = padding;
            this.rivet.recalculateNextFrame();
        }
    }

    public boolean isHovered() {
        return this.hovered;
    }

    @Override
    public void onMouseEnter() {
        this.hovered = true;
    }

    @Override
    public void onMouseLeave() {
        this.hovered = false;
    }

    @Override
    public void onMouseUp(MouseButtonEvent event) {
        this.clickListener.accept(event);
    }

    @Override
    public void render(Renderer renderer, Size size) {
        if (this.hovered) {
            renderer.fillRect(0, 0, size.width(), size.height(), Color.GREEN);
        } else {
            renderer.fillRect(0, 0, size.width(), size.height(), Color.RED);
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
    public void computeLayout(Size size) {
        this.childBounds = FullSizeLayout.INSTANCE.layoutComponents(new Size(
                size.width() - this.innerPadding.left() - this.innerPadding.right(),
                size.height() - this.innerPadding.top() - this.innerPadding.bottom()
        ), List.of(this.child)).get(this.child);
    }

}
