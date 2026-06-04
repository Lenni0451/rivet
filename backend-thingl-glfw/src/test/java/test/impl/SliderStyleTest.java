package test.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.*;
import net.lenni0451.rivet.component.impl.*;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.layout.flow.HorizontalFlowLayout;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridLayoutOptions;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.ThemeOption;
import test.TestBase;

public class SliderStyleTest extends TestBase {

    static void main() {
        new SliderStyleTest().run();
    }

    @Override
    protected void init(final Rivet rivet) {
        Container container = new Container(new VerticalListLayout(5, true));
        Slider slider = new Slider(0, 100, 50);
        container.addChild(this.colorOption(rivet, "Bar Color", slider.barColor()));
        container.addChild(this.colorOption(rivet, "Active Bar Color", slider.activeBarColor()));
        container.addChild(this.colorOption(rivet, "Thumb Color", slider.thumbColor()));
        container.addChild(this.colorOption(rivet, "Thumb Click Color", slider.thumbClickColor()));
        container.addChild(this.colorOption(rivet, "Thumb Outline Color", slider.thumbOutlineColor()));
        container.addChild(this.colorOption(rivet, "Thumb Click Outline Color", slider.thumbClickOutlineColor()));
        container.addChild(this.colorOption(rivet, "Tick Color", slider.tickColor()));
        container.addChild(this.floatOption(rivet, "Bar Height", slider.barHeight()));
        container.addChild(this.floatOption(rivet, "Thumb Width", slider.thumbWidth()));
        container.addChild(this.floatOption(rivet, "Thumb Height", slider.thumbHeight()));
        container.addChild(this.floatOption(rivet, "Bar Corner Radius", slider.barCornerRadius()));
        container.addChild(this.floatOption(rivet, "Thumb Corner Radius", slider.thumbCornerRadius()));
        container.addChild(this.floatOption(rivet, "Thumb Outline Width", slider.thumbOutlineWidth()));
        container.addChild(this.booleanOption(rivet, "Thumb Encased", slider.thumbEncased()));
        container.addChild(this.enumOption(rivet, "Thumb Shape", slider.thumbShape()));
        container.addChild(this.booleanOption(rivet, "Show Tooltip", slider.showTooltip()));
        container.addChild(this.stringOption(rivet, "Tooltip Format", slider.tooltipFormat()));
        container.addChild(slider);
        container.addChild(new SolidColor().fixedSize(new Size(1, 500)));
        rivet.root().addChild(new ScrollContainer(container));
    }

    private Component colorOption(final Rivet rivet, final String name, final ThemeOption<Color> option) {
        return new ComboBox(
                name,
                new ScrollContainer(new DecoratedContainer(
                        new SolidColor(s -> s.color(Color.GRAY.withAlpha(150))),
                        c -> {},
                        new ColorPicker(rivet.theme().get(option.key())),
                        picker -> picker.colorChangeListener().add(option::set)
                ))
        );
    }

    private Component floatOption(final Rivet rivet, final String name, final ThemeOption<Float> option) {
        Label currentValue = new Label(String.format("%,.1f", rivet.theme().get(option.key())));
        return new Container(new GridLayout(5, 5))
                .addChild(new Label(name).layoutOptions(new GridLayoutOptions(0, 0)))
                .addChild(new Slider(0, 50, 0.1F, rivet.theme().get(option.key())), slider -> {
                    slider.layoutOptions(new GridLayoutOptions(1, 0).withFill(GridFill.HORIZONTAL).withWeightX(1));
                    slider.valueChangeListener().add(d -> {
                        option.set(d.floatValue());
                        currentValue.text(String.format("%,.1f", d.floatValue()));
                    });
                })
                .addChild(currentValue.layoutOptions(new GridLayoutOptions(2, 0)));
    }

    private Component booleanOption(final Rivet rivet, final String name, final ThemeOption<Boolean> option) {
        Checkbox checkbox = new Checkbox(name, rivet.theme().get(option.key()));
        checkbox.toggleListener().add(option::set);
        return checkbox;
    }

    private <E extends Enum<E>> Component enumOption(final Rivet rivet, final String name, final ThemeOption<E> option) {
        return new ComboBox(
                name,
                new ScrollContainer(new DecoratedContainer(
                        new SolidColor(),
                        s -> s.color(Color.GRAY.withAlpha(150)),
                        new Container(new HorizontalFlowLayout(5, 5)),
                        container -> {
                            for (Enum val : rivet.theme().get(option.key()).getClass().getEnumConstants()) {
                                container.addChild(new Button(val.toString(), () -> {
                                    option.set((E) val);
                                }));
                            }
                        }
                ))
        );
    }

    private Component stringOption(final Rivet rivet, final String name, final ThemeOption<String> option) {
        return new Container(new GridLayout(5, 5))
                .addChild(new Label(name).layoutOptions(new GridLayoutOptions(0, 0)))
                .addChild(new TextField(rivet.theme().get(option.key())), textField -> {
                    textField.layoutOptions(new GridLayoutOptions(1, 0).withFill(GridFill.HORIZONTAL).withWeightX(1));
                    textField.valueChangeListener().add(option::set);
                });
    }

}
