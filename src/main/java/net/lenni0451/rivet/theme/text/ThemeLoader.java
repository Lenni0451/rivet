package net.lenni0451.rivet.theme.text;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.DynamicAnimationConfig;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.component.container.tabcontainer.TabAlignment;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeKey;
import net.lenni0451.rivet.theme.text.parser.*;

import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@UtilityClass
public class ThemeLoader {

    private static final Map<Class<?>, Parser<?>> parsers = new HashMap<>();

    static {
        parsers.put(Color.class, new ColorParser());
        parsers.put(Boolean.class, Boolean::valueOf);
        parsers.put(Character.class, s -> {
            if (s.length() != 1) {
                throw new IllegalArgumentException("Expected a single character but got: " + s);
            } else {
                return s.charAt(0);
            }
        });
        parsers.put(Integer.class, Integer::valueOf);
        parsers.put(Long.class, Long::valueOf);
        parsers.put(Float.class, Float::valueOf);
        parsers.put(String.class, s -> s);
        parsers.put(Padding.class, new PaddingParser());
        parsers.put(Corners.class, new CornersParser());
        parsers.put(AnimationConfig.class, new AnimationConfigParser());
        parsers.put(DynamicAnimationConfig.class, new DynamicAnimationConfigParser());
        parsers.put(Button.ClickOn.class, new EnumParser<>(Button.ClickOn.values()));
        parsers.put(Slider.ThumbShape.class, new EnumParser<>(Slider.ThumbShape.values()));
        parsers.put(ScrollContainer.ScrollBarType.class, new EnumParser<>(ScrollContainer.ScrollBarType.values()));
        parsers.put(TabAlignment.class, new EnumParser<>(TabAlignment.values()));
    }

    public static void load(@WillNotClose final InputStream is, final Theme.Values values, final ExceptionHandler lineErrorHandler) throws IOException {
        Properties properties = new Properties();
        properties.load(is);
        properties.forEach((k, v) -> {
            String key = ((String) k).trim();
            String value = ((String) v).trim();
            try {
                parse(values, key, value);
            } catch (Throwable t) {
                lineErrorHandler.tryHandle(key + "=" + value, t);
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


    @FunctionalInterface
    public interface ExceptionHandler {
        ExceptionHandler RETHROW = (l, t) -> {
            throw new IllegalStateException("Unable to parse line: " + l, t);
        };

        void handle(final String line, final Throwable cause) throws Throwable;

        @SneakyThrows
        default void tryHandle(final String line, final Throwable cause) {
            this.handle(line, cause);
        }
    }

}
