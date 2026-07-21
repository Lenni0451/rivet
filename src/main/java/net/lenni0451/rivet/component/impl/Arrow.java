package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import net.lenni0451.rivet.utils.MathUtils;

import java.util.function.Supplier;

@Getter
@Accessors(fluent = true, chain = true, makeFinal = true)
public class Arrow extends Component {

    private Supplier<Float> progress;

    private final ThemeOption<Color> color = new ThemeOption<>(this, Theme.ARROW_COLOR);
    private final ThemeOption<Color> disabledColor = new ThemeOption<>(this, Theme.ARROW_DISABLED_COLOR);
    private final ThemeOption<Float> lineWidth = new ThemeOption<>(this, Theme.ARROW_LINE_WIDTH);
    private final ThemeOption<Float> size = new ThemeOption<>(this, Theme.ARROW_SIZE);

    public Arrow() {
        this(() -> 0F);
    }

    public Arrow(final Supplier<Float> progressSupplier) {
        this.progress = progressSupplier;

        this.capabilities().mouseInput(false);
    }

    public final Arrow progress(final Supplier<Float> progressSupplier) {
        this.progress = progressSupplier;
        return this;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float width = size.width() / 2;
        float height = size.height() / 4;
        float widthGap = (size.width() - width) / 2F;
        float heightGap = (size.height() - height) / 2F;
        Color color = this.disabled() ? this.disabledColor.value() : this.color.value();
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
