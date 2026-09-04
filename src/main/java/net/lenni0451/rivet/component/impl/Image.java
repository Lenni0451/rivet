package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

@RequiredArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public class Image extends Component {

    @Getter
    private final Texture texture;
    @Getter
    @Setter
    private Color color = Color.WHITE;
    @Getter
    @Setter
    private ScaleMode scaleMode = ScaleMode.STRETCH;

    @Override
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        switch (this.scaleMode) {
            case STRETCH -> renderer.image(this.texture, 0, 0, size.width(), size.height(), this.color);
            case FIT -> {
                float textureAspectRatio = (float) this.texture.width() / this.texture.height();
                float componentAspectRatio = size.width() / size.height();
                if (textureAspectRatio > componentAspectRatio) {
                    float height = size.width() / textureAspectRatio;
                    renderer.image(this.texture, 0, (size.height() - height) / 2F, size.width(), height, this.color);
                } else {
                    float width = size.height() * textureAspectRatio;
                    renderer.image(this.texture, (size.width() - width) / 2F, 0, width, size.height(), this.color);
                }
            }
            case FILL -> {
                float textureAspectRatio = (float) this.texture.width() / this.texture.height();
                float componentAspectRatio = size.width() / size.height();
                if (textureAspectRatio > componentAspectRatio) {
                    float width = size.height() * textureAspectRatio;
                    renderer.image(this.texture, (size.width() - width) / 2F, 0, width, size.height(), this.color);
                } else {
                    float height = size.width() / textureAspectRatio;
                    renderer.image(this.texture, 0, (size.height() - height) / 2F, size.width(), height, this.color);
                }
            }
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return new Size(this.texture.width(), this.texture.height());
    }


    public enum ScaleMode {
        STRETCH, FIT, FILL
    }

}
