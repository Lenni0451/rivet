package net.lenni0451.rivet.layout.tile;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.layout.LayoutOptions;

@With
@WithBy
public record TileOptions(int column, int row) implements LayoutOptions {

    public static TileOptions at(final int column, final int row) {
        return new TileOptions(column, row);
    }

}
