package test.impl;

import lombok.SneakyThrows;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import test.TestBase;

import java.io.File;

public class ImageComparator extends TestBase {

    void main() {
        this.run();
    }

    @Override
    @SneakyThrows
    protected void init(final Rivet rivet) {
        Texture texture1 = rivet.backend().assetLoader().loadTexture(new File("image1.png"));
        Texture texture2 = rivet.backend().assetLoader().loadTexture(new File("image2.png"));
        rivet.root().add(new ImageViewer(texture1, texture2));
    }


    private static class ImageViewer extends Component {
        private final Texture texture1;
        private final Texture texture2;
        private final float aspectRatio;
        private float progress = 0.5F;

        public ImageViewer(final Texture texture1, final Texture texture2) {
            this.texture1 = texture1;
            this.texture2 = texture2;
            this.aspectRatio = (float) texture1.width() / (float) texture1.height();
        }

        @Override
        protected boolean onMouseMoveInternal(final MouseMoveEvent event, final Size size) {
            if (event.isHeld(MouseButton.LEFT)) {
                Rectangle imageArea = this.calculateImageArea(size);
                this.progress = (event.x() - imageArea.x()) / imageArea.width();
            }
            return true;
        }

        @Override
        protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
            Rectangle imageArea = this.calculateImageArea(size);
            float x = imageArea.x();
            float y = imageArea.y();
            float width = imageArea.width();
            float progressWidth = width * this.progress;
            float height = imageArea.height();

            if (this.progress > 0) {
                renderer.scissor(x, y, progressWidth, height, () -> {
                    renderer.image(this.texture1, x, y, width, height, Color.WHITE);
                });
            }
            if (this.progress < 1) {
                renderer.scissor(x + progressWidth, y, width - progressWidth, height, () -> {
                    renderer.image(this.texture2, x, y, width, height, Color.WHITE);
                });
            }
            renderer.line(x + progressWidth, y, x + progressWidth, y + height, 2, Color.WHITE);
        }

        @Override
        public Size computeIdealSize(final Size constraints) {
            return constraints;
        }

        private Rectangle calculateImageArea(final Size size) {
            float width = size.width();
            float height = size.height();
            float currentAspectRatio = width / height;
            if (currentAspectRatio > this.aspectRatio) {
                width = height * this.aspectRatio;
            } else {
                height = width / this.aspectRatio;
            }

            float x = (size.width() - width) / 2;
            float y = (size.height() - height) / 2;
            return new Rectangle(x, y, width, height);
        }
    }

}
