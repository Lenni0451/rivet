package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

@Getter
@Setter
@Accessors(fluent = true, chain = true, makeFinal = true)
public class Separator extends Component {

    private Orientation orientation;
    private final ThemeOption<Color> color = new ThemeOption<>(this, Theme.SEPARATOR_COLOR);
    private final ThemeOption<Float> thickness = new ThemeOption<>(this, Theme.SEPARATOR_THICKNESS);

    public Separator() {
        this(Orientation.HORIZONTAL);
    }

    public Separator(final Orientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        Color color = this.color.value();
        float thickness = this.thickness.value();
        switch (this.orientation) {
            case HORIZONTAL -> renderer.optimizedFillRoundedRect(
                    0, (size.height() - thickness) / 2F,
                    size.width(), thickness,
                    0, color
            );
            case VERTICAL -> renderer.optimizedFillRoundedRect(
                    (size.width() - thickness) / 2F, 0,
                    thickness, size.height(),
                    0, color
            );
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return switch (this.orientation) {
            case HORIZONTAL -> new Size(0, this.thickness.value());
            case VERTICAL -> new Size(this.thickness.value(), 0);
        };
    }


    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

}
