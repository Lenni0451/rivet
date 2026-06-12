package net.lenni0451.rivet.theme.text;

import lombok.SneakyThrows;
import net.lenni0451.commons.animation.AnimationMode;
import net.lenni0451.commons.animation.EasingBehavior;
import net.lenni0451.commons.animation.easing.EasingFunction;
import net.lenni0451.commons.animation.easing.EasingMode;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.AnimationFrameConfig;
import net.lenni0451.rivet.animation.DynamicAnimationConfig;
import net.lenni0451.rivet.component.container.Button;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.component.container.tabcontainer.TabAlignment;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeKey;
import net.lenni0451.rivet.theme.text.parser.Parser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ThemeLoaderTest {

    @SneakyThrows
    private static <V> void check(final ThemeKey<V> key, final String s, final V expected) {
        Map<ThemeKey<?>, Object> values = new HashMap<>();
        InputStream is = new ByteArrayInputStream((key.name() + "=" + s).getBytes(StandardCharsets.UTF_8));
        ThemeLoader.load(is, new Theme.Values(values), (line, cause) -> {
            throw cause;
        });
        assertEquals(1, values.size());
        assertTrue(values.containsKey(key));
        assertEquals(expected, values.get(key));
    }


    @Test
    void allThemeKeyTypesImplemented() throws Throwable {
        Field parsersField = ThemeLoader.class.getDeclaredField("parsers");
        parsersField.setAccessible(true);
        Map<Class<?>, Parser<?>> parsers = (Map<Class<?>, Parser<?>>) parsersField.get(null);

        for (ThemeKey<?> key : Theme.registeredKeys()) {
            if (!parsers.containsKey(key.type())) {
                fail("No parser implemented for theme key type: " + key.type());
            }
        }
    }

    @Test
    void parseTypes() {
        // Color
        check(Theme.TEXT_COLOR, "#AABBCC", Color.fromRGB(0xAABBCC));
        check(Theme.TEXT_COLOR, "rgb(#AABBCC)", Color.fromRGB(0xAA, 0xBB, 0xCC));

        // Boolean
        check(Theme.SLIDER_THUMB_ENCASED, "true", true);
        check(Theme.SLIDER_THUMB_ENCASED, "false", false);

        // Character
        check(Theme.TEXT_FIELD_PASSWORD_CHAR, "a", 'a');
        check(Theme.TEXT_FIELD_PASSWORD_CHAR, "*", '*');

        // Integer // Currently disabled because there is no key using it
//        check(Theme.BUTTON_ANIMATION_DURATION, "123", 123);
//        check(Theme.BUTTON_ANIMATION_DURATION, "-123", -123);

        // Long
        check(Theme.SCROLL_NESTED_SCROLL_TIMEOUT, "999999999999", 999_999_999_999L);
        check(Theme.SCROLL_NESTED_SCROLL_TIMEOUT, "-999999999999", -999_999_999_999L);

        // Float
        check(Theme.SCROLL_BAR_WIDTH, "0.12", 0.12F);
        check(Theme.SCROLL_BAR_WIDTH, "-9.12", -9.12F);

        // String
        check(Theme.SLIDER_TOOLTIP_FORMAT, "abc", "abc");
        check(Theme.SLIDER_TOOLTIP_FORMAT, "defg", "defg");

        // Padding
        check(Theme.BUTTON_INNER_PADDING, "5.5", new Padding(5.5F));
        check(Theme.BUTTON_INNER_PADDING, "0 1 2 3", new Padding(0, 1, 2, 3));
        check(Theme.BUTTON_INNER_PADDING, "top=1 bottom=3 left=0 right=2", new Padding(0, 1, 2, 3));

        // Corners
        check(Theme.TAB_CORNER_RADIUS, "5.5", new Corners(5.5F));
        check(Theme.TAB_CORNER_RADIUS, "0 1 2 3", new Corners(0, 1, 2, 3));
        check(Theme.TAB_CORNER_RADIUS, "bottomLeft=1 topRight=3 topLeft=0 bottomRight=2", new Corners(0, 1, 2, 3));

        // AnimationConfig
        check(Theme.TEXT_FIELD_CURSOR_ANIMATION, "loop; sine ease_out 1 1 250 keep; endValue=0 duration=500", new AnimationConfig(
                AnimationMode.LOOP,
                new AnimationFrameConfig(EasingFunction.SINE, EasingMode.EASE_OUT, 1F, 1F, 250, EasingBehavior.KEEP),
                AnimationFrameConfig.builder().endValue(0).duration(500).build()
        ));

        // DynamicAnimationConfig
        check(Theme.SCROLL_ANIMATION, "back ease_in_out 100", new DynamicAnimationConfig(EasingFunction.BACK, EasingMode.EASE_IN_OUT, 100));

        // Button.ClickOn
        for (Button.ClickOn value : Button.ClickOn.values()) {
            check(Theme.BUTTON_CLICK_ON, value.toString().toLowerCase(Locale.ROOT), value);
        }

        // Slider.ThumbShape
        for (Slider.ThumbShape value : Slider.ThumbShape.values()) {
            check(Theme.SLIDER_THUMB_SHAPE, value.toString().toLowerCase(Locale.ROOT), value);
        }

        // ScrollContainer.ScrollBarType
        for (ScrollContainer.ScrollBarType value : ScrollContainer.ScrollBarType.values()) {
            check(Theme.SCROLL_BAR_TYPE, value.toString().toLowerCase(Locale.ROOT), value);
        }

        // TabAlignment
        for (TabAlignment value : TabAlignment.values()) {
            check(Theme.TAB_ALIGNMENT, value.toString().toLowerCase(Locale.ROOT), value);
        }
    }

}
