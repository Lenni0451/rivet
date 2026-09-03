package net.lenni0451.rivet.theme.loader;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.lenni0451.rivet.parser.Parser;
import net.lenni0451.rivet.parser.ParserRegistry;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeKey;

import javax.annotation.WillNotClose;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@UtilityClass
public class ThemeLoader {

    private static final ParserRegistry PARSERS = ParserRegistry.standard();

    public static <T> void registerParser(final Class<T> type, final Parser<T> parser) {
        PARSERS.register(type, parser);
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

        values.put(themeKey, PARSERS.parse(themeKey.type(), value));
    }

    public static void save(@WillNotClose final OutputStream os, final Theme theme) throws IOException {
        Properties properties = new Properties();
        for (ThemeKey key : Theme.registeredKeys()) {
            if (PARSERS.supports(key.type())) {
                String formatted = PARSERS.format(key.type(), theme.get(key));
                properties.put(key.name(), formatted);
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
