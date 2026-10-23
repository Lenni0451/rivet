package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.impl.DragNumberInput;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

import java.util.Optional;

public class DragNumberInputParser implements XmlParser<DragNumberInput> {

    private final XmlParser<DragNumberInput> parser = ElementParserBuilder.createComponent(ctx -> {
                Optional<Component> child = ctx.childOrReference("child", Component.class);
                double min = ctx.requireAttribute("min", double.class);
                double max = ctx.requireAttribute("max", double.class);
                double value = ctx.requireAttribute("value", double.class);
                Optional<Double> step = ctx.attribute("step", double.class);

                return child.map(childComponent -> {
                    return step.map(stepValue -> new DragNumberInput(childComponent, min, max, stepValue, value))
                            .orElseGet(() -> new DragNumberInput(childComponent, min, max, value));
                }).orElseGet(() -> {
                    return step.map(stepValue -> new DragNumberInput(min, max, stepValue, value))
                            .orElseGet(() -> new DragNumberInput(min, max, value));
                });
            })
            .themeOption("backgroundColor", Color.class, DragNumberInput::backgroundColor)
            .themeOption("outlineColor", Color.class, DragNumberInput::outlineColor)
            .themeOption("outlineWidth", Float.class, DragNumberInput::outlineWidth)
            .themeOption("cornerRadius", Corners.class, DragNumberInput::cornerRadius)
            .themeOption("innerPadding", Padding.class, DragNumberInput::innerPadding)
            .themeOption("valueFormat", String.class, DragNumberInput::valueFormat)
            .themeOption("hoverBackgroundColor", Color.class, DragNumberInput::hoverBackgroundColor)
            .themeOption("hoverOutlineColor", Color.class, DragNumberInput::hoverOutlineColor)
            .themeOption("clickBackgroundColor", Color.class, DragNumberInput::clickBackgroundColor)
            .themeOption("clickOutlineColor", Color.class, DragNumberInput::clickOutlineColor)
            .themeOption("disabledBackgroundColor", Color.class, DragNumberInput::disabledBackgroundColor)
            .themeOption("disabledOutlineColor", Color.class, DragNumberInput::disabledOutlineColor)
            .themeOption("hoverAnimationConfig", AnimationConfig.class, DragNumberInput::hoverAnimationConfig)
            .themeOption("clickAnimationConfig", AnimationConfig.class, DragNumberInput::clickAnimationConfig)
            .build();

    @Override
    public DragNumberInput parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
