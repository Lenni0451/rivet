package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.animation.easing.EasingFunction;
import net.lenni0451.commons.animation.easing.EasingMode;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.Shaped;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.backend.text.ShapedTextBlock;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.property.FloatProperty;
import net.lenni0451.rivet.property.ObjectProperty;
import net.lenni0451.rivet.text.TextWrapper;
import net.lenni0451.rivet.text.model.*;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import javax.annotation.Nullable;
import java.util.List;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class Label extends Component {

    @Getter
    private final ObjectProperty<Font> font = new ObjectProperty<>();
    @Getter
    private final ObjectProperty<String> text;
    @Getter
    private final ObjectProperty<TextFormat> textFormat;
    @Getter
    private final ObjectProperty<TextLine> textLine;
    @Getter
    private final FloatProperty scale = new FloatProperty(1F);
    @Getter
    private final ObjectProperty<TextOrigin.Horizontal> horizontalOrigin = new ObjectProperty<>(TextOrigin.Horizontal.VISUAL_CENTER);
    @Getter
    private final ObjectProperty<TextOrigin.Vertical> verticalOrigin = new ObjectProperty<>(TextOrigin.Vertical.LOGICAL_CENTER);
    @Getter
    private final ObjectProperty<ShapedTextBlock.LineAlignment> lineAlignment = new ObjectProperty<>(ShapedTextBlock.LineAlignment.LEFT);

    private Shaped shaped;
    private boolean reshape;
    private long scrollStartTime = System.currentTimeMillis();
    private boolean overflowing;

    @Getter
    private final ThemeOption<Color> textColor = new ThemeOption<>(this, Theme.General.TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> disabledTextColor = new ThemeOption<>(this, Theme.General.DISABLED_TEXT_COLOR);
    @Getter
    private final ThemeOption<OverflowBehavior> overflowBehavior = new ThemeOption<>(this, Theme.Label.OVERFLOW_BEHAVIOR);
    @Getter
    private final ThemeOption<Float> scrollSpeed = new ThemeOption<>(this, Theme.Label.SCROLL_SPEED);
    @Getter
    private final ThemeOption<Integer> scrollPause = new ThemeOption<>(this, Theme.Label.SCROLL_PAUSE);
    @Getter
    private final ThemeOption<EasingFunction> scrollEasingFunction = new ThemeOption<>(this, Theme.Label.SCROLL_EASING_FUNCTION);
    @Getter
    private final ThemeOption<EasingMode> scrollEasingMode = new ThemeOption<>(this, Theme.Label.SCROLL_EASING_MODE);

    public Label(final String text) {
        this(text, null);
    }

    public Label(final String text, @Nullable final TextFormat format) {
        this.text = new ObjectProperty<>(text);
        this.textFormat = new ObjectProperty<>(format);
        this.textLine = new ObjectProperty<>();
        this.init();
    }

    public Label(final TextLine line) {
        this.text = new ObjectProperty<>("");
        this.textFormat = new ObjectProperty<>();
        this.textLine = new ObjectProperty<>(line);
        this.init();
    }

    private void init() {
        this.font.syncListener().add(f -> this.reshapeAndLayout());
        this.text.syncListener().add(t -> {
            if (this.textLine.get() != null) {
                this.textLine.set(null);
            }
            this.reshapeAndLayout();
            this.restartScrollAnimation();
        });
        this.textFormat.syncListener().add(f -> this.reshapeAndLayout());
        this.textLine.syncListener().add(l -> {
            this.reshapeAndLayout();
            this.restartScrollAnimation();
        });
        this.scale.syncListener().add(s -> this.reshapeAndLayout());

        this.textColor.changeListener().add(c -> this.reshape = true);
        this.disabledTextColor.changeListener().add(c -> this.reshape = true);
        this.overflowBehavior.changeListener().add(o -> {
            this.reshapeAndLayout();
            this.restartScrollAnimation();
        });
        this.scrollSpeed.changeListener().add(s -> this.restartScrollAnimation());
        this.scrollPause.changeListener().add(p -> this.restartScrollAnimation());
    }

    public final Label font(@Nullable final Font font) {
        this.font.set(font);
        return this;
    }

    public final Label text(final String text) {
        if (this.textLine.get() != null) {
            this.textLine.set(null);
        }
        this.text.set(text);
        return this;
    }

    public final Label text(final String text, @Nullable final TextFormat textFormat) {
        if (this.textLine.get() != null) {
            this.textLine.set(null);
        }
        this.text.set(text);
        this.textFormat.set(textFormat);
        return this;
    }

    public final Label text(final TextLine line) {
        this.textLine.set(line);
        return this;
    }

    public final Label textFormat(@Nullable final TextFormat textFormat) {
        this.textFormat.set(textFormat);
        return this;
    }

    public final Label scale(final float scale) {
        this.scale.set(scale);
        return this;
    }

    public final Label horizontalOrigin(final TextOrigin.Horizontal horizontalOrigin) {
        this.horizontalOrigin.set(horizontalOrigin);
        return this;
    }

    public final Label verticalOrigin(final TextOrigin.Vertical verticalOrigin) {
        this.verticalOrigin.set(verticalOrigin);
        return this;
    }

    public final Label lineAlignment(final ShapedTextBlock.LineAlignment lineAlignment) {
        this.lineAlignment.set(lineAlignment);
        return this;
    }

    private void reshapeAndLayout() {
        this.reshape = true;
        this.invalidateLayout();
    }

    private void restartScrollAnimation() {
        this.scrollStartTime = System.currentTimeMillis();
    }

    private float scrollOffset(final float overflow) {
        float speed = this.scrollSpeed.get();
        if (speed <= 0) return 0;
        float pause = Math.max(0, this.scrollPause.get());
        float move = overflow / speed * 1000F;
        float cycle = 2 * pause + 2 * move;
        float time = (System.currentTimeMillis() - this.scrollStartTime) % cycle;

        EasingMode easingMode = this.scrollEasingMode.get();
        EasingFunction easingFunction = this.scrollEasingFunction.get();
        float progress;
        if (time < pause) {
            progress = 0;
        } else if (time < pause + move) {
            progress = easingMode.call(easingFunction, (time - pause) / move);
        } else if (time < 2 * pause + move) {
            progress = 1;
        } else {
            progress = 1 - easingMode.call(easingFunction, (time - 2 * pause - move) / move);
        }
        return overflow * progress;
    }

    private void shapeText(final Size size) {
        if (this.reshape || this.shaped == null) {
            this.shaped = this.createShapedText(size);
            this.reshape = false;
        }
    }

    private Shaped createShapedText(final Size size) {
        Font font = this.font.getOrElse(this.rivet().backend().font());
        TextLine line = this.createTextLine();
        if (this.overflowBehavior.get().equals(OverflowBehavior.WRAP)) {
            return TextWrapper.wrapLine(font, line, size.width() / this.scale.get());
        }

        List<TextLine> lines = TextWrapper.splitExplicitLines(line);
        if (lines.size() == 1) {
            return font.shapeText(lines.get(0));
        } else {
            return font.shapeText(new TextBlock(lines));
        }
    }

    private TextLine createTextLine() {
        TextLine textLine = this.textLine.get();
        if (textLine != null) {
            return textLine;
        }
        TextFormat format = this.textFormat.get();
        Color color;
        if (format != null) {
            if (format.color().equals(Color.TRANSPARENT)) {
                color = this.disabled().get() ? this.disabledTextColor.get() : this.textColor.get();
            } else {
                if (this.disabled().get() && format.color().equals(this.textColor.get())) {
                    color = this.disabledTextColor.get();
                } else {
                    color = format.color();
                }
            }
        } else {
            format = TextFormat.DEFAULT;
            color = this.disabled().get() ? this.disabledTextColor.get() : this.textColor.get();
        }
        return this.createTextLine(this.text.getOrElse(""), format.withColor(color));
    }

    protected TextLine createTextLine(final String text, final TextFormat format) {
        return new TextLine(new TextSection(text, format));
    }

    @Override
    protected void onAddedInternal() {
        this.reshape = true;
        this.restartScrollAnimation();
    }

    @Override
    protected void onDisabledInternal() {
        this.reshape = true;
    }

    @Override
    protected void onEnabledInternal() {
        this.reshape = true;
    }

    @Override
    protected void onThemeChangedInternal() {
        this.reshape = true;
        this.restartScrollAnimation();
    }

    @Override
    protected boolean onMouseDownInternal(final MouseButtonEvent event, final Size size) {
        return false;
    }

    @Override
    protected boolean onMouseMoveInternal(final MouseMoveEvent event, final Size size) {
        return false;
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        this.shapeText(size);
        float scale = this.scale.get();
        TextOrigin.Horizontal horizontalOrigin = this.horizontalOrigin.get();
        TextOrigin.Vertical verticalOrigin = this.verticalOrigin.get();
        ShapedTextBlock.LineAlignment lineAlignment = this.lineAlignment.get();

        TextOrigin.Horizontal renderOrigin;
        float x;
        float textScale = this.textScale(size, scale);
        float overflow = this.shaped.visualBounds().width() * textScale - size.width();
        if (this.overflowBehavior.get().equals(OverflowBehavior.SCROLL) && overflow > 0) {
            renderOrigin = TextOrigin.Horizontal.VISUAL_LEFT;
            x = -this.scrollOffset(overflow) / textScale;
        } else {
            renderOrigin = horizontalOrigin;
            x = horizontalOrigin.position(size.width() / textScale);
        }
        float y = verticalOrigin.position(size.height() / textScale);
        Runnable textRenderer;
        if (this.shaped instanceof ShapedTextBlock shapedTextBlock) {
            textRenderer = () -> renderer.scale(textScale, () -> renderer.text(shapedTextBlock, x, y, renderOrigin, verticalOrigin, lineAlignment));
        } else if (this.shaped instanceof ShapedText shapedText) {
            textRenderer = () -> renderer.scale(textScale, () -> renderer.text(shapedText, x, y, renderOrigin, verticalOrigin));
        } else {
            throw new IllegalStateException("Unknown shaped type: " + this.shaped.getClass().getName());
        }

        boolean clipsHorizontally = this.shaped.visualBounds().width() * textScale > size.width();
        boolean clipsVertically = this.shaped.logicalBounds().height() * textScale > size.height();
        if (clipsHorizontally || clipsVertically) {
            renderer.scissor(0, 0, size.width(), size.height(), textRenderer);
        } else {
            textRenderer.run();
        }
    }

    private float textScale(final Size size, final float scale) {
        if (this.overflowBehavior.get().equals(OverflowBehavior.SCALE)) {
            float widthRatio = size.width() / (this.shaped.visualBounds().width() * scale);
            float heightRatio = size.height() / (this.shaped.logicalBounds().height() * scale);
            float ratio = Math.min(widthRatio, heightRatio);
            if (ratio < 1) return scale * ratio;
        }
        return scale;
    }

    @Override
    protected Size idealSizeInternal(final Size constraints) {
        Shaped shaped = this.createShapedText(constraints);
        float scale = this.scale.get();
        return new Size(
                shaped.visualBounds().width() * scale,
                shaped.logicalBounds().height() * scale
        );
    }

    @Override
    protected void layoutInternal(final Size size) {
        OverflowBehavior overflowBehavior = this.overflowBehavior.get();
        if (overflowBehavior.equals(OverflowBehavior.WRAP)) {
            this.reshape = true;
            this.shapeText(size);
        }
        if (overflowBehavior.equals(OverflowBehavior.SCROLL)) {
            this.shapeText(size);
            boolean overflowing = this.shaped.visualBounds().width() * this.scale.get() > size.width();
            if (overflowing && !this.overflowing) {
                this.restartScrollAnimation();
            }
            this.overflowing = overflowing;
        } else {
            this.overflowing = false;
        }
    }


    public enum OverflowBehavior {
        CLIP, SCALE, WRAP, SCROLL
    }

}
