package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.input.mouse.ClickOn;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

import java.util.Set;

public class ButtonParser implements XmlParser<Button> {

    private final XmlParser<Button> parser = ElementParserBuilder.createComponent(ctx -> ctx
                    .attribute("text", String.class)
                    .map(Button::new)
                    .or(() -> ctx.child(Component.class).map(Button::new))
                    .orElseThrow(() -> new IllegalArgumentException("Button must have a text attribute or a child component")))
            .referenceAttribute("handledButtons", Set.class, (button, buttons) -> {
                button.handledButtons().clear();
                button.handledButtons().addAll(buttons);
            })
            .themeOption("cornerRadius", Corners.class, Button::cornerRadius)
            .themeOption("outlineWidth", Float.class, Button::outlineWidth)
            .themeOption("backgroundColor", Color.class, Button::backgroundColor)
            .themeOption("outlineColor", Color.class, Button::outlineColor)
            .themeOption("hoverBackgroundColor", Color.class, Button::hoverBackgroundColor)
            .themeOption("hoverOutlineColor", Color.class, Button::hoverOutlineColor)
            .themeOption("clickBackgroundColor", Color.class, Button::clickBackgroundColor)
            .themeOption("clickOutlineColor", Color.class, Button::clickOutlineColor)
            .themeOption("disabledBackgroundColor", Color.class, Button::disabledBackgroundColor)
            .themeOption("disabledOutlineColor", Color.class, Button::disabledOutlineColor)
            .themeOption("hoverAnimationConfig", AnimationConfig.class, Button::hoverAnimationConfig)
            .themeOption("clickAnimationConfig", AnimationConfig.class, Button::clickAnimationConfig)
            .themeOption("innerPadding", Padding.class, Button::innerPadding)
            .themeOption("clickOn", ClickOn.class, Button::clickOn)
            .build();

    @Override
    public Button parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
