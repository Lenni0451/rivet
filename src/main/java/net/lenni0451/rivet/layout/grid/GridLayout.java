package net.lenni0451.rivet.layout.grid;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.*;
import java.util.function.BiConsumer;

@With
@WithBy
public record GridLayout(int horizontalGap, int verticalGap, boolean homogeneousColumns, boolean homogeneousRows, boolean shrinkColumns, boolean shrinkRows) implements Layout {

    public static final GridLayout DEFAULT = new GridLayout();

    public GridLayout {
        horizontalGap = Math.max(0, horizontalGap);
        verticalGap = Math.max(0, verticalGap);
    }

    public GridLayout() {
        this(0, 0);
    }

    public GridLayout(final int horizontalGap, final int verticalGap) {
        this(horizontalGap, verticalGap, false, false, false, false);
    }

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        ResolvedGrid grid = this.resolveGrid(components);
        if (grid.isEmpty()) {
            return Size.EMPTY;
        }

        AxisInfo columnsInfo = this.calculateColumns(constraints, grid.components(), grid.columns());
        AxisInfo rowsInfo = this.calculateRows(constraints, grid.components(), grid.rows(), columnsInfo.sizes());

        float width = this.sum(columnsInfo.sizes()) + this.getGaps(grid.columns(), this.horizontalGap);
        float height = this.sum(rowsInfo.sizes()) + this.getGaps(grid.rows(), this.verticalGap);
        return new Size(width, height);
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        ResolvedGrid grid = this.resolveGrid(components);
        if (grid.isEmpty()) return;

        AxisInfo columnsInfo = this.calculateColumns(containerSize, grid.components(), grid.columns());
        float[] columnWidths = columnsInfo.sizes();
        this.adjustSizes(columnWidths, containerSize.width(), grid.columns(), this.horizontalGap, this.shrinkColumns, columnsInfo.weights(), columnsInfo.totalWeight());

        AxisInfo rowsInfo = this.calculateRows(containerSize, grid.components(), grid.rows(), columnWidths);
        float[] rowHeights = rowsInfo.sizes();
        this.adjustSizes(rowHeights, containerSize.height(), grid.rows(), this.verticalGap, this.shrinkRows, rowsInfo.weights(), rowsInfo.totalWeight());

        float[] columnOffsets = this.calculateOffsets(columnWidths, this.horizontalGap);
        float[] rowOffsets = this.calculateOffsets(rowHeights, this.verticalGap);

        for (ResolvedComponent resolved : grid.components()) {
            Component component = resolved.component();
            GridOptions options = resolved.options();

            float cellX = columnOffsets[options.column()];
            float cellY = rowOffsets[options.row()];
            float cellWidth = this.sumRange(columnWidths, options.column(), options.columnSpan()) + this.getGaps(options.columnSpan(), this.horizontalGap);
            float cellHeight = this.sumRange(rowHeights, options.row(), options.rowSpan()) + this.getGaps(options.rowSpan(), this.verticalGap);

            float availableWidth = Math.max(0, cellWidth - options.padding().horizontal());
            float availableHeight = Math.max(0, cellHeight - options.padding().vertical());

            boolean fillHorizontal = options.fill().horizontal();
            boolean fillVertical = options.fill().vertical();
            Size idealSize = null;
            if (!fillHorizontal || !fillVertical) {
                idealSize = component.computeIdealSize(new Size(availableWidth, availableHeight));
            }

            float width = fillHorizontal ? availableWidth : Math.min(this.widthOf(component, idealSize), availableWidth);
            float height = fillVertical ? availableHeight : Math.min(this.heightOf(component, idealSize), availableHeight);
            width = this.widthOf(component, width);
            height = this.heightOf(component, height);

            float x = cellX + options.padding().left() + options.anchor().offsetX(availableWidth, width);
            float y = cellY + options.padding().top() + options.anchor().offsetY(availableHeight, height);

            setBounds.accept(component, new Rectangle(x, y, width, height));
        }
    }

    private AxisInfo calculateColumns(final Size constraints, final List<ResolvedComponent> components, final int columns) {
        List<AxisItem> items = new ArrayList<>(components.size());
        for (ResolvedComponent resolved : components) {
            Component component = resolved.component();
            GridOptions options = resolved.options();
            Size idealSize = component.computeIdealSize(constraints.minus(options.padding()));
            float width = this.widthOf(component, idealSize) + options.padding().horizontal();
            items.add(new AxisItem(options.column(), options.columnSpan(), width, options.weightX()));
        }
        return this.calculateAxis(columns, this.horizontalGap, this.homogeneousColumns, items);
    }

    private AxisInfo calculateRows(final Size constraints, final List<ResolvedComponent> components, final int rows, final float[] columnWidths) {
        List<AxisItem> items = new ArrayList<>(components.size());
        for (ResolvedComponent resolved : components) {
            Component component = resolved.component();
            GridOptions options = resolved.options();
            float cellWidth = this.sumRange(columnWidths, options.column(), options.columnSpan()) + this.getGaps(options.columnSpan(), this.horizontalGap);
            float availableWidth = Math.max(0, cellWidth - options.padding().horizontal());
            float availableHeight = Math.max(0, constraints.height() - options.padding().vertical());
            Size idealSize = component.computeIdealSize(new Size(availableWidth, availableHeight));
            float height = this.heightOf(component, idealSize) + options.padding().vertical();
            items.add(new AxisItem(options.row(), options.rowSpan(), height, options.weightY()));
        }
        return this.calculateAxis(rows, this.verticalGap, this.homogeneousRows, items);
    }

    private AxisInfo calculateAxis(final int count, final int gap, final boolean homogeneous, final List<AxisItem> items) {
        float[] sizes = new float[count];
        float[] weights = new float[count];
        List<AxisItem> spanningItems = new ArrayList<>();

        // 1st pass: non-spanning items
        for (AxisItem item : items) {
            if (item.span() == 1) {
                sizes[item.index()] = Math.max(sizes[item.index()], item.size());
                weights[item.index()] = Math.max(weights[item.index()], item.weight());
            } else {
                spanningItems.add(item);
            }
        }

        // 2nd pass: spanning items
        if (!spanningItems.isEmpty()) {
            spanningItems.sort(Comparator.comparingInt(AxisItem::span));

            for (AxisItem item : spanningItems) {
                float currentWeight = this.sumRange(weights, item.index(), item.span());
                if (item.weight() > currentWeight) {
                    float weightDifference = item.weight() - currentWeight;
                    if (currentWeight > 0) {
                        for (int offset = 0; offset < item.span(); offset++) {
                            weights[item.index() + offset] += weightDifference * (weights[item.index() + offset] / currentWeight);
                        }
                    } else {
                        weights[item.index() + item.span() - 1] += weightDifference;
                    }
                }
            }

            if (!homogeneous) {
                for (AxisItem item : spanningItems) {
                    float currentSize = this.sumRange(sizes, item.index(), item.span()) + this.getGaps(item.span(), gap);
                    if (item.size() > currentSize) {
                        float sizeDifference = item.size() - currentSize;
                        float currentWeight = this.sumRange(weights, item.index(), item.span());
                        if (currentWeight > 0) {
                            for (int offset = 0; offset < item.span(); offset++) {
                                sizes[item.index() + offset] += sizeDifference * (weights[item.index() + offset] / currentWeight);
                            }
                        } else {
                            float currentSizesSum = this.sumRange(sizes, item.index(), item.span());
                            if (currentSizesSum > 0) {
                                for (int offset = 0; offset < item.span(); offset++) {
                                    sizes[item.index() + offset] += sizeDifference * (sizes[item.index() + offset] / currentSizesSum);
                                }
                            } else {
                                for (int offset = 0; offset < item.span(); offset++) {
                                    sizes[item.index() + offset] += sizeDifference / item.span();
                                }
                            }
                        }
                    }
                }
            }
        }

        // Apply homogeneity
        if (homogeneous) {
            float maxSize = this.max(sizes);
            for (AxisItem item : spanningItems) {
                float requiredPerTrack = (item.size() - this.getGaps(item.span(), gap)) / item.span();
                maxSize = Math.max(maxSize, requiredPerTrack);
            }
            Arrays.fill(sizes, maxSize);
        }

        return new AxisInfo(sizes, weights, this.sum(weights));
    }

    private GridOptions getSafeOptions(final Component component) {
        if (component.layoutOptions() instanceof GridOptions options) {
            return options;
        }
        return GridOptions.EMPTY;
    }

    private ResolvedGrid resolveGrid(final Collection<Component> components) {
        if (components.isEmpty()) {
            return ResolvedGrid.EMPTY;
        }

        Set<Integer> occupiedColumns = new TreeSet<>();
        Set<Integer> occupiedRows = new TreeSet<>();
        for (Component component : components) {
            GridOptions options = this.getSafeOptions(component);
            for (int offset = 0; offset < options.columnSpan(); offset++) {
                occupiedColumns.add(options.column() + offset);
            }
            for (int offset = 0; offset < options.rowSpan(); offset++) {
                occupiedRows.add(options.row() + offset);
            }
        }

        Map<Integer, Integer> columnMap = new HashMap<>();
        int columnIndex = 0;
        for (int column : occupiedColumns) {
            columnMap.put(column, columnIndex++);
        }

        Map<Integer, Integer> rowMap = new HashMap<>();
        int rowIndex = 0;
        for (int row : occupiedRows) {
            rowMap.put(row, rowIndex++);
        }

        List<ResolvedComponent> resolved = new ArrayList<>(components.size());
        for (Component component : components) {
            GridOptions options = this.getSafeOptions(component);
            int virtualColumn = columnMap.get(options.column());
            int virtualRow = rowMap.get(options.row());
            resolved.add(new ResolvedComponent(component, options.at(virtualColumn, virtualRow)));
        }

        return new ResolvedGrid(resolved, occupiedColumns.size(), occupiedRows.size());
    }

    private void adjustSizes(final float[] sizes, final float containerSpace, final int count, final int gap, final boolean shrink, final float[] weights, final float totalWeight) {
        float totalSize = this.sum(sizes) + this.getGaps(count, gap);

        if (shrink && containerSpace < totalSize) {
            float totalGaps = this.getGaps(count, gap);
            float available = Math.max(0, containerSpace - totalGaps);
            float originalSum = totalSize - totalGaps;
            if (originalSum > 0) {
                float scale = available / originalSum;
                for (int i = 0; i < count; i++) {
                    sizes[i] *= scale;
                }
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
                float deficit = -extraSpace;
                float remainingWeight = totalWeight;
                boolean[] exhausted = new boolean[count];
                while (deficit > 0 && remainingWeight > 0) {
                    boolean clampedAny = false;
                    float passDeficit = deficit;
                    float passWeight = remainingWeight;
                    for (int i = 0; i < count; i++) {
                        if (!exhausted[i] && weights[i] > 0) {
                            float reduction = passDeficit * (weights[i] / passWeight);
                            if (reduction >= sizes[i]) {
                                deficit -= sizes[i];
                                remainingWeight -= weights[i];
                                sizes[i] = 0;
                                exhausted[i] = true;
                                clampedAny = true;
                            }
                        }
                    }
                    if (!clampedAny) {
                        for (int i = 0; i < count; i++) {
                            if (!exhausted[i] && weights[i] > 0) {
                                sizes[i] = Math.max(0, sizes[i] - passDeficit * (weights[i] / passWeight));
                            }
                        }
                        break;
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
        for (float f : array) sum += f;
        return sum;
    }

    private float sumRange(final float[] array, final int start, final int length) {
        float sum = 0;
        for (int i = 0; i < length; i++) sum += array[start + i];
        return sum;
    }

    private float max(final float[] array) {
        float max = 0;
        for (float f : array) max = Math.max(max, f);
        return max;
    }

    private float getGaps(final int count, final int gap) {
        return count > 0 ? (count - 1) * gap : 0;
    }


    private record ResolvedComponent(Component component, GridOptions options) {
    }

    private record ResolvedGrid(List<ResolvedComponent> components, int columns, int rows) {
        private static final ResolvedGrid EMPTY = new ResolvedGrid(List.of(), 0, 0);

        public boolean isEmpty() {
            return this.columns == 0 || this.rows == 0;
        }
    }

    private record AxisItem(int index, int span, float size, float weight) {
    }

    private record AxisInfo(float[] sizes, float[] weights, float totalWeight) {
    }

}
