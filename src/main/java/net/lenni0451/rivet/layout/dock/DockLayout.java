package net.lenni0451.rivet.layout.dock;

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
public record DockLayout(float gap) implements Layout {

    public static final DockLayout DEFAULT = new DockLayout();

    public DockLayout() {
        this(0);
    }

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        float xOffset = 0;
        float yOffset = 0;
        float totalWidth = 0;
        float totalHeight = 0;
        for (Component component : components) {
            float remainingWidth = Math.max(0, constraints.width() - xOffset);
            float remainingHeight = Math.max(0, constraints.height() - yOffset);

            DockPosition position = component.layoutOptions() instanceof DockPosition pos ? pos : DockPosition.CENTER;
            Size idealSize = component.computeIdealSize(new Size(remainingWidth, remainingHeight));
            float width = this.widthOf(component, idealSize);
            float height = this.heightOf(component, idealSize);
            switch (position) {
                case LEFT, RIGHT -> {
                    xOffset += width + this.gap;
                    totalWidth = Math.max(totalWidth, xOffset - this.gap);
                    totalHeight = Math.max(totalHeight, yOffset + height);
                }
                case TOP, BOTTOM -> {
                    yOffset += height + this.gap;
                    totalWidth = Math.max(totalWidth, xOffset + width);
                    totalHeight = Math.max(totalHeight, yOffset - this.gap);
                }
                case CENTER -> {
                    totalWidth = Math.max(totalWidth, xOffset + width);
                    totalHeight = Math.max(totalHeight, yOffset + height);
                }
            }
        }
        return new Size(totalWidth, totalHeight);
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        float left = 0;
        float top = 0;
        float right = containerSize.width();
        float bottom = containerSize.height();
        for (Component component : components) {
            DockPosition position = component.layoutOptions() instanceof DockPosition pos ? pos : DockPosition.CENTER;
            float remainingWidth = Math.max(0, right - left);
            float remainingHeight = Math.max(0, bottom - top);
            Size idealSize = component.computeIdealSize(new Size(remainingWidth, remainingHeight));
            switch (position) {
                case LEFT -> {
                    float width = this.widthOf(component, Math.min(remainingWidth, idealSize.width()));
                    float height = this.heightOf(component, remainingHeight);
                    setBounds.accept(component, new Rectangle(left, top, width, height));
                    left += width + this.gap;
                }
                case RIGHT -> {
                    float width = this.widthOf(component, Math.min(remainingWidth, idealSize.width()));
                    float height = this.heightOf(component, remainingHeight);
                    setBounds.accept(component, new Rectangle(right - width, top, width, height));
                    right -= width + this.gap;
                }
                case TOP -> {
                    float width = this.widthOf(component, remainingWidth);
                    float height = this.heightOf(component, Math.min(remainingHeight, idealSize.height()));
                    setBounds.accept(component, new Rectangle(left, top, width, height));
                    top += height + this.gap;
                }
                case BOTTOM -> {
                    float width = this.widthOf(component, remainingWidth);
                    float height = this.heightOf(component, Math.min(remainingHeight, idealSize.height()));
                    setBounds.accept(component, new Rectangle(left, bottom - height, width, height));
                    bottom -= height + this.gap;
                }
                case CENTER -> {
                    float width = this.widthOf(component, remainingWidth);
                    float height = this.heightOf(component, remainingHeight);
                    setBounds.accept(component, new Rectangle(left, top, width, height));
                    left = right;
                    top = bottom;
                }
            }
        }
    }

}
