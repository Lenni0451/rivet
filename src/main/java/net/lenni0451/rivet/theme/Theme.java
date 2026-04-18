package net.lenni0451.rivet.theme;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.component.impl.Slider;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class Theme {

    // General
    public static final ThemeKey<Color> TEXT_COLOR = new ThemeKey<>("general.text_color", Color.class);

    // Button
    public static final ThemeKey<Integer> BUTTON_CORNER_RADIUS = new ThemeKey<>("button.corner_radius", Integer.class);
    public static final ThemeKey<Integer> BUTTON_OUTLINE_WIDTH = new ThemeKey<>("button.outline_width", Integer.class);
    public static final ThemeKey<Color> BUTTON_INACTIVE_COLOR = new ThemeKey<>("button.inactive_color", Color.class);
    public static final ThemeKey<Color> BUTTON_INACTIVE_OUTLINE_COLOR = new ThemeKey<>("button.inactive_outline_color", Color.class);
    public static final ThemeKey<Color> BUTTON_ACTIVE_COLOR = new ThemeKey<>("button.active_color", Color.class);
    public static final ThemeKey<Color> BUTTON_ACTIVE_OUTLINE_COLOR = new ThemeKey<>("button.active_outline_color", Color.class);
    public static final ThemeKey<Color> BUTTON_CLICK_COLOR = new ThemeKey<>("button.click_color", Color.class);
    public static final ThemeKey<Color> BUTTON_CLICK_OUTLINE_COLOR = new ThemeKey<>("button.click_outline_color", Color.class);
    public static final ThemeKey<Integer> BUTTON_ANIMATION_DURATION = new ThemeKey<>("button.animation_duration", Integer.class);

    // Slider
    public static final ThemeKey<Color> SLIDER_BAR_COLOR = new ThemeKey<>("slider.bar_color", Color.class);
    public static final ThemeKey<Color> SLIDER_THUMB_COLOR = new ThemeKey<>("slider.thumb_color", Color.class);
    public static final ThemeKey<Color> SLIDER_THUMB_CLICK_COLOR = new ThemeKey<>("slider.thumb_click_color", Color.class);
    public static final ThemeKey<Color> SLIDER_TICK_COLOR = new ThemeKey<>("slider.tick_color", Color.class);
    public static final ThemeKey<Float> SLIDER_BAR_HEIGHT = new ThemeKey<>("slider.bar_height", Float.class);
    public static final ThemeKey<Float> SLIDER_THUMB_WIDTH = new ThemeKey<>("slider.thumb_width", Float.class);
    public static final ThemeKey<Float> SLIDER_THUMB_HEIGHT = new ThemeKey<>("slider.thumb_height", Float.class);
    public static final ThemeKey<Float> SLIDER_BAR_CORNER_RADIUS = new ThemeKey<>("slider.bar_corner_radius", Float.class);
    public static final ThemeKey<Float> SLIDER_THUMB_CORNER_RADIUS = new ThemeKey<>("slider.thumb_corner_radius", Float.class);
    public static final ThemeKey<Boolean> SLIDER_THUMB_ENCASED = new ThemeKey<>("slider.thumb_encased", Boolean.class);
    public static final ThemeKey<Slider.ThumbShape> SLIDER_THUMB_SHAPE = new ThemeKey<>("slider.thumb_shape", Slider.ThumbShape.class);

    // ScrollContainer
    public static final ThemeKey<Color> SCROLL_BAR_COLOR = new ThemeKey<>("scroll.bar_color", Color.class);
    public static final ThemeKey<Color> SCROLL_BAR_HOVER_COLOR = new ThemeKey<>("scroll.bar_hover_color", Color.class);
    public static final ThemeKey<Color> SCROLL_BAR_CLICK_COLOR = new ThemeKey<>("scroll.bar_click_color", Color.class);
    public static final ThemeKey<Float> SCROLL_BAR_WIDTH = new ThemeKey<>("scroll.bar_width", Float.class);
    public static final ThemeKey<Float> SCROLL_BAR_CORNER_RADIUS = new ThemeKey<>("scroll.bar_corner_radius", Float.class);
    public static final ThemeKey<Float> SCROLL_BAR_OUTLINE_WIDTH = new ThemeKey<>("scroll.bar_outline_width", Float.class);
    public static final ThemeKey<Color> SCROLL_BAR_OUTLINE_COLOR = new ThemeKey<>("scroll.bar_outline_color", Color.class);
    public static final ThemeKey<Float> SCROLL_SPEED = new ThemeKey<>("scroll.speed", Float.class);
    public static final ThemeKey<Boolean> SCROLL_SMOOTH = new ThemeKey<>("scroll.smooth", Boolean.class);
    public static final ThemeKey<Integer> SCROLL_ANIMATION_DURATION = new ThemeKey<>("scroll.animation_duration", Integer.class);


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
