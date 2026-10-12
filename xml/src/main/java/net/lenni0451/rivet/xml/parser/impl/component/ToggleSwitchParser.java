package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.component.impl.ToggleSwitch;
import net.lenni0451.rivet.input.mouse.ClickOn;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class ToggleSwitchParser implements XmlParser<ToggleSwitch> {

    private final XmlParser<ToggleSwitch> parser = ElementParserBuilder.createComponent(ctx -> ctx.attribute("toggled", boolean.class)
                    .map(ToggleSwitch::new)
                    .orElseGet(ToggleSwitch::new))
            .attribute("toggled", boolean.class, ToggleSwitch::toggled)
            .themeOption("cornerRadius", Corners.class, ToggleSwitch::cornerRadius)
            .themeOption("outlineWidth", Float.class, ToggleSwitch::outlineWidth)
            .themeOption("offColor", Color.class, ToggleSwitch::offColor)
            .themeOption("offOutlineColor", Color.class, ToggleSwitch::offOutlineColor)
            .themeOption("onColor", Color.class, ToggleSwitch::onColor)
            .themeOption("onOutlineColor", Color.class, ToggleSwitch::onOutlineColor)
            .themeOption("offThumbColor", Color.class, ToggleSwitch::offThumbColor)
            .themeOption("onThumbColor", Color.class, ToggleSwitch::onThumbColor)
            .themeOption("offThumbOutlineColor", Color.class, ToggleSwitch::offThumbOutlineColor)
            .themeOption("onThumbOutlineColor", Color.class, ToggleSwitch::onThumbOutlineColor)
            .themeOption("thumbOutlineWidth", Float.class, ToggleSwitch::thumbOutlineWidth)
            .themeOption("thumbRatio", Float.class, ToggleSwitch::thumbRatio)
            .themeOption("hoverOffColor", Color.class, ToggleSwitch::hoverOffColor)
            .themeOption("hoverOnColor", Color.class, ToggleSwitch::hoverOnColor)
            .themeOption("hoverOffOutlineColor", Color.class, ToggleSwitch::hoverOffOutlineColor)
            .themeOption("hoverOnOutlineColor", Color.class, ToggleSwitch::hoverOnOutlineColor)
            .themeOption("hoverOffThumbColor", Color.class, ToggleSwitch::hoverOffThumbColor)
            .themeOption("hoverOnThumbColor", Color.class, ToggleSwitch::hoverOnThumbColor)
            .themeOption("hoverOffThumbOutlineColor", Color.class, ToggleSwitch::hoverOffThumbOutlineColor)
            .themeOption("hoverOnThumbOutlineColor", Color.class, ToggleSwitch::hoverOnThumbOutlineColor)
            .themeOption("disabledOffColor", Color.class, ToggleSwitch::disabledOffColor)
            .themeOption("disabledOnColor", Color.class, ToggleSwitch::disabledOnColor)
            .themeOption("disabledOffOutlineColor", Color.class, ToggleSwitch::disabledOffOutlineColor)
            .themeOption("disabledOnOutlineColor", Color.class, ToggleSwitch::disabledOnOutlineColor)
            .themeOption("disabledOffThumbColor", Color.class, ToggleSwitch::disabledOffThumbColor)
            .themeOption("disabledOnThumbColor", Color.class, ToggleSwitch::disabledOnThumbColor)
            .themeOption("disabledOffThumbOutlineColor", Color.class, ToggleSwitch::disabledOffThumbOutlineColor)
            .themeOption("disabledOnThumbOutlineColor", Color.class, ToggleSwitch::disabledOnThumbOutlineColor)
            .themeOption("hoverAnimationConfig", AnimationConfig.class, ToggleSwitch::hoverAnimationConfig)
            .themeOption("toggleAnimationConfig", AnimationConfig.class, ToggleSwitch::toggleAnimationConfig)
            .themeOption("toggleOn", ClickOn.class, ToggleSwitch::toggleOn)
            .themeOption("thumbEncased", Boolean.class, ToggleSwitch::thumbEncased)
            .themeOption("railRatio", Float.class, ToggleSwitch::railRatio)
            .build();

    @Override
    public ToggleSwitch parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
