package net.lenni0451.rivet.theme.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.theme.Theme;

public class DefaultDark extends Theme {

    @Override
    protected void register(final Registrar registrar) {
        // Bumped to 4 for a softer, more modern rounded edge (similar to macOS or modern web)
        registrar.accept(Theme.BUTTON_CORNER_RADIUS, 4);

        // 1 pixel is perfect for a crisp, subtle border
        registrar.accept(Theme.BUTTON_OUTLINE_WIDTH, 1);

        // Inactive State: A smooth, deep elevated gray (not pure black)
        registrar.accept(Theme.BUTTON_INACTIVE_COLOR, Color.fromRGB(45, 45, 48));
        // Inactive Outline: Slightly lighter than the background to give it structural definition
        registrar.accept(Theme.BUTTON_INACTIVE_OUTLINE_COLOR, Color.fromRGB(65, 65, 70));

        // Active State: A visibly lighter gray to indicate interaction (hover/press)
        registrar.accept(Theme.BUTTON_ACTIVE_COLOR, Color.fromRGB(65, 65, 70));
        // Active Outline: A brighter border to make the active button pop and provide clear feedback
        registrar.accept(Theme.BUTTON_ACTIVE_OUTLINE_COLOR, Color.fromRGB(100, 100, 105));
    }

}
