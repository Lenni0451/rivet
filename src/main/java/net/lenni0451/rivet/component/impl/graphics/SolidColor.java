package net.lenni0451.rivet.component.impl.graphics;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.function.Consumer;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class SolidColor extends Component {

    private Color color;
    private Color outlineColor;
    private float outlineWidth;
    private float cornerRadius;

    public SolidColor(final Rivet rivet) {
        this(rivet, s -> {});
    }

    public SolidColor(final Rivet rivet, final Consumer<SolidColor> initializer) {
        super(rivet);
        this.color = Color.TRANSPARENT;
        this.outlineColor = Color.TRANSPARENT;
        this.outlineWidth = rivet.backend().getTextHeight() / 8F;
        this.cornerRadius = 0;
        initializer.accept(this);
    }

    @Override
    public void render(final Renderer renderer, final Rectangle bounds) {
        if (this.color.getAlpha() > 0) {
            renderer.fillOptimizedRoundedRect(0, 0, bounds.width(), bounds.height(), this.cornerRadius, this.color);
        }
        if (this.outlineColor.getAlpha() > 0) {
            renderer.outlineOptimizedRoundedRect(0, 0, bounds.width(), bounds.height(), this.cornerRadius, this.outlineWidth, this.outlineColor);
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return Size.EMPTY;
    }

}
