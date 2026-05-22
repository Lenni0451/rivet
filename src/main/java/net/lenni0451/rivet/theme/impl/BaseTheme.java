package net.lenni0451.rivet.theme.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.base.ScrollContainer;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeKey;

import java.util.Map;

public abstract class BaseTheme extends Theme {

    @Override
    protected void addValues(final Rivet rivet, final Map<ThemeKey<?>, Object> values) {
        values.put(Theme.BUTTON_CORNER_RADIUS, 4);
        values.put(Theme.BUTTON_OUTLINE_WIDTH, 2);
        values.put(Theme.BUTTON_ANIMATION_DURATION, 150);
        values.put(Theme.BUTTON_INNER_PADDING, new Padding(20, 5, 20, 5));

        values.put(Theme.SLIDER_BAR_HEIGHT, rivet.backend().getTextHeight() / 3F);
        values.put(Theme.SLIDER_THUMB_WIDTH, rivet.backend().getTextHeight() / 3F * 2F);
        values.put(Theme.SLIDER_THUMB_HEIGHT, rivet.backend().getTextHeight() / 3F * 2F);
        values.put(Theme.SLIDER_BAR_CORNER_RADIUS, Float.MAX_VALUE);
        values.put(Theme.SLIDER_THUMB_CORNER_RADIUS, Float.MAX_VALUE);
        values.put(Theme.SLIDER_THUMB_ENCASED, false);
        values.put(Theme.SLIDER_THUMB_SHAPE, Slider.ThumbShape.CIRCLE);
        values.put(Theme.SLIDER_TOOLTIP_CORNER_RADIUS, 4F);
        values.put(Theme.SLIDER_TOOLTIP_TRIANGLE_SIZE, rivet.backend().getTextHeight() / 4F);
        values.put(Theme.SLIDER_TOOLTIP_PADDING, new Padding(5F, 0, 5F, 0));
        values.put(Theme.SLIDER_TOOLTIP_FORMAT, "%,f");

        values.put(Theme.SCROLL_BAR_WIDTH, 8F);
        values.put(Theme.SCROLL_BAR_CORNER_RADIUS, 4F);
        values.put(Theme.SCROLL_BAR_OUTLINE_WIDTH, 0F);
        values.put(Theme.SCROLL_SPEED, rivet.backend().getTextHeight() * 4);
        values.put(Theme.SCROLL_SMOOTH, true);
        values.put(Theme.SCROLL_ANIMATION_DURATION, 100);
        values.put(Theme.SCROLL_NESTED_SCROLL_TIMEOUT, 150L);
        values.put(Theme.SCROLL_BAR_TYPE, ScrollContainer.ScrollBarType.FLOATING);
        values.put(Theme.SCROLL_RAIL_CLICK_JUMP, true);
        values.put(Theme.SCROLL_RAIL_OUTLINE_WIDTH, 0F);

        values.put(Theme.TEXT_FIELD_CURSOR_WIDTH, 1F);
        values.put(Theme.TEXT_FIELD_OUTLINE_WIDTH, 1F);
        values.put(Theme.TEXT_FIELD_CORNER_RADIUS, 0F);
        values.put(Theme.TEXT_FIELD_INNER_PADDING, new Padding(5, 5, 5, 5));

        values.put(Theme.CHECKBOX_CORNER_RADIUS, 2F);
        values.put(Theme.CHECKBOX_OUTLINE_WIDTH, 1F);
        values.put(Theme.CHECKBOX_CHECK_WIDTH, 2F);
        values.put(Theme.CHECKBOX_TEXT_GAP, 0F);
        values.put(Theme.CHECKBOX_ANIMATION_DURATION, 100);

        values.put(Theme.COLOR_PICKER_OUTLINE_WIDTH, 1F);
        values.put(Theme.COLOR_PICKER_PICKER_SIZE, rivet.backend().getTextHeight() * 8F);
        values.put(Theme.COLOR_PICKER_SLIDER_WIDTH, rivet.backend().getTextHeight());
        values.put(Theme.COLOR_PICKER_GAP, rivet.backend().getTextHeight() / 3F);
        values.put(Theme.COLOR_PICKER_SELECTOR_SIZE, 4F);
    }

}
