package test.web;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.AssetLoader;
import net.lenni0451.rivet.backend.teavm.TeaVMApplication;
import net.lenni0451.rivet.backend.teavm.TeaVMAssetLoader;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.container.PaddedContainer;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.component.container.tabcontainer.TabContainer;
import net.lenni0451.rivet.component.impl.*;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.layout.list.VerticalListLayout;

import java.util.ArrayList;
import java.util.List;

// Build with './gradlew :examples:assembleWeb' and serve {@code examples/build/web}.
public final class WebMain extends TeaVMApplication {

    public static void main(final String[] args) {
        new WebMain().run();
    }

    @Override
    public Font createFont(final AssetLoader assetLoader) {
        return ((TeaVMAssetLoader) assetLoader).loadFont(20, TeaVMAssetLoader.fetch("NotoSans-Regular.ttf"), TeaVMAssetLoader.fetch("lucide.ttf"));
    }

    @Override
    public void init(final Rivet rivet) {
        TabContainer tabs = new TabContainer();
        tabs.addTab(new Label("Controls"), this.page(this.controls()));
        tabs.addTab(new Label("Text"), this.page(this.text()));
        tabs.addTab(new Label("Colors"), this.page(new ColorPicker(Color.fromRGB(80, 160, 255))));
        rivet.root().add(tabs);
    }

    private Component page(final Component content) {
        return new ScrollContainer(new PaddedContainer(16, content), false, true);
    }

    private Component controls() {
        Container container = new Container(new VerticalListLayout(10, true));
        Label clickLabel = new Label("Not clicked yet");
        int[] clicks = {0};
        container.add(new Button("Click me", () -> clickLabel.text().set("Clicked " + (++clicks[0]) + " time(s)")));
        container.add(clickLabel);
        container.add(new Separator());
        container.add(new TextField("Type here (IME and paste supported)"));
        container.add(new TextArea("Multi line\ntext area"));
        container.add(new Separator());
        container.add(new Checkbox("Checkbox"));
        container.add(new ToggleSwitch(true));
        container.add(new DragNumberInput(0, 100, 1, 42));

        List<ProgressBar> progressBars = new ArrayList<>();
        container.add(new Slider(0, 100, 0.01, 50), slider -> slider.value().changeListener().add(value -> {
            for (ProgressBar bar : progressBars) {
                bar.progress().set((float) value / 100F);
            }
        }));
        for (ProgressBar.TextPosition textPosition : ProgressBar.TextPosition.values()) {
            container.add(new ProgressBar(), bar -> {
                bar.textPosition().set(textPosition);
                bar.progress().set(0.5F);
                progressBars.add(bar);
            });
        }
        return container;
    }

    private Component text() {
        Container container = new Container(new VerticalListLayout(8, true));
        container.add(new FormattedLabel("Plain Text"));
        container.add(new FormattedLabel("<bold>Bold</bold> <italic>Italic</italic> <underlined>Underlined</underlined> <strikethrough>Strikethrough</strikethrough>"));
        container.add(new FormattedLabel("<shadow>Shadow Text</shadow>"));
        container.add(new FormattedLabel("<color=cyan outline_color=yellow>Cyan Text + Yellow Outline</color>"));
        container.add(new FormattedLabel("<bold italic shadow color=red outline_color=yellow>Bold + Italic + Shadow + Outline</bold></italic></shadow></outline_color></color>"));
        container.add(new FormattedLabel("Kerning: AVATAR WAVE Type To"));
        container.add(new FormattedLabel("Unicode: äöü ß € → ✓ 漢字 Ελληνικά"));
        container.add(new FormattedLabel("Icons (lucide):   "));
        return container;
    }

}
