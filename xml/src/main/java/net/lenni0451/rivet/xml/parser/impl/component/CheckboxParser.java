package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.component.impl.Checkbox;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

import java.util.Optional;

public class CheckboxParser implements XmlParser<Checkbox> {

    private final XmlParser<Checkbox> parser = ElementParserBuilder.createComponent(ctx -> {
                Optional<String> text = ctx.attribute("text", String.class);
                Optional<Boolean> checked = ctx.attribute("checked", boolean.class);
                if (text.isPresent() && checked.isPresent()) return new Checkbox(text.get(), checked.get());
                else if (text.isPresent()) return new Checkbox(text.get());
                else if (checked.isPresent()) return new Checkbox(checked.get());
                else return new Checkbox();
            })
            .referenceAttribute("font", Font.class, Checkbox::font)
            .attribute("text", String.class, Checkbox::text)
            .attribute("checked", boolean.class, Checkbox::checked)
            .themeOption("cornerRadius", Corners.class, Checkbox::cornerRadius)
            .themeOption("outlineWidth", Float.class, Checkbox::outlineWidth)
            .themeOption("backgroundColor", Color.class, Checkbox::backgroundColor)
            .themeOption("outlineColor", Color.class, Checkbox::outlineColor)
            .themeOption("checkColor", Color.class, Checkbox::checkColor)
            .themeOption("checkWidth", Float.class, Checkbox::checkWidth)
            .themeOption("textGap", Float.class, Checkbox::textGap)
            .themeOption("hoverBackgroundColor", Color.class, Checkbox::hoverBackgroundColor)
            .themeOption("hoverOutlineColor", Color.class, Checkbox::hoverOutlineColor)
            .themeOption("disabledBackgroundColor", Color.class, Checkbox::disabledBackgroundColor)
            .themeOption("disabledOutlineColor", Color.class, Checkbox::disabledOutlineColor)
            .themeOption("disabledCheckColor", Color.class, Checkbox::disabledCheckColor)
            .themeOption("hoverAnimationConfig", AnimationConfig.class, Checkbox::hoverAnimationConfig)
            .themeOption("checkAnimationConfig", AnimationConfig.class, Checkbox::checkAnimationConfig)
            .build();

    @Override
    public Checkbox parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
