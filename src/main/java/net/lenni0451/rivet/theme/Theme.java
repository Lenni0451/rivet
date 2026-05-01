package net.lenni0451.rivet.theme;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.base.ScrollContainer;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.math.Padding;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Theme {

    private static final List<ThemeKey<?>> ALL_KEYS = new ArrayList<>();

    // General
    public static final ThemeKey<Color> TEXT_COLOR = register("general.text_color", Color.class);

    // Button
    public static final ThemeKey<Integer> BUTTON_CORNER_RADIUS = register("button.corner_radius", Integer.class);
    public static final ThemeKey<Integer> BUTTON_OUTLINE_WIDTH = register("button.outline_width", Integer.class);
    public static final ThemeKey<Color> BUTTON_INACTIVE_COLOR = register("button.inactive_color", Color.class);
    public static final ThemeKey<Color> BUTTON_INACTIVE_OUTLINE_COLOR = register("button.inactive_outline_color", Color.class);
    public static final ThemeKey<Color> BUTTON_ACTIVE_COLOR = register("button.active_color", Color.class);
    public static final ThemeKey<Color> BUTTON_ACTIVE_OUTLINE_COLOR = register("button.active_outline_color", Color.class);
    public static final ThemeKey<Color> BUTTON_CLICK_COLOR = register("button.click_color", Color.class);
    public static final ThemeKey<Color> BUTTON_CLICK_OUTLINE_COLOR = register("button.click_outline_color", Color.class);
    public static final ThemeKey<Integer> BUTTON_ANIMATION_DURATION = register("button.animation_duration", Integer.class);

    // Slider
    public static final ThemeKey<Color> SLIDER_BAR_COLOR = register("slider.bar_color", Color.class);
    public static final ThemeKey<Color> SLIDER_THUMB_COLOR = register("slider.thumb_color", Color.class);
    public static final ThemeKey<Color> SLIDER_THUMB_CLICK_COLOR = register("slider.thumb_click_color", Color.class);
    public static final ThemeKey<Color> SLIDER_TICK_COLOR = register("slider.tick_color", Color.class);
    public static final ThemeKey<Float> SLIDER_BAR_HEIGHT = register("slider.bar_height", Float.class);
    public static final ThemeKey<Float> SLIDER_THUMB_WIDTH = register("slider.thumb_width", Float.class);
    public static final ThemeKey<Float> SLIDER_THUMB_HEIGHT = register("slider.thumb_height", Float.class);
    public static final ThemeKey<Float> SLIDER_BAR_CORNER_RADIUS = register("slider.bar_corner_radius", Float.class);
    public static final ThemeKey<Float> SLIDER_THUMB_CORNER_RADIUS = register("slider.thumb_corner_radius", Float.class);
    public static final ThemeKey<Boolean> SLIDER_THUMB_ENCASED = register("slider.thumb_encased", Boolean.class);
    public static final ThemeKey<Slider.ThumbShape> SLIDER_THUMB_SHAPE = register("slider.thumb_shape", Slider.ThumbShape.class);
    public static final ThemeKey<Color> SLIDER_TOOLTIP_BACKGROUND_COLOR = register("slider.tooltip_background_color", Color.class);
    public static final ThemeKey<Color> SLIDER_TOOLTIP_TEXT_COLOR = register("slider.tooltip_text_color", Color.class);
    public static final ThemeKey<Float> SLIDER_TOOLTIP_CORNER_RADIUS = register("slider.tooltip_corner_radius", Float.class);
    public static final ThemeKey<Float> SLIDER_TOOLTIP_TRIANGLE_SIZE = register("slider.tooltip_triangle_size", Float.class);
    public static final ThemeKey<Padding> SLIDER_TOOLTIP_PADDING = register("slider.tooltip_padding", Padding.class);
    public static final ThemeKey<String> SLIDER_TOOLTIP_FORMAT = register("slider.tooltip_format", String.class);

    // ScrollContainer
    public static final ThemeKey<Color> SCROLL_BAR_COLOR = register("scroll.bar_color", Color.class);
    public static final ThemeKey<Color> SCROLL_BAR_HOVER_COLOR = register("scroll.bar_hover_color", Color.class);
    public static final ThemeKey<Color> SCROLL_BAR_CLICK_COLOR = register("scroll.bar_click_color", Color.class);
    public static final ThemeKey<Float> SCROLL_BAR_WIDTH = register("scroll.bar_width", Float.class);
    public static final ThemeKey<Float> SCROLL_BAR_CORNER_RADIUS = register("scroll.bar_corner_radius", Float.class);
    public static final ThemeKey<Float> SCROLL_BAR_OUTLINE_WIDTH = register("scroll.bar_outline_width", Float.class);
    public static final ThemeKey<Color> SCROLL_BAR_OUTLINE_COLOR = register("scroll.bar_outline_color", Color.class);
    public static final ThemeKey<Float> SCROLL_SPEED = register("scroll.speed", Float.class);
    public static final ThemeKey<Boolean> SCROLL_SMOOTH = register("scroll.smooth", Boolean.class);
    public static final ThemeKey<Integer> SCROLL_ANIMATION_DURATION = register("scroll.animation_duration", Integer.class);
    public static final ThemeKey<Long> SCROLL_NESTED_SCROLL_TIMEOUT = register("scroll.nested_scroll_timeout", Long.class);
    public static final ThemeKey<ScrollContainer.ScrollBarType> SCROLL_BAR_TYPE = register("scroll.bar_type", ScrollContainer.ScrollBarType.class);
    public static final ThemeKey<Boolean> SCROLL_RAIL_CLICK_JUMP = register("scroll.rail_click_jump", Boolean.class);
    public static final ThemeKey<Color> SCROLL_RAIL_COLOR = register("scroll.rail_color", Color.class);
    public static final ThemeKey<Color> SCROLL_RAIL_OUTLINE_COLOR = register("scroll.rail_outline_color", Color.class);
    public static final ThemeKey<Float> SCROLL_RAIL_OUTLINE_WIDTH = register("scroll.rail_outline_width", Float.class);

    // TextField
    public static final ThemeKey<Color> TEXT_FIELD_BACKGROUND_COLOR = register("text_field.background_color", Color.class);
    public static final ThemeKey<Color> TEXT_FIELD_OUTLINE_COLOR = register("text_field.outline_color", Color.class);
    public static final ThemeKey<Color> TEXT_FIELD_FOCUSED_OUTLINE_COLOR = register("text_field.focused_outline_color", Color.class);
    public static final ThemeKey<Color> TEXT_FIELD_SELECTION_COLOR = register("text_field.selection_color", Color.class);
    public static final ThemeKey<Color> TEXT_FIELD_CURSOR_COLOR = register("text_field.cursor_color", Color.class);
    public static final ThemeKey<Float> TEXT_FIELD_CURSOR_WIDTH = register("text_field.cursor_width", Float.class);
    public static final ThemeKey<Float> TEXT_FIELD_OUTLINE_WIDTH = register("text_field.outline_width", Float.class);
    public static final ThemeKey<Float> TEXT_FIELD_CORNER_RADIUS = register("text_field.corner_radius", Float.class);

    // Checkbox
    public static final ThemeKey<Float> CHECKBOX_CORNER_RADIUS = register("checkbox.corner_radius", Float.class);
    public static final ThemeKey<Float> CHECKBOX_OUTLINE_WIDTH = register("checkbox.outline_width", Float.class);
    public static final ThemeKey<Color> CHECKBOX_BACKGROUND_COLOR = register("checkbox.background_color", Color.class);
    public static final ThemeKey<Color> CHECKBOX_OUTLINE_COLOR = register("checkbox.outline_color", Color.class);
    public static final ThemeKey<Color> CHECKBOX_CHECK_COLOR = register("checkbox.check_color", Color.class);
    public static final ThemeKey<Float> CHECKBOX_CHECK_WIDTH = register("checkbox.check_width", Float.class);
    public static final ThemeKey<Float> CHECKBOX_TEXT_GAP = register("checkbox.text_gap", Float.class);
    public static final ThemeKey<Integer> CHECKBOX_ANIMATION_DURATION = register("checkbox.animation_duration", Integer.class);

    // ColorPicker
    public static final ThemeKey<Float> COLOR_PICKER_OUTLINE_WIDTH = register("color_picker.outline_width", Float.class);
    public static final ThemeKey<Color> COLOR_PICKER_OUTLINE_COLOR = register("color_picker.outline_color", Color.class);
    public static final ThemeKey<Float> COLOR_PICKER_PICKER_SIZE = register("color_picker.picker_size", Float.class);
    public static final ThemeKey<Float> COLOR_PICKER_SLIDER_WIDTH = register("color_picker.slider_width", Float.class);
    public static final ThemeKey<Float> COLOR_PICKER_GAP = register("color_picker.gap", Float.class);
    public static final ThemeKey<Float> COLOR_PICKER_SELECTOR_SIZE = register("color_picker.selector_size", Float.class);

    private static <T> ThemeKey<T> register(final String key, final Class<T> type) {
        ThemeKey<T> themeKey = new ThemeKey<>(key, type);
        ALL_KEYS.add(themeKey);
        return themeKey;
    }


    private final Map<ThemeKey<?>, Object> values = new HashMap<>();

    public void apply(final Rivet rivet) {
        this.values.clear();
        this.addValues(rivet, this.values);
        this.validate();
    }

    protected abstract void addValues(final Rivet rivet, final Map<ThemeKey<?>, Object> values);

    private void validate() {
        for (ThemeKey<?> key : ALL_KEYS) {
            if (!this.values.containsKey(key)) {
                throw new IllegalStateException("Theme key '" + key.name() + "' is not set");
            }
        }
    }

    public <T> T get(final ThemeKey<T> key) {
        T value = this.getOrDefault(key, null);
        if (value == null) throw new IllegalStateException("No value for key " + key);
        return value;
    }

    public <T> T getOrDefault(final ThemeKey<T> key, @Nullable final T defaultValue) {
        Object value = this.values.get(key);
        if (value == null) return defaultValue;
        return (T) value;
    }

}
