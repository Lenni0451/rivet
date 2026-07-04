package net.lenni0451.rivet.layout.grid;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * This class is completely AI generated.
 */
@With
@WithBy
public record GridLayout(int horizontalGap, int verticalGap, boolean homogeneousColumns, boolean homogeneousRows, boolean shrinkColumns, boolean shrinkRows) implements Layout {

    public GridLayout() {
        this(0, 0);
    }

    public GridLayout(final int horizontalGap, final int verticalGap) {
        this(horizontalGap, verticalGap, false, false, false, false);
    }

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        final CompressedGrid compressed = this.compressGrid(components);
        if (compressed.isEmpty()) {
            return Size.EMPTY;
        }

        final ColumnsInfo columnsInfo = this.calculateColumns(constraints, components, compressed, compressed.columns());
        final RowsInfo rowsInfo = this.calculateRows(constraints, components, compressed, compressed.rows(), columnsInfo.widths());

        float width = this.sum(columnsInfo.widths()) + this.getGaps(compressed.columns(), this.horizontalGap);
        float height = this.sum(rowsInfo.heights()) + this.getGaps(compressed.rows(), this.verticalGap);

        return new Size(width, height);
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        final CompressedGrid compressed = this.compressGrid(components);
        if (compressed.isEmpty()) return;

        final ColumnsInfo columnsInfo = this.calculateColumns(containerSize, components, compressed, compressed.columns());
        final float[] colWidths = columnsInfo.widths().clone();

        // Adjust column widths for shrinking or weighting (growing)
        this.adjustSizes(colWidths, containerSize.width(), compressed.columns(), this.horizontalGap,
                this.shrinkColumns, columnsInfo.weights(), columnsInfo.totalWeight());

        final RowsInfo rowsInfo = this.calculateRows(containerSize, components, compressed, compressed.rows(), colWidths);
        final float[] rowHeights = rowsInfo.heights().clone();

        // Adjust row heights for shrinking or weighting (growing)
        this.adjustSizes(rowHeights, containerSize.height(), compressed.rows(), this.verticalGap,
                this.shrinkRows, rowsInfo.weights(), rowsInfo.totalWeight());

        // Pre-calculate offsets
        final float[] colOffsets = this.calculateOffsets(colWidths, this.horizontalGap);
        final float[] rowOffsets = this.calculateOffsets(rowHeights, this.verticalGap);

        for (final Component component : components) {
            final GridLayoutOptions options = compressed.getVirtualOptions(this.getSafeOptions(component));

            final float cellX = colOffsets[options.column()];
            final float cellY = rowOffsets[options.row()];

            float cellWidth = this.sumRange(colWidths, options.column(), options.columnSpan())
                    + this.getGaps(options.columnSpan(), this.horizontalGap);
            float cellHeight = this.sumRange(rowHeights, options.row(), options.rowSpan())
                    + this.getGaps(options.rowSpan(), this.verticalGap);

            final float availableWidth = Math.max(0, cellWidth - options.padding().horizontal());
            final float availableHeight = Math.max(0, cellHeight - options.padding().vertical());

            final Size componentConstraints = new Size(availableWidth, availableHeight);
            final Size idealSize = component.computeIdealSize(componentConstraints);

            final float idealWidth = options.width() != null ? options.width() : this.widthOf(component, idealSize);
            final float idealHeight = options.height() != null ? options.height() : this.heightOf(component, idealSize);

            float width = idealWidth;
            float height = idealHeight;

            if (options.fill() == GridFill.HORIZONTAL || options.fill() == GridFill.BOTH) width = availableWidth;
            if (options.fill() == GridFill.VERTICAL || options.fill() == GridFill.BOTH) height = availableHeight;

            width = Math.max(0, Math.min(width, Math.min(availableWidth, component.maxSize().width())));
            height = Math.max(0, Math.min(height, Math.min(availableHeight, component.maxSize().height())));

            float x = cellX + options.padding().left();
            float y = cellY + options.padding().top();

            switch (options.anchor()) {
                case TOP_LEFT -> {
                }
                case TOP -> x += (availableWidth - width) / 2F;
                case TOP_RIGHT -> x += availableWidth - width;
                case LEFT -> y += (availableHeight - height) / 2F;
                case CENTER -> {
                    x += (availableWidth - width) / 2F;
                    y += (availableHeight - height) / 2F;
                }
                case RIGHT -> {
                    x += availableWidth - width;
                    y += (availableHeight - height) / 2F;
                }
                case BOTTOM_LEFT -> y += availableHeight - height;
                case BOTTOM -> {
                    x += (availableWidth - width) / 2F;
                    y += availableHeight - height;
                }
                case BOTTOM_RIGHT -> {
                    x += availableWidth - width;
                    y += availableHeight - height;
                }
            }

            setBounds.accept(component, new Rectangle(x, y, width, height));
        }
    }

    private ColumnsInfo calculateColumns(final Size constraints, final Collection<Component> components, final CompressedGrid compressed, final int columns) {
        final float[] columnWidths = new float[columns];
        final float[] columnWeights = new float[columns];

        // 1st pass: non-spanning columns
        for (final Component component : components) {
            final GridLayoutOptions options = compressed.getVirtualOptions(this.getSafeOptions(component));
            if (options.columnSpan() == 1) {
                final Size idealSize = component.computeIdealSize(constraints);
                float width = (options.width() != null ? options.width() : this.widthOf(component, idealSize)) + options.padding().horizontal();
                columnWidths[options.column()] = Math.max(columnWidths[options.column()], width);
                columnWeights[options.column()] = Math.max(columnWeights[options.column()], options.weightX());
            }
        }

        // 2nd pass: spanning columns
        for (final Component component : components) {
            final GridLayoutOptions options = compressed.getVirtualOptions(this.getSafeOptions(component));
            if (options.columnSpan() > 1) {
                final Size idealSize = component.computeIdealSize(constraints);
                float width = (options.width() != null ? options.width() : this.widthOf(component, idealSize)) + options.padding().horizontal();

                float currentWidth = this.sumRange(columnWidths, options.column(), options.columnSpan()) + this.getGaps(options.columnSpan(), this.horizontalGap);
                float currentWeight = this.sumRange(columnWeights, options.column(), options.columnSpan());

                if (width > currentWidth) {
                    final float diff = width - currentWidth;
                    for (int i = 0; i < options.columnSpan(); i++) {
                        columnWidths[options.column() + i] += diff / options.columnSpan();
                    }
                }
                if (options.weightX() > currentWeight) {
                    final float diff = options.weightX() - currentWeight;
                    for (int i = 0; i < options.columnSpan(); i++) {
                        columnWeights[options.column() + i] += diff / options.columnSpan();
                    }
                }
            }
        }

        // Apply homogeneity
        if (this.homogeneousColumns) {
            float maxWidth = this.max(columnWidths);
            Arrays.fill(columnWidths, maxWidth);
        }

        return new ColumnsInfo(columnWidths, columnWeights, this.sum(columnWeights));
    }

    private RowsInfo calculateRows(final Size constraints, final Collection<Component> components, final CompressedGrid compressed, final int rows, final float[] colWidths) {
        final float[] rowHeights = new float[rows];
        final float[] rowWeights = new float[rows];
        final Map<Component, Float> componentHeights = new IdentityHashMap<>();

        // 1st pass: non-spanning rows
        for (final Component component : components) {
            final GridLayoutOptions options = compressed.getVirtualOptions(this.getSafeOptions(component));

            float cellWidth = this.sumRange(colWidths, options.column(), options.columnSpan()) + this.getGaps(options.columnSpan(), this.horizontalGap);
            final float availableWidth = Math.max(0, cellWidth - options.padding().horizontal());

            final Size componentConstraints = new Size(availableWidth, constraints.height() - options.padding().vertical());
            final Size idealSize = component.computeIdealSize(componentConstraints);

            float height = (options.height() != null ? options.height() : this.heightOf(component, idealSize)) + options.padding().vertical();
            componentHeights.put(component, height);

            if (options.rowSpan() == 1) {
                rowHeights[options.row()] = Math.max(rowHeights[options.row()], height);
                rowWeights[options.row()] = Math.max(rowWeights[options.row()], options.weightY());
            }
        }

        // 2nd pass: spanning rows
        for (final Component component : components) {
            final GridLayoutOptions options = compressed.getVirtualOptions(this.getSafeOptions(component));
            if (options.rowSpan() > 1) {
                final float height = componentHeights.get(component);

                float currentHeight = this.sumRange(rowHeights, options.row(), options.rowSpan()) + this.getGaps(options.rowSpan(), this.verticalGap);
                float currentWeight = this.sumRange(rowWeights, options.row(), options.rowSpan());

                if (height > currentHeight) {
                    final float diff = height - currentHeight;
                    for (int i = 0; i < options.rowSpan(); i++) {
                        rowHeights[options.row() + i] += diff / options.rowSpan();
                    }
                }
                if (options.weightY() > currentWeight) {
                    final float diff = options.weightY() - currentWeight;
                    for (int i = 0; i < options.rowSpan(); i++) {
                        rowWeights[options.row() + i] += diff / options.rowSpan();
                    }
                }
            }
        }

        // Apply homogeneity
        if (this.homogeneousRows) {
            float maxHeight = this.max(rowHeights);
            Arrays.fill(rowHeights, maxHeight);
        }

        return new RowsInfo(rowHeights, rowWeights, this.sum(rowWeights));
    }

    // --- Helper Methods ---

    private GridLayoutOptions getSafeOptions(final Component component) {
        if (component.layoutOptions() instanceof GridLayoutOptions options) {
            return options;
        }
        return GridLayoutOptions.EMPTY;
    }

    private CompressedGrid compressGrid(final Collection<Component> components) {
        final Set<Integer> occupiedCols = new TreeSet<>();
        final Set<Integer> occupiedRows = new TreeSet<>();
        for (final Component component : components) {
            final GridLayoutOptions options = this.getSafeOptions(component);
            for (int i = 0; i < options.columnSpan(); i++) {
                occupiedCols.add(options.column() + i);
            }
            for (int i = 0; i < options.rowSpan(); i++) {
                occupiedRows.add(options.row() + i);
            }
        }

        final Map<Integer, Integer> colMap = new HashMap<>();
        int colIndex = 0;
        for (final int col : occupiedCols) {
            colMap.put(col, colIndex++);
        }

        final Map<Integer, Integer> rowMap = new HashMap<>();
        int rowIndex = 0;
        for (final int row : occupiedRows) {
            rowMap.put(row, rowIndex++);
        }

        return new CompressedGrid(colMap, rowMap, occupiedCols.size(), occupiedRows.size());
    }

    private void adjustSizes(final float[] sizes, final float containerSpace, final int count, final int gap, final boolean shrink, final float[] weights, final float totalWeight) {
        float totalSize = this.sum(sizes) + this.getGaps(count, gap);

        if (shrink && containerSpace < totalSize) {
            float totalGaps = this.getGaps(count, gap);
            float available = Math.max(0, containerSpace - totalGaps);
            float originalSum = totalSize - totalGaps;
            if (originalSum > 0) {
                float scale = available / originalSum;
                for (int i = 0; i < count; i++) sizes[i] *= scale;
            } else {
                Arrays.fill(sizes, 0);
            }
        } else {
            float extraSpace = containerSpace - totalSize;
            if (extraSpace > 0 && totalWeight > 0) {
                for (int i = 0; i < count; i++) {
                    sizes[i] += extraSpace * (weights[i] / totalWeight);
                }
            } else if (extraSpace < 0 && totalWeight > 0) {
                for (int i = 0; i < count; i++) {
                    if (weights[i] > 0) {
                        sizes[i] = Math.max(0, sizes[i] + extraSpace * (weights[i] / totalWeight));
                    }
                }
            }
        }
    }

    private float[] calculateOffsets(final float[] sizes, final int gap) {
        float[] offsets = new float[sizes.length];
        float current = 0;
        for (int i = 0; i < sizes.length; i++) {
            offsets[i] = current;
            current += sizes[i] + gap;
        }
        return offsets;
    }

    private float sum(final float[] array) {
        float sum = 0;
        for (float v : array) sum += v;
        return sum;
    }

    private float sumRange(final float[] array, final int start, final int length) {
        float sum = 0;
        for (int i = 0; i < length; i++) sum += array[start + i];
        return sum;
    }

    private float max(final float[] array) {
        float max = 0;
        for (float v : array) max = Math.max(max, v);
        return max;
    }

    private float getGaps(final int count, final int gap) {
        return count > 0 ? (count - 1) * gap : 0;
    }

    // --- Records ---

    private record CompressedGrid(Map<Integer, Integer> colMap, Map<Integer, Integer> rowMap, int columns, int rows) {
        public boolean isEmpty() {
            return this.columns == 0 || this.rows == 0;
        }

        public GridLayoutOptions getVirtualOptions(final GridLayoutOptions options) {
            final int virtualCol = this.colMap.getOrDefault(options.column(), 0);
            final int virtualRow = this.rowMap.getOrDefault(options.row(), 0);
            return options.withColumn(virtualCol).withRow(virtualRow);
        }
    }

    private record ColumnsInfo(float[] widths, float[] weights, float totalWeight) {
    }

    private record RowsInfo(float[] heights, float[] weights, float totalWeight) {
    }

}
