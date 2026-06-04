package net.lenni0451.rivet.theme.text;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeKey;
import net.lenni0451.rivet.theme.text.parser.ColorParser;
import net.lenni0451.rivet.theme.text.parser.EnumParser;
import net.lenni0451.rivet.theme.text.parser.PaddingParser;
import net.lenni0451.rivet.theme.text.parser.Parser;

import javax.annotation.WillClose;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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
        parsers.put(Button.ClickOn.class, new EnumParser<>(Button.ClickOn.values()));
        parsers.put(Slider.ThumbShape.class, new EnumParser<>(Slider.ThumbShape.values()));
        parsers.put(ScrollContainer.ScrollBarType.class, new EnumParser<>(ScrollContainer.ScrollBarType.values()));
    }

    public static void load(@WillClose final InputStream is, final Theme.Values values, final ExceptionHandler lineErrorHandler) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length != 2) {
                    lineErrorHandler.tryHandle(line, new IllegalStateException("Invalid line format, expected 'key=value'"));
                    continue;
                }
                try {
                    parse(values, parts[0], parts[1]);
                } catch (Throwable t) {
                    lineErrorHandler.tryHandle(line, t);
                }
            }
        }
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
