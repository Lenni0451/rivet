package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.rivet.component.impl.slider.RangeSlider;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class RangeSliderParser implements XmlParser<RangeSlider> {

    private final XmlParser<RangeSlider> parser = SliderParser.applySliderAttributes(
                    ElementParserBuilder.createComponent(ctx -> {
                        double min = ctx.requireAttribute("min", double.class);
                        double max = ctx.requireAttribute("max", double.class);
                        double lowerValue = ctx.requireAttribute("lowerValue", double.class);
                        double upperValue = ctx.requireAttribute("upperValue", double.class);
                        return ctx.attribute("step", double.class)
                                .map(step -> new RangeSlider(min, max, step, lowerValue, upperValue))
                                .orElseGet(() -> new RangeSlider(min, max, lowerValue, upperValue));
                    })
            )
            .attribute("lowerValue", double.class, RangeSlider::lowerValue)
            .attribute("upperValue", double.class, RangeSlider::upperValue)
            .build();

    @Override
    public RangeSlider parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
