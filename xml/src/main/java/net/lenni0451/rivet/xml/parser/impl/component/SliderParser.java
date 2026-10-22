package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.component.impl.slider.AbstractSlider;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.component.impl.slider.SliderTicks;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class SliderParser implements XmlParser<Slider> {

    public static <T extends AbstractSlider<?>> ElementParserBuilder<T> applySliderAttributes(final ElementParserBuilder<T> builder) {
        return builder
                .referenceAttribute("font", Font.class, AbstractSlider::font)
                .attribute("min", double.class, (slider, value) -> slider.min(value))
                .attribute("max", double.class, (slider, value) -> slider.max(value))
                .attribute("step", double.class, (slider, value) -> slider.step(value))
                .referenceAttribute("ticks", SliderTicks.class, AbstractSlider::ticks)
                .child(SliderTicks.class, AbstractSlider::ticks)
                .themeOption("barColor", Color.class, AbstractSlider::barColor)
                .themeOption("barFillColor", Color.class, AbstractSlider::barFillColor)
                .themeOption("thumbColor", Color.class, AbstractSlider::thumbColor)
                .themeOption("thumbClickColor", Color.class, AbstractSlider::thumbClickColor)
                .themeOption("tickColor", Color.class, AbstractSlider::tickColor)
                .themeOption("barHeight", Float.class, AbstractSlider::barHeight)
                .themeOption("thumbWidth", Float.class, AbstractSlider::thumbWidth)
                .themeOption("thumbHeight", Float.class, AbstractSlider::thumbHeight)
                .themeOption("barCornerRadius", Corners.class, AbstractSlider::barCornerRadius)
                .themeOption("thumbCornerRadius", Corners.class, AbstractSlider::thumbCornerRadius)
                .themeOption("thumbEncased", Boolean.class, AbstractSlider::thumbEncased)
                .themeOption("thumbShape", AbstractSlider.ThumbShape.class, AbstractSlider::thumbShape)
                .themeOption("thumbOutlineColor", Color.class, AbstractSlider::thumbOutlineColor)
                .themeOption("thumbClickOutlineColor", Color.class, AbstractSlider::thumbClickOutlineColor)
                .themeOption("thumbOutlineWidth", Float.class, AbstractSlider::thumbOutlineWidth)
                .themeOption("showTooltip", Boolean.class, AbstractSlider::showTooltip)
                .themeOption("tooltipFormat", String.class, AbstractSlider::tooltipFormat)
                .themeOption("disabledBarColor", Color.class, AbstractSlider::disabledBarColor)
                .themeOption("disabledBarFillColor", Color.class, AbstractSlider::disabledBarFillColor)
                .themeOption("disabledThumbColor", Color.class, AbstractSlider::disabledThumbColor)
                .themeOption("disabledThumbOutlineColor", Color.class, AbstractSlider::disabledThumbOutlineColor)
                .themeOption("disabledTickColor", Color.class, AbstractSlider::disabledTickColor)
                .themeOption("thumbHoverColor", Color.class, AbstractSlider::thumbHoverColor)
                .themeOption("thumbHoverOutlineColor", Color.class, AbstractSlider::thumbHoverOutlineColor)
                .themeOption("hoverAnimationConfig", AnimationConfig.class, AbstractSlider::hoverAnimationConfig)
                .themeOption("clickAnimationConfig", AnimationConfig.class, AbstractSlider::clickAnimationConfig)
                .themeOption("ensureValuesReachable", Boolean.class, AbstractSlider::ensureValuesReachable)
                .themeOption("clickableRail", Boolean.class, AbstractSlider::clickableRail)
                .themeOption("zeroAlignedValues", Boolean.class, AbstractSlider::zeroAlignedValues);
    }


    private final XmlParser<Slider> parser = applySliderAttributes(
            ElementParserBuilder.createComponent(ctx -> {
                double min = ctx.requireAttribute("min", double.class);
                double max = ctx.requireAttribute("max", double.class);
                double value = ctx.requireAttribute("value", double.class);
                return ctx.attribute("step", double.class)
                        .map(step -> new Slider(min, max, step, value))
                        .orElseGet(() -> new Slider(min, max, value));
            })
    )
            .attribute("value", double.class, Slider::value)
            .build();

    @Override
    public Slider parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
