package test.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.layout.tile.TileLayout;
import net.lenni0451.rivet.layout.tile.TileLayoutOptions;
import test.TestBase;

public class TileLayoutTest extends TestBase {

    void main() {
        this.run();
    }

    @Override
    protected void init(final Rivet rivet) {
        Container container = new Container(new TileLayout(3, 3, 10, 10));
        for (int i = 0; i < 9; i++) {
            final int finalI = i;
            final int column = i % 3;
            final int row = i / 3;
            if (column == 1 && row == 0) continue;
            if (column == 0 && row == 1) continue;
            if (column == 2 && row == 1) continue;
            if (column == 1 && row == 2) continue;
            container.addChild(new Button("Testing " + i, () -> {
                System.out.println("Clicked " + finalI);
            }), button -> button.layoutOptions(new TileLayoutOptions(column, row)));
        }
        rivet.root().addChild(container);
    }

}
