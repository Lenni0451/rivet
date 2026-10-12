package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.CollapsibleContainer;
import net.lenni0451.rivet.input.mouse.ClickOn;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

import java.util.List;

public class CollapsibleContainerParser implements XmlParser<CollapsibleContainer> {

    private final XmlParser<CollapsibleContainer> parser = ElementParserBuilder.createComponent(ctx -> {
                Component text = ctx.componentOrTextAttribute("header").orElse(null);
                Component child = ctx.referenceAttribute("child", Component.class).orElse(null);
                List<Component> children = ctx.children(Component.class);
                if (children.isEmpty()) {
                    if (text == null || child == null) {
                        throw new IllegalArgumentException("CollapsibleContainer must have a header and child component");
                    }
                } else if (children.size() == 1) {
                    if (text == null) text = children.get(0);
                    else if (child == null) child = children.get(0);
                    else throw new IllegalArgumentException("CollapsibleContainer has too many children, only header and child are allowed");

                    if (text == null || child == null) {
                        throw new IllegalArgumentException("CollapsibleContainer must have a header and child component");
                    }
                } else if (children.size() == 2) {
                    if (text != null || child != null) {
                        throw new IllegalArgumentException("CollapsibleContainer has too many children, only header and child are allowed");
                    }
                    text = children.get(0);
                    child = children.get(1);
                } else {
                    throw new IllegalArgumentException("CollapsibleContainer has too many children, only header and child are allowed");
                }
                return new CollapsibleContainer(text, child);
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
