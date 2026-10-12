package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.tabcontainer.TabAlignment;
import net.lenni0451.rivet.component.container.tabcontainer.TabContainer;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

public class TabContainerParser implements XmlParser<TabContainer> {

    private final XmlParser<TabContainer> parser = ElementParserBuilder.createComponent(TabContainer::new)
            .child(TabParser.TabDefinition.class, (container, tab) -> container.addTab(tab.header(), tab.content()))
            .referenceAttribute("leftComponent", Component.class, TabContainer::addLeftComponent)
            .referenceAttribute("rightComponent", Component.class, TabContainer::addRightComponent)
            .themeOption("headerBackgroundColor", Color.class, TabContainer::headerBackgroundColor)
            .themeOption("separatorColor", Color.class, TabContainer::separatorColor)
            .themeOption("separatorThickness", Float.class, TabContainer::separatorThickness)
            .themeOption("tabAlignment", TabAlignment.class, TabContainer::tabAlignment)
            .themeOption("tabSameSize", Boolean.class, TabContainer::tabSameSize)
            .themeOption("tabVerticalGap", Float.class, TabContainer::tabVerticalGap)
            .themeOption("tabGap", Float.class, TabContainer::tabGap)
            .build();

    @Override
    public TabContainer parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
