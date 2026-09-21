package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.property.FloatProperty;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

@Getter
@Accessors(fluent = true, chain = true, makeFinal = true)
public class Arrow extends Component {

    private final FloatProperty progress;

    private final ThemeOption<Color> color = new ThemeOption<>(this, Theme.Arrow.COLOR);
    private final ThemeOption<Color> disabledColor = new ThemeOption<>(this, Theme.Arrow.DISABLED_COLOR);
    private final ThemeOption<Float> lineWidth = new ThemeOption<>(this, Theme.Arrow.LINE_WIDTH);
    private final ThemeOption<Float> size = new ThemeOption<>(this, Theme.Arrow.SIZE);

    public Arrow() {
        this.progress = new FloatProperty(0);
    }

    public Arrow(final float initialValue) {
        this.progress = new FloatProperty(initialValue);
    }

    {
        this.capabilities().mouseInput(false);
    }

    public final Arrow progress(final float progress) {
        this.progress.set(progress);
        return this;
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        float width = size.width() / 2;
        float height = size.height() / 4;
        float widthGap = (size.width() - width) / 2F;
        float heightGap = (size.height() - height) / 2F;
        Color color = this.disabled().get() ? this.disabledColor.value() : this.color.value();
        float lineWidth = this.lineWidth.value();
        float progress = this.progress.get();

        renderer.polyLine(
                new Point[]{
                        new Point(widthGap, MathUtils.lerp(heightGap, size.height() - heightGap, progress)),
                        new Point(size.width() / 2F, MathUtils.lerp(size.height() - heightGap, heightGap, progress)),
                        new Point(size.width() - widthGap, MathUtils.lerp(heightGap, size.height() - heightGap, progress)),
                },
                lineWidth,
                color
        );
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        float arrowSize = this.size.value();
        return new Size(arrowSize, arrowSize);
    }

}
