package test.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.ProgressBar;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.layout.grid.GridAnchor;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridLayoutOptions;
import test.TestBase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressBarTest extends TestBase {

    void main() {
        this.run();
    }

    @Override
    protected void init(final Rivet rivet) {
        List<ProgressBar> progressBars = new ArrayList<>();
        Container container = new Container(new GridLayout(5, 5));
        AtomicInteger y = new AtomicInteger(0);
        for (ProgressBar.TextPosition textPosition : ProgressBar.TextPosition.values()) {
            container.addChild(new Label(textPosition.name()), label -> {
                label.layoutOptions(new GridLayoutOptions(0, y.get()).withAnchor(GridAnchor.LEFT));
            });
            container.addChild(new ProgressBar(), bar -> {
                progressBars.add(bar);
                bar.textPosition().set(textPosition);
                bar.layoutOptions(new GridLayoutOptions(1, y.getAndIncrement()).withFill(GridFill.HORIZONTAL).withWeightX(1));
            });
        }
        container.addChild(new Slider(0, 100, 0.01, 50), slider -> {
            slider.layoutOptions(new GridLayoutOptions(0, y.getAndIncrement()).withFill(GridFill.HORIZONTAL).withWeightX(1).withColumnSpan(2));
            slider.valueChangeListener().add(value -> {
                for (ProgressBar bar : progressBars) {
                    bar.progress(value.floatValue() / 100F);
                }
            });
            slider.showTooltip().set(false);
        });
        rivet.root().addChild(container);
    }

}
