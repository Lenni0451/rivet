package net.lenni0451.rivet.theme;

import net.lenni0451.commons.color.Color;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class Theme {

    // Button
    public static final ThemeKey<Integer> BUTTON_CORNER_RADIUS = new ThemeKey<>("button.corner_radius", Integer.class);
    public static final ThemeKey<Integer> BUTTON_OUTLINE_WIDTH = new ThemeKey<>("button.outline_width", Integer.class);
    public static final ThemeKey<Color> BUTTON_INACTIVE_COLOR = new ThemeKey<>("button.inactive_color", Color.class);
    public static final ThemeKey<Color> BUTTON_INACTIVE_OUTLINE_COLOR = new ThemeKey<>("button.inactive_outline_color", Color.class);
    public static final ThemeKey<Color> BUTTON_ACTIVE_COLOR = new ThemeKey<>("button.active_color", Color.class);
    public static final ThemeKey<Color> BUTTON_ACTIVE_OUTLINE_COLOR = new ThemeKey<>("button.active_outline_color", Color.class);


    @Nullable
    private final Theme parent;
    private final Map<ThemeKey<?>, Object> values = new HashMap<>();

    public Theme() {
        this(null);
    }

    public Theme(@Nullable final Theme parent) {
        this.parent = parent;
        this.register(this.values::put);
    }

    protected abstract void register(final Registrar registrar);

    public <T> T get(final ThemeKey<T> key) {
        Object value = this.values.get(key);
        if (value == null) {
            if (this.parent != null) {
                return this.parent.get(key);
            }
            throw new IllegalStateException("No value for key " + key);
        }
        return (T) value;
    }


    @FunctionalInterface
    public interface Registrar {
        <T> void accept(final ThemeKey<T> key, final T value);
    }

}
