package net.lenni0451.rivet.backend.text;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.math.Rectangle;

import java.util.List;
import java.util.function.Function;

@Getter
@Accessors(fluent = true, chain = true, makeFinal = true)
public class ShapedTextBlock implements Shaped {

    private final List<ShapedText> lines;
    private final Rectangle visualBounds;
    private final Rectangle logicalBounds;

    public ShapedTextBlock(final List<ShapedText> lines) {
        this.lines = List.copyOf(lines);
        this.visualBounds = this.calculateBounds(ShapedText::visualBounds);
        this.logicalBounds = this.calculateBounds(ShapedText::logicalBounds);
    }

    private Rectangle calculateBounds(final Function<ShapedText, Rectangle> boundsGetter) {
        if (this.lines.isEmpty()) return Rectangle.EMPTY;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float y = 0F;
        for (ShapedText line : this.lines) {
            Rectangle bounds = boundsGetter.apply(line).add(0, y);
            minX = Math.min(minX, bounds.x());
            minY = Math.min(minY, bounds.y());
            maxX = Math.max(maxX, bounds.maxX());
            maxY = Math.max(maxY, bounds.maxY());
            y += line.logicalBounds().height();
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }


    public enum LineAlignment {
        LEFT, CENTER, RIGHT
    }

}
