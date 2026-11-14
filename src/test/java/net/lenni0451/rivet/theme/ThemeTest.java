package net.lenni0451.rivet.theme;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void allDeclaredKeysAreRegistered() throws IllegalAccessException {
        Set<ThemeKey<?>> registered = new HashSet<>(Theme.registeredKeys());
        for (Class<?> declaredClass : Theme.class.getDeclaredClasses()) {
            for (Field field : declaredClass.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || !ThemeKey.class.isAssignableFrom(field.getType())) continue;
                ThemeKey<?> key = (ThemeKey<?>) field.get(null);
                assertTrue(registered.contains(key), "Theme key " + declaredClass.getSimpleName() + "." + field.getName()
                        + " is not registered.");
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
