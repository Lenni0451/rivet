package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.impl.ProgressBar;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class ProgressBarParser implements XmlParser<ProgressBar> {

    private final XmlParser<ProgressBar> parser = ElementParserBuilder.createComponent(ctx -> ctx.attribute("progress", float.class)
                    .map(ProgressBar::new)
                    .orElseGet(ProgressBar::new))
            .themeOption("textFormat", String.class, ProgressBar::textFormat)
            .themeOption("textPosition", ProgressBar.TextPosition.class, ProgressBar::textPosition)
            .themeOption("textPadding", Float.class, ProgressBar::textPadding)
            .themeOption("trackCornerRadius", Corners.class, ProgressBar::trackCornerRadius)
            .themeOption("indicatorCornerRadius", Corners.class, ProgressBar::indicatorCornerRadius)
            .themeOption("trackColor", Color.class, ProgressBar::trackColor)
            .themeOption("indicatorColor", Color.class, ProgressBar::indicatorColor)
            .themeOption("borderColor", Color.class, ProgressBar::borderColor)
            .themeOption("borderWidth", Float.class, ProgressBar::borderWidth)
            .themeOption("textColor", Color.class, ProgressBar::textColor)
            .themeOption("stripes", Boolean.class, ProgressBar::stripes)
            .themeOption("stripeColor", Color.class, ProgressBar::stripeColor)
            .themeOption("stripeWidth", Float.class, ProgressBar::stripeWidth)
            .themeOption("stripeGap", Float.class, ProgressBar::stripeGap)
            .themeOption("stripeSpeed", Float.class, ProgressBar::stripeSpeed)
            .themeOption("stripeAngle", Float.class, ProgressBar::stripeAngle)
            .themeOption("stripeAnimated", Boolean.class, ProgressBar::stripeAnimated)
            .build();

    @Override
    public ProgressBar parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
