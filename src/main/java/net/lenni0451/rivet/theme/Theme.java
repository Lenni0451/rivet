package net.lenni0451.rivet.theme;

import lombok.RequiredArgsConstructor;
import net.lenni0451.commons.animation.AnimationMode;
import net.lenni0451.commons.animation.EasingBehavior;
import net.lenni0451.commons.animation.easing.EasingFunction;
import net.lenni0451.commons.animation.easing.EasingMode;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.AnimationFrameConfig;
import net.lenni0451.rivet.animation.DynamicAnimationConfig;
import net.lenni0451.rivet.component.container.CollapsibleContainer.ArrowPosition;
import net.lenni0451.rivet.component.container.tabcontainer.TabAlignment;
import net.lenni0451.rivet.component.impl.Label.OverflowBehavior;
import net.lenni0451.rivet.component.impl.ProgressBar.TextPosition;
import net.lenni0451.rivet.component.impl.ScrollBar.ScrollBarType;
import net.lenni0451.rivet.component.impl.slider.Slider.ThumbShape;
import net.lenni0451.rivet.input.mouse.ClickOn;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Padding;

import java.util.*;
import java.util.function.Function;

import static net.lenni0451.rivet.utils.MathUtils.roundMin;

public abstract class Theme {

    private static final List<ThemeKey<?>> REGISTERED_KEYS = new ArrayList<>();

    static {
        try {
            for (Class<?> declaredClass : Theme.class.getDeclaredClasses()) {
                Class.forName(declaredClass.getName(), true, declaredClass.getClassLoader());
            }
        } catch (Throwable t) {
            throw new ExceptionInInitializerError("Failed to initialize theme keys: " + t.getMessage());
        }
    }

    public static <T> ThemeKey<T> register(final String key, final Class<T> type, final Function<Rivet, T> defaultValue) {
        ThemeKey<T> themeKey = new ThemeKey<>(key, type, defaultValue);
        REGISTERED_KEYS.add(themeKey);
        return themeKey;
    }

    public static List<ThemeKey<?>> registeredKeys() {
        return Collections.unmodifiableList(REGISTERED_KEYS);
    }


    private Rivet rivet;
    private final Map<ThemeKey<?>, Object> values = new HashMap<>();

    public final void apply(final Rivet rivet) {
        if (this.rivet != null) {
            throw new IllegalStateException("Theme has already been applied");
        }
        this.rivet = rivet;
        this.values.clear();

        Values valuesAdder = new Values(this.values);
        this.addValues(rivet, valuesAdder);
        valuesAdder.close();
    }

    protected abstract void addValues(final Rivet rivet, final Values values);

    public final <T> T get(final ThemeKey<T> key) {
        if (this.rivet == null) {
            throw new IllegalStateException("Theme has not been applied yet");
        }
        T value = (T) this.values.get(key);
        if (value != null) return value;
        value = key.defaultValue().apply(this.rivet);
        this.values.put(key, value);
        return value;
    }


    public static class General {
        public static final ThemeKey<Color> TEXT_COLOR = register("general.text_color", Color.class, r -> Color.WHITE);
        public static final ThemeKey<Color> DISABLED_TEXT_COLOR = register("general.disabled_text_color", Color.class, r -> Color.fromRGB(150, 150, 150));
    }

    public static class Label {
        public static final ThemeKey<OverflowBehavior> OVERFLOW_BEHAVIOR = register("label.overflow_behavior", OverflowBehavior.class, r -> OverflowBehavior.CLIP);
    }

    public static class Button {
        // Geometry & Layout
        public static final ThemeKey<Corners> CORNER_RADIUS = register("button.corner_radius", Corners.class, r -> new Corners(roundMin(r.backend().font().height() / 10F, 0)));
        public static final ThemeKey<Float> OUTLINE_WIDTH = register("button.outline_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
        public static final ThemeKey<Padding> INNER_PADDING = register("button.inner_padding", Padding.class, r -> {
            float textHeight = r.backend().font().height();
            return new Padding(roundMin(textHeight / 3F, 0), roundMin(textHeight / 10F, 0), roundMin(textHeight / 3F, 0), roundMin(textHeight / 10F, 0));
        });

        // Colors - Normal
        public static final ThemeKey<Color> BACKGROUND_COLOR = register("button.background_color", Color.class, r -> Color.fromRGB(45, 45, 48));
        public static final ThemeKey<Color> OUTLINE_COLOR = register("button.outline_color", Color.class, r -> Color.fromRGB(65, 65, 70));

        // Colors - Hover
        public static final ThemeKey<Color> HOVER_BACKGROUND_COLOR = register("button.hover_background_color", Color.class, r -> Color.fromRGB(65, 65, 70));
        public static final ThemeKey<Color> HOVER_OUTLINE_COLOR = register("button.hover_outline_color", Color.class, r -> Color.fromRGB(100, 100, 105));

        // Colors - Click/Pressed
        public static final ThemeKey<Color> CLICK_BACKGROUND_COLOR = register("button.click_background_color", Color.class, r -> Color.fromRGB(55, 55, 60));
        public static final ThemeKey<Color> CLICK_OUTLINE_COLOR = register("button.click_outline_color", Color.class, r -> Color.fromRGB(110, 110, 115));

        // Colors - Disabled
        public static final ThemeKey<Color> DISABLED_BACKGROUND_COLOR = register("button.disabled_background_color", Color.class, r -> Color.fromRGB(35, 35, 38));
        public static final ThemeKey<Color> DISABLED_OUTLINE_COLOR = register("button.disabled_outline_color", Color.class, r -> Color.fromRGB(50, 50, 55));

        // Animations
        public static final ThemeKey<AnimationConfig> HOVER_ANIMATION = register("button.hover_animation", AnimationConfig.class, r -> DefaultTheme.HOVER_ANIMATION);
        public static final ThemeKey<AnimationConfig> CLICK_ANIMATION = register("button.click_animation", AnimationConfig.class, r -> DefaultTheme.CLICK_ANIMATION);

        // Behaviors & Settings
        public static final ThemeKey<ClickOn> CLICK_ON = register("button.click_on", ClickOn.class, r -> ClickOn.UP);
    }

    public static class Slider {
        // Geometry & Layout
        public static final ThemeKey<Float> BAR_HEIGHT = register("slider.bar_height", Float.class, r -> roundMin(r.backend().font().height() / 3F, 1));
        public static final ThemeKey<Corners> BAR_CORNER_RADIUS = register("slider.bar_corner_radius", Corners.class, r -> new Corners(Float.MAX_VALUE));
        public static final ThemeKey<Float> THUMB_WIDTH = register("slider.thumb_width", Float.class, r -> roundMin(r.backend().font().height() / 3F, 1) * 2F);
        public static final ThemeKey<Float> THUMB_HEIGHT = register("slider.thumb_height", Float.class, r -> roundMin(r.backend().font().height() / 3F, 1) * 2F);
        public static final ThemeKey<Corners> THUMB_CORNER_RADIUS = register("slider.thumb_corner_radius", Corners.class, r -> new Corners(Float.MAX_VALUE));
        public static final ThemeKey<Float> THUMB_OUTLINE_WIDTH = register("slider.thumb_outline_width", Float.class, r -> 0F);
        public static final ThemeKey<Corners> TOOLTIP_CORNER_RADIUS = register("slider.tooltip_corner_radius", Corners.class, r -> new Corners(roundMin(r.backend().font().height() / 10F, 0)));
        public static final ThemeKey<Float> TOOLTIP_TRIANGLE_SIZE = register("slider.tooltip_triangle_size", Float.class, r -> roundMin(r.backend().font().height() / 4F, 1));
        public static final ThemeKey<Padding> TOOLTIP_PADDING = register("slider.tooltip_padding", Padding.class, r -> {
            float textHeight = r.backend().font().height();
            return new Padding(roundMin(textHeight / 10F, 0), 0, roundMin(textHeight / 10F, 0), 0);
        });

        // Colors - Base / Inactive
        public static final ThemeKey<Color> BAR_COLOR = register("slider.bar_color", Color.class, r -> Color.fromRGB(65, 65, 70));
        public static final ThemeKey<Color> THUMB_COLOR = register("slider.thumb_color", Color.class, r -> Color.fromRGB(120, 120, 125));
        public static final ThemeKey<Color> THUMB_OUTLINE_COLOR = register("slider.thumb_outline_color", Color.class, r -> Color.fromRGB(140, 140, 145));
        public static final ThemeKey<Color> TICK_COLOR = register("slider.tick_color", Color.class, r -> Color.fromRGB(160, 160, 165));
        public static final ThemeKey<Color> TOOLTIP_BACKGROUND_COLOR = register("slider.tooltip_background_color", Color.class, r -> Color.fromRGB(45, 45, 48));
        public static final ThemeKey<Color> TOOLTIP_TEXT_COLOR = register("slider.tooltip_text_color", Color.class, r -> Color.WHITE);

        // Colors - States (Hover / Click)
        public static final ThemeKey<Color> BAR_FILL_COLOR = register("slider.bar_fill_color", Color.class, r -> Color.fromRGB(80, 80, 85));
        public static final ThemeKey<Color> THUMB_HOVER_COLOR = register("slider.thumb_hover_color", Color.class, r -> Color.fromRGB(140, 140, 145));
        public static final ThemeKey<Color> THUMB_HOVER_OUTLINE_COLOR = register("slider.thumb_hover_outline_color", Color.class, r -> Color.fromRGB(160, 160, 165));
        public static final ThemeKey<Color> THUMB_CLICK_COLOR = register("slider.thumb_click_color", Color.class, r -> Color.fromRGB(100, 100, 105));
        public static final ThemeKey<Color> THUMB_CLICK_OUTLINE_COLOR = register("slider.thumb_click_outline_color", Color.class, r -> Color.fromRGB(120, 120, 125));

        // Colors - Disabled
        public static final ThemeKey<Color> DISABLED_BAR_COLOR = register("slider.disabled_bar_color", Color.class, r -> Color.fromRGB(45, 45, 48));
        public static final ThemeKey<Color> DISABLED_BAR_FILL_COLOR = register("slider.disabled_bar_fill_color", Color.class, r -> Color.fromRGB(55, 55, 58));
        public static final ThemeKey<Color> DISABLED_THUMB_COLOR = register("slider.disabled_thumb_color", Color.class, r -> Color.fromRGB(75, 75, 78));
        public static final ThemeKey<Color> DISABLED_THUMB_OUTLINE_COLOR = register("slider.disabled_thumb_outline_color", Color.class, r -> Color.fromRGB(95, 95, 98));
        public static final ThemeKey<Color> DISABLED_TICK_COLOR = register("slider.disabled_tick_color", Color.class, r -> Color.fromRGB(100, 100, 105));

        // Animations
        public static final ThemeKey<AnimationConfig> HOVER_ANIMATION = register("slider.hover_animation", AnimationConfig.class, r -> DefaultTheme.HOVER_ANIMATION);
        public static final ThemeKey<AnimationConfig> CLICK_ANIMATION = register("slider.click_animation", AnimationConfig.class, r -> DefaultTheme.CLICK_ANIMATION);

        // Behaviors & Settings
        public static final ThemeKey<Boolean> THUMB_ENCASED = register("slider.thumb_encased", Boolean.class, r -> false);
        public static final ThemeKey<ThumbShape> THUMB_SHAPE = register("slider.thumb_shape", ThumbShape.class, r -> ThumbShape.CIRCLE);
        public static final ThemeKey<Boolean> SHOW_TOOLTIP = register("slider.show_tooltip", Boolean.class, r -> true);
        public static final ThemeKey<String> TOOLTIP_FORMAT = register("slider.tooltip_format", String.class, r -> "%,f");
        public static final ThemeKey<Boolean> ENSURE_VALUES_REACHABLE = register("slider.ensure_values_reachable", Boolean.class, r -> false);
    }

    public static class ScrollBar {
        // Geometry & Layout
        public static final ThemeKey<Float> BAR_WIDTH = register("scroll_bar.bar_width", Float.class, r -> roundMin(r.backend().font().height() / 7F, 1));
        public static final ThemeKey<Corners> BAR_CORNER_RADIUS = register("scroll_bar.bar_corner_radius", Corners.class, r -> new Corners(Float.MAX_VALUE));
        public static final ThemeKey<Float> BAR_OUTLINE_WIDTH = register("scroll_bar.bar_outline_width", Float.class, r -> 0F);
        public static final ThemeKey<Float> RAIL_OUTLINE_WIDTH = register("scroll_bar.rail_outline_width", Float.class, r -> 0F);

        // Colors - Base / Inactive
        public static final ThemeKey<Color> BAR_COLOR = register("scroll_bar.bar_color", Color.class, r -> Color.fromRGBA(120, 120, 125, 100));
        public static final ThemeKey<Color> BAR_OUTLINE_COLOR = register("scroll_bar.bar_outline_color", Color.class, r -> Color.fromRGB(65, 65, 70));
        public static final ThemeKey<Color> RAIL_COLOR = register("scroll_bar.rail_color", Color.class, r -> Color.fromRGB(37, 37, 38));
        public static final ThemeKey<Color> RAIL_OUTLINE_COLOR = register("scroll_bar.rail_outline_color", Color.class, r -> Color.fromRGB(51, 51, 52));

        // Colors - States
        public static final ThemeKey<Color> BAR_HOVER_COLOR = register("scroll_bar.bar_hover_color", Color.class, r -> Color.fromRGBA(140, 140, 145, 150));
        public static final ThemeKey<Color> BAR_CLICK_COLOR = register("scroll_bar.bar_click_color", Color.class, r -> Color.fromRGBA(100, 100, 105, 200));

        // Colors - Disabled
        public static final ThemeKey<Color> BAR_DISABLED_COLOR = register("scroll_bar.bar_disabled_color", Color.class, r -> Color.fromRGBA(100, 100, 105, 50));
        public static final ThemeKey<Color> BAR_DISABLED_OUTLINE_COLOR = register("scroll_bar.bar_disabled_outline_color", Color.class, r -> Color.fromRGBA(65, 65, 70, 50));
        public static final ThemeKey<Color> RAIL_DISABLED_COLOR = register("scroll_bar.rail_disabled_color", Color.class, r -> Color.fromRGBA(37, 37, 38, 100));
        public static final ThemeKey<Color> RAIL_DISABLED_OUTLINE_COLOR = register("scroll_bar.rail_disabled_outline_color", Color.class, r -> Color.fromRGBA(51, 51, 52, 100));

        // Behaviors & Settings
        public static final ThemeKey<ScrollBarType> BAR_TYPE = register("scroll_bar.bar_type", ScrollBarType.class, r -> ScrollBarType.FLOATING);
        public static final ThemeKey<Boolean> RAIL_CLICK_JUMP = register("scroll_bar.rail_click_jump", Boolean.class, r -> true);
    }

    public static class ScrollContainer {
        // Animations
        public static final ThemeKey<DynamicAnimationConfig> ANIMATION = register("scroll_container.animation", DynamicAnimationConfig.class, r -> new DynamicAnimationConfig(EasingFunction.SINE, EasingMode.EASE_OUT, 100));

        // Behaviors & Settings
        public static final ThemeKey<Float> SPEED = register("scroll_container.speed", Float.class, r -> roundMin(r.backend().font().height() * 4, 1));
        public static final ThemeKey<Boolean> SMOOTH = register("scroll_container.smooth", Boolean.class, r -> true);
        public static final ThemeKey<Long> NESTED_SCROLL_TIMEOUT = register("scroll_container.nested_scroll_timeout", Long.class, r -> 150L);
    }

    public static class TextField {
        // Geometry & Layout
        public static final ThemeKey<Float> CURSOR_WIDTH = register("text_field.cursor_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
        public static final ThemeKey<Float> OUTLINE_WIDTH = register("text_field.outline_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
        public static final ThemeKey<Corners> CORNER_RADIUS = register("text_field.corner_radius", Corners.class, r -> new Corners(0F));
        public static final ThemeKey<Padding> INNER_PADDING = register("text_field.inner_padding", Padding.class, r -> {
            float textHeight = r.backend().font().height();
            return new Padding(roundMin(textHeight / 5F, 0), roundMin(textHeight / 10F, 0), roundMin(textHeight / 5F, 0), roundMin(textHeight / 10F, 0));
        });

        // Colors - Base / Inactive
        public static final ThemeKey<Color> TEXT_COLOR = register("text_field.text_color", Color.class, r -> r.theme().get(General.TEXT_COLOR));
        public static final ThemeKey<Color> HINT_COLOR = register("text_field.hint_color", Color.class, r -> Color.GRAY);
        public static final ThemeKey<Color> BACKGROUND_COLOR = register("text_field.background_color", Color.class, r -> Color.fromRGB(30, 30, 30));
        public static final ThemeKey<Color> OUTLINE_COLOR = register("text_field.outline_color", Color.class, r -> Color.GRAY);
        public static final ThemeKey<Color> CURSOR_COLOR = register("text_field.cursor_color", Color.class, r -> Color.WHITE);

        // Colors - States
        public static final ThemeKey<Color> FOCUSED_OUTLINE_COLOR = register("text_field.focused_outline_color", Color.class, r -> Color.WHITE);
        public static final ThemeKey<Color> INVALID_TEXT_COLOR = register("text_field.invalid_text_color", Color.class, r -> Color.fromRGB(255, 100, 100));
        public static final ThemeKey<Color> INVALID_OUTLINE_COLOR = register("text_field.invalid_outline_color", Color.class, r -> Color.fromRGB(255, 100, 100));
        public static final ThemeKey<Color> SELECTION_COLOR = register("text_field.selection_color", Color.class, r -> Color.fromRGBA(100, 100, 255, 100));

        // Colors - Disabled
        public static final ThemeKey<Color> DISABLED_TEXT_COLOR = register("text_field.disabled_text_color", Color.class, r -> Color.fromRGB(150, 150, 150));
        public static final ThemeKey<Color> DISABLED_BACKGROUND_COLOR = register("text_field.disabled_background_color", Color.class, r -> Color.fromRGB(20, 20, 20));
        public static final ThemeKey<Color> DISABLED_OUTLINE_COLOR = register("text_field.disabled_outline_color", Color.class, r -> Color.fromRGB(45, 45, 45));

        // Animations
        public static final ThemeKey<AnimationConfig> CURSOR_ANIMATION = register("text_field.cursor_animation", AnimationConfig.class, r -> new AnimationConfig(
                AnimationMode.LOOP,
                List.of(
                        new AnimationFrameConfig(EasingFunction.SINE, EasingMode.EASE_OUT, 1F, 1F, 250, EasingBehavior.KEEP),
                        new AnimationFrameConfig(EasingFunction.SINE, EasingMode.EASE_OUT, 1F, 0F, 500, EasingBehavior.KEEP),
                        new AnimationFrameConfig(EasingFunction.SINE, EasingMode.EASE_OUT, 0F, 1F, 500, EasingBehavior.KEEP)
                )
        ));
        public static final ThemeKey<AnimationConfig> FOCUS_ANIMATION = register("text_field.focus_animation", AnimationConfig.class, r -> DefaultTheme.HOVER_ANIMATION);

        // Behaviors & Settings
        public static final ThemeKey<Character> PASSWORD_CHAR = register("text_field.password_char", Character.class, r -> '•');
    }

    public static class Checkbox {
        // Geometry & Layout
        public static final ThemeKey<Corners> CORNER_RADIUS = register("checkbox.corner_radius", Corners.class, r -> new Corners(roundMin(r.backend().font().height() / 25F, 1)));
        public static final ThemeKey<Float> OUTLINE_WIDTH = register("checkbox.outline_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
        public static final ThemeKey<Float> CHECK_WIDTH = register("checkbox.check_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
        public static final ThemeKey<Float> TEXT_GAP = register("checkbox.text_gap", Float.class, r -> 0F);

        // Colors - Base / Inactive
        public static final ThemeKey<Color> BACKGROUND_COLOR = register("checkbox.background_color", Color.class, r -> Color.fromRGB(30, 30, 30));
        public static final ThemeKey<Color> OUTLINE_COLOR = register("checkbox.outline_color", Color.class, r -> Color.GRAY);
        public static final ThemeKey<Color> CHECK_COLOR = register("checkbox.check_color", Color.class, r -> Color.WHITE);

        // Colors - States (Hover)
        public static final ThemeKey<Color> HOVER_BACKGROUND_COLOR = register("checkbox.hover_background_color", Color.class, r -> Color.fromRGB(45, 45, 48));
        public static final ThemeKey<Color> HOVER_OUTLINE_COLOR = register("checkbox.hover_outline_color", Color.class, r -> Color.fromRGB(120, 120, 125));

        // Colors - Disabled
        public static final ThemeKey<Color> DISABLED_BACKGROUND_COLOR = register("checkbox.disabled_background_color", Color.class, r -> Color.fromRGB(20, 20, 20));
        public static final ThemeKey<Color> DISABLED_OUTLINE_COLOR = register("checkbox.disabled_outline_color", Color.class, r -> Color.fromRGB(50, 50, 50));
        public static final ThemeKey<Color> DISABLED_CHECK_COLOR = register("checkbox.disabled_check_color", Color.class, r -> Color.fromRGB(100, 100, 100));

        // Animations
        public static final ThemeKey<AnimationConfig> HOVER_ANIMATION = register("checkbox.hover_animation", AnimationConfig.class, r -> DefaultTheme.HOVER_ANIMATION);
        public static final ThemeKey<AnimationConfig> CHECK_ANIMATION = register("checkbox.check_animation", AnimationConfig.class, r -> DefaultTheme.CLICK_ANIMATION);
    }

    public static class ColorPicker {
        // Geometry & Layout
        public static final ThemeKey<Float> OUTLINE_WIDTH = register("color_picker.outline_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
        public static final ThemeKey<Float> PICKER_SIZE = register("color_picker.picker_size", Float.class, r -> roundMin(r.backend().font().height() * 8F, 1));
        public static final ThemeKey<Float> SLIDER_WIDTH = register("color_picker.slider_width", Float.class, r -> roundMin(r.backend().font().height(), 1));
        public static final ThemeKey<Float> GAP = register("color_picker.gap", Float.class, r -> roundMin(r.backend().font().height() / 3F, 1));
        public static final ThemeKey<Float> SELECTOR_SIZE = register("color_picker.selector_size", Float.class, r -> roundMin(r.backend().font().height() / 8F, 1));
        public static final ThemeKey<Float> PICKER_INDICATOR_OUTLINE_WIDTH = register("color_picker.picker_indicator_outline_width", Float.class, r -> 1F);
        public static final ThemeKey<Float> SLIDER_INDICATOR_OUTLINE_WIDTH = register("color_picker.slider_indicator_outline_width", Float.class, r -> 1F);
        public static final ThemeKey<Float> PICKER_INDICATOR_RADIUS = register("color_picker.picker_indicator_radius", Float.class, r -> roundMin(r.backend().font().height() / 8F, 1));
        public static final ThemeKey<Float> SLIDER_INDICATOR_INNER_WIDTH = register("color_picker.slider_indicator_inner_width", Float.class, r -> 2F);

        // Behaviors & Settings
        public static final ThemeKey<Boolean> SHOW_ALPHA = register("color_picker.show_alpha", Boolean.class, r -> true);
        public static final ThemeKey<Boolean> SHOW_PREVIEW = register("color_picker.show_preview", Boolean.class, r -> true);
        public static final ThemeKey<Boolean> ALLOW_SCALING = register("color_picker.allow_scaling", Boolean.class, r -> true);

        // Colors - Base / Inactive
        public static final ThemeKey<Color> OUTLINE_COLOR = register("color_picker.outline_color", Color.class, r -> Color.GRAY);
    }

    public static class ComboBox {
        // Geometry & Layout
        public static final ThemeKey<Float> MAX_POPUP_HEIGHT = register("combo_box.max_popup_height", Float.class, r -> roundMin(r.backend().font().height() * 10F, 1));

        // Behaviors & Settings
        public static final ThemeKey<Boolean> INTERCEPT_OUTSIDE_CLICKS = register("combo_box.intercept_outside_clicks", Boolean.class, r -> true);
    }

    public static class Separator {
        // Geometry & Layout
        public static final ThemeKey<Float> THICKNESS = register("separator.thickness", Float.class, r -> roundMin(r.backend().font().height() / 20F, 1));

        // Colors - Base / Inactive
        public static final ThemeKey<Color> COLOR = register("separator.color", Color.class, r -> Color.fromRGB(65, 65, 70));
    }

    public static class Tab {
        // Geometry & Layout
        public static final ThemeKey<Corners> CORNER_RADIUS = register("tab.corner_radius", Corners.class, r -> {
            float cornerRadius = roundMin(r.backend().font().height() / 10F, 0);
            return new Corners(cornerRadius, 0F, 0F, cornerRadius);
        });
        public static final ThemeKey<Float> OUTLINE_WIDTH = register("tab.outline_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
        public static final ThemeKey<Padding> INNER_PADDING = register("tab.inner_padding", Padding.class, r -> new Padding(5));
        public static final ThemeKey<Float> SEPARATOR_THICKNESS = register("tab.separator_thickness", Float.class, r -> 0F);
        public static final ThemeKey<Float> VERTICAL_GAP = register("tab.vertical_gap", Float.class, r -> 0F);
        public static final ThemeKey<Float> TAB_GAP = register("tab.tab_gap", Float.class, r -> 0F);

        // Colors - Base / Inactive
        public static final ThemeKey<Color> INACTIVE_COLOR = register("tab.inactive_color", Color.class, r -> Color.fromRGB(35, 35, 38));
        public static final ThemeKey<Color> INACTIVE_OUTLINE_COLOR = register("tab.inactive_outline_color", Color.class, r -> Color.fromRGB(55, 55, 60));
        public static final ThemeKey<Color> HEADER_BACKGROUND_COLOR = register("tab.header_background_color", Color.class, r -> Color.TRANSPARENT);
        public static final ThemeKey<Color> SEPARATOR_COLOR = register("tab.separator_color", Color.class, r -> Color.fromRGB(65, 65, 70));

        // Colors - States (Hover / Active)
        public static final ThemeKey<Color> ACTIVE_COLOR = register("tab.active_color", Color.class, r -> Color.fromRGB(45, 45, 48));
        public static final ThemeKey<Color> ACTIVE_OUTLINE_COLOR = register("tab.active_outline_color", Color.class, r -> Color.fromRGB(65, 65, 70));
        public static final ThemeKey<Color> HOVER_COLOR = register("tab.hover_color", Color.class, r -> Color.fromRGB(50, 50, 55));
        public static final ThemeKey<Color> HOVER_OUTLINE_COLOR = register("tab.hover_outline_color", Color.class, r -> Color.fromRGB(80, 80, 85));

        // Animations
        public static final ThemeKey<AnimationConfig> HOVER_ANIMATION = register("tab.hover_animation", AnimationConfig.class, r -> DefaultTheme.HOVER_ANIMATION);
        public static final ThemeKey<AnimationConfig> ACTIVE_ANIMATION = register("tab.active_animation", AnimationConfig.class, r -> DefaultTheme.CLICK_ANIMATION);

        // Behaviors & Settings
        public static final ThemeKey<TabAlignment> ALIGNMENT = register("tab.alignment", TabAlignment.class, r -> TabAlignment.LEFT);
        public static final ThemeKey<Boolean> SAME_SIZE = register("tab.same_size", Boolean.class, r -> false);
        public static final ThemeKey<ClickOn> SELECT_ON = register("tab.select_on", ClickOn.class, r -> ClickOn.UP);
    }

    public static class Tooltip {
        // Behaviors & Settings
        public static final ThemeKey<Long> DELAY = register("tooltip.delay", Long.class, r -> 500L);
        public static final ThemeKey<Boolean> REMOVE_ON_MOUSE_MOVE = register("tooltip.remove_on_mouse_move", Boolean.class, r -> true);
        public static final ThemeKey<Integer> MOUSE_OFFSET = register("tooltip.mouse_offset", Integer.class, r -> 20);
    }

    public static class ProgressBar {
        // Geometry & Layout
        public static final ThemeKey<Float> BORDER_WIDTH = register("progress_bar.border_width", Float.class, r -> 0F);
        public static final ThemeKey<Corners> TRACK_CORNER_RADIUS = register("progress_bar.track_corner_radius", Corners.class, r -> new Corners(0F));
        public static final ThemeKey<Corners> INDICATOR_CORNER_RADIUS = register("progress_bar.indicator_corner_radius", Corners.class, r -> new Corners(0F));
        public static final ThemeKey<Float> TEXT_PADDING = register("progress_bar.text_padding", Float.class, r -> 5F);
        public static final ThemeKey<Float> STRIPE_WIDTH = register("progress_bar.stripe_width", Float.class, r -> roundMin(r.backend().font().height() * 0.75F, 1));
        public static final ThemeKey<Float> STRIPE_GAP = register("progress_bar.stripe_gap", Float.class, r -> roundMin(r.backend().font().height() * 0.75F, 1));
        public static final ThemeKey<Float> STRIPE_ANGLE = register("progress_bar.stripe_angle", Float.class, r -> 45F);

        // Colors - Base / Inactive
        public static final ThemeKey<Color> TRACK_COLOR = register("progress_bar.track_color", Color.class, r -> Color.fromRGB(45, 45, 48));
        public static final ThemeKey<Color> INDICATOR_COLOR = register("progress_bar.indicator_color", Color.class, r -> Color.fromRGB(0, 122, 204));
        public static final ThemeKey<Color> BORDER_COLOR = register("progress_bar.border_color", Color.class, r -> Color.TRANSPARENT);
        public static final ThemeKey<Color> TEXT_COLOR = register("progress_bar.text_color", Color.class, r -> Color.WHITE);
        public static final ThemeKey<Color> STRIPE_COLOR = register("progress_bar.stripe_color", Color.class, r -> Color.fromRGBA(255, 255, 255, 60));

        // Behaviors & Settings
        public static final ThemeKey<String> TEXT_FORMAT = register("progress_bar.text_format", String.class, r -> "%,.0f%%");
        public static final ThemeKey<TextPosition> TEXT_POSITION = register("progress_bar.text_position", TextPosition.class, r -> TextPosition.FOLLOW_CENTER);
        public static final ThemeKey<Boolean> STRIPES = register("progress_bar.stripes", Boolean.class, r -> false);
        public static final ThemeKey<Float> STRIPE_SPEED = register("progress_bar.stripe_speed", Float.class, r -> roundMin(r.backend().font().height() * 1.5F, 1));
        public static final ThemeKey<Boolean> STRIPE_ANIMATED = register("progress_bar.stripe_animated", Boolean.class, r -> true);
    }

    public static class CollapsibleContainer {
        // Animations
        public static final ThemeKey<AnimationConfig> COLLAPSE_ANIMATION = register("collapsible_container.collapse_animation", AnimationConfig.class, r -> new AnimationConfig(
                AnimationMode.DEFAULT,
                new AnimationFrameConfig(EasingFunction.CIRC, EasingMode.EASE_OUT, 0F, 1F, 200, EasingBehavior.KEEP)
        ));

        // Behaviors & Settings
        public static final ThemeKey<ClickOn> COLLAPSE_ON = register("collapsible_container.collapse_on", ClickOn.class, r -> ClickOn.UP);
        public static final ThemeKey<ArrowPosition> ARROW_POSITION = register("collapsible_container.arrow_position", ArrowPosition.class, r -> ArrowPosition.LEFT);
    }

    public static class Arrow {
        // Geometry & Layout
        public static final ThemeKey<Float> LINE_WIDTH = register("arrow.line_width", Float.class, r -> roundMin(r.backend().font().height() / 15F, 1));
        public static final ThemeKey<Float> SIZE = register("arrow.size", Float.class, r -> r.backend().font().height());

        // Colors - Base / Inactive
        public static final ThemeKey<Color> COLOR = register("arrow.color", Color.class, r -> Color.WHITE);

        // Colors - Disabled
        public static final ThemeKey<Color> DISABLED_COLOR = register("arrow.disabled_color", Color.class, r -> Color.fromRGB(150, 150, 150));
    }

    public static class DragNumberInput {
        // Geometry & Layout
        public static final ThemeKey<Float> OUTLINE_WIDTH = register("drag_number_input.outline_width", Float.class, r -> 1F);
        public static final ThemeKey<Corners> CORNER_RADIUS = register("drag_number_input.corner_radius", Corners.class, r -> new Corners(roundMin(r.backend().font().height() / 4F, 0)));
        public static final ThemeKey<Padding> INNER_PADDING = register("drag_number_input.inner_padding", Padding.class, r -> {
            float textHeight = r.backend().font().height();
            return new Padding(roundMin(textHeight / 3F, 0), roundMin(textHeight / 10F, 0), roundMin(textHeight / 3F, 0), roundMin(textHeight / 10F, 0));
        });

        // Colors - Base / Inactive
        public static final ThemeKey<Color> BACKGROUND_COLOR = register("drag_number_input.background_color", Color.class, r -> Color.fromRGB(50, 50, 50));
        public static final ThemeKey<Color> OUTLINE_COLOR = register("drag_number_input.outline_color", Color.class, r -> Color.fromRGB(100, 100, 105));

        // Colors - States (Hover / Click)
        public static final ThemeKey<Color> HOVER_BACKGROUND_COLOR = register("drag_number_input.hover_background_color", Color.class, r -> Color.fromRGB(60, 60, 60));
        public static final ThemeKey<Color> HOVER_OUTLINE_COLOR = register("drag_number_input.hover_outline_color", Color.class, r -> Color.fromRGB(120, 120, 125));
        public static final ThemeKey<Color> CLICK_BACKGROUND_COLOR = register("drag_number_input.click_background_color", Color.class, r -> Color.fromRGB(40, 40, 40));
        public static final ThemeKey<Color> CLICK_OUTLINE_COLOR = register("drag_number_input.click_outline_color", Color.class, r -> Color.fromRGB(80, 80, 85));

        // Colors - Disabled
        public static final ThemeKey<Color> DISABLED_BACKGROUND_COLOR = register("drag_number_input.disabled_background_color", Color.class, r -> Color.fromRGB(30, 30, 30));
        public static final ThemeKey<Color> DISABLED_OUTLINE_COLOR = register("drag_number_input.disabled_outline_color", Color.class, r -> Color.fromRGB(50, 50, 55));

        // Animations
        public static final ThemeKey<AnimationConfig> HOVER_ANIMATION = register("drag_number_input.hover_animation", AnimationConfig.class, r -> DefaultTheme.HOVER_ANIMATION);
        public static final ThemeKey<AnimationConfig> CLICK_ANIMATION = register("drag_number_input.click_animation", AnimationConfig.class, r -> DefaultTheme.CLICK_ANIMATION);

        // Behaviors & Settings
        public static final ThemeKey<String> VALUE_FORMAT = register("drag_number_input.value_format", String.class, r -> "%,f");
    }

    public static class ToggleSwitch {
        // Geometry & Layout
        public static final ThemeKey<Corners> CORNER_RADIUS = register("toggle_switch.corner_radius", Corners.class, r -> new Corners(Float.MAX_VALUE));
        public static final ThemeKey<Float> OUTLINE_WIDTH = register("toggle_switch.outline_width", Float.class, r -> 0F);
        public static final ThemeKey<Float> THUMB_OUTLINE_WIDTH = register("toggle_switch.thumb_outline_width", Float.class, r -> 0F);
        public static final ThemeKey<Float> THUMB_RATIO = register("toggle_switch.thumb_ratio", Float.class, r -> 0.9F);
        public static final ThemeKey<Float> RAIL_RATIO = register("toggle_switch.rail_ratio", Float.class, r -> 0.6F);

        // Colors - Off state
        public static final ThemeKey<Color> OFF_COLOR = register("toggle_switch.off_color", Color.class, r -> Color.fromRGB(65, 65, 70));
        public static final ThemeKey<Color> OFF_OUTLINE_COLOR = register("toggle_switch.off_outline_color", Color.class, r -> Color.fromRGB(85, 85, 90));
        public static final ThemeKey<Color> OFF_THUMB_COLOR = register("toggle_switch.off_thumb_color", Color.class, r -> Color.fromRGB(200, 200, 200));
        public static final ThemeKey<Color> OFF_THUMB_OUTLINE_COLOR = register("toggle_switch.off_thumb_outline_color", Color.class, r -> Color.TRANSPARENT);

        // Colors - On state
        public static final ThemeKey<Color> ON_COLOR = register("toggle_switch.on_color", Color.class, r -> Color.fromRGB(0, 122, 204));
        public static final ThemeKey<Color> ON_OUTLINE_COLOR = register("toggle_switch.on_outline_color", Color.class, r -> Color.fromRGB(0, 142, 224));
        public static final ThemeKey<Color> ON_THUMB_COLOR = register("toggle_switch.on_thumb_color", Color.class, r -> Color.fromRGB(200, 200, 200));
        public static final ThemeKey<Color> ON_THUMB_OUTLINE_COLOR = register("toggle_switch.on_thumb_outline_color", Color.class, r -> Color.TRANSPARENT);

        // Colors - Hover (Off & On)
        public static final ThemeKey<Color> HOVER_OFF_COLOR = register("toggle_switch.hover_off_color", Color.class, r -> Color.fromRGB(75, 75, 80));
        public static final ThemeKey<Color> HOVER_ON_COLOR = register("toggle_switch.hover_on_color", Color.class, r -> Color.fromRGB(0, 142, 224));
        public static final ThemeKey<Color> HOVER_OFF_OUTLINE_COLOR = register("toggle_switch.hover_off_outline_color", Color.class, r -> Color.fromRGB(95, 95, 100));
        public static final ThemeKey<Color> HOVER_ON_OUTLINE_COLOR = register("toggle_switch.hover_on_outline_color", Color.class, r -> Color.fromRGB(0, 162, 244));
        public static final ThemeKey<Color> HOVER_OFF_THUMB_COLOR = register("toggle_switch.hover_off_thumb_color", Color.class, r -> Color.fromRGB(220, 220, 220));
        public static final ThemeKey<Color> HOVER_ON_THUMB_COLOR = register("toggle_switch.hover_on_thumb_color", Color.class, r -> Color.fromRGB(220, 220, 220));
        public static final ThemeKey<Color> HOVER_OFF_THUMB_OUTLINE_COLOR = register("toggle_switch.hover_off_thumb_outline_color", Color.class, r -> Color.TRANSPARENT);
        public static final ThemeKey<Color> HOVER_ON_THUMB_OUTLINE_COLOR = register("toggle_switch.hover_on_thumb_outline_color", Color.class, r -> Color.TRANSPARENT);

        // Colors - Disabled (Off & On)
        public static final ThemeKey<Color> DISABLED_OFF_COLOR = register("toggle_switch.disabled_off_color", Color.class, r -> Color.fromRGB(45, 45, 48));
        public static final ThemeKey<Color> DISABLED_ON_COLOR = register("toggle_switch.disabled_on_color", Color.class, r -> Color.fromRGB(0, 82, 144));
        public static final ThemeKey<Color> DISABLED_OFF_OUTLINE_COLOR = register("toggle_switch.disabled_off_outline_color", Color.class, r -> Color.fromRGB(55, 55, 58));
        public static final ThemeKey<Color> DISABLED_ON_OUTLINE_COLOR = register("toggle_switch.disabled_on_outline_color", Color.class, r -> Color.fromRGB(0, 92, 154));
        public static final ThemeKey<Color> DISABLED_OFF_THUMB_COLOR = register("toggle_switch.disabled_off_thumb_color", Color.class, r -> Color.fromRGB(120, 120, 125));
        public static final ThemeKey<Color> DISABLED_ON_THUMB_COLOR = register("toggle_switch.disabled_on_thumb_color", Color.class, r -> Color.fromRGB(120, 120, 125));
        public static final ThemeKey<Color> DISABLED_OFF_THUMB_OUTLINE_COLOR = register("toggle_switch.disabled_off_thumb_outline_color", Color.class, r -> Color.TRANSPARENT);
        public static final ThemeKey<Color> DISABLED_ON_THUMB_OUTLINE_COLOR = register("toggle_switch.disabled_on_thumb_outline_color", Color.class, r -> Color.TRANSPARENT);

        // Animations
        public static final ThemeKey<AnimationConfig> HOVER_ANIMATION = register("toggle_switch.hover_animation", AnimationConfig.class, r -> DefaultTheme.HOVER_ANIMATION);
        public static final ThemeKey<AnimationConfig> TOGGLE_ANIMATION = register("toggle_switch.toggle_animation", AnimationConfig.class, r -> DefaultTheme.CLICK_ANIMATION);

        // Behaviors & Settings
        public static final ThemeKey<ClickOn> TOGGLE_ON = register("toggle_switch.toggle_on", ClickOn.class, r -> ClickOn.UP);
        public static final ThemeKey<Boolean> THUMB_ENCASED = register("toggle_switch.thumb_encased", Boolean.class, r -> true);
    }


    @RequiredArgsConstructor
    public static final class Values {
        private final Map<ThemeKey<?>, Object> values;
        private boolean closed;

        public <T> void put(final ThemeKey<T> key, final T value) {
            if (this.closed) {
                throw new IllegalStateException("Cannot put values after the theme has been applied");
            }
            this.values.put(key, value);
        }

        void close() {
            this.closed = true;
        }
    }

}
