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
import net.lenni0451.rivet.layout.grid.GridOptions;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.theme.ThemeOption;
import test.TestBase;

public class SliderStyleTest extends TestBase {

    void main() {
        this.run();
    }

    @Override
    protected void init(final Rivet rivet) {
        Container container = new Container(new VerticalListLayout(5, true));
        Slider slider = new Slider(0, 100, 50);
        container.add(this.colorOption(rivet, "Bar Color", slider.barColor()));
        container.add(this.colorOption(rivet, "Bar Fill Color", slider.barFillColor()));
        container.add(this.colorOption(rivet, "Thumb Color", slider.thumbColor()));
        container.add(this.colorOption(rivet, "Thumb Hover Color", slider.thumbHoverColor()));
        container.add(this.colorOption(rivet, "Thumb Click Color", slider.thumbClickColor()));
        container.add(this.colorOption(rivet, "Thumb Outline Color", slider.thumbOutlineColor()));
        container.add(this.colorOption(rivet, "Thumb Hover Outline Color", slider.thumbHoverOutlineColor()));
        container.add(this.colorOption(rivet, "Thumb Click Outline Color", slider.thumbClickOutlineColor()));
        container.add(this.colorOption(rivet, "Tick Color", slider.tickColor()));
        container.add(this.colorOption(rivet, "Disabled Bar Color", slider.disabledBarColor()));
        container.add(this.colorOption(rivet, "Disabled Bar Fill Color", slider.disabledBarFillColor()));
        container.add(this.colorOption(rivet, "Disabled Thumb Color", slider.disabledThumbColor()));
        container.add(this.colorOption(rivet, "Disabled Thumb Outline Color", slider.disabledThumbOutlineColor()));
        container.add(this.colorOption(rivet, "Disabled Tick Color", slider.disabledTickColor()));
        container.add(this.floatOption(rivet, "Bar Height", slider.barHeight()));
        container.add(this.floatOption(rivet, "Thumb Width", slider.thumbWidth()));
        container.add(this.floatOption(rivet, "Thumb Height", slider.thumbHeight()));
        container.add(this.cornersOption(rivet, "Bar Corner Radius", slider.barCornerRadius()));
        container.add(this.cornersOption(rivet, "Thumb Corner Radius", slider.thumbCornerRadius()));
        container.add(this.floatOption(rivet, "Thumb Outline Width", slider.thumbOutlineWidth()));
        container.add(this.booleanOption(rivet, "Thumb Encased", slider.thumbEncased()));
        container.add(this.enumOption(rivet, "Thumb Shape", slider.thumbShape()));
        container.add(this.booleanOption(rivet, "Show Tooltip", slider.showTooltip()));
        container.add(this.stringOption(rivet, "Tooltip Format", slider.tooltipFormat()));
        container.add(this.booleanOption(rivet, "Ensure Values Reachable", slider.ensureValuesReachable()));
        container.add(slider);
        container.add(new SolidColor().fixedSize(1, 500));
        rivet.root().add(new ScrollContainer(container));
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
                .add(new Label(name).layoutOptions(new GridOptions(0, 0)))
                .add(new Slider(0, 50, 0.1F, rivet.theme().get(option.key())), slider -> {
                    slider.layoutOptions(new GridOptions(1, 0).withFill(GridFill.HORIZONTAL).withWeightX(1));
                    slider.valueChangeListener().add(d -> {
                        option.set(d.floatValue());
                        currentValue.text(String.format("%,.1f", d.floatValue()));
                    });
                })
                .add(currentValue.layoutOptions(new GridOptions(2, 0)));
    }

    private Component cornersOption(final Rivet rivet, final String name, final ThemeOption<Corners> option) {
        Label currentValue = new Label(String.format("%,.1f", rivet.theme().get(option.key()).topLeft()));
        return new Container(new GridLayout(5, 5))
                .add(new Label(name).layoutOptions(new GridOptions(0, 0)))
                .add(new Slider(0, 50, 0.1F, rivet.theme().get(option.key()).topLeft()), slider -> {
                    slider.layoutOptions(new GridOptions(1, 0).withFill(GridFill.HORIZONTAL).withWeightX(1));
                    slider.valueChangeListener().add(d -> {
                        option.set(new Corners(d.floatValue()));
                        currentValue.text(String.format("%,.1f", d.floatValue()));
                    });
                })
                .add(currentValue.layoutOptions(new GridOptions(2, 0)));
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
                                container.add(new Button(val.toString(), () -> {
                                    option.set((E) val);
                                }));
                            }
                        }
                ))
        );
    }

    private Component stringOption(final Rivet rivet, final String name, final ThemeOption<String> option) {
        return new Container(new GridLayout(5, 5))
                .add(new Label(name).layoutOptions(new GridOptions(0, 0)))
                .add(new TextField(rivet.theme().get(option.key())), textField -> {
                    textField.layoutOptions(new GridOptions(1, 0).withFill(GridFill.HORIZONTAL).withWeightX(1));
                    textField.valueChangeListener().add(option::set);
                });
    }

}
