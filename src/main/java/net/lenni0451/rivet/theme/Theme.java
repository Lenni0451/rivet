package net.lenni0451.rivet.theme;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.impl.Slider;

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
    public static final ThemeKey<Integer> BUTTON_ANIMATION_DURATION = new ThemeKey<>("button.animation_duration", Integer.class);

    // Slider
    public static final ThemeKey<Color> SLIDER_BAR_COLOR = new ThemeKey<>("slider.bar_color", Color.class);
    public static final ThemeKey<Color> SLIDER_KNOB_COLOR = new ThemeKey<>("slider.knob_color", Color.class);
    public static final ThemeKey<Color> SLIDER_TICK_COLOR = new ThemeKey<>("slider.tick_color", Color.class);
    public static final ThemeKey<Integer> SLIDER_BAR_HEIGHT = new ThemeKey<>("slider.bar_height", Integer.class);
    public static final ThemeKey<Integer> SLIDER_KNOB_RADIUS = new ThemeKey<>("slider.knob_radius", Integer.class);
    public static final ThemeKey<Integer> SLIDER_BAR_CORNER_RADIUS = new ThemeKey<>("slider.bar_corner_radius", Integer.class);
    public static final ThemeKey<Integer> SLIDER_KNOB_CORNER_RADIUS = new ThemeKey<>("slider.knob_corner_radius", Integer.class);
    public static final ThemeKey<Boolean> SLIDER_KNOB_ENCASED = new ThemeKey<>("slider.knob_encased", Boolean.class);
    public static final ThemeKey<Slider.KnobShape> SLIDER_KNOB_SHAPE = new ThemeKey<>("slider.knob_shape", Slider.KnobShape.class);


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
        T value = this.getOrDefault(key, null);
        if (value == null) throw new IllegalStateException("No value for key " + key);
        return value;
    }

    public <T> T getOrDefault(final ThemeKey<T> key, @Nullable final T defaultValue) {
        Object value = this.values.get(key);
        if (value == null) {
            if (this.parent != null) {
                return this.parent.getOrDefault(key, defaultValue);
            }
            return defaultValue;
        }
        return (T) value;
    }


    @FunctionalInterface
    public interface Registrar {
        <T> void accept(final ThemeKey<T> key, final T value);
    }

}
