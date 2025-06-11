package net.lenni0451.rivet.component.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.renderer.Renderer;
import org.joml.Matrix4fStack;

import java.util.function.IntConsumer;

public class Button extends Component implements Renderable {

    private final String text;
    private final IntConsumer onClick;

    public Button(final String text, final IntConsumer onClick) {
        this.text = text;
        this.onClick = onClick;
    }

    @Override
    public void computePreferredSize() {
        //TODO: Measure the text size and set the preferred size accordingly
        this.preferredSize.set(100, 30);
    }


    @Override
    public void render(Renderer renderer, Matrix4fStack positionMatrix, Size size) {
        renderer.filledRectangle(positionMatrix, 0, 0, size.width(), size.height(), Color.RED);
    }

}
