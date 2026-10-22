package net.lenni0451.rivet.xml.parser.impl.property;

import net.lenni0451.rivet.component.impl.slider.SliderTicks;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class SliderTicksParser implements XmlParser<SliderTicks> {

    private final XmlParser<SliderTicks> parser = ElementParserBuilder.create(ctx -> {
        double major = ctx.attribute("major", double.class)
                .or(() -> ctx.attribute("majorTickSpacing", double.class))
                .orElseThrow(() -> new IllegalArgumentException("SliderTicks must have a major tick spacing"));
        double minor = ctx.attribute("minor", double.class)
                .or(() -> ctx.attribute("minorTickSpacing", double.class))
                .orElseThrow(() -> new IllegalArgumentException("SliderTicks must have a minor tick spacing"));
        return ctx.referenceAttribute("labelProvider", SliderTicks.TickLabelProvider.class)
                .map(labelProvider -> new SliderTicks(major, minor, labelProvider))
                .orElseGet(() -> new SliderTicks(major, minor));
    }).build();

    @Override
    public SliderTicks parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
