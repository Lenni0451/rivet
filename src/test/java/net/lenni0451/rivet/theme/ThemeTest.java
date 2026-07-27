package net.lenni0451.rivet.theme;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThemeTest {

    @Test
    void verifyNames() throws IllegalAccessException {
        for (Class<?> declaredClass : Theme.class.getDeclaredClasses()) {
            Map<String, ThemeKey<?>> themeKeys = this.getThemeKeys(declaredClass);
            for (Map.Entry<String, ThemeKey<?>> entry : themeKeys.entrySet()) {
                String expectedName = this.toSnakeCase(declaredClass.getSimpleName()) + "." + entry.getKey().toLowerCase(Locale.ROOT);
                String actualName = entry.getValue().name();
                if (!expectedName.equals(actualName)) {
                    assertEquals(expectedName, actualName);
                }
            }
        }
    }

    private Map<String, ThemeKey<?>> getThemeKeys(final Class<?> clazz) throws IllegalAccessException {
        Map<String, ThemeKey<?>> themeKeys = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(ThemeKey.class)) {
                ThemeKey option = (ThemeKey) field.get(null);
                themeKeys.put(field.getName(), option);
            }
        }
        return themeKeys;
    }

    private String toSnakeCase(final String camelCase) {
        StringBuilder out = new StringBuilder();
        for (char c : camelCase.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!out.isEmpty()) {
                    out.append('_');
                }
                out.append(Character.toLowerCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

}
