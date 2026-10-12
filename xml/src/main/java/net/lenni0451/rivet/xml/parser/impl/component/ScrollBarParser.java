package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.impl.ScrollBar;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class ScrollBarParser implements XmlParser<ScrollBar> {

    private final XmlParser<ScrollBar> parser = ElementParserBuilder.createComponent(ctx -> new ScrollBar(
                    ctx.requireAttribute("orientation", ScrollBar.Orientation.class)
            ))
            .attribute("scroll", float.class, ScrollBar::scroll)
            .attribute("contentSize", float.class, ScrollBar::contentSize)
            .attribute("visibleSize", float.class, ScrollBar::visibleSize)
            .themeOption("barColor", Color.class, ScrollBar::barColor)
            .themeOption("barHoverColor", Color.class, ScrollBar::barHoverColor)
            .themeOption("barClickColor", Color.class, ScrollBar::barClickColor)
            .themeOption("barWidth", Float.class, ScrollBar::barWidth)
            .themeOption("barCornerRadius", Corners.class, ScrollBar::barCornerRadius)
            .themeOption("barOutlineWidth", Float.class, ScrollBar::barOutlineWidth)
            .themeOption("barOutlineColor", Color.class, ScrollBar::barOutlineColor)
            .themeOption("barType", ScrollBar.ScrollBarType.class, ScrollBar::barType)
            .themeOption("railClickJump", Boolean.class, ScrollBar::railClickJump)
            .themeOption("railColor", Color.class, ScrollBar::railColor)
            .themeOption("railOutlineColor", Color.class, ScrollBar::railOutlineColor)
            .themeOption("railOutlineWidth", Float.class, ScrollBar::railOutlineWidth)
            .themeOption("disabledBarColor", Color.class, ScrollBar::disabledBarColor)
            .themeOption("disabledBarOutlineColor", Color.class, ScrollBar::disabledBarOutlineColor)
            .themeOption("disabledRailColor", Color.class, ScrollBar::disabledRailColor)
            .themeOption("disabledRailOutlineColor", Color.class, ScrollBar::disabledRailOutlineColor)
            .build();

    @Override
    public ScrollBar parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
