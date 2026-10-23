package net.lenni0451.rivet.xml.parser.impl;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.LayoutOptions;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeKey;
import net.lenni0451.rivet.theme.ThemeOption;
import net.lenni0451.rivet.xml.XmlConstants;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.template.TemplateComponentParser;
import net.lenni0451.rivet.xml.utils.XmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.*;

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
        return applyComponentAttributes(builder, attributeName -> true);
    }

    public static <T extends Component> ElementParserBuilder<T> applyComponentAttributes(final ElementParserBuilder<T> builder, final Predicate<String> attributeFilter) {
        if (attributeFilter.test(XmlConstants.ATTRIBUTE_ID)) {
            builder.attribute(XmlConstants.ATTRIBUTE_ID, String.class, (component, id) -> {}); // Handled in BuiltXmlParser
        }
        if (attributeFilter.test(XmlConstants.ATTRIBUTE_MIN_SIZE)) {
            builder.attribute(XmlConstants.ATTRIBUTE_MIN_SIZE, Size.class, Component::minSize);
        }
        if (attributeFilter.test(XmlConstants.ATTRIBUTE_MAX_SIZE)) {
            builder.attribute(XmlConstants.ATTRIBUTE_MAX_SIZE, Size.class, Component::maxSize);
        }
        if (attributeFilter.test(XmlConstants.ATTRIBUTE_FIXED_SIZE)) {
            builder.attribute(XmlConstants.ATTRIBUTE_FIXED_SIZE, Size.class, Component::fixedSize);
        }
        builder.child(LayoutOptions.class, Component::layoutOptions);
        builder.child(Component.Capabilities.class, Component::capabilities);
        if (attributeFilter.test(XmlConstants.ATTRIBUTE_DISABLED)) {
            builder.attribute(XmlConstants.ATTRIBUTE_DISABLED, boolean.class, Component::disabled);
        }
        return builder;
    }

    private static <T, V> BiFunction<T, V, T> toUpdater(final BiConsumer<T, V> setter) {
        return (instance, value) -> {
            setter.accept(instance, value);
            return instance;
        };
    }


    private final Function<NodeContext, T> factory;
    private final List<AttributeParser<T>> attributeParsers = new ArrayList<>();
    private final List<ChildParser<T, ?>> childParsers = new ArrayList<>();

    private ElementParserBuilder(final Function<NodeContext, T> factory) {
        this.factory = factory;
    }

    public <V> ElementParserBuilder<T> mapAttribute(final String name, final Class<V> type, final BiFunction<T, V, T> updater) {
        this.attributeParsers.add(new StandardAttributeParser<>(name, type, updater, false, null));
        return this;
    }

    public <V> ElementParserBuilder<T> attribute(final String name, final Class<V> type, final BiConsumer<T, V> setter) {
        return this.mapAttribute(name, type, toUpdater(setter));
    }

    public <V> ElementParserBuilder<T> mapRequiredAttribute(final String name, final Class<V> type, final BiFunction<T, V, T> updater) {
        this.attributeParsers.add(new StandardAttributeParser<>(name, type, updater, true, null));
        return this;
    }

    public <V> ElementParserBuilder<T> requiredAttribute(final String name, final Class<V> type, final BiConsumer<T, V> setter) {
        return this.mapRequiredAttribute(name, type, toUpdater(setter));
    }

    public <V> ElementParserBuilder<T> mapAttributeWithDefault(final String name, final Class<V> type, final V defaultValue, final BiFunction<T, V, T> updater) {
        this.attributeParsers.add(new StandardAttributeParser<>(name, type, updater, false, defaultValue));
        return this;
    }

    public <V> ElementParserBuilder<T> attributeWithDefault(final String name, final Class<V> type, final V defaultValue, final BiConsumer<T, V> setter) {
        return this.mapAttributeWithDefault(name, type, defaultValue, toUpdater(setter));
    }

    public <V> ElementParserBuilder<T> mapReferenceAttribute(final String name, final Class<V> type, final BiFunction<T, V, T> updater) {
        this.attributeParsers.add(new StandardAttributeParser<>(name, type, updater, false, null, true));
        return this;
    }

    public <V> ElementParserBuilder<T> referenceAttribute(final String name, final Class<V> type, final BiConsumer<T, V> setter) {
        return this.mapReferenceAttribute(name, type, toUpdater(setter));
    }

    public <V> ElementParserBuilder<T> mapRequiredReferenceAttribute(final String name, final Class<V> type, final BiFunction<T, V, T> updater) {
        this.attributeParsers.add(new StandardAttributeParser<>(name, type, updater, true, null, true));
        return this;
    }

    public <V> ElementParserBuilder<T> requiredReferenceAttribute(final String name, final Class<V> type, final BiConsumer<T, V> setter) {
        return this.mapRequiredReferenceAttribute(name, type, toUpdater(setter));
    }

    public <V> ElementParserBuilder<T> themeOption(final String name, final Class<V> valueType, final Function<T, ThemeOption<V>> option) {
        this.attributeParsers.add(new ThemeOptionAttributeParser<>(name, valueType, option));
        return this;
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

    private interface AttributeParser<T> {
        T parse(final T instance, final NodeContext nodeContext);
    }

    private record StandardAttributeParser<T, V>(String name, Class<V> type, BiFunction<T, V, T> updater, boolean required, V defaultValue, boolean referenceOnly) implements AttributeParser<T> {
        public StandardAttributeParser(final String name, final Class<V> type, final BiFunction<T, V, T> updater, final boolean required, final V defaultValue) {
            this(name, type, updater, required, defaultValue, false);
        }

        @Override
        public T parse(final T instance, final NodeContext nodeContext) {
            Optional<V> value = this.referenceOnly
                    ? nodeContext.referenceAttribute(this.name, this.type)
                    : nodeContext.attribute(this.name, this.type);
            if (value.isPresent()) {
                return this.updater.apply(instance, value.get());
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

    private record ThemeOptionAttributeParser<T, V>(String name, Class<V> valueType, Function<T, ThemeOption<V>> optionGetter) implements AttributeParser<T> {
        @Override
        public T parse(final T instance, final NodeContext nodeContext) {
            if (!nodeContext.element().hasAttribute(this.name)) {
                return instance;
            }
            String rawValue = nodeContext.context().resolveTemplateParameter(nodeContext.element().getAttribute(this.name));
            ThemeOption<V> option = this.optionGetter.apply(instance);
            Class<V> boxedValueType = XmlUtils.box(this.valueType);

            if (XmlUtils.isThemeReference(rawValue)) {
                String key = XmlUtils.extractThemeReferenceKey(rawValue);
                ThemeKey<?> themeKey = Theme.requireKey(key);
                if (!boxedValueType.isAssignableFrom(XmlUtils.box(themeKey.type()))) {
                    throw new IllegalArgumentException("Theme key '" + key + "' of type " + themeKey.type().getName() + " is not compatible with attribute '" + this.name + "' of type " + this.valueType.getName());
                }
                option.set((ThemeKey<V>) themeKey);
            } else if (XmlUtils.isVariableReference(rawValue)) {
                String variableName = XmlUtils.extractVariableReferenceName(rawValue);
                Optional<V> variable = nodeContext.context().getVariable(variableName, this.valueType);
                if (variable.isPresent()) {
                    option.set(variable.get());
                } else {
                    Optional<String> constant = nodeContext.context().getConstant(variableName);
                    if (constant.isPresent()) {
                        option.set(nodeContext.context().stringParserRegistry().parse(this.valueType, constant.get()));
                    } else {
                        throw new IllegalArgumentException("Variable '" + variableName + "' not found or not of type " + this.valueType.getName());
                    }
                }
            } else {
                V parsedValue = nodeContext.context().stringParserRegistry().parse(this.valueType, rawValue);
                option.set(parsedValue);
            }
            return instance;
        }
    }

    private record BuiltXmlParser<T>(Function<NodeContext, T> factory, List<AttributeParser<T>> attributeParsers, List<ChildParser<T, ?>> childParsers) implements XmlParser<T> {
        @Override
        public T parse(final XmlParserContext context, final Node node) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                throw new IllegalArgumentException("Expected an element node");
            }
            Element element = (Element) node;

            NodeContext nodeContext = NodeContext.of(context, element);
            T instance = this.factory.apply(nodeContext);

            for (AttributeParser<T> parser : this.attributeParsers) {
                instance = parser.parse(instance, nodeContext);
            }

            XmlParser<?> elementTagParser = context.registry().getParserByTag(element.getNodeName());
            for (Element child : nodeContext.childElements()) {
                String tagName = child.getNodeName();
                if (XmlConstants.TAG_SLOT.equals(tagName)) {
                    if (elementTagParser instanceof TemplateComponentParser) {
                        continue;
                    }
                    throw new IllegalArgumentException("<" + XmlConstants.TAG_SLOT + "> can only be used inside template definitions");
                }
                Class<?> type = context.registry().getTypeByTag(tagName);
                if (type == null) {
                    throw new IllegalArgumentException("Unknown tag: " + tagName);
                }
                for (ChildParser<T, ?> parser : this.childParsers) {
                    if (parser.matches(type)) {
                        Object parsedChild = context.registry().parseNode(child, context);
                        instance = parser.parse(instance, parsedChild);
                        break;
                    }
                }
            }

            boolean isTemplateIdParameter = elementTagParser instanceof TemplateComponentParser templateParser
                    && (templateParser.parameters().containsKey(XmlConstants.ATTRIBUTE_ID) || templateParser.themeOptions().containsKey(XmlConstants.ATTRIBUTE_ID));
            if (instance instanceof Component component && element.hasAttribute(XmlConstants.ATTRIBUTE_ID) && !isTemplateIdParameter) {
                String id = context.resolveTemplateParameter(element.getAttribute(XmlConstants.ATTRIBUTE_ID));
                if (id.isEmpty()) {
                    throw new IllegalArgumentException("Component ID cannot be empty");
                }
                context.registerComponent(id, component);
            }

            return instance;
        }
    }

}
