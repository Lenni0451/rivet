package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.rivet.animation.DynamicAnimationConfig;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class ScrollContainerParser implements XmlParser<ScrollContainer> {

    private final XmlParser<ScrollContainer> parser = ElementParserBuilder.createComponent(ctx -> new ScrollContainer(
                    ctx.requireChildOrReference("child", Component.class),
                    ctx.attributeOrDefault("horizontalScrolling", boolean.class, false),
                    ctx.attributeOrDefault("verticalScrolling", boolean.class, true)
            ))
            .attribute("autoScroll", boolean.class, ScrollContainer::autoScroll)
            .attribute("autoScrollThreshold", float.class, ScrollContainer::autoScrollThreshold)
            .attribute("presentInfiniteSize", boolean.class, ScrollContainer::presentInfiniteSize)
            .attribute("scrollX", float.class, (scrollContainer, value) -> scrollContainer.scrollX(value, true))
            .attribute("scrollY", float.class, (scrollContainer, value) -> scrollContainer.scrollY(value, true))
            .themeOption("scrollSpeed", Float.class, ScrollContainer::scrollSpeed)
            .themeOption("smoothScrolling", Boolean.class, ScrollContainer::smoothScrolling)
            .themeOption("animationConfig", DynamicAnimationConfig.class, ScrollContainer::animationConfig)
            .themeOption("nestedScrollTimeout", Long.class, ScrollContainer::nestedScrollTimeout)
            .build();

    @Override
    public ScrollContainer parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
