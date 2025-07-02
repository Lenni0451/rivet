package net.lenni0451.rivet.component.base;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.container.Container;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.math.impl.FloatPadding;
import org.joml.Matrix4fStack;
import org.joml.Vector2f;

import java.util.function.IntConsumer;

public abstract class Button extends Container {

    private final IntConsumer onClick;
    private final FloatPadding innerPadding = new FloatPadding(5, 5, 5, 5);
    private boolean hovered = false;

    public Button(final Component child, final IntConsumer onClick) {
        this.addChild(child);
        this.onClick = onClick;
    }

    public Padding getInnerPadding() {
        return this.innerPadding;
    }

    public void setInnerPadding(final int left, final int top, final int right, final int bottom) {
        this.innerPadding.set(left, top, right, bottom);
        this.triggerLayoutChange();
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
    public void onMouseUp(float mouseX, float mouseY, int button, int modifiers) {
        this.onClick.accept(button);
    }

    @Override
    public void render(Renderer renderer, Matrix4fStack positionMatrix, Size size) {
        if (this.hovered) {
            renderer.filledRectangle(positionMatrix, 0, 0, size.width(), size.height(), Color.GREEN);
        } else {
            renderer.filledRectangle(positionMatrix, 0, 0, size.width(), size.height(), Color.RED);
        }
        super.render(renderer, positionMatrix, size);
    }

    @Override
    protected void computePreferredSize() {
        for (Component child : this.getChildren()) {
            //A button should always only have one child
            //In case it for some reason has more than one child, the last one will be used
            Size preferredSize = child.getActualPreferredSize();
            this.preferredSize.set(
                    preferredSize.width() + this.innerPadding.left + this.innerPadding.right,
                    preferredSize.height() + this.innerPadding.top + this.innerPadding.bottom
            );
        }
    }

    @Override
    protected void computeLayout0(Vector2f size) {
        for (Component child : this.getChildren()) {
            this.setChildBounds(
                    child,
                    this.innerPadding.left,
                    this.innerPadding.top,
                    size.x - this.innerPadding.left - this.innerPadding.right,
                    size.y - this.innerPadding.top - this.innerPadding.bottom
            );
        }
    }

}
