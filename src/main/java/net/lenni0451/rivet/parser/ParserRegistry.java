package net.lenni0451.rivet.parser;

import net.lenni0451.commons.animation.easing.EasingFunction;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.DynamicAnimationConfig;
import net.lenni0451.rivet.math.*;
import net.lenni0451.rivet.parser.impl.*;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ParserRegistry {

    public static ParserRegistry standard() {
        ParserRegistry registry = new ParserRegistry();
        registry.register(Boolean.class, new BooleanParser());
        registry.register(Character.class, new CharacterParser());
        registry.register(Byte.class, new NumberParser<>(Byte::valueOf));
        registry.register(Short.class, new NumberParser<>(Short::valueOf));
        registry.register(Integer.class, new NumberParser<>(Integer::valueOf));
        registry.register(Long.class, new NumberParser<>(Long::valueOf));
        registry.register(Float.class, new NumberParser<>(Float::valueOf));
        registry.register(Double.class, new NumberParser<>(Double::valueOf));
        registry.register(String.class, new StringParser());
        registry.register(Color.class, new ColorParser());
        registry.register(Padding.class, new PaddingParser());
        registry.register(Corners.class, new CornersParser());
        registry.register(Size.class, new SizeParser());
        registry.register(Point.class, new PointParser());
        registry.register(Rectangle.class, new RectangleParser());
        registry.register(EasingFunction.class, new EasingFunctionParser());
        registry.register(AnimationConfig.class, new AnimationConfigParser());
        registry.register(DynamicAnimationConfig.class, new DynamicAnimationConfigParser());
        return registry;
    }


    private final Map<Class<?>, Parser<?>> parsers = new LinkedHashMap<>();

    public <T> ParserRegistry register(final Class<T> type, final Parser<? extends T> parser) {
        this.parsers.put(this.box(type), parser);
        return this;
    }

    public boolean supports(final Class<?> type) {
        return type.isEnum() || this.parsers.containsKey(this.box(type));
    }

    public <T> T parse(final Class<T> type, final String value) {
        Class<T> boxedType = this.box(type);
        Parser<?> parser = this.findParser(boxedType);
        Object parsed;
        try {
            parsed = parser.parse(value);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Unable to parse '" + value + "' as " + boxedType.getTypeName(), e);
        }
        if (parsed == null) {
            throw new IllegalArgumentException("Unable to parse '" + value + "' as " + boxedType.getTypeName());
        }
        if (!boxedType.isInstance(parsed)) {
            throw new IllegalStateException("Parser for " + boxedType.getTypeName() + " returned " + parsed.getClass().getTypeName());
        }
        return boxedType.cast(parsed);
    }

    public <T> String format(final Class<T> type, final T value) {
        Class<T> boxedType = this.box(type);
        if (!boxedType.isInstance(value)) {
            throw new IllegalArgumentException("Expected " + boxedType.getTypeName() + " but got " + value.getClass().getTypeName());
        }
        Parser<T> parser = (Parser<T>) this.findParser(boxedType);
        String formatted;
        try {
            formatted = parser.toString(value);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Unable to format value as " + boxedType.getTypeName(), e);
        }
        if (formatted == null) {
            throw new IllegalStateException("Parser for " + boxedType.getTypeName() + " could not format the value");
        }
        return formatted;
    }

    public ParserRegistry copy() {
        ParserRegistry copy = new ParserRegistry();
        copy.parsers.putAll(this.parsers);
        return copy;
    }

    private Parser<?> findParser(final Class<?> type) {
        if (type.isEnum()) {
            return new EnumParser<>(type.asSubclass(Enum.class));
        }
        Parser<?> parser = this.parsers.get(type);
        if (parser == null) {
            throw new UnsupportedOperationException("Unsupported value type: " + type.getTypeName());
        }
        return parser;
    }

    private <T> Class<T> box(final Class<T> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return (Class<T>) Boolean.class;
        if (type == char.class) return (Class<T>) Character.class;
        if (type == byte.class) return (Class<T>) Byte.class;
        if (type == short.class) return (Class<T>) Short.class;
        if (type == int.class) return (Class<T>) Integer.class;
        if (type == long.class) return (Class<T>) Long.class;
        if (type == float.class) return (Class<T>) Float.class;
        if (type == double.class) return (Class<T>) Double.class;
        return type;
    }

}
