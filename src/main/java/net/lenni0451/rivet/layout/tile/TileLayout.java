package net.lenni0451.rivet.layout.tile;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public record TileLayout(int columns, int rows, int horizontalGap, int verticalGap) implements Layout {

    public TileLayout(final int columns, final int rows) {
        this(columns, rows, 0, 0);
    }

    @Override
    public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
        Grid grid = this.calculateGrid(components);
        Size cellSize = Size.EMPTY;
        for (Component component : components) {
            Size idealSize = component.computeIdealSize(constraints);
            cellSize = new Size(
                    Math.max(cellSize.width(), idealSize.width()),
                    Math.max(cellSize.height(), idealSize.height())
            );
        }
        return new Size(
                cellSize.width() * grid.columns + Math.max(this.verticalGap * (grid.columns - 1), 0),
                cellSize.height() * grid.rows + Math.max(this.horizontalGap * (grid.rows - 1), 0)
        );
    }

    @Override
    public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
        Grid grid = this.calculateGrid(components);
        Size cellSize = new Size(
                (containerSize.width() - Math.max(this.verticalGap * (grid.columns - 1), 0)) / grid.columns,
                (containerSize.height() - Math.max(this.horizontalGap * (grid.rows - 1), 0)) / grid.rows
        );
        BitSet occupied = new BitSet(grid.count());
        List<Component> notLayouted = new ArrayList<>();
        for (Component component : components) {
            if (component.layoutOptions() instanceof TileLayoutOptions options) {
                int column = options.column();
                int row = options.row();
                if (column < 0 || row < 0 || column >= grid.columns || row >= grid.rows) {
                    notLayouted.add(component);
                } else {
                    int index = row * grid.columns + column;
                    if (occupied.get(index)) {
                        notLayouted.add(component);
                    } else {
                        occupied.set(index);
                        setBounds.accept(component, new Rectangle(
                                column * (cellSize.width() + this.verticalGap),
                                row * (cellSize.height() + this.horizontalGap),
                                cellSize.width(),
                                cellSize.height()
                        ));
                    }
                }
            } else {
                notLayouted.add(component);
            }
        }
        for (Component component : notLayouted) {
            int index = -1;
            for (int i = 0; i < grid.count(); i++) {
                if (!occupied.get(i)) {
                    occupied.set(i);
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                throw new IllegalStateException("Grid layout failed to resize grid (" + grid + ", " + components.size() + ")");
            }
            int column = index % grid.columns;
            int row = index / grid.columns;
            setBounds.accept(component, new Rectangle(
                    column * (cellSize.width() + this.verticalGap),
                    row * (cellSize.height() + this.horizontalGap),
                    cellSize.width(),
                    cellSize.height()
            ));
        }
    }

    private Grid calculateGrid(final Collection<Component> components) {
        int columns = this.columns;
        int rows = this.rows;
        if (columns * rows < components.size()) {
            columns = (int) Math.ceil((float) components.size() / rows);
        }
        return new Grid(columns, rows);
    }


    private record Grid(int columns, int rows) {
        public int count() {
            return this.columns * this.rows;
        }
    }

}
