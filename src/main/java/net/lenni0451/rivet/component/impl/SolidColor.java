package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.property.CornersProperty;
import net.lenni0451.rivet.property.FloatProperty;
import net.lenni0451.rivet.property.ObjectProperty;

import java.util.function.Consumer;

@Getter
@Setter
@Accessors(fluent = true, chain = true, makeFinal = true)
public class SolidColor extends Component {

    private final ObjectProperty<Color> color = new ObjectProperty<>(Color.TRANSPARENT);
    private final ObjectProperty<Color> outlineColor = new ObjectProperty<>(Color.TRANSPARENT);
    private final FloatProperty outlineWidth = new FloatProperty(-1);
    private final CornersProperty cornerRadius = new CornersProperty();

    public SolidColor() {
        this(s -> {});
    }

    public SolidColor(final Color color) {
        this(s -> s.color.set(color));
    }

    public SolidColor(final Consumer<SolidColor> initializer) {
        initializer.accept(this);
    }

    public final SolidColor color(final Color color) {
        this.color.set(color);
        return this;
    }

    public final SolidColor outlineColor(final Color outlineColor) {
        this.outlineColor.set(outlineColor);
        return this;
    }

    public final SolidColor outlineWidth(final float outlineWidth) {
        this.outlineWidth.set(outlineWidth);
        return this;
    }

    public final SolidColor cornerRadius(final float allCorners) {
        this.cornerRadius.set(allCorners);
        return this;
    }

    public final SolidColor cornerRadius(final float topLeft, final float bottomLeft, final float bottomRight, final float topRight) {
        this.cornerRadius.set(topLeft, bottomLeft, bottomRight, topRight);
        return this;
    }

    public final SolidColor cornerRadius(final Corners cornerRadius) {
        this.cornerRadius.set(cornerRadius);
        return this;
    }

    @Override
    protected void onAddedInternal() {
        if (this.outlineWidth.get() == -1) {
            this.outlineWidth.set(this.rivet().backend().font().height() / 8F);
        }
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        Color color = this.color.get();
        Color outlineColor = this.outlineColor.get();
        float outlineWidth = this.outlineWidth.get();
        Corners cornerRadius = this.cornerRadius.get();

        if (color.getAlpha() > 0) {
            renderer.optimizedFillRoundedRect(0, 0, size.width(), size.height(), cornerRadius, color);
        }
        if (outlineColor.getAlpha() > 0 && outlineWidth > 0) {
            renderer.optimizedOutlineRoundedRect(0, 0, size.width(), size.height(), cornerRadius, outlineWidth, outlineColor);
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return Size.EMPTY;
    }

}
