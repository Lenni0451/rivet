package net.lenni0451.rivet.editor.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.*;
import net.lenni0451.rivet.editor.properties.MutableProperty;
import net.lenni0451.rivet.layout.absolute.AbsoluteLayout;
import net.lenni0451.rivet.math.Corners;

import java.util.ArrayList;
import java.util.List;

public class ComponentRegistry {

    private static final List<RegisteredComponent> components = new ArrayList<>();

    static {
        register(new RegisteredComponent(
                "Label",
                () -> new Label("Label"),
                List.of(
                        new MutableProperty<Label, String>("Text", String.class, Label::text, Label::text),
                        new MutableProperty<Label, Float>("Scale", Float.class, Label::scale, Label::scale)
                )
        ));
        register(new RegisteredComponent(
                "Button",
                () -> new Button("Button")
        ));
        register(new RegisteredComponent(
                "Checkbox",
                () -> new Checkbox("Checkbox", false),
                List.of(
                        new MutableProperty<Checkbox, String>("Text", String.class, Checkbox::text, Checkbox::text),
                        new MutableProperty<Checkbox, Boolean>("Checked", Boolean.class, Checkbox::checked, Checkbox::checked)
                )
        ));
        register(new RegisteredComponent(
                "ToggleSwitch",
                () -> new ToggleSwitch().toggled(false),
                List.of(
                        new MutableProperty<ToggleSwitch, Boolean>("Toggled", Boolean.class, ToggleSwitch::toggled, ToggleSwitch::toggled)
                )
        ));
        register(new RegisteredComponent(
                "TextField",
                () -> new TextField().text(""),
                List.of(
                        new MutableProperty<TextField, String>("Text", String.class, TextField::text, TextField::text),
                        new MutableProperty<TextField, String>("Hint", String.class, TextField::hint, TextField::hint)
                )
        ));
        register(new RegisteredComponent(
                "SolidColor",
                () -> new SolidColor(Color.BLUE).cornerRadius(4f),
                List.of(
                        new MutableProperty<SolidColor, Color>("Color", Color.class, SolidColor::color, SolidColor::color),
                        new MutableProperty<SolidColor, Color>("Outline Color", Color.class, SolidColor::outlineColor, SolidColor::outlineColor),
                        new MutableProperty<SolidColor, Float>("Outline Width", Float.class, SolidColor::outlineWidth, SolidColor::outlineWidth),
                        new MutableProperty<SolidColor, Corners>("Corner Radius", Corners.class, SolidColor::cornerRadius, SolidColor::cornerRadius)
                )
        ));
        register(new RegisteredComponent(
                "Container",
                () -> new Container(AbsoluteLayout.INSTANCE).minSize(100, 100)
        ));
    }

    public static <C extends Component> void register(final RegisteredComponent component) {
        components.add(component);
    }

    public static List<RegisteredComponent> components() {
        return List.copyOf(components);
    }

}
