package net.lenni0451.rivet.theme.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.theme.Theme;

public class DefaultDark extends Theme {

    @Override
    protected void register(final Registrar registrar) {
        registrar.accept(Theme.BUTTON_CORNER_RADIUS, 4);
        registrar.accept(Theme.BUTTON_OUTLINE_WIDTH, 2);
        registrar.accept(Theme.BUTTON_INACTIVE_COLOR, Color.fromRGB(45, 45, 48));
        registrar.accept(Theme.BUTTON_INACTIVE_OUTLINE_COLOR, Color.fromRGB(65, 65, 70));
        registrar.accept(Theme.BUTTON_ACTIVE_COLOR, Color.fromRGB(65, 65, 70));
        registrar.accept(Theme.BUTTON_ACTIVE_OUTLINE_COLOR, Color.fromRGB(100, 100, 105));
        registrar.accept(Theme.BUTTON_ANIMATION_DURATION, 150);

        registrar.accept(Theme.SLIDER_BAR_COLOR, Color.fromRGB(65, 65, 70));
        registrar.accept(Theme.SLIDER_THUMB_COLOR, Color.fromRGB(120, 120, 125));
        registrar.accept(Theme.SLIDER_TICK_COLOR, Color.fromRGB(160, 160, 165));
    }

}
