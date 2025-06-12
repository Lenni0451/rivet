package net.lenni0451.rivet.component.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.MouseListener;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.renderer.Renderer;
import org.joml.Matrix4fStack;

import java.util.function.IntConsumer;

public class Button extends Component implements Renderable, MouseListener {

    private final String text;
    private final IntConsumer onClick;
    private boolean hovered = false;

    public Button(final String text, final IntConsumer onClick) {
        this.text = text;
        this.onClick = onClick;
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
    public void render(Renderer renderer, Matrix4fStack positionMatrix, Size size) {
        if (this.hovered) {
            renderer.filledRectangle(positionMatrix, 0, 0, size.width(), size.height(), Color.GREEN);
        } else {
            renderer.filledRectangle(positionMatrix, 0, 0, size.width(), size.height(), Color.RED);
        }
    }

    @Override
    protected void computePreferredSize() {
        //TODO: Measure the text size and set the preferred size accordingly
        this.preferredSize.set(100, 30);
    }

}
