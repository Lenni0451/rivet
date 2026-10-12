package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.container.DynamicListContainer;
import net.lenni0451.rivet.component.container.ReorderableContainer;
import net.lenni0451.rivet.dragdrop.DropMarkerStrategy;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DynamicListContainerParser implements XmlParser<DynamicListContainer<?>> {

    private final XmlParser<DynamicListContainer<?>> parser = ElementParserBuilder.<DynamicListContainer<?>>createComponent(ctx -> {
                Layout layout = ctx.requireChildOrReference("layout", Layout.class);
                Optional<List> list = ctx.referenceAttribute("list", List.class);
                Optional<Supplier> listSupplier = ctx.referenceAttribute("listSupplier", Supplier.class);
                Optional<DropMarkerStrategy> dropMarkerStrategy = ctx.referenceAttribute("dropMarkerStrategy", DropMarkerStrategy.class);
                Optional<Predicate> dropFilter = ctx.referenceAttribute("dropFilter", Predicate.class);
                Function componentFactory = ctx.requireReferenceAttribute("componentFactory", Function.class);
                if (list.isPresent() == listSupplier.isPresent()) {
                    throw new IllegalArgumentException("Either 'list' or 'listSupplier' must be provided, but not both");
                }
                if (dropMarkerStrategy.isPresent() != dropFilter.isPresent()) {
                    throw new IllegalArgumentException("Either 'dropMarkerStrategy' and 'dropFilter' must be provided, or neither");
                }
                if (list.isPresent()) {
                    if (dropMarkerStrategy.isPresent()) {
                        return new DynamicListContainer<>(layout, dropMarkerStrategy.get(), dropFilter.get(), list.get(), componentFactory);
                    } else {
                        return new DynamicListContainer<>(layout, list.get(), componentFactory);
                    }
                } else {
                    if (dropMarkerStrategy.isPresent()) {
                        return new DynamicListContainer<>(layout, dropMarkerStrategy.get(), dropFilter.get(), listSupplier.get(), componentFactory);
                    } else {
                        return new DynamicListContainer<>(layout, listSupplier.get(), componentFactory);
                    }
                }
            })
            .attribute("markerColor", Color.class, ReorderableContainer::markerColor)
            .build();

    @Override
    public DynamicListContainer<?> parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
