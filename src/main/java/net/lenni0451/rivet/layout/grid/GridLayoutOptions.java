package net.lenni0451.rivet.layout.grid;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.layout.LayoutOptions;
import net.lenni0451.rivet.math.Padding;

@With
@WithBy
public record GridLayoutOptions(
        int column, int row,
        int columnSpan, int rowSpan,
        float weightX, float weightY,
        GridAnchor anchor,
        GridFill fill,
        Padding padding
) implements LayoutOptions {

    public static final GridLayoutOptions EMPTY = new GridLayoutOptions(0, 0, 1, 1, 0, 0, GridAnchor.CENTER, GridFill.NONE, Padding.EMPTY);

    public GridLayoutOptions(final int column, final int row) {
        this(column, row, 1, 1, 0, 0, GridAnchor.CENTER, GridFill.NONE, Padding.EMPTY);
    }

    public GridLayoutOptions at(final int column, final int row) {
        return this.withColumn(column).withRow(row);
    }

    public GridLayoutOptions span(final int columnSpan, final int rowSpan) {
        return this.withColumnSpan(columnSpan).withRowSpan(rowSpan);
    }

    public GridLayoutOptions weight(final float weightX, final float weightY) {
        return this.withWeightX(weightX).withWeightY(weightY);
    }

}
