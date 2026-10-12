package net.lenni0451.rivet.xml.parser.impl;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.LayoutOptions;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.ThemeOption;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.template.TemplateComponentParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ElementParserBuilder<T> {

    public static <T> ElementParserBuilder<T> create(final Supplier<T> factory) {
        return new ElementParserBuilder<>(ctx -> factory.get());
    }

    public static <T> ElementParserBuilder<T> create(final Function<NodeContext, T> factory) {
        return new ElementParserBuilder<>(factory);
    }

    public static <T extends Component> ElementParserBuilder<T> createComponent(final Supplier<T> factory) {
        return createComponent(ctx -> factory.get());
    }

    public static <T extends Component> ElementParserBuilder<T> createComponent(final Function<NodeContext, T> factory) {
        return applyComponentAttributes(create(factory));
    }

    public static <T extends Component> ElementParserBuilder<T> applyComponentAttributes(final ElementParserBuilder<T> builder) {
        return builder
                .attribute("id", String.class, (component, id) -> {}) // Handled in BuiltXmlParser
                .attribute("minSize", Size.class, Component::minSize)
                .attribute("maxSize", Size.class, Component::maxSize)
                .attribute("fixedSize", Size.class, Component::fixedSize)
                .child(LayoutOptions.class, Component::layoutOptions)
                .child(Component.Capabilities.class, Component::capabilities)
                .attribute("disabled", boolean.class, Component::disabled);
    }

    private static <T, V> BiFunction<T, V, T> toUpdater(final BiConsumer<T, V> setter) {
        return (t, v) -> {
            setter.accept(t, v);
            return t;
        };
    }


    private final Function<NodeContext, T> factory;
    private final List<AttributeParser<T, ?>> attributeParsers = new ArrayList<>();
    private final List<ChildParser<T, ?>> childParsers = new ArrayList<>();

    private ElementParserBuilder(final Function<NodeContext, T> factory) {
        this.factory = factory;
    }

    public <V> ElementParserBuilder<T> mapAttribute(final String name, final Class<V> type, final BiFunction<T, V, T> updater) {
        this.attributeParsers.add(new AttributeParser<>(name, type, updater, false, null));
        return this;
    }

    public <V> ElementParserBuilder<T> attribute(final String name, final Class<V> type, final BiConsumer<T, V> setter) {
        return this.mapAttribute(name, type, toUpdater(setter));
    }

    public <V> ElementParserBuilder<T> mapRequiredAttribute(final String name, final Class<V> type, final BiFunction<T, V, T> updater) {
        this.attributeParsers.add(new AttributeParser<>(name, type, updater, true, null));
        return this;
    }

    public <V> ElementParserBuilder<T> requiredAttribute(final String name, final Class<V> type, final BiConsumer<T, V> setter) {
        return this.mapRequiredAttribute(name, type, toUpdater(setter));
    }

    public <V> ElementParserBuilder<T> mapAttributeWithDefault(final String name, final Class<V> type, final V defaultValue, final BiFunction<T, V, T> updater) {
        this.attributeParsers.add(new AttributeParser<>(name, type, updater, false, defaultValue));
        return this;
    }

    public <V> ElementParserBuilder<T> attributeWithDefault(final String name, final Class<V> type, final V defaultValue, final BiConsumer<T, V> setter) {
        return this.mapAttributeWithDefault(name, type, defaultValue, toUpdater(setter));
    }

    public <V> ElementParserBuilder<T> mapReferenceAttribute(final String name, final Class<V> type, final BiFunction<T, V, T> updater) {
        this.attributeParsers.add(new AttributeParser<>(name, type, updater, false, null, true));
        return this;
    }

    public <V> ElementParserBuilder<T> referenceAttribute(final String name, final Class<V> type, final BiConsumer<T, V> setter) {
        return this.mapReferenceAttribute(name, type, toUpdater(setter));
    }

    public <V> ElementParserBuilder<T> mapRequiredReferenceAttribute(final String name, final Class<V> type, final BiFunction<T, V, T> updater) {
        this.attributeParsers.add(new AttributeParser<>(name, type, updater, true, null, true));
        return this;
    }

    public <V> ElementParserBuilder<T> requiredReferenceAttribute(final String name, final Class<V> type, final BiConsumer<T, V> setter) {
        return this.mapRequiredReferenceAttribute(name, type, toUpdater(setter));
    }

    public <V> ElementParserBuilder<T> themeOption(final String name, final Class<V> valueType, final Function<T, ThemeOption<V>> option) {
        return this.attribute(name, valueType, (component, value) -> option.apply(component).set(value));
    }

    public <V> ElementParserBuilder<T> mapChild(final Class<V> type, final BiFunction<T, V, T> updater) {
        this.childParsers.add(new ChildParser<>(type, updater));
        return this;
    }

    public <V> ElementParserBuilder<T> child(final Class<V> type, final BiConsumer<T, V> setter) {
        return this.mapChild(type, toUpdater(setter));
    }

    public XmlParser<T> build() {
        return new BuiltXmlParser<>(this.factory, new ArrayList<>(this.attributeParsers), new ArrayList<>(this.childParsers));
    }


    private record ChildParser<T, V>(Class<V> type, BiFunction<T, V, T> updater) {
        public boolean matches(final Class<?> childType) {
            return this.type.isAssignableFrom(childType);
        }

        public T parse(final T instance, final Object parsedChild) {
            return ((BiFunction<T, Object, T>) this.updater).apply(instance, parsedChild);
        }
    }

    private record AttributeParser<T, V>(String name, Class<V> type, BiFunction<T, V, T> updater, boolean required, V defaultValue, boolean referenceOnly) {
        public AttributeParser(final String name, final Class<V> type, final BiFunction<T, V, T> updater, final boolean required, final V defaultValue) {
            this(name, type, updater, required, defaultValue, false);
        }

        public T parse(final T instance, final NodeContext nodeContext) {
            Optional<V> val = this.referenceOnly
                    ? nodeContext.referenceAttribute(this.name, this.type)
                    : nodeContext.attribute(this.name, this.type);
            if (val.isPresent()) {
                return this.updater.apply(instance, val.get());
            }
            if (this.required) {
                throw new IllegalArgumentException("Missing required attribute '" + this.name + "'");
            }
            if (this.defaultValue != null) {
                return this.updater.apply(instance, this.defaultValue);
            }
            return instance;
        }
    }

    private record BuiltXmlParser<T>(Function<NodeContext, T> factory, List<AttributeParser<T, ?>> attributeParsers, List<ChildParser<T, ?>> childParsers) implements XmlParser<T> {
        @Override
        public T parse(final XmlParserContext context, final Node node) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                throw new IllegalArgumentException("Expected an element node");
            }
            Element element = (Element) node;

            NodeContext nodeContext = NodeContext.of(context, element);
            T instance = this.factory.apply(nodeContext);

            for (AttributeParser<T, ?> parser : this.attributeParsers) {
                instance = parser.parse(instance, nodeContext);
            }

            if (!this.childParsers.isEmpty()) {
                for (Element child : nodeContext.childElements()) {
                    String tagName = child.getNodeName();
                    if ("Slot".equals(tagName)) {
                        XmlParser<?> tagParser = context.registry().getParserByTag(element.getNodeName());
                        if (tagParser instanceof TemplateComponentParser) {
                            continue;
                        }
                        throw new IllegalArgumentException("<Slot> can only be used inside template definitions");
                    }
                    Class<?> type = context.registry().getTypeByTag(tagName);
                    if (type != null) {
                        for (ChildParser<T, ?> parser : this.childParsers) {
                            if (parser.matches(type)) {
                                Object parsedChild = context.registry().parseNode(child, context);
                                instance = parser.parse(instance, parsedChild);
                                break;
                            }
                        }
                    }
                }
            }

            if (instance instanceof Component component && element.hasAttribute("id")) {
                String id = context.resolveTemplateParameter(element.getAttribute("id"));
                if (id.isEmpty()) {
                    throw new IllegalArgumentException("Component ID cannot be empty");
                }
                context.registerComponent(id, component);
            }

            return instance;
        }
    }

}
