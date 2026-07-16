package net.lenni0451.rivet.layout.border;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collection;
import java.util.function.BiConsumer;

@With
@WithBy
public record BorderLayout(TopLeftPriority topLeftPriority, BottomLeftPriority bottomLeftPriority, BottomRightPriority bottomRightPriority, TopRightPriority topRightPriority) implements Layout {

    public static final BorderLayout DEFAULT = new BorderLayout();

    public BorderLayout() {
        this(TopLeftPriority.TOP, BottomLeftPriority.BOTTOM, BottomRightPriority.BOTTOM, TopRightPriority.TOP);
    }

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        Size top = Size.EMPTY;
        Size bottom = Size.EMPTY;
        Size left = Size.EMPTY;
        Size right = Size.EMPTY;
        Size center = Size.EMPTY;
        for (Component component : components) {
            BorderPosition position = component.layoutOptions() instanceof BorderPosition pos ? pos : BorderPosition.CENTER;
            Size idealSize = component.computeIdealSize(constraints);
            float idealWidth = this.widthOf(component, idealSize);
            float idealHeight = this.heightOf(component, idealSize);
            switch (position) {
                case TOP -> top = top.max(idealWidth, idealHeight);
                case BOTTOM -> bottom = bottom.max(idealWidth, idealHeight);
                case LEFT -> left = left.max(idealWidth, idealHeight);
                case RIGHT -> right = right.max(idealWidth, idealHeight);
                case CENTER -> center = center.max(idealWidth, idealHeight);
            }
        }

        float topRowWidth = (this.topLeftPriority.equals(TopLeftPriority.LEFT) ? left.width() : 0) + top.width() + (this.topRightPriority.equals(TopRightPriority.RIGHT) ? right.width() : 0);
        float middleRowWidth = left.width() + center.width() + right.width();
        float bottomRowWidth = (this.bottomLeftPriority.equals(BottomLeftPriority.LEFT) ? left.width() : 0) + bottom.width() + (this.bottomRightPriority.equals(BottomRightPriority.RIGHT) ? right.width() : 0);
        float leftColumnHeight = (this.topLeftPriority.equals(TopLeftPriority.TOP) ? top.height() : 0) + left.height() + (this.bottomLeftPriority.equals(BottomLeftPriority.BOTTOM) ? bottom.height() : 0);
        float middleColumnHeight = top.height() + center.height() + bottom.height();
        float rightColumnHeight = (this.topRightPriority.equals(TopRightPriority.TOP) ? top.height() : 0) + right.height() + (this.bottomRightPriority.equals(BottomRightPriority.BOTTOM) ? bottom.height() : 0);
        return new Size(
                Math.max(Math.max(topRowWidth, middleRowWidth), bottomRowWidth),
                Math.max(Math.max(leftColumnHeight, middleColumnHeight), rightColumnHeight)
        );
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        float topHeight = 0;
        float bottomHeight = 0;
        float leftWidth = 0;
        float rightWidth = 0;
        for (Component component : components) {
            BorderPosition position = component.layoutOptions() instanceof BorderPosition pos ? pos : BorderPosition.CENTER;
            Size idealSize = component.computeIdealSize(containerSize);
            float idealWidth = this.widthOf(component, idealSize);
            float idealHeight = this.heightOf(component, idealSize);
            switch (position) {
                case TOP -> topHeight = Math.max(topHeight, idealHeight);
                case BOTTOM -> bottomHeight = Math.max(bottomHeight, idealHeight);
                case LEFT -> leftWidth = Math.max(leftWidth, idealWidth);
                case RIGHT -> rightWidth = Math.max(rightWidth, idealWidth);
            }
        }
        for (Component component : components) {
            BorderPosition position = component.layoutOptions() instanceof BorderPosition pos ? pos : BorderPosition.CENTER;
            setBounds.accept(component, switch (position) {
                case TOP -> {
                    float leftStart = this.topLeftPriority.equals(TopLeftPriority.LEFT) ? leftWidth : 0;
                    float rightEnd = this.topRightPriority.equals(TopRightPriority.RIGHT) ? containerSize.width() - rightWidth : containerSize.width();
                    yield new Rectangle(leftStart, 0, rightEnd - leftStart, topHeight);
                }
                case BOTTOM -> {
                    float leftStart = this.bottomLeftPriority.equals(BottomLeftPriority.LEFT) ? leftWidth : 0;
                    float rightEnd = this.bottomRightPriority.equals(BottomRightPriority.RIGHT) ? containerSize.width() - rightWidth : containerSize.width();
                    yield new Rectangle(leftStart, containerSize.height() - bottomHeight, rightEnd - leftStart, bottomHeight);
                }
                case LEFT -> {
                    float topStart = this.topLeftPriority.equals(TopLeftPriority.TOP) ? topHeight : 0;
                    float bottomEnd = this.bottomLeftPriority.equals(BottomLeftPriority.BOTTOM) ? containerSize.height() - bottomHeight : containerSize.height();
                    yield new Rectangle(0, topStart, leftWidth, bottomEnd - topStart);
                }
                case RIGHT -> {
                    float topStart = this.topRightPriority.equals(TopRightPriority.TOP) ? topHeight : 0;
                    float bottomEnd = this.bottomRightPriority.equals(BottomRightPriority.BOTTOM) ? containerSize.height() - bottomHeight : containerSize.height();
                    yield new Rectangle(containerSize.width() - rightWidth, topStart, rightWidth, bottomEnd - topStart);
                }
                case CENTER -> new Rectangle(leftWidth, topHeight, containerSize.width() - leftWidth - rightWidth, containerSize.height() - topHeight - bottomHeight);
            });
        }
    }

}
