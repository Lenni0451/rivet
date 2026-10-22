package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.CollapsibleContainer;
import net.lenni0451.rivet.input.mouse.ClickOn;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import net.lenni0451.rivet.xml.parser.impl.NodeContext;
import org.w3c.dom.Node;

public class CollapsibleContainerParser implements XmlParser<CollapsibleContainer> {

    private final XmlParser<CollapsibleContainer> parser = ElementParserBuilder.createComponent(ctx -> {
                NodeContext.TwoSlotComponents<Component, Component> slots = ctx.resolveTwoSlots(
                        ctx.componentOrTextAttribute("header").orElse(null),
                        ctx.referenceAttribute("child", Component.class).orElse(null),
                        "CollapsibleContainer",
                        "header",
                        "child"
                );
                return new CollapsibleContainer(slots.first(), slots.second());
            })
            .attribute("collapsed", boolean.class, CollapsibleContainer::collapsed)
            .themeOption("arrowColor", Color.class, CollapsibleContainer::arrowColor)
            .themeOption("arrowDisabledColor", Color.class, CollapsibleContainer::arrowDisabledColor)
            .themeOption("arrowLineWidth", Float.class, CollapsibleContainer::arrowLineWidth)
            .themeOption("arrowSize", Float.class, CollapsibleContainer::arrowSize)
            .themeOption("collapseOn", ClickOn.class, CollapsibleContainer::collapseOn)
            .themeOption("collapseAnimationConfig", AnimationConfig.class, CollapsibleContainer::collapseAnimationConfig)
            .themeOption("arrowPosition", CollapsibleContainer.ArrowPosition.class, CollapsibleContainer::arrowPosition)
            .build();

    @Override
    public CollapsibleContainer parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
