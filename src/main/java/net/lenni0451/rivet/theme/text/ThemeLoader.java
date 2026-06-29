package net.lenni0451.rivet.theme.text;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.DynamicAnimationConfig;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.component.container.tabcontainer.TabAlignment;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.input.mouse.ClickOn;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeKey;
import net.lenni0451.rivet.theme.text.parser.*;

import javax.annotation.WillNotClose;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@UtilityClass
public class ThemeLoader {

    private static final Map<Class<?>, Parser<?>> parsers = new HashMap<>();

    static {
        registerParser(Color.class, new ColorParser());
        registerParser(Boolean.class, new BooleanParser());
        registerParser(Character.class, new CharacterParser());
        registerParser(Byte.class, new NumberParser<>(Byte::valueOf));
        registerParser(Short.class, new NumberParser<>(Short::valueOf));
        registerParser(Integer.class, new NumberParser<>(Integer::valueOf));
        registerParser(Long.class, new NumberParser<>(Long::valueOf));
        registerParser(Float.class, new NumberParser<>(Float::valueOf));
        registerParser(Double.class, new NumberParser<>(Double::valueOf));
        registerParser(String.class, new StringParser());
        registerParser(Padding.class, new PaddingParser());
        registerParser(Corners.class, new CornersParser());
        registerParser(AnimationConfig.class, new AnimationConfigParser());
        registerParser(DynamicAnimationConfig.class, new DynamicAnimationConfigParser());
        registerParser(ClickOn.class, new EnumParser<>(ClickOn.values()));
        registerParser(Slider.ThumbShape.class, new EnumParser<>(Slider.ThumbShape.values()));
        registerParser(ScrollContainer.ScrollBarType.class, new EnumParser<>(ScrollContainer.ScrollBarType.values()));
        registerParser(TabAlignment.class, new EnumParser<>(TabAlignment.values()));
        registerParser(Label.OverflowBehavior.class, new EnumParser<>(Label.OverflowBehavior.values()));
    }

    public static <T> void registerParser(final Class<T> type, final Parser<T> parser) {
        parsers.put(type, parser);
    }

    public static void load(@WillNotClose final InputStream is, final Theme.Values values, final ExceptionHandler errorHandler) throws IOException {
        Properties properties = new Properties();
        properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        properties.forEach((k, v) -> {
            String key = ((String) k).trim();
            String value = ((String) v).trim();
            try {
                parse(values, key, value);
            } catch (Throwable t) {
                errorHandler.tryHandle(key, value, t);
            }
        });
    }

    private static void parse(final Theme.Values values, final String key, final String value) {
        ThemeKey themeKey = Theme.registeredKeys().stream().filter(k -> k.name().equals(key)).findFirst().orElse(null);
        if (themeKey == null) {
            throw new IllegalArgumentException("Unknown key: " + key);
        }

        Parser<?> parser = parsers.get(themeKey.type());
        if (parser == null) {
            throw new UnsupportedOperationException("Unsupported theme value type: " + themeKey.type());
        }

        Object parsedValue = parser.parse(value);
        if (parsedValue == null) {
            throw new IllegalStateException("Unable to parse theme value: " + value);
        } else if (!themeKey.type().isAssignableFrom(parsedValue.getClass())) {
            throw new IllegalStateException("Parsed value type does not match expected type for key " + key + ": expected " + themeKey.type() + " but got " + parsedValue.getClass());
        }

        values.put(themeKey, parsedValue);
    }

    public static void save(@WillNotClose final OutputStream os, final Theme theme) throws IOException {
        Properties properties = new Properties();
        for (ThemeKey<?> key : Theme.registeredKeys()) {
            Object value = theme.get(key);
            Parser parser = parsers.get(key.type());
            if (parser != null) {
                properties.put(key.name(), parser.toString(value));
            }
        }
        properties.store(new OutputStreamWriter(os, StandardCharsets.UTF_8), "Automatically generated theme file");
    }


    @FunctionalInterface
    public interface ExceptionHandler {
        ExceptionHandler RETHROW = (k, v, t) -> {
            throw new IllegalStateException("Unable to parse option '" + k + "' with value '" + v + "'", t);
        };

        void handle(final String key, final String value, final Throwable cause) throws Throwable;

        @SneakyThrows
        default void tryHandle(final String key, final String value, final Throwable cause) {
            this.handle(key, value, cause);
        }
    }

}
