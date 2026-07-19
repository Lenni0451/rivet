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
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.component.container.tabcontainer.TabAlignment;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.ProgressBar;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.input.mouse.ClickOn;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Padding;

import java.util.*;
import java.util.function.Function;

import static net.lenni0451.rivet.utils.MathUtils.roundMin;

public abstract class Theme {

    private static final List<ThemeKey<?>> REGISTERED_KEYS = new ArrayList<>();

    // General
    public static final ThemeKey<Color> TEXT_COLOR = register("general.text_color", Color.class, r -> Color.WHITE);
    public static final ThemeKey<Color> DISABLED_TEXT_COLOR = register("general.disabled_text_color", Color.class, r -> Color.fromRGB(150, 150, 150));

    // Label
    public static final ThemeKey<Label.OverflowBehavior> LABEL_OVERFLOW_BEHAVIOR = register("label.overflow_behavior", Label.OverflowBehavior.class, r -> Label.OverflowBehavior.CLIP);

    // Button
    public static final ThemeKey<Float> BUTTON_CORNER_RADIUS = register("button.corner_radius", Float.class, r -> roundMin(r.backend().font().height() / 10F, 0));
    public static final ThemeKey<Float> BUTTON_OUTLINE_WIDTH = register("button.outline_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
    public static final ThemeKey<Color> BUTTON_INACTIVE_COLOR = register("button.inactive_color", Color.class, r -> Color.fromRGB(45, 45, 48));
    public static final ThemeKey<Color> BUTTON_INACTIVE_OUTLINE_COLOR = register("button.inactive_outline_color", Color.class, r -> Color.fromRGB(65, 65, 70));
    public static final ThemeKey<Color> BUTTON_ACTIVE_COLOR = register("button.active_color", Color.class, r -> Color.fromRGB(65, 65, 70));
    public static final ThemeKey<Color> BUTTON_ACTIVE_OUTLINE_COLOR = register("button.active_outline_color", Color.class, r -> Color.fromRGB(100, 100, 105));
    public static final ThemeKey<Color> BUTTON_CLICK_COLOR = register("button.click_color", Color.class, r -> Color.fromRGB(55, 55, 60));
    public static final ThemeKey<Color> BUTTON_CLICK_OUTLINE_COLOR = register("button.click_outline_color", Color.class, r -> Color.fromRGB(110, 110, 115));
    public static final ThemeKey<AnimationConfig> BUTTON_HOVER_ANIMATION = register("button.hover_animation", AnimationConfig.class, r -> DefaultTheme.HOVER_ANIMATION);
    public static final ThemeKey<AnimationConfig> BUTTON_CLICK_ANIMATION = register("button.click_animation", AnimationConfig.class, r -> DefaultTheme.CLICK_ANIMATION);
    public static final ThemeKey<Padding> BUTTON_INNER_PADDING = register("button.inner_padding", Padding.class, r -> {
        float textHeight = r.backend().font().height();
        return new Padding(roundMin(textHeight / 3F, 0), roundMin(textHeight / 10F, 0), roundMin(textHeight / 3F, 0), roundMin(textHeight / 10F, 0));
    });
    public static final ThemeKey<ClickOn> BUTTON_CLICK_ON = register("button.click_on", ClickOn.class, r -> ClickOn.UP);
    public static final ThemeKey<Color> BUTTON_DISABLED_COLOR = register("button.disabled_color", Color.class, r -> Color.fromRGB(35, 35, 38));
    public static final ThemeKey<Color> BUTTON_DISABLED_OUTLINE_COLOR = register("button.disabled_outline_color", Color.class, r -> Color.fromRGB(50, 50, 55));

    // Slider
    public static final ThemeKey<Color> SLIDER_BAR_COLOR = register("slider.bar_color", Color.class, r -> Color.fromRGB(65, 65, 70));
    public static final ThemeKey<Color> SLIDER_ACTIVE_BAR_COLOR = register("slider.active_bar_color", Color.class, r -> Color.fromRGB(80, 80, 85));
    public static final ThemeKey<Color> SLIDER_THUMB_COLOR = register("slider.thumb_color", Color.class, r -> Color.fromRGB(120, 120, 125));
    public static final ThemeKey<Color> SLIDER_THUMB_CLICK_COLOR = register("slider.thumb_click_color", Color.class, r -> Color.fromRGB(100, 100, 105));
    public static final ThemeKey<Color> SLIDER_TICK_COLOR = register("slider.tick_color", Color.class, r -> Color.fromRGB(160, 160, 165));
    public static final ThemeKey<Float> SLIDER_BAR_HEIGHT = register("slider.bar_height", Float.class, r -> roundMin(r.backend().font().height() / 3F, 1));
    public static final ThemeKey<Float> SLIDER_THUMB_WIDTH = register("slider.thumb_width", Float.class, r -> roundMin(r.backend().font().height() / 3F, 1) * 2F);
    public static final ThemeKey<Float> SLIDER_THUMB_HEIGHT = register("slider.thumb_height", Float.class, r -> roundMin(r.backend().font().height() / 3F, 1) * 2F);
    public static final ThemeKey<Float> SLIDER_BAR_CORNER_RADIUS = register("slider.bar_corner_radius", Float.class, r -> Float.MAX_VALUE);
    public static final ThemeKey<Float> SLIDER_THUMB_CORNER_RADIUS = register("slider.thumb_corner_radius", Float.class, r -> Float.MAX_VALUE);
    public static final ThemeKey<Boolean> SLIDER_THUMB_ENCASED = register("slider.thumb_encased", Boolean.class, r -> false);
    public static final ThemeKey<Slider.ThumbShape> SLIDER_THUMB_SHAPE = register("slider.thumb_shape", Slider.ThumbShape.class, r -> Slider.ThumbShape.CIRCLE);
    public static final ThemeKey<Color> SLIDER_THUMB_OUTLINE_COLOR = register("slider.thumb_outline_color", Color.class, r -> Color.fromRGB(140, 140, 145));
    public static final ThemeKey<Color> SLIDER_THUMB_CLICK_OUTLINE_COLOR = register("slider.thumb_click_outline_color", Color.class, r -> Color.fromRGB(120, 120, 125));
    public static final ThemeKey<Float> SLIDER_THUMB_OUTLINE_WIDTH = register("slider.thumb_outline_width", Float.class, r -> 0F);
    public static final ThemeKey<Color> SLIDER_TOOLTIP_BACKGROUND_COLOR = register("slider.tooltip_background_color", Color.class, r -> Color.fromRGB(45, 45, 48));
    public static final ThemeKey<Color> SLIDER_TOOLTIP_TEXT_COLOR = register("slider.tooltip_text_color", Color.class, r -> Color.WHITE);
    public static final ThemeKey<Float> SLIDER_TOOLTIP_CORNER_RADIUS = register("slider.tooltip_corner_radius", Float.class, r -> roundMin(r.backend().font().height() / 10F, 0));
    public static final ThemeKey<Float> SLIDER_TOOLTIP_TRIANGLE_SIZE = register("slider.tooltip_triangle_size", Float.class, r -> roundMin(r.backend().font().height() / 4F, 1));
    public static final ThemeKey<Padding> SLIDER_TOOLTIP_PADDING = register("slider.tooltip_padding", Padding.class, r -> {
        float textHeight = r.backend().font().height();
        return new Padding(roundMin(textHeight / 10F, 0), 0, roundMin(textHeight / 10F, 0), 0);
    });
    public static final ThemeKey<Boolean> SLIDER_SHOW_TOOLTIP = register("slider.show_tooltip", Boolean.class, r -> true);
    public static final ThemeKey<String> SLIDER_TOOLTIP_FORMAT = register("slider.tooltip_format", String.class, r -> "%,f");
    public static final ThemeKey<Color> SLIDER_DISABLED_BAR_COLOR = register("slider.disabled_bar_color", Color.class, r -> Color.fromRGB(45, 45, 48));
    public static final ThemeKey<Color> SLIDER_DISABLED_ACTIVE_BAR_COLOR = register("slider.disabled_active_bar_color", Color.class, r -> Color.fromRGB(55, 55, 58));
    public static final ThemeKey<Color> SLIDER_DISABLED_THUMB_COLOR = register("slider.disabled_thumb_color", Color.class, r -> Color.fromRGB(75, 75, 78));
    public static final ThemeKey<Color> SLIDER_DISABLED_THUMB_OUTLINE_COLOR = register("slider.disabled_thumb_outline_color", Color.class, r -> Color.fromRGB(95, 95, 98));
    public static final ThemeKey<Color> SLIDER_DISABLED_TICK_COLOR = register("slider.disabled_tick_color", Color.class, r -> Color.fromRGB(100, 100, 105));
    public static final ThemeKey<Color> SLIDER_THUMB_HOVER_COLOR = register("slider.thumb_hover_color", Color.class, r -> Color.fromRGB(140, 140, 145));
    public static final ThemeKey<Color> SLIDER_THUMB_HOVER_OUTLINE_COLOR = register("slider.thumb_hover_outline_color", Color.class, r -> Color.fromRGB(160, 160, 165));
    public static final ThemeKey<AnimationConfig> SLIDER_HOVER_ANIMATION = register("slider.hover_animation", AnimationConfig.class, r -> DefaultTheme.HOVER_ANIMATION);
    public static final ThemeKey<AnimationConfig> SLIDER_CLICK_ANIMATION = register("slider.click_animation", AnimationConfig.class, r -> DefaultTheme.CLICK_ANIMATION);

    // ScrollContainer
    public static final ThemeKey<Color> SCROLL_BAR_COLOR = register("scroll.bar_color", Color.class, r -> Color.fromRGBA(120, 120, 125, 100));
    public static final ThemeKey<Color> SCROLL_BAR_HOVER_COLOR = register("scroll.bar_hover_color", Color.class, r -> Color.fromRGBA(140, 140, 145, 150));
    public static final ThemeKey<Color> SCROLL_BAR_CLICK_COLOR = register("scroll.bar_click_color", Color.class, r -> Color.fromRGBA(100, 100, 105, 200));
    public static final ThemeKey<Float> SCROLL_BAR_WIDTH = register("scroll.bar_width", Float.class, r -> roundMin(r.backend().font().height() / 7F, 1));
    public static final ThemeKey<Float> SCROLL_BAR_CORNER_RADIUS = register("scroll.bar_corner_radius", Float.class, r -> Float.MAX_VALUE);
    public static final ThemeKey<Float> SCROLL_BAR_OUTLINE_WIDTH = register("scroll.bar_outline_width", Float.class, r -> 0F);
    public static final ThemeKey<Color> SCROLL_BAR_OUTLINE_COLOR = register("scroll.bar_outline_color", Color.class, r -> Color.fromRGB(65, 65, 70));
    public static final ThemeKey<Float> SCROLL_SPEED = register("scroll.speed", Float.class, r -> roundMin(r.backend().font().height() * 4, 1));
    public static final ThemeKey<Boolean> SCROLL_SMOOTH = register("scroll.smooth", Boolean.class, r -> true);
    public static final ThemeKey<DynamicAnimationConfig> SCROLL_ANIMATION = register("scroll.animation", DynamicAnimationConfig.class, r -> new DynamicAnimationConfig(EasingFunction.SINE, EasingMode.EASE_OUT, 100));
    public static final ThemeKey<Long> SCROLL_NESTED_SCROLL_TIMEOUT = register("scroll.nested_scroll_timeout", Long.class, r -> 150L);
    public static final ThemeKey<ScrollContainer.ScrollBarType> SCROLL_BAR_TYPE = register("scroll.bar_type", ScrollContainer.ScrollBarType.class, r -> ScrollContainer.ScrollBarType.FLOATING);
    public static final ThemeKey<Boolean> SCROLL_RAIL_CLICK_JUMP = register("scroll.rail_click_jump", Boolean.class, r -> true);
    public static final ThemeKey<Color> SCROLL_RAIL_COLOR = register("scroll.rail_color", Color.class, r -> Color.fromRGB(37, 37, 38));
    public static final ThemeKey<Color> SCROLL_RAIL_OUTLINE_COLOR = register("scroll.rail_outline_color", Color.class, r -> Color.fromRGB(51, 51, 52));
    public static final ThemeKey<Float> SCROLL_RAIL_OUTLINE_WIDTH = register("scroll.rail_outline_width", Float.class, r -> 0F);
    public static final ThemeKey<Color> SCROLL_BAR_DISABLED_COLOR = register("scroll.bar_disabled_color", Color.class, r -> Color.fromRGBA(100, 100, 105, 50));
    public static final ThemeKey<Color> SCROLL_BAR_DISABLED_OUTLINE_COLOR = register("scroll.bar_disabled_outline_color", Color.class, r -> Color.fromRGBA(65, 65, 70, 50));
    public static final ThemeKey<Color> SCROLL_RAIL_DISABLED_COLOR = register("scroll.rail_disabled_color", Color.class, r -> Color.fromRGBA(37, 37, 38, 100));
    public static final ThemeKey<Color> SCROLL_RAIL_DISABLED_OUTLINE_COLOR = register("scroll.rail_disabled_outline_color", Color.class, r -> Color.fromRGBA(51, 51, 52, 100));

    // TextField
    public static final ThemeKey<Color> TEXT_FIELD_TEXT_COLOR = register("text_field.text_color", Color.class, r -> r.theme().get(TEXT_COLOR));
    public static final ThemeKey<Color> TEXT_FIELD_INVALID_TEXT_COLOR = register("text_field.invalid_text_color", Color.class, r -> Color.fromRGB(255, 100, 100));
    public static final ThemeKey<Color> TEXT_FIELD_HINT_COLOR = register("text_field.hint_color", Color.class, r -> Color.GRAY);
    public static final ThemeKey<Color> TEXT_FIELD_BACKGROUND_COLOR = register("text_field.background_color", Color.class, r -> Color.fromRGB(30, 30, 30));
    public static final ThemeKey<Color> TEXT_FIELD_OUTLINE_COLOR = register("text_field.outline_color", Color.class, r -> Color.GRAY);
    public static final ThemeKey<Color> TEXT_FIELD_FOCUSED_OUTLINE_COLOR = register("text_field.focused_outline_color", Color.class, r -> Color.WHITE);
    public static final ThemeKey<Color> TEXT_FIELD_INVALID_OUTLINE_COLOR = register("text_field.invalid_outline_color", Color.class, r -> Color.fromRGB(255, 100, 100));
    public static final ThemeKey<Color> TEXT_FIELD_SELECTION_COLOR = register("text_field.selection_color", Color.class, r -> Color.fromRGBA(100, 100, 255, 100));
    public static final ThemeKey<Color> TEXT_FIELD_CURSOR_COLOR = register("text_field.cursor_color", Color.class, r -> Color.WHITE);
    public static final ThemeKey<Float> TEXT_FIELD_CURSOR_WIDTH = register("text_field.cursor_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
    public static final ThemeKey<Float> TEXT_FIELD_OUTLINE_WIDTH = register("text_field.outline_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
    public static final ThemeKey<Float> TEXT_FIELD_CORNER_RADIUS = register("text_field.corner_radius", Float.class, r -> 0F);
    public static final ThemeKey<Padding> TEXT_FIELD_INNER_PADDING = register("text_field.inner_padding", Padding.class, r -> {
        float textHeight = r.backend().font().height();
        return new Padding(roundMin(textHeight / 5F, 0), roundMin(textHeight / 10F, 0), roundMin(textHeight / 5F, 0), roundMin(textHeight / 10F, 0));
    });
    public static final ThemeKey<Character> TEXT_FIELD_PASSWORD_CHAR = register("text_field.password_char", Character.class, r -> '•');
    public static final ThemeKey<Color> TEXT_FIELD_DISABLED_TEXT_COLOR = register("text_field.disabled_text_color", Color.class, r -> Color.fromRGB(150, 150, 150));
    public static final ThemeKey<Color> TEXT_FIELD_DISABLED_BACKGROUND_COLOR = register("text_field.disabled_background_color", Color.class, r -> Color.fromRGB(20, 20, 20));
    public static final ThemeKey<Color> TEXT_FIELD_DISABLED_OUTLINE_COLOR = register("text_field.disabled_outline_color", Color.class, r -> Color.fromRGB(45, 45, 45));
    public static final ThemeKey<AnimationConfig> TEXT_FIELD_CURSOR_ANIMATION = register("text_field.cursor_animation", AnimationConfig.class, r -> new AnimationConfig(
            AnimationMode.LOOP,
            List.of(
                    new AnimationFrameConfig(EasingFunction.SINE, EasingMode.EASE_OUT, 1F, 1F, 250, EasingBehavior.KEEP),
                    new AnimationFrameConfig(EasingFunction.SINE, EasingMode.EASE_OUT, 1F, 0F, 500, EasingBehavior.KEEP),
                    new AnimationFrameConfig(EasingFunction.SINE, EasingMode.EASE_OUT, 0F, 1F, 500, EasingBehavior.KEEP)
            )
    ));
    public static final ThemeKey<AnimationConfig> TEXT_FIELD_FOCUS_ANIMATION = register("text_field.focus_animation", AnimationConfig.class, r -> DefaultTheme.HOVER_ANIMATION);

    // Checkbox
    public static final ThemeKey<Float> CHECKBOX_CORNER_RADIUS = register("checkbox.corner_radius", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
    public static final ThemeKey<Float> CHECKBOX_OUTLINE_WIDTH = register("checkbox.outline_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
    public static final ThemeKey<Color> CHECKBOX_BACKGROUND_COLOR = register("checkbox.background_color", Color.class, r -> Color.fromRGB(30, 30, 30));
    public static final ThemeKey<Color> CHECKBOX_OUTLINE_COLOR = register("checkbox.outline_color", Color.class, r -> Color.GRAY);
    public static final ThemeKey<Color> CHECKBOX_CHECK_COLOR = register("checkbox.check_color", Color.class, r -> Color.WHITE);
    public static final ThemeKey<Float> CHECKBOX_CHECK_WIDTH = register("checkbox.check_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
    public static final ThemeKey<Float> CHECKBOX_TEXT_GAP = register("checkbox.text_gap", Float.class, r -> 0F);
    public static final ThemeKey<Color> CHECKBOX_HOVER_BACKGROUND_COLOR = register("checkbox.hover_background_color", Color.class, r -> Color.fromRGB(45, 45, 48));
    public static final ThemeKey<Color> CHECKBOX_HOVER_OUTLINE_COLOR = register("checkbox.hover_outline_color", Color.class, r -> Color.fromRGB(120, 120, 125));
    public static final ThemeKey<Color> CHECKBOX_DISABLED_BACKGROUND_COLOR = register("checkbox.disabled_background_color", Color.class, r -> Color.fromRGB(20, 20, 20));
    public static final ThemeKey<Color> CHECKBOX_DISABLED_OUTLINE_COLOR = register("checkbox.disabled_outline_color", Color.class, r -> Color.fromRGB(50, 50, 50));
    public static final ThemeKey<Color> CHECKBOX_DISABLED_CHECK_COLOR = register("checkbox.disabled_check_color", Color.class, r -> Color.fromRGB(100, 100, 100));
    public static final ThemeKey<AnimationConfig> CHECKBOX_HOVER_ANIMATION = register("checkbox.hover_animation", AnimationConfig.class, r -> DefaultTheme.HOVER_ANIMATION);
    public static final ThemeKey<AnimationConfig> CHECKBOX_CHECK_ANIMATION = register("checkbox.check_animation", AnimationConfig.class, r -> DefaultTheme.CLICK_ANIMATION);

    // ColorPicker
    public static final ThemeKey<Float> COLOR_PICKER_OUTLINE_WIDTH = register("color_picker.outline_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
    public static final ThemeKey<Color> COLOR_PICKER_OUTLINE_COLOR = register("color_picker.outline_color", Color.class, r -> Color.GRAY);
    public static final ThemeKey<Float> COLOR_PICKER_PICKER_SIZE = register("color_picker.picker_size", Float.class, r -> roundMin(r.backend().font().height() * 8F, 1));
    public static final ThemeKey<Float> COLOR_PICKER_SLIDER_WIDTH = register("color_picker.slider_width", Float.class, r -> roundMin(r.backend().font().height(), 1));
    public static final ThemeKey<Float> COLOR_PICKER_GAP = register("color_picker.gap", Float.class, r -> roundMin(r.backend().font().height() / 3F, 1));
    public static final ThemeKey<Float> COLOR_PICKER_SELECTOR_SIZE = register("color_picker.selector_size", Float.class, r -> roundMin(r.backend().font().height() / 8F, 1));

    // ComboBox
    public static final ThemeKey<Color> COMBOBOX_ARROW_COLOR = register("combobox.arrow_color", Color.class, r -> Color.WHITE);
    public static final ThemeKey<Color> COMBOBOX_DISABLED_ARROW_COLOR = register("combobox.disabled_arrow_color", Color.class, r -> Color.fromRGB(150, 150, 150));
    public static final ThemeKey<Float> COMBOBOX_ARROW_SIZE = register("combobox.arrow_size", Float.class, r -> roundMin(r.backend().font().height() / 2F, 1));
    public static final ThemeKey<Float> COMBOBOX_MAX_POPUP_HEIGHT = register("combobox.max_popup_height", Float.class, r -> roundMin(r.backend().font().height() * 10F, 1));
    public static final ThemeKey<Boolean> COMBOBOX_INTERCEPT_OUTSIDE_CLICKS = register("combobox.intercept_outside_clicks", Boolean.class, r -> true);

    // Separator
    public static final ThemeKey<Color> SEPARATOR_COLOR = register("separator.color", Color.class, r -> Color.fromRGB(65, 65, 70));
    public static final ThemeKey<Float> SEPARATOR_THICKNESS = register("separator.thickness", Float.class, r -> roundMin(r.backend().font().height() / 20F, 1));

    // Tab
    public static final ThemeKey<Corners> TAB_CORNER_RADIUS = register("tab.corner_radius", Corners.class, r -> {
        float cornerRadius = roundMin(r.backend().font().height() / 10F, 0);
        return new Corners(cornerRadius, 0F, 0F, cornerRadius);
    });
    public static final ThemeKey<Float> TAB_OUTLINE_WIDTH = register("tab.outline_width", Float.class, r -> roundMin(r.backend().font().height() / 25F, 1));
    public static final ThemeKey<Color> TAB_INACTIVE_COLOR = register("tab.inactive_color", Color.class, r -> Color.fromRGB(35, 35, 38));
    public static final ThemeKey<Color> TAB_INACTIVE_OUTLINE_COLOR = register("tab.inactive_outline_color", Color.class, r -> Color.fromRGB(55, 55, 60));
    public static final ThemeKey<Color> TAB_ACTIVE_COLOR = register("tab.active_color", Color.class, r -> Color.fromRGB(45, 45, 48));
    public static final ThemeKey<Color> TAB_ACTIVE_OUTLINE_COLOR = register("tab.active_outline_color", Color.class, r -> Color.fromRGB(65, 65, 70));
    public static final ThemeKey<Color> TAB_HOVER_COLOR = register("tab.hover_color", Color.class, r -> Color.fromRGB(50, 50, 55));
    public static final ThemeKey<Color> TAB_HOVER_OUTLINE_COLOR = register("tab.hover_outline_color", Color.class, r -> Color.fromRGB(80, 80, 85));
    public static final ThemeKey<Padding> TAB_INNER_PADDING = register("tab.inner_padding", Padding.class, r -> new Padding(5));
    public static final ThemeKey<Color> TAB_HEADER_BACKGROUND_COLOR = register("tab.header_background_color", Color.class, r -> Color.TRANSPARENT);
    public static final ThemeKey<Color> TAB_SEPARATOR_COLOR = register("tab.separator_color", Color.class, r -> Color.fromRGB(65, 65, 70));
    public static final ThemeKey<Float> TAB_SEPARATOR_THICKNESS = register("tab.separator_thickness", Float.class, r -> 0F);
    public static final ThemeKey<TabAlignment> TAB_ALIGNMENT = register("tab.alignment", TabAlignment.class, r -> TabAlignment.LEFT);
    public static final ThemeKey<Boolean> TAB_SAME_SIZE = register("tab.same_size", Boolean.class, r -> false);
    public static final ThemeKey<Float> TAB_VERTICAL_GAP = register("tab.vertical_gap", Float.class, r -> 0F);
    public static final ThemeKey<Float> TAB_TAB_GAP = register("tab.tab_gap", Float.class, r -> 0F);
    public static final ThemeKey<AnimationConfig> TAB_HOVER_ANIMATION = register("tab.hover_animation", AnimationConfig.class, r -> DefaultTheme.HOVER_ANIMATION);
    public static final ThemeKey<AnimationConfig> TAB_ACTIVE_ANIMATION = register("tab.active_animation", AnimationConfig.class, r -> DefaultTheme.CLICK_ANIMATION);
    public static final ThemeKey<ClickOn> TAB_CLICK_ON = register("tab.click_on", ClickOn.class, r -> ClickOn.UP);

    // Tooltip
    public static final ThemeKey<Long> TOOLTIP_DELAY = register("tooltip.delay", Long.class, r -> 500L);
    public static final ThemeKey<Boolean> TOOLTIP_REMOVE_ON_MOUSE_MOVE = register("tooltip.remove_on_mouse_move", Boolean.class, r -> true);
    public static final ThemeKey<Integer> TOOLTIP_MOUSE_OFFSET = register("tooltip.mouse_offset", Integer.class, r -> 20);

    // ProgressBar
    public static final ThemeKey<String> PROGRESS_BAR_TEXT_FORMAT = register("progress_bar.text_format", String.class, r -> "%,.0f%%");
    public static final ThemeKey<ProgressBar.TextPosition> PROGRESS_BAR_TEXT_POSITION = register("progress_bar.text_position", ProgressBar.TextPosition.class, r -> ProgressBar.TextPosition.FOLLOW_CENTER);
    public static final ThemeKey<Float> PROGRESS_BAR_TEXT_PADDING = register("progress_bar.text_padding", Float.class, r -> 5F);
    public static final ThemeKey<Float> PROGRESS_BAR_TRACK_CORNER_RADIUS = register("progress_bar.track_corner_radius", Float.class, r -> 0F);
    public static final ThemeKey<Float> PROGRESS_BAR_INDICATOR_CORNER_RADIUS = register("progress_bar.indicator_corner_radius", Float.class, r -> 0F);
    public static final ThemeKey<Color> PROGRESS_BAR_TRACK_COLOR = register("progress_bar.track_color", Color.class, r -> Color.fromRGB(45, 45, 48));
    public static final ThemeKey<Color> PROGRESS_BAR_INDICATOR_COLOR = register("progress_bar.indicator_color", Color.class, r -> Color.fromRGB(0, 122, 204));
    public static final ThemeKey<Color> PROGRESS_BAR_BORDER_COLOR = register("progress_bar.border_color", Color.class, r -> Color.TRANSPARENT);
    public static final ThemeKey<Float> PROGRESS_BAR_BORDER_WIDTH = register("progress_bar.border_width", Float.class, r -> 0F);
    public static final ThemeKey<Color> PROGRESS_BAR_TEXT_COLOR = register("progress_bar.text_color", Color.class, r -> Color.WHITE);
    public static final ThemeKey<Boolean> PROGRESS_BAR_STRIPES = register("progress_bar.stripes", Boolean.class, r -> false);
    public static final ThemeKey<Color> PROGRESS_BAR_STRIPE_COLOR = register("progress_bar.stripe_color", Color.class, r -> Color.fromRGBA(255, 255, 255, 60));
    public static final ThemeKey<Float> PROGRESS_BAR_STRIPE_WIDTH = register("progress_bar.stripe_width", Float.class, r -> roundMin(r.backend().font().height() * 0.75F, 1));
    public static final ThemeKey<Float> PROGRESS_BAR_STRIPE_GAP = register("progress_bar.stripe_gap", Float.class, r -> roundMin(r.backend().font().height() * 0.75F, 1));
    public static final ThemeKey<Float> PROGRESS_BAR_STRIPE_SPEED = register("progress_bar.stripe_speed", Float.class, r -> roundMin(r.backend().font().height() * 1.5F, 1));
    public static final ThemeKey<Float> PROGRESS_BAR_STRIPE_ANGLE = register("progress_bar.stripe_angle", Float.class, r -> 45F);
    public static final ThemeKey<Boolean> PROGRESS_BAR_STRIPE_ANIMATED = register("progress_bar.stripe_animated", Boolean.class, r -> true);

    // CollapsibleContainer
    public static final ThemeKey<Color> COLLAPSIBLE_CONTAINER_ARROW_COLOR = register("collapsible_container.arrow_color", Color.class, r -> Color.WHITE);
    public static final ThemeKey<Color> COLLAPSIBLE_CONTAINER_DISABLED_ARROW_COLOR = register("collapsible_container.disabled_arrow_color", Color.class, r -> Color.fromRGB(150, 150, 150));
    public static final ThemeKey<Float> COLLAPSIBLE_CONTAINER_ARROW_WIDTH = register("collapsible_container.arrow_width", Float.class, r -> roundMin(r.backend().font().height() / 15F, 1));
    public static final ThemeKey<Float> COLLAPSIBLE_CONTAINER_ARROW_SIZE = register("collapsible_container.arrow_size", Float.class, r -> r.backend().font().height());
    public static final ThemeKey<ClickOn> COLLAPSIBLE_CONTAINER_CLICK_ON = register("collapsible_container.click_on", ClickOn.class, r -> ClickOn.UP);
    public static final ThemeKey<AnimationConfig> COLLAPSIBLE_CONTAINER_COLLAPSE_ANIMATION = register("collapsible_container.collapse_animation", AnimationConfig.class, r -> new AnimationConfig(
            AnimationMode.DEFAULT,
            new AnimationFrameConfig(EasingFunction.CIRC, EasingMode.EASE_OUT, 0F, 1F, 200, EasingBehavior.KEEP)
    ));

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
