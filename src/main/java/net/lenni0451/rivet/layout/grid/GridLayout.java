package net.lenni0451.rivet.layout.grid;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class GridLayout implements Layout {

    private final int horizontalGap;
    private final int verticalGap;
    @Getter
    @Setter
    private boolean homogeneousColumns = false;
    @Getter
    @Setter
    private boolean homogeneousRows = false;

    public GridLayout() {
        this(0, 0);
    }

    @Override
    public Size computeIdealSize(final Collection<Component> components) {
        GridInfo info = new GridInfo(components);
        float width = 0;
        float height = 0;
        for (float columnWidth : info.columnWidths) {
            width += columnWidth;
        }
        for (float rowHeight : info.rowHeights) {
            height += rowHeight;
        }
        if (info.columns > 0) {
            width += (info.columns - 1) * this.horizontalGap;
        }
        if (info.rows > 0) {
            height += (info.rows - 1) * this.verticalGap;
        }
        return new Size(width, height);
    }

    @Override
    public Map<Component, Rectangle> layoutComponents(final Size containerSize, final Collection<Component> components) {
        GridInfo info = new GridInfo(components);
        float totalWidth = 0;
        float totalHeight = 0;
        for (float columnWidth : info.columnWidths) {
            totalWidth += columnWidth;
        }
        for (float rowHeight : info.rowHeights) {
            totalHeight += rowHeight;
        }
        if (info.columns > 0) {
            totalWidth += (info.columns - 1) * this.horizontalGap;
        }
        if (info.rows > 0) {
            totalHeight += (info.rows - 1) * this.verticalGap;
        }

        float extraWidth = Math.max(0, containerSize.width() - totalWidth);
        float extraHeight = Math.max(0, containerSize.height() - totalHeight);

        float[] colWidths = info.columnWidths.clone();
        float[] rowHeights = info.rowHeights.clone();

        if (extraWidth > 0 && info.totalWeightX > 0) {
            for (int i = 0; i < info.columns; i++) {
                colWidths[i] += extraWidth * (info.columnWeights[i] / info.totalWeightX);
            }
        }
        if (extraHeight > 0 && info.totalWeightY > 0) {
            for (int i = 0; i < info.rows; i++) {
                rowHeights[i] += extraHeight * (info.rowWeights[i] / info.totalWeightY);
            }
        }

        float[] colOffsets = new float[info.columns];
        float[] rowOffsets = new float[info.rows];
        float currentX = 0;
        for (int i = 0; i < info.columns; i++) {
            colOffsets[i] = currentX;
            currentX += colWidths[i] + this.horizontalGap;
        }
        float currentY = 0;
        for (int i = 0; i < info.rows; i++) {
            rowOffsets[i] = currentY;
            currentY += rowHeights[i] + this.verticalGap;
        }

        Map<Component, Rectangle> layout = new IdentityHashMap<>();
        for (Component component : components) {
            GridLayoutOptions options = (GridLayoutOptions) component.layoutOptions();
            if (options == null) {
                options = GridLayoutOptions.EMPTY;
            }

            float cellX = colOffsets[options.column()];
            float cellY = rowOffsets[options.row()];
            float cellWidth = 0;
            for (int i = 0; i < options.columnSpan(); i++) {
                cellWidth += colWidths[options.column() + i];
                if (i > 0) {
                    cellWidth += this.horizontalGap;
                }
            }
            float cellHeight = 0;
            for (int i = 0; i < options.rowSpan(); i++) {
                cellHeight += rowHeights[options.row() + i];
                if (i > 0) {
                    cellHeight += this.verticalGap;
                }
            }

            float availableWidth = cellWidth - options.padding().left() - options.padding().right();
            float availableHeight = cellHeight - options.padding().top() - options.padding().bottom();
            float idealWidth = this.widthOf(component, options);
            float idealHeight = this.heightOf(component, options);

            float width = idealWidth;
            float height = idealHeight;

            if (options.fill() == GridFill.HORIZONTAL || options.fill() == GridFill.BOTH) {
                width = availableWidth;
            }
            if (options.fill() == GridFill.VERTICAL || options.fill() == GridFill.BOTH) {
                height = availableHeight;
            }
            width = Math.max(0, Math.min(width, Math.min(availableWidth, component.maxSize().width())));
            height = Math.max(0, Math.min(height, Math.min(availableHeight, component.maxSize().height())));

            float x = cellX + options.padding().left();
            float y = cellY + options.padding().top();

            switch (options.anchor()) {
                case NORTH_WEST -> {
                }
                case NORTH -> x += (availableWidth - width) / 2F;
                case NORTH_EAST -> x += availableWidth - width;
                case WEST -> y += (availableHeight - height) / 2F;
                case CENTER -> {
                    x += (availableWidth - width) / 2F;
                    y += (availableHeight - height) / 2F;
                }
                case EAST -> {
                    x += availableWidth - width;
                    y += (availableHeight - height) / 2F;
                }
                case SOUTH_WEST -> y += availableHeight - height;
                case SOUTH -> {
                    x += (availableWidth - width) / 2F;
                    y += availableHeight - height;
                }
                case SOUTH_EAST -> {
                    x += availableWidth - width;
                    y += availableHeight - height;
                }
            }

            layout.put(component, new Rectangle(x, y, width, height));
        }
        return layout;
    }

    private float widthOf(final Component component, final GridLayoutOptions options) {
        if (options.width() != null) return options.width();
        return this.widthOf(component);
    }

    private float heightOf(final Component component, final GridLayoutOptions options) {
        if (options.height() != null) return options.height();
        return this.heightOf(component);
    }


    private class GridInfo {
        private int columns = 0;
        private int rows = 0;
        private final float[] columnWidths;
        private final float[] rowHeights;
        private final float[] columnWeights;
        private final float[] rowWeights;
        private float totalWeightX = 0;
        private float totalWeightY = 0;

        public GridInfo(final Collection<Component> components) {
            for (Component component : components) {
                GridLayoutOptions options = (GridLayoutOptions) component.layoutOptions();
                if (options == null) options = GridLayoutOptions.EMPTY;
                this.columns = Math.max(this.columns, options.column() + options.columnSpan());
                this.rows = Math.max(this.rows, options.row() + options.rowSpan());
            }

            this.columnWidths = new float[this.columns];
            this.rowHeights = new float[this.rows];
            this.columnWeights = new float[this.columns];
            this.rowWeights = new float[this.rows];

            // 1st pass: non-spanning components
            for (Component component : components) {
                GridLayoutOptions options = (GridLayoutOptions) component.layoutOptions();
                if (options == null) options = GridLayoutOptions.EMPTY;
                float width = GridLayout.this.widthOf(component, options) + options.padding().left() + options.padding().right();
                float height = GridLayout.this.heightOf(component, options) + options.padding().top() + options.padding().bottom();

                if (options.columnSpan() == 1) {
                    this.columnWidths[options.column()] = Math.max(this.columnWidths[options.column()], width);
                    this.columnWeights[options.column()] = Math.max(this.columnWeights[options.column()], options.weightX());
                }
                if (options.rowSpan() == 1) {
                    this.rowHeights[options.row()] = Math.max(this.rowHeights[options.row()], height);
                    this.rowWeights[options.row()] = Math.max(this.rowWeights[options.row()], options.weightY());
                }
            }

            // 2nd pass: spanning components
            for (Component component : components) {
                GridLayoutOptions options = (GridLayoutOptions) component.layoutOptions();
                if (options == null) options = GridLayoutOptions.EMPTY;
                float width = GridLayout.this.widthOf(component, options) + options.padding().left() + options.padding().right();
                float height = GridLayout.this.heightOf(component, options) + options.padding().top() + options.padding().bottom();

                if (options.columnSpan() > 1) {
                    float currentWidth = 0;
                    float currentWeight = 0;
                    for (int i = 0; i < options.columnSpan(); i++) {
                        currentWidth += this.columnWidths[options.column() + i];
                        currentWeight += this.columnWeights[options.column() + i];
                        if (i > 0) currentWidth += GridLayout.this.horizontalGap;
                    }
                    if (width > currentWidth) {
                        float diff = width - currentWidth;
                        for (int i = 0; i < options.columnSpan(); i++) {
                            this.columnWidths[options.column() + i] += diff / options.columnSpan();
                        }
                    }
                    if (options.weightX() > currentWeight) {
                        float diff = options.weightX() - currentWeight;
                        for (int i = 0; i < options.columnSpan(); i++) {
                            this.columnWeights[options.column() + i] += diff / options.columnSpan();
                        }
                    }
                }
                if (options.rowSpan() > 1) {
                    float currentHeight = 0;
                    float currentWeight = 0;
                    for (int i = 0; i < options.rowSpan(); i++) {
                        currentHeight += this.rowHeights[options.row() + i];
                        currentWeight += this.rowWeights[options.row() + i];
                        if (i > 0) currentHeight += GridLayout.this.verticalGap;
                    }
                    if (height > currentHeight) {
                        float diff = height - currentHeight;
                        for (int i = 0; i < options.rowSpan(); i++) {
                            this.rowHeights[options.row() + i] += diff / options.rowSpan();
                        }
                    }
                    if (options.weightY() > currentWeight) {
                        float diff = options.weightY() - currentWeight;
                        for (int i = 0; i < options.rowSpan(); i++) {
                            this.rowHeights[options.row() + i] += diff / options.rowSpan();
                        }
                    }
                }
            }

            // Apply homogeneity
            if (GridLayout.this.homogeneousColumns) {
                float maxWidth = 0;
                for (float w : this.columnWidths) maxWidth = Math.max(maxWidth, w);
                for (int i = 0; i < this.columns; i++) this.columnWidths[i] = maxWidth;
            }
            if (GridLayout.this.homogeneousRows) {
                float maxHeight = 0;
                for (float h : this.rowHeights) maxHeight = Math.max(maxHeight, h);
                for (int i = 0; i < this.rows; i++) this.rowHeights[i] = maxHeight;
            }

            for (float w : this.columnWeights) this.totalWeightX += w;
            for (float w : this.rowWeights) this.totalWeightY += w;
        }
    }

}
