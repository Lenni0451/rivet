package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.impl.ColorPicker;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class ColorPickerParser implements XmlParser<ColorPicker> {

    private final XmlParser<ColorPicker> parser = ElementParserBuilder.createComponent(ctx -> ctx.attribute("color", Color.class)
                    .map(ColorPicker::new)
                    .orElseGet(ColorPicker::new))
            .themeOption("outlineWidth", Float.class, ColorPicker::outlineWidth)
            .themeOption("outlineColor", Color.class, ColorPicker::outlineColor)
            .themeOption("pickerSize", Float.class, ColorPicker::pickerSize)
            .themeOption("sliderWidth", Float.class, ColorPicker::sliderWidth)
            .themeOption("gap", Float.class, ColorPicker::gap)
            .themeOption("selectorSize", Float.class, ColorPicker::selectorSize)
            .themeOption("showAlpha", Boolean.class, ColorPicker::showAlpha)
            .themeOption("showPreview", Boolean.class, ColorPicker::showPreview)
            .themeOption("allowScaling", Boolean.class, ColorPicker::allowScaling)
            .themeOption("pickerIndicatorOutlineWidth", Float.class, ColorPicker::pickerIndicatorOutlineWidth)
            .themeOption("sliderIndicatorOutlineWidth", Float.class, ColorPicker::sliderIndicatorOutlineWidth)
            .themeOption("pickerIndicatorRadius", Float.class, ColorPicker::pickerIndicatorRadius)
            .themeOption("sliderIndicatorInnerWidth", Float.class, ColorPicker::sliderIndicatorInnerWidth)
            .build();

    @Override
    public ColorPicker parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
